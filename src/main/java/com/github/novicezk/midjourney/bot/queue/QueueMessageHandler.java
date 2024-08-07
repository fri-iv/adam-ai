package com.github.novicezk.midjourney.bot.queue;

import com.github.novicezk.midjourney.bot.AdamBotInitializer;
import com.github.novicezk.midjourney.bot.error.ErrorUtil;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.error.ErrorMessageHandler;
import com.github.novicezk.midjourney.bot.utils.ImageDownloader;
import com.github.novicezk.midjourney.enums.MessageType;
import com.github.novicezk.midjourney.loadbalancer.DiscordInstance;
import com.github.novicezk.midjourney.util.ContentParseData;
import com.github.novicezk.midjourney.util.ConvertUtils;
import com.github.novicezk.midjourney.wss.handle.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class QueueMessageHandler extends MessageHandler {
    private static final String CONTENT_REGEX = "\\*\\*(.*?)\\*\\* - <@\\d+> \\((.*?)\\)";
    private static final String LINK_PATTERN = "<([^>]+)>";

    @Override
    public void handle(DiscordInstance instance, MessageType messageType, DataObject message) {
        Guild guild = AdamBotInitializer.getApiInstance().getGuildById(Config.getGuildId());
        String content = getMessageContent(message);

        handlePossibleError(instance, messageType, message, content);
        handleCompletedTask(messageType, message, content, guild);
    }

    private void handlePossibleError(DiscordInstance instance, MessageType messageType, DataObject message, String content) {
        String failReason = ErrorUtil.isError(instance, messageType, message, content, getReferenceMessageId(message));
        if (failReason != null) {
            handleCriticalFail(message, failReason);
        }
    }

    private void handleCompletedTask(MessageType messageType, DataObject message, String content, Guild guild) {
        if (MessageType.CREATE.equals(messageType) && hasImage(message) && guild != null) {
            ContentParseData parseData = ConvertUtils.parseContent(content, CONTENT_REGEX);
            if (parseData != null) {
                handleTaskCompletion(message, guild, parseData);
            }
        }
    }

    private void handleCriticalFail(DataObject message, String failReason) {
        Guild guild = AdamBotInitializer.getApiInstance().getGuildById(Config.getGuildId());
        if (guild != null && message.getObject("interaction_metadata").hasKey("user_id")) {
            String userId = message.getObject("interaction_metadata").getString("user_id");
            ErrorMessageHandler.sendMessage(guild, userId, "Critical miss! \uD83C\uDFB2\uD83E\uDD26 \nTry again or upload new image!", failReason);
        }
    }

    private void handleTaskCompletion(DataObject message, Guild guild, ContentParseData parseData) {
        TextChannel channel = guild.getTextChannelById(Config.getSendingChannel());
        String userId = getAuthorId(message);
        if (channel != null && userId != null) {
            try {
                File imageFile = ImageDownloader.downloadImage(getImageUrl(message), "image.jpg");
                FileUpload file = FileUpload.fromData(imageFile);
                sendTaskCompletionMessage(channel, userId, parseData, file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendTaskCompletionMessage(TextChannel channel, String userId, ContentParseData parseData, FileUpload file) {
        Pattern pattern = Pattern.compile(LINK_PATTERN);
        Matcher matcher = pattern.matcher(parseData.getPrompt());
        String prompt = matcher.replaceAll("$1");

        QueueEntry entry = QueueManager.removeFromQueue(prompt);

        // do not show any generations instead of those generated in ai-arts channel
        if (!prompt.contains("signature") || !prompt.contains("AVIS")) {
            return;
        }

        String postMessage = "<@" + userId + ">";
        if (entry != null) {
            postMessage = "<@" + entry.getUserId() + ">\n\n" + entry.getMessage();
        }

        Button createButton = Button.success("ai-art-create", "Create Avatar \uD83D\uDCAB");
        Button faqButton = Button.of(ButtonStyle.LINK, Config.getFaqChannelUrl(), "What's that?");
        Button rerollButton = Button.secondary("re-roll", Emoji.fromUnicode("\uD83D\uDD04"));
        Button deleteButton = Button.danger("delete", Emoji.fromUnicode("\uD83D\uDDD1\uFE0F"));

        channel.sendMessage(postMessage)
                .addFiles(file)
                .setActionRow(createButton, faqButton, rerollButton, deleteButton)
                .queue();
    }
}
