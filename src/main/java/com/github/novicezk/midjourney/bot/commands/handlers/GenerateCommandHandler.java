package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.util.GeneratingRequestHandler;
import com.github.novicezk.midjourney.controller.SubmitController;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

@Slf4j
public class GenerateCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "generate";

    private final SubmitController submitController;

    public GenerateCommandHandler(final SubmitController submitController) {
        this.submitController = submitController;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        // handle imagine request
        new GeneratingRequestHandler(submitController)
                .doImagine(event);
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }
}
