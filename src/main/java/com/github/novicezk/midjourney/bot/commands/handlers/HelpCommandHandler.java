package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.util.CommandsHelper;
import com.github.novicezk.midjourney.bot.utils.ColorUtil;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.List;

public class HelpCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "help";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.replyEmbeds(EmbedUtil.createEmbed(
                "List of All Commands",
                CommandsHelper.getAllCommands(),
                "Commands list may be updated",
                ColorUtil.getDefaultColor()
        )).setEphemeral(true).queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        return List.of(Commands.slash(HelpCommandHandler.COMMAND_NAME, "View all available bot commands"));
    }
}
