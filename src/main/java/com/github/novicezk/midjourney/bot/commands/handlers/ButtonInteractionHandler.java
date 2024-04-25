package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.PrivateMessageSender;
import com.github.novicezk.midjourney.bot.events.EventsManager;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.github.novicezk.midjourney.bot.utils.MessageUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.NotNull;

public class ButtonInteractionHandler {

    private final PrivateMessageSender privateMessageSender;

    public ButtonInteractionHandler(PrivateMessageSender privateMessageSender) {
        this.privateMessageSender = privateMessageSender;
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
        } else if (!event.getMessage().getContentRaw().contains(buttonUserId) && !isGodfather) {
            handleUnauthorizedButton(event);
        } else if (event.getComponentId().equals("delete")) {
            handleDeleteButton(event);
        } else if (event.getComponentId().contains("create-avatar")) {
            handleCreateAvatarButton(event);
        }
    }

    private void handleCreateAvatarButton(ButtonInteractionEvent event) {
        privateMessageSender.sendToUser(event, "Hi there!\n\n" +
                "Our team has been notified about your request and we'll get in touch as soon as we're available. Feel free to share your thoughts here or simply wait for our contact.");
        event.getHook().sendMessageEmbeds(
                EmbedUtil.createEmbed("We've sent you a private message please check your DMs.")
        ).queue();

        boolean isFaqEvent = event.getComponentId().equals("faq:create-avatar");
        String title = isFaqEvent ? "Create button from FAQ" : "Create button from create-avatar channel";
        String channelId = isFaqEvent ? "<#1091727782498816010>" : "<#1092429060270985247>";
        privateMessageSender.notifyContactManager(
                event.getJDA(),
                title,
                String.format("%s\nReceived a request from <@%s> to create an avatar.", channelId, event.getUser().getId())
        );
    }

    private void handleCreateButton(ButtonInteractionEvent event) {
        privateMessageSender.sendToUser(event, "Hi there!\n\n" +
                "If you're looking for an avatar like the one in the picture just reach out to <@" + Config.getContactManagerId() + ">!");

        String messageLink = MessageUtil.getLinkToMessage(event.getMessage(), event.getGuild(), event.getChannel());
        privateMessageSender.notifyContactManager(
                event.getJDA(),
                "Create button from AI Arts",
                String.format("%sReceived a request from <@%s> to create an avatar", messageLink, event.getUser().getId())
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

    private void handleDeleteButton(ButtonInteractionEvent event) {
        event.getChannel().deleteMessageById(event.getMessageId()).queue();
        event.getHook().sendMessageEmbeds(
                EmbedUtil.createEmbedSuccess("The post has been deleted.")
        ).queue();
    }
}
