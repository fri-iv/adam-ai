package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.Collections;
import java.util.List;

public class PingChannelCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "ping-channel";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member == null || !CommandsUtil.isUserAuthorized(member)) {
            OnErrorAction.onMissingRoleMessage(event);
            return;
        }

        if (!Config.getProjectsCategory().equals(event.getChannel().asTextChannel().getParentCategoryId())) {
            OnErrorAction.sendMessage(event, "This command can't be used in this category", false);
            return;
        }

        event.getChannel().sendMessage("Hello to @everyone! Please pay attention to this channel.").queue();
        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbed("done!")).queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        return Collections.singletonList(Commands.slash(COMMAND_NAME, "Admins only"));
    }
}
