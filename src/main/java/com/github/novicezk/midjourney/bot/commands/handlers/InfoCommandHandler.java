package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.Collections;
import java.util.List;

public class InfoCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "info";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        if (!Config.getProjectsCategory().equals(event.getChannel().asTextChannel().getParentCategoryId())) {
            OnErrorAction.sendMessage(event, "This command can't be used in this category", false);
            return;
        }

        TextChannel channel = event.getChannel().asTextChannel();
        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbed(channel.getTopic())).queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        return Collections.singletonList(Commands.slash(COMMAND_NAME, "Get project details"));
    }
}
