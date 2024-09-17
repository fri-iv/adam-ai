package com.github.novicezk.midjourney.bot.utils;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

public class ChannelUtil {
    public static void updateChannelTopic(TextChannel channel, String status) {
        String topic = channel.getTopic();
        if (topic == null) {
            return;
        }

        String[] lines = topic.split("\\n");
        lines[0] = String.format("Project status: **%s**", status);

        StringBuilder topicBuilder = new StringBuilder();
        for (String line : lines) {
            topicBuilder.append(line).append("\n");
        }

        if (!topicBuilder.isEmpty()) {
            topicBuilder.setLength(topicBuilder.length() - 1);
        }

        channel.getManager().setTopic(topicBuilder.toString()).queue();
    }
}
