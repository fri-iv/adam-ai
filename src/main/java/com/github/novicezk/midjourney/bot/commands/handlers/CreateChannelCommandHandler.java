package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;

public class CreateChannelCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "create-channel";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Guild guild = event.getGuild();
        OptionMapping categoryOption = event.getOption("category");
        OptionMapping nameOption = event.getOption("name");
        OptionMapping customerOption = event.getOption("customer");
        OptionMapping artistOption = event.getOption("artist");

        if (categoryOption == null || nameOption == null || customerOption == null || artistOption == null || guild == null) {
            OnErrorAction.onMissingFieldMessage(event);
            return;
        }

        String categoryId = categoryOption.getAsString();
        String channelName = nameOption.getAsString();
        Member customer = customerOption.getAsMember();
        Member artist = artistOption.getAsMember();

        Category category = guild.getCategoryById(categoryId);
        if (category == null || customer == null || artist == null) {
            OnErrorAction.sendMessage(event, "Category not found or customer/artist missing.", true);
            return;
        }

        event.getHook()
                .sendMessageEmbeds(EmbedUtil.createEmbed("Private channel created!"))
                .setEphemeral(true)
                .queue();

        category.createTextChannel(channelName)
                .addPermissionOverride(customer, Permission.VIEW_CHANNEL.getRawValue(), 0)
                .addPermissionOverride(artist, Permission.VIEW_CHANNEL.getRawValue(), 0)
                .queue(channel -> {
                    channel.sendMessage(String.format("""
                                    Hello, <@%s>!

                                    This is a private channel where you can chat with your artist and track your project's status.
                                    Feel free to ask any questions!
                                    
                                    artist <@%s>
                                    payments <@%s>
                                    """,
                                    customer.getId(),
                                    artist.getId(),
                                    Config.getContactManagerId()
                            ))
                            .queue();
                });
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        OptionData category = new OptionData(OptionType.CHANNEL, "category", "Channel category");
        OptionData name = new OptionData(OptionType.STRING, "name", "Channel name");
        OptionData customer = new OptionData(OptionType.MENTIONABLE, "customer", "Participants");
        OptionData artist = new OptionData(OptionType.MENTIONABLE, "artist", "Participants");

        return Collections.singletonList(Commands
                .slash(COMMAND_NAME, "Admins only")
                .addOptions(category, name, customer, artist));
    }
}
