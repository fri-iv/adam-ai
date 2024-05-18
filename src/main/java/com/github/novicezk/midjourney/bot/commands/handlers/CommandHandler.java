package com.github.novicezk.midjourney.bot.commands.handlers;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.List;

public interface CommandHandler {
    void handle(SlashCommandInteractionEvent event);
    boolean supports(String eventName);
    List<CommandData> getCommandData();
}
