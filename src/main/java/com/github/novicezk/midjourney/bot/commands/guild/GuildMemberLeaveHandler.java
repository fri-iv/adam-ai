package com.github.novicezk.midjourney.bot.commands.guild;

import com.github.novicezk.midjourney.bot.events.EventsManager;
import com.github.novicezk.midjourney.bot.user.UserJoinTimeManager;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;

import java.time.Duration;
import java.time.ZonedDateTime;

@Slf4j
public class GuildMemberLeaveHandler {
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        String userId = event.getUser().getId();

        // Calculate duration of user presence in the guild
        ZonedDateTime joinTime = UserJoinTimeManager.getUserJoinTime(userId);
        String formattedDuration = "";

        if (joinTime != null) {
            ZonedDateTime leaveTime = ZonedDateTime.now();
            Duration duration = Duration.between(joinTime, leaveTime);

            formattedDuration = formatDuration(duration);
        }

        String leaveReason;
        if (event.getGuild().retrieveBanList().stream().anyMatch(ban -> ban.getUser().getId().equals(userId))) {
            leaveReason = "User has been banned";
        } else {
            leaveReason = "User left the guild";
        }

        leaveReason += formattedDuration;
        EventsManager.onMemberLeave(event, leaveReason);
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;

        StringBuilder formattedDuration = new StringBuilder();
        if (days > 0) {
            formattedDuration.append(days).append(" days, ");
        }
        if (hours > 0) {
            formattedDuration.append(hours).append(" hours, ");
        }
        if (minutes > 0 || (days == 0 && hours == 0)) {
            formattedDuration.append(minutes).append(" min");
        }

        return ", server time " + formattedDuration;
    }
}
