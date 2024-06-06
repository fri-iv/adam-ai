package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.utils.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class PingCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "ping";
    private static final Random random = new Random();

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        int probability = random.nextInt(100) + 1; // Generate random number from 1 to 100

        String text;
        if (probability <= 1) {
            text = ":partying_face: I can't believe you did this :tada:";
        } else if (probability <= 5) {
            text = "what's the score btw?";
        } else if (probability <= 20) {
            text = "what was that?";
        } else {
            text = "pong";
        }

        text += "\nVersion: " + Config.getAppVersion();

        event.reply(text).setEphemeral(true).queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        return Collections.singletonList(Commands.slash(PingCommandHandler.COMMAND_NAME, "Default ping command(or?)"));
    }
}
