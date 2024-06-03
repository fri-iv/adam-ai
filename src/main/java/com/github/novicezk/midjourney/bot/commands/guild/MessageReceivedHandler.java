package com.github.novicezk.midjourney.bot.commands.guild;

import com.github.novicezk.midjourney.bot.commands.util.BotUtil;
import com.github.novicezk.midjourney.bot.events.EventsManager;
import com.github.novicezk.midjourney.bot.utils.ColorUtil;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

@Slf4j
public class MessageReceivedHandler {
    private static final Pattern URL_PATTERN = Pattern.compile(
            "\\b((https?|ftp|file)://|www\\.|ftp\\.)[-A-Z0-9+&@#/%?=~_|$!:,.;]*[A-Z0-9+&@#/%=~_|$]",
            Pattern.CASE_INSENSITIVE);

    private final PrivateMessageSender privateMessageSender;

    public MessageReceivedHandler(PrivateMessageSender privateMessageSender) {
        this.privateMessageSender = privateMessageSender;
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        boolean isPrivate = event.getChannelType().equals(ChannelType.PRIVATE)
                && !event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId());

        boolean forbiddenMessageChannels = !BotUtil.isAdamBot(event.getAuthor().getId())
                && (event.getChannel().getId().equals(Config.getArtsChannel())
                || event.getChannel().getId().equals(Config.getQueueChannel())
                || event.getChannel().getId().equals(Config.getFaqChannel()));

        // check for private messages to bot
        if (isPrivate) {
            privateMessageSender.sendToContactManager(event, event.getMessage().getAttachments());
        }

        // check if user send message to AI category
        if (forbiddenMessageChannels) {
            event.getMessage().delete().queue();
        }

        if (!event.getMessage().getEmbeds().isEmpty()) {
            handlePaymentMessage(event, event.getMessage().getEmbeds().get(0).getDescription());
        }

        if (!isPrivate && URL_PATTERN.matcher(event.getMessage().getContentRaw()).find()) {
            handleFilterLinks(event);
        }
    }

    private void handlePaymentMessage(MessageReceivedEvent event, @Nullable String description) {
        if (description != null && description.contains("Pending Payment")) {
            double paid = 100;
            double total = 200;
            double remaining = 100;

            Button copyButton = Button.primary("copy-paypal-email", "Copy PayPal Email");
            event.getChannel().sendMessageEmbeds(EmbedUtil.createEmbed(
                            "We're ready to proceed with your payment",
                            String.format("Please send the remaining balance to our **PayPal** account at **%s**\n" +
                                            "\n" +
                                            "Total price: **$%,.2f**\n" +
                                            "Already paid: **$%,.2f**\n" +
                                            "Remaining balance: **$%,.2f**\n" +
                                            "\n" +
                                            "Once the payment has been made, please notify <@%s> so we can proceed with the next steps.\n" +
                                            "\n" +
                                            "`/payment` for more information or to see additional payment options",
                                    Config.getPaypalEmail(),
                                    total,
                                    paid,
                                    remaining,
                                    Config.getContactManagerId()
                            ),
                            "Any fees incurred will be your responsibility. Thank you!",
                            ColorUtil.getSuccessColor()
                    ))
                    .addActionRow(copyButton)
                    .queue();
        }
    }

    private void handleFilterLinks(MessageReceivedEvent event) {
        Member member = event.getMember();
        Role linksAllowed = event.getGuild().getRoleById(Config.getRoleLinksAllowed());
        if (member != null && linksAllowed != null && !hasRole(member, linksAllowed)) {
            // Delete the message
            event.getMessage().delete().queue();

            // Mute the user
            Role muteRole = event.getGuild().getRoleById(Config.getRoleMuted());
            if (muteRole != null) {
                event.getGuild().addRoleToMember(member, muteRole).queue();

                privateMessageSender.notifyMutedMember(event.getMember());
                EventsManager.onMutedMember(event);
            }
        }
    }

    private boolean hasRole(Member member, Role linksAllowed) {
        for (Role role : member.getRoles()) {
            if (role.getPosition() > linksAllowed.getPosition()) {
                return true;
            }
        }
        return false;
    }
}
