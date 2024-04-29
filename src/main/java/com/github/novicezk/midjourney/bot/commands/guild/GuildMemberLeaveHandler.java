package com.github.novicezk.midjourney.bot.commands.guild;

import com.github.novicezk.midjourney.bot.events.EventsManager;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;

public class GuildMemberLeaveHandler {
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        String userId = event.getUser().getId();

        String leaveReason;
        if (event.getGuild().retrieveBanList().stream().anyMatch(ban -> ban.getUser().getId().equals(userId))) {
            leaveReason = "The user has been banned";
        } else {
            leaveReason = "The user left the guild";
        }

        EventsManager.onMemberLeave(event, leaveReason);
    }
}
