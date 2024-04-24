package com.github.novicezk.midjourney.bot.commands.handlers;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Random;

public class PingCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "ping";
    private static final Random random = new Random();

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        int probability = random.nextInt(100) + 1; // Generate random number from 1 to 100

        String text;
        if (probability <= 1) {
            text = ":partying_face: wow looks like you **win!** :tada:";
        } else if (probability <= 5) {
            text = "what's the score btw?";
        } else if (probability <= 20) {
            text = "what was that?";
        } else {
            text = "pong";
        }

        event.reply(text).setEphemeral(true).queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }
}
