package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.trello.TrelloCardFetcher;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.julienvey.trello.domain.Card;
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

public class CreateProjectCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "create-project";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Guild guild = event.getGuild();
        OptionMapping trelloOption = event.getOption("trello-link");
        OptionMapping customerOption = event.getOption("customer");
        OptionMapping artistOption = event.getOption("artist");

        if (trelloOption == null || customerOption == null || artistOption == null || guild == null) {
            OnErrorAction.onMissingFieldMessage(event);
            return;
        }

        String categoryId = Config.getProjectsCategory();
        String trelloLink = trelloOption.getAsString();
        Member customer = customerOption.getAsMember();
        Member artist = artistOption.getAsMember();

        TrelloCardFetcher fetcher = new TrelloCardFetcher();
        Card trelloCard = fetcher.getTrelloCardByLink(trelloLink);

        Category category = guild.getCategoryById(categoryId);
        if (category == null || customer == null || artist == null) {
            OnErrorAction.sendMessage(event, "Category not found or customer/artist missing.", true);
            return;
        }

        event.getHook()
                .sendMessageEmbeds(EmbedUtil.createEmbed("Private channel created!"))
                .setEphemeral(true)
                .queue();

        category.createTextChannel(String.format("%s-%s", trelloCard.getIdShort(), trelloCard.getName()))
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
        OptionData trello = new OptionData(OptionType.STRING, "trello-link", "https://trello.com/");
        OptionData customer = new OptionData(OptionType.MENTIONABLE, "customer", "Participants");
        OptionData artist = new OptionData(OptionType.MENTIONABLE, "artist", "Participants");

        return Collections.singletonList(Commands
                .slash(COMMAND_NAME, "Admins only")
                .addOptions(trello, customer, artist));
    }
}
