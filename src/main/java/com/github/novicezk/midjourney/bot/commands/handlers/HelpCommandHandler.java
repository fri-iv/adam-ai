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
                String.format("""
                1. `/generate` – Generate a random avatar image for inspiration.
                2. `/upload-image` – Upload your own image to use as an avatar reference.
                3. `/get-images` – View all the images you have uploaded.
                4. `/get-queue` – Check the current queue status.
                5. `/clear-queue` – Clear the queue (<@&%s> role required).
                6. `/get-log` – Get access to the log files.
                7. `/contract` – Manage contracts (<@&%s> role required).
                8. `/create-embed` – Create an embed message (<@&%s> role required).
                9. `/ping` – Check the bot's.""",
                        Config.getAdminsRoleId(),
                        Config.getAdminsRoleId(),
                        Config.getAdminsRoleId()
                ),
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
