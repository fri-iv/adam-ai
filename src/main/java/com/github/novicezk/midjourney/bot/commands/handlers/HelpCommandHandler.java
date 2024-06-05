package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.utils.ColorUtil;
import com.github.novicezk.midjourney.bot.utils.Config;
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
                """
                AI Generating Commands

                ・`/generate` – Generate a concept art image of an avatar for inspiration
                ・`/upload-image` – Upload your own image to use as an avatar reference
                ・`/get-images` – View all the images you have uploaded
                ・`/get-queue` – Check the current queue status
                ・`/clear-queue` – Clear the queue (Admin role required)

                Project commands:

                ・`/price` - Calculate the **final** price
                ・`/settings-project` - Update price information
                ・`/payment` - Get relevant payment information
                ・`/kofi-price` - Calculate the price for a **ko-fi** donation
                ・`/info` - Get current info about the project

                Administrate commands:

                ・`/mute` - Add the **MUTED** role to a user
                ・`/delete-message` - **Delete** any message in the channels
                ・`/pin-message` - Pin messages in any channels
                ・`/ping-channel` - Ping a **private** channel with the *@everyone* tag

                Dev commands:

                ・`/contract` – Manage contracts
                ・`/create-embed` – Create an embed message
                ・`/get-log` – Get access to the log files
                ・`/analytics` – Get detailed statistics about channels
                ・`/create-project` – Create a new private channel from the request
                
                ・`/help` – Get a list of all commands
                ・`/ping` – Get the current bot version
                """,
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
