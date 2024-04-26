package com.github.novicezk.midjourney.bot.commands.guild;

import com.github.novicezk.midjourney.bot.utils.Config;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class MessageReceivedHandler {
    private final PrivateMessageSender privateMessageSender;

    public MessageReceivedHandler(PrivateMessageSender privateMessageSender) {
        this.privateMessageSender = privateMessageSender;
    }

    public void onMessageReceived(MessageReceivedEvent event) {
        // check for private messages to bot
        if (event.getChannelType().equals(ChannelType.PRIVATE) && !event.getAuthor().getId().equals(event.getJDA().getSelfUser().getId())) {
            privateMessageSender.sendToContactManager(event);
        }

        // check if user send message to AI category
        if (!event.getAuthor().isBot()
                && (event.getChannel().getId().equals(Config.getArtsChannel())
                || event.getChannel().getId().equals(Config.getQueueChannel())
                || event.getChannel().getId().equals(Config.getFaqChannel()))) {
            event.getMessage().delete().queue();
        }
    }
}
