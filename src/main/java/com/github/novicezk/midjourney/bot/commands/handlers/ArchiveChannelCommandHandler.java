package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class ArchiveChannelCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "archive-channel";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member == null || !CommandsUtil.isUserAuthorized(event, member)) {
            OnErrorAction.onMissingRoleMessage(event);
            return;
        }

        OptionMapping unarchiveOption = event.getOption("unarchive");
        boolean unarchive = unarchiveOption != null && unarchiveOption.getAsBoolean();

        if (!unarchive && !Config.getProjectsCategory().equals(event.getChannel().asTextChannel().getParentCategoryId()) ||
                unarchive && !Config.getCategoryArchive().equals(event.getChannel().asTextChannel().getParentCategoryId())) {
            OnErrorAction.sendMessage(event, "This command can't be used in this category", false);
            return;
        }

        Guild guild = event.getGuild();
        if (guild == null) {
            OnErrorAction.onMissingFieldMessage(event);
            return;
        }

        TextChannel channel = event.getChannel().asTextChannel();
        if (!unarchive) {
            archiveChannel(guild, channel);
        } else {
            unarchiveChannel(guild, channel);
        }

        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbedSuccess("done")).setEphemeral(true).queue();
    }

    private void archiveChannel(Guild guild, TextChannel channel) {
        Category archiveCategory = guild.getCategoryById(Config.getCategoryArchive());
        channel.getManager()
                .setParent(archiveCategory)
                .queue(success -> {
                    channel.getManager().putPermissionOverride(
                            channel.getGuild().getPublicRole(),
                            null,
                            EnumSet.of(Permission.MESSAGE_SEND, Permission.VIEW_CHANNEL)
                    ).queue();

                    sendArchivedMessages(channel);
                });
    }

        private void unarchiveChannel(Guild guild, TextChannel channel) {
            Category projectsCategory = guild.getCategoryById(Config.getProjectsCategory());
            channel.getManager()
                    .setParent(projectsCategory)
                    .queue(success -> {
                        channel.getManager().putPermissionOverride(
                                channel.getGuild().getPublicRole(),
                                EnumSet.of(Permission.MESSAGE_SEND),
                                EnumSet.of(Permission.VIEW_CHANNEL)
                        ).queue();

                        sendUnarchivedMessages(channel);
                    });
        }

    private void sendArchivedMessages(TextChannel channel) {
        Button requestButton = Button.success("new-request", "New Request \uD83D\uDCAB");

        channel.sendMessageEmbeds(EmbedUtil.createEmbedSuccess(
                "This channel has been archived",
                String.format("To create a new request, please click the button below or contact <@%s>", Config.getContactManagerId())
        )).addActionRow(requestButton).queue();
    }

    private void sendUnarchivedMessages(TextChannel channel) {
        channel.sendMessageEmbeds(EmbedUtil.createEmbedSuccess(
                "Channel restored!",
                "This channel is back online! Continue your discussions here."
        )).queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        OptionData unarchive = new OptionData(OptionType.BOOLEAN, "unarchive", "Restore the channel", false);

        return Collections.singletonList(Commands.slash(COMMAND_NAME, "Admins only")
                .addOptions(unarchive));
    }
}
