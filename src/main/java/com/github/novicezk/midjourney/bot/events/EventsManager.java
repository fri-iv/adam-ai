package com.github.novicezk.midjourney.bot.events;

import com.github.novicezk.midjourney.bot.AdamBotInitializer;
import com.github.novicezk.midjourney.bot.events.model.ButtonEventData;
import com.github.novicezk.midjourney.bot.events.model.CommandEventData;
import com.github.novicezk.midjourney.bot.events.model.ErrorEventData;
import com.github.novicezk.midjourney.bot.events.model.LeaveEventData;
import com.github.novicezk.midjourney.bot.utils.ColorUtil;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

@Slf4j
public class EventsManager {
    public static void onCommand(SlashCommandInteractionEvent event) {
        String logText = "c:" + event.getName();
        EventsStorage.logCommandInvocation(logText, event.getUser().getId());
        MixpanelManager.trackEvent(new CommandEventData(event));
        sendLogToDiscord(event.getGuild(), event.getUser().getId(), event.getUser().getName(), logText);
    }

    public static void onButtonClick(ButtonInteractionEvent event) {
        String logText = "b:" + event.getComponentId();
        EventsStorage.logButtonInteraction(logText, event.getUser().getId());
        MixpanelManager.trackEvent(new ButtonEventData(event));
        sendLogToDiscord(event.getGuild(), event.getUser().getId(), event.getUser().getName(), logText);
    }

    public static void onWelcomeGenerate(GuildMemberJoinEvent event) {
        String logText = "Welcome generate";
        EventsStorage.logButtonInteraction(logText, event.getUser().getId());
        MixpanelManager.trackEvent(new CommandEventData(event, logText));
        sendLogToDiscord(event.getGuild(), event.getUser().getId(), event.getUser().getName(), logText);
    }

    public static void onMemberLeave(GuildMemberRemoveEvent event, String reason) {
        MixpanelManager.trackEvent(new LeaveEventData(event, reason));
        sendLogToDiscord(event.getGuild(), event.getUser().getId(), event.getUser().getName(), reason, ColorUtil.getWarningColor());
    }

    public static void onErrorEvent(String userId, String failReason) {
        MixpanelManager.trackEvent(new ErrorEventData(userId, failReason));
    }

    public static void onMidjourneyFailure(String failReason) {
        String errorMessage = "Midjourney task finished FAILURE - " + failReason;
        log.info(errorMessage);

        Guild guild = AdamBotInitializer.getApiInstance().getGuildById(Config.getGuildId());
        sendLogToDiscord(guild, null, null, errorMessage, ColorUtil.getErrorColor());
    }

    public static void onMutedMember(MessageReceivedEvent event) {
        String errorMessage = "User muted:\n\n" + event.getMessage().getContentRaw();
        log.info(errorMessage);

        String id = event.getAuthor().getId();
        String username = event.getAuthor().getName();

        sendLogToDiscord(event.getGuild(), id, username, errorMessage, ColorUtil.getErrorColor());
    }

    private static void sendLogToDiscord(@Nullable Guild guild, String userId, String username, String text) {
        sendLogToDiscord(guild, userId, username, text, ColorUtil.getDefaultColor());
    }

    private static void sendLogToDiscord(
            @Nullable Guild guild,
            @Nullable String userId,
            @Nullable String username,
            String text,
            Color color
    ) {
        if (guild == null || guild.getTextChannelById(Config.getLogsChannel()) == null) {
            return;
        }

        String postText = text;
        if (userId != null && username != null) {
            postText = String.format("%s — <@%s>, %s", text, userId, username);
        }

        TextChannel logChannel = guild.getTextChannelById(Config.getLogsChannel());
        logChannel.sendMessageEmbeds(EmbedUtil.createEmbed(
                null,
                postText,
                Config.getAppVersion(),
                color
        )).queue();
    }
}
