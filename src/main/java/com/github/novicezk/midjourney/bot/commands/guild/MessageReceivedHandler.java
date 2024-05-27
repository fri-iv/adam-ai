package com.github.novicezk.midjourney.bot.commands.guild;

import com.github.novicezk.midjourney.bot.events.EventsManager;
import com.github.novicezk.midjourney.bot.utils.Config;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

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
        boolean isPrivate = event.getChannelType().equals(ChannelType.PRIVATE) && !event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId());
        boolean forbiddenMessageChannels = !event.getAuthor().isBot()
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

        if (!isPrivate && URL_PATTERN.matcher(event.getMessage().getContentRaw()).find()) {
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
