package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.commands.guild.PrivateMessageSender;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.webhook.TrelloManager;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.julienvey.trello.domain.Card;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
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

    private final PrivateMessageSender privateMessageSender;

    public CreateProjectCommandHandler(PrivateMessageSender privateMessageSender) {
        this.privateMessageSender = privateMessageSender;
    }

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
                    String channelLink = "<#" + channel.getId() + ">";

                    sendMessageToPrivateChannel(channel, trelloCard.getName(), artist.getId(), priceOption.getAsDouble());
                    sendPrivateMessageToClient(customer.getUser(), channelLink);
                    sendPrivateMessageToArtist(artist.getUser(), channelLink, trelloCard.getName(), trelloLink);

                    updateChannelTopic(channel, trelloCard, priceOption.getAsDouble());
                });
    }

    private void sendMessageToPrivateChannel(TextChannel channel, String projectName, String artistId, double price) {
        channel.sendMessageEmbeds(EmbedUtil.createEmbedCute(
                        "Welcome to AviHero!",
                        String.format("""
                                    Your project **%s** is now in progress!
                                    Chat here to discuss details.

                                    **3D Artist:** <@%s>
                                    **Project Manager:** <@%s>

                                    **Price:** $%,.2f

                                    `/info` for the current project status
                                    `/payment` for payment details
                                    """,
                                projectName,
                                artistId,
                                Config.getContactManagerId(),
                                price
                        )))
                .queue();
    }

    private void sendPrivateMessageToArtist(User user, String channelLink, String projectName, String projectLink) {
        privateMessageSender.sendMessageEmbedToUser(user, String.format("""
                            ### Project %s started!
                            %s

                            %s
                            Use the link above to update the client and discuss any details.

                            <#%s>
                            Follow service rules for safe and comfortable chats.
                            Also you can learn bot commands to manage the channel there.
                            """,
                projectName,
                projectLink,
                channelLink,
                Config.getDevRulesChannel()
        ));
    }

    private void sendPrivateMessageToClient(User user, String channelLink) {
        privateMessageSender.sendMessageEmbedToUser(user, String.format("""
                            ### Hello! We've started your project!

                            %s
                            To track progress and discuss details click the link above.

                            For your safety, remember:

                            ・Discuss project details in the provided channel **only**.
                            ・Contact <@%s> for payment information.
                            ・Only <@%s> is authorized to accept payments.
                            """,
                channelLink,
                Config.getContactManagerId(),
                Config.getContactManagerId()
        ));
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
        OptionData customer = new OptionData(OptionType.USER, "customer", "Participants");
        OptionData artist = new OptionData(OptionType.USER, "artist", "Participants");
        OptionData price = new OptionData(OptionType.NUMBER, "price", "Project price");

        return Collections.singletonList(Commands
                .slash(COMMAND_NAME, "Admins only")
                .addOptions(trello, customer, artist, price));
    }
}
