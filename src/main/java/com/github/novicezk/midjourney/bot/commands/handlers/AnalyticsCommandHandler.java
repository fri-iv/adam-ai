package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.*;

public class AnalyticsCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "analytics";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member == null || !CommandsUtil.isUserAuthorized(member)) {
            OnErrorAction.onMissingRoleMessage(event);
            return;
        }

        Guild guild = event.getGuild();
        OptionMapping channelMapping = event.getOption("channel");
        if (guild == null || channelMapping == null) {
            OnErrorAction.onMissingFieldMessage(event);
            return;
        }


        GuildChannelUnion channelUnion = channelMapping.getAsChannel();
        TextChannel channel = guild.getTextChannelById(channelUnion.getId());

        if (channel == null) {
            OnErrorAction.sendMessage(event, "Channel is not found", true);
            return;
        }

        // get last 100 messages
        List<Message> messages = channel.getHistory().retrievePast(100).complete();
        Map<String, Integer> messageCountByDate = new HashMap<>();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd EEE");

        for (Message message : messages) {
            OffsetDateTime timestamp = message.getTimeCreated();
            String date = dateFormat.format(Date.from(timestamp.toInstant()));
            messageCountByDate.put(date, messageCountByDate.getOrDefault(date, 0) + 1);
        }

        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(messageCountByDate.entrySet());
        sortedEntries.sort(Map.Entry.comparingByKey());

        StringBuilder statsMessage = new StringBuilder("Message Statistics for <#" + channel.getId() + ">:\n\n");
        for (Map.Entry<String, Integer> entry : sortedEntries) {
            statsMessage.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }

        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbed("Analytics", statsMessage.toString()))
                .setEphemeral(true)
                .queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        OptionData analyticsChannel = new OptionData(OptionType.CHANNEL, "channel", "Name", true);
        return Collections.singletonList(Commands.slash(AnalyticsCommandHandler.COMMAND_NAME, "Admins only")
                .addOptions(analyticsChannel));
    }
}
