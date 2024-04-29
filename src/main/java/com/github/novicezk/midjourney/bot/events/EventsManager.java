package com.github.novicezk.midjourney.bot.events;

import com.github.novicezk.midjourney.bot.events.model.ButtonEventData;
import com.github.novicezk.midjourney.bot.events.model.CommandEventData;
import com.github.novicezk.midjourney.bot.events.model.ErrorEventData;
import com.github.novicezk.midjourney.bot.events.model.LeaveEventData;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.jetbrains.annotations.Nullable;

public class EventsManager {
    public static void onCommand(SlashCommandInteractionEvent event) {
        String logText = "c:" + event.getName();
        EventsStorage.logCommandInvocation(logText, event.getUser().getId());
        MixpanelManager.trackEvent(new CommandEventData(event));
        sendLogToDiscord(event.getGuild(), event.getUser().getId(), event.getUser().getName(), logText);
    }

    public static void onButtonClick(ButtonInteractionEvent event) {
        String logText = "b:" + event.getComponentId();
        EventsStorage.logButtonInteraction(logText, event.getUser().getId());
        MixpanelManager.trackEvent(new ButtonEventData(event));
        sendLogToDiscord(event.getGuild(), event.getUser().getId(), event.getUser().getName(), logText);
    }

    public static void onWelcomeGenerate(GuildMemberJoinEvent event) {
        String logText = "Welcome generate";
        EventsStorage.logButtonInteraction(logText, event.getUser().getId());
        MixpanelManager.trackEvent(new CommandEventData(event, logText));
        sendLogToDiscord(event.getGuild(), event.getUser().getId(), event.getUser().getName(), logText);
    }

    public static void onMemberLeave(GuildMemberRemoveEvent event, String reason) {
        MixpanelManager.trackEvent(new LeaveEventData(event, reason));
        sendLogToDiscord(event.getGuild(), event.getUser().getId(), event.getUser().getName(), reason);
    }

    public static void onErrorEvent(String userId, String failReason) {
        MixpanelManager.trackEvent(new ErrorEventData(userId, failReason));
    }

    private static void sendLogToDiscord(@Nullable Guild guild, String userId, String username, String text) {
        if (guild == null || guild.getTextChannelById(Config.getLogsChannel()) == null) {
            return;
        }

        TextChannel logChannel = guild.getTextChannelById(Config.getLogsChannel());
        logChannel.sendMessageEmbeds(EmbedUtil.createEmbed(String.format("%s — <@%s>, %s", text, userId, username))).queue();
    }
}
