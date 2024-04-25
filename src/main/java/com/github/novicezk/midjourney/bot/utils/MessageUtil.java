package com.github.novicezk.midjourney.bot.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import org.jetbrains.annotations.Nullable;

public class MessageUtil {
    public static String getLinkToMessage(Message message, @Nullable Guild guild, MessageChannelUnion channel) {
        if (guild == null || channel == null || message == null) {
            return "";
        }

        String guildId = guild.getId();
        String channelId = channel.getId();
        String messageId = message.getId();

        return "https://discord.com/channels/" + guildId + "/" + channelId + "/" + messageId + "\n";
    }
}
