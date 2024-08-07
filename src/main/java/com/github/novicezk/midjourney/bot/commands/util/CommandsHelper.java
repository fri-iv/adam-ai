package com.github.novicezk.midjourney.bot.commands.util;

public class CommandsHelper {

    public static String getAllCommands() {
        return """
                AI Generating Commands

                ・`/generate` – Generate a concept art image of an avatar for inspiration
                ・`/upload-image` – Upload your own image to use as an avatar reference
                ・`/get-images` – View all the images you have uploaded
                ・`/get-queue` – Check the current queue status
                ・`/clear-queue` – Clear the queue (Admin role required)

                Administrate commands:

                ・`/mute` - Add the **MUTED** role to a user
                ・`/delete-message` - **Delete** any message in the channels
                ・`/pin-message` - Pin messages in any channels
                ・`/ping-channel` - Ping a private channel with the *@everyone* tag

                Project commands:

                ・`/price` - Calculate the final price
                ・`/settings-project` - Update price information
                ・`/payment` - Get relevant payment information
                ・`/kofi-price` - Calculate the price for a ko-fi donation
                ・`/info` - Get current info about the project

                Dev commands:

                ・`/contract` – Manage contracts
                ・`/create-embed` – Create an embed message
                ・`/get-log` – Get access to the log files
                ・`/analytics` – Get detailed statistics about channels
                ・`/create-project` – Create a new private channel from the request
                
                ・`/help` – Get a list of all commands
                ・`/ping` – Get the current bot version
                """;
    }
}
