package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.github.novicezk.midjourney.bot.utils.FileUtil;
import com.github.novicezk.midjourney.bot.webhook.TrelloManager;
import com.julienvey.trello.domain.Card;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class UpdatePublishCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "update-publish";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member == null || !CommandsUtil.isUserAuthorized(event, member)) {
            OnErrorAction.onMissingRoleMessage(event);
            return;
        }

        OptionMapping productionOption = event.getOption("production");
        boolean prod = productionOption != null && productionOption.getAsBoolean();

        String channelId = prod ? Config.getUpdatesChannel() : Config.getDebugChannel();
        Guild guild = event.getGuild();

        if (guild == null || guild.getTextChannelById(channelId) == null) {
            OnErrorAction.onMissingFieldMessage(event);
            return;
        }

        sendUpdateToChannel(event, guild.getTextChannelById(channelId));
    }

    private void sendUpdateToChannel(SlashCommandInteractionEvent event, TextChannel channel) {
        OptionMapping attachment = event.getOption("attachment");
        OptionMapping attachment2 = event.getOption("attachment2");
        OptionMapping attachment3 = event.getOption("attachment3");
        OptionMapping attachment4 = event.getOption("attachment4");

        List<Message.Attachment> attachments = Stream.of(
                attachment != null ? attachment.getAsAttachment() : null,
                attachment2 != null ? attachment2.getAsAttachment() : null,
                attachment3 != null ? attachment3.getAsAttachment() : null,
                attachment4 != null ? attachment4.getAsAttachment() : null
        ).filter(java.util.Objects::nonNull).toList();

        OptionMapping trelloOption = event.getOption("trello-link");
        OptionMapping artistOption = event.getOption("author");
        OptionMapping artistCommentOption = event.getOption("comment");
        if (trelloOption == null) {
            OnErrorAction.onMissingFieldMessage(event);
            return;
        }

        String trelloLink = trelloOption.getAsString();
        Card trelloCard = TrelloManager.getTrelloCardByLink(trelloLink);

        String authorString = "";
        if (artistOption != null && artistOption.getAsMember() != null) {
            Member artist = artistOption.getAsMember();
            authorString = "\n**Author:** <@" + artist.getId() + ">";
        }

        String comment = "";
        if (artistCommentOption != null) {
            comment = "\n\n**Artist's comment:** " + artistCommentOption.getAsString();
        }


        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbedSuccess("Done")).setEphemeral(true).queue();
        channel.sendMessage(String.format(
                """
                **Project №:** %s
                **Name:** %s %s %s
                """, trelloCard.getIdShort(), trelloCard.getName(), authorString, comment).trim())
                .addFiles(FileUtil.getFilesFromAttachments(attachments)).queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        OptionData author = new OptionData(OptionType.USER, "author", "Artist name", false);
        OptionData number = new OptionData(OptionType.STRING, "trello-link", "https://trello.com/", false);
        OptionData comment = new OptionData(OptionType.STRING, "comment", "Artist's comment", false);
        OptionData publish = new OptionData(OptionType.BOOLEAN, "production", "Default false", false);

        OptionData attachment = new OptionData(OptionType.ATTACHMENT, "attachment", "Choose your attachment", true);
        OptionData attachment2 = new OptionData(OptionType.ATTACHMENT, "attachment2", "Optional attachment", false);
        OptionData attachment3 = new OptionData(OptionType.ATTACHMENT, "attachment3", "Optional attachment", false);
        OptionData attachment4 = new OptionData(OptionType.ATTACHMENT, "attachment4", "Optional attachment", false);

        return Collections.singletonList(Commands
                .slash(COMMAND_NAME, "Upload content")
                .addOptions(attachment, attachment2, attachment3, attachment4, number, author, comment, publish));
    }
}
