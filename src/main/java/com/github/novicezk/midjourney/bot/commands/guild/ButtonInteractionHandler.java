package com.github.novicezk.midjourney.bot.commands.guild;

import com.github.novicezk.midjourney.bot.commands.util.CommandsHelper;
import com.github.novicezk.midjourney.bot.commands.util.GeneratingRequestHandler;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.events.EventsManager;
import com.github.novicezk.midjourney.bot.model.TopicSettings;
import com.github.novicezk.midjourney.bot.utils.ColorUtil;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.github.novicezk.midjourney.bot.utils.MessageUtil;
import com.github.novicezk.midjourney.controller.SubmitController;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class ButtonInteractionHandler {

    private final PrivateMessageSender privateMessageSender;
    private final SubmitController submitController;

    public ButtonInteractionHandler(SubmitController submitController, PrivateMessageSender privateMessageSender) {
        this.privateMessageSender = privateMessageSender;
        this.submitController = submitController;
    }

    public void handleButtonInteraction(@NotNull ButtonInteractionEvent event) {
        EventsManager.onButtonClick(event);

        event.deferReply().setEphemeral(true).queue();
        String buttonUserId = event.getUser().getId();
        Member member = event.getMember();
        Guild guild = event.getGuild();
        boolean isGodfather = member != null
                && member.getRoles().stream().anyMatch(role -> role.getId().equals(Config.getGodfatherId()));

        if (event.getComponentId().equals("ai-art-create") || event.getComponentId().equals("create")) {
            handleCreateButton(event);
        } else if (event.getComponentId().equals("testers") && guild != null) {
            handleTestersButton(event, guild);
        } else if (event.getComponentId().equals("delete")) {
            handleDeleteButton(event, buttonUserId, isGodfather);
        } else if (event.getComponentId().contains("create-avatar")) {
            handleCreateAvatarButton(event);
        } else if (event.getComponentId().equals("re-roll")) {
            handleReRollButton(event);
        } else if (event.getComponentId().equals("copy-paypal-email")) {
            handleHelpButton(event);
            handleCopyPayPalEmail(event);
        } else if (event.getComponentId().startsWith("set-total-price")
                || event.getComponentId().startsWith("add-total-price")) {
            handleChangeTotalPriceButton(event, event.getComponentId().startsWith("set-total-price"));
        } else if (event.getComponentId().equals("help-button")) {
            handleHelpButton(event);
        } else if (event.getComponentId().equals("payment-complete")) {
            handlePaymentCompleteButton(event);
        }
    }

    private void handlePaymentCompleteButton(ButtonInteractionEvent event) {
        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbed("Thanks! Your manager has been notified. We'll process your payment as soon as possible.")).queue();

        TextChannel channel = event.getChannel().asTextChannel();
        channel.sendMessageEmbeds(EmbedUtil.createEmbedSuccess("Client has sent the payment")).queue();

        // Notify contact manager
        String notificationChannel = "<#" + event.getChannel().getId() + ">";
        privateMessageSender.notifyContactManager(
                event.getJDA(),
                event.getMessage().getAttachments(),
                "Client has sent the payment!",
                String.format("%s\nPayment confirmation received from this channel.", notificationChannel)
        );
    }

    private void handleHelpButton(ButtonInteractionEvent event) {
        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbed(
                "List of All Commands",
                CommandsHelper.getAllCommands(),
                "Commands list may be updated",
                ColorUtil.getDefaultColor()
        )).queue();
    }

    private void handleChangeTotalPriceButton(ButtonInteractionEvent event, boolean setPrice) {
        if (!Config.getProjectsCategory().equals(event.getChannel().asTextChannel().getParentCategoryId())) {
            OnErrorAction.sendMessage(event, "This button can't be used in this category", false);
            return;
        }

        double price = Double.parseDouble(event.getComponentId().substring("set-total-price:".length()));
        TextChannel channel = event.getChannel().asTextChannel();
        TopicSettings topicSettings = new TopicSettings(channel.getTopic());

        if (setPrice) {
            topicSettings.setTotal(price);
        } else {
            topicSettings.setTotal(topicSettings.getTotal() + price);
        }
        channel.getManager().setTopic(topicSettings.getTopicSummary()).queue();
        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbedSuccess("done")).queue();
        channel.sendMessageEmbeds(EmbedUtil.createEmbedSuccess("The total price has been changed!", topicSettings.getTopicPrice())).queue();
    }

    private void handleCopyPayPalEmail(ButtonInteractionEvent event) {
        privateMessageSender.sendMessageFromButtonClick(event, Config.getPaypalEmail());
        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbed("We've sent you the email address in a private message. You can copy it from there.")).queue();
    }

    private void handleReRollButton(ButtonInteractionEvent event) {
        // handle re-roll request
        new GeneratingRequestHandler(submitController)
                .doReRoll(event);
    }

    private void handleCreateAvatarButton(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();
        String messageContent;
        String notificationTitle;
        String notificationChannel = "";

        if (componentId.equals("wel:create-avatar")) {
            messageContent = "Our team has been notified about your request and we'll get in touch as soon as we're available. Feel free to share your thoughts here or simply wait for our contact.";
            notificationTitle = "Create Avatar click! Direct message";
        } else {
            messageContent = "We've sent you a private message please check your DMs.";
            notificationTitle = componentId.equals("faq:create-avatar") ? "Create Avatar click! FAQ" : "Create Avatar click! create-avatar channel";

            if (componentId.equals("faq:create-avatar")) {
                notificationChannel = "<#" + Config.getFaqChannel() + ">\n";
            } else if (componentId.equals("welch:create-avatar")) {
                notificationChannel = "<#" + Config.getWelcomeChannel() + ">\n";
                notificationTitle = "Create Avatar click! Welcome channel";
            } else {
                notificationChannel = "<#" + Config.getCreateAvatarChannel() + ">\n";
            }

            privateMessageSender.sendMessageFromButtonClick(event, "Hi there!\n\n" +
                    "Our team has been notified about your request and we'll get in touch as soon as we're available. Feel free to share your thoughts here or simply wait for our contact.");
        }

        // Send message to the user
        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbed(messageContent)).queue();

        // Notify contact manager
        privateMessageSender.notifyContactManager(
                event.getJDA(),
                event.getMessage().getAttachments(),
                notificationTitle,
                String.format("%sReceived a request from <@%s> (%s) to create an avatar.", notificationChannel, event.getUser().getId(), event.getUser().getName())
        );
    }

    private void handleCreateButton(ButtonInteractionEvent event) {
        privateMessageSender.sendMessageFromButtonClick(event, "Hi there!\n\n" +
                "If you're looking for an avatar like the one in the picture just reach out to <@" + Config.getContactManagerId() + ">!");

        String messageLink = MessageUtil.getLinkToMessage(event.getMessage(), event.getGuild(), event.getChannel());
        privateMessageSender.notifyContactManager(
                event.getJDA(),
                event.getMessage().getAttachments(),
                "Create button from AI Arts",
                String.format("%sReceived a request from <@%s> (%s) to create an avatar", messageLink, event.getUser().getId(), event.getUser().getName())
        );

        event.getHook().sendMessageEmbeds(
                EmbedUtil.createEmbed("We've sent you a private message please check your DMs.")
        ).queue();
    }

    private void handleTestersButton(ButtonInteractionEvent event, Guild guild) {
        event.getHook().sendMessageEmbeds(
                EmbedUtil.createEmbed("Welcome to <@&" + Config.getRoleTester() + ">! You can now use the `/generate` command.")
        ).queue();

        guild.addRoleToMember(event.getMember(), guild.getRoleById(Config.getRoleTester())).queue();
    }

    private void handleUnauthorizedButton(ButtonInteractionEvent event) {
        event.getHook().sendMessageEmbeds(
                EmbedUtil.createEmbedWarning("Only the original author can delete the request.")
        ).queue();
    }

    private void handleDeleteButton(ButtonInteractionEvent event, String buttonUserId, boolean isGodfather) {
        if (!event.getMessage().getContentRaw().contains(buttonUserId) && !isGodfather) {
            handleUnauthorizedButton(event);
            return;
        }

        event.getChannel().deleteMessageById(event.getMessageId()).queue();
        event.getHook().sendMessageEmbeds(
                EmbedUtil.createEmbedSuccess("The post has been deleted.")
        ).queue();
    }
}
