package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.trello.TrelloManager;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.julienvey.trello.domain.Card;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
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

        Member member = event.getMember();
        if (member == null || !CommandsUtil.isUserAuthorized(member)) {
            OnErrorAction.onMissingRoleMessage(event);
            return;
        }

        Guild guild = event.getGuild();
        OptionMapping trelloOption = event.getOption("trello-link");
        OptionMapping customerOption = event.getOption("customer");
        OptionMapping artistOption = event.getOption("artist");
        OptionMapping priceOption = event.getOption("price");

        if (trelloOption == null || customerOption == null || artistOption == null || guild == null || priceOption == null) {
            OnErrorAction.onMissingFieldMessage(event);
            return;
        }

        String categoryId = Config.getProjectsCategory();
        String trelloLink = trelloOption.getAsString();
        Member customer = customerOption.getAsMember();
        Member artist = artistOption.getAsMember();

        Card trelloCard = TrelloManager.getTrelloCardByLink(trelloLink);

        Category category = guild.getCategoryById(categoryId);
        if (category == null || customer == null || artist == null) {
            OnErrorAction.sendMessage(event, "Category not found or customer/artist missing.", true);
            return;
        }

        event.getHook()
                .sendMessageEmbeds(EmbedUtil.createEmbed("Private channel created!"))
                .setEphemeral(true)
                .queue();

        category.createTextChannel(String.format("%s・%s", trelloCard.getIdShort(), trelloCard.getName()))
                .addPermissionOverride(customer, Permission.VIEW_CHANNEL.getRawValue(), 0)
                .addPermissionOverride(artist, Permission.VIEW_CHANNEL.getRawValue(), 0)
                .queue(channel -> {
                    channel.sendMessage(String.format("""
                                    ## Welcome to **Avatar Studio**!

                                    <@%s>
                                    Your project **%s** is now in progress!
                                    Chat here to discuss details.
                                    
                                    **3D Artist:** <@%s>
                                    **Project Manager:** <@%s>
                                    **Price:** $%,.2f

                                    `/info` for the current project status
                                    `/payment` for payment details
                                    """,
                                    customer.getId(),
                                    trelloCard.getName(),
                                    artist.getId(),
                                    Config.getContactManagerId(),
                                    priceOption.getAsDouble()
                            ))
                            .queue();

                    updateChannelTopic(channel, trelloCard, priceOption.getAsDouble());
                });

    }

    private void updateChannelTopic(TextChannel channel, Card trelloCard, double price) {
        String columnName = TrelloManager.getColumnByCard(trelloCard).getName();
        String topic = String.format("""
                Project status: **%s**
                
                Total price: **$%,.2f**
                Already paid: **$0**
                Remaining balance: **$%,.2f**
                """,
                columnName,
                price,
                price
        );

        channel.getManager().setTopic(topic).queue();
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
        OptionData price = new OptionData(OptionType.NUMBER, "price", "Project price");

        return Collections.singletonList(Commands
                .slash(COMMAND_NAME, "Admins only")
                .addOptions(trello, customer, artist, price));
    }
}
