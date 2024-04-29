package com.github.novicezk.midjourney.bot.events.model;

import com.github.novicezk.midjourney.bot.events.EventUtil;
import com.github.novicezk.midjourney.bot.utils.SeasonTracker;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import org.json.JSONObject;

public class LeaveEventData implements MixpanelEventData {
    private static final String EVENT_NAME = "leave_guild";
    private final JSONObject data = new JSONObject();

    public LeaveEventData(GuildMemberRemoveEvent event, String reason) {
        data.put("user-id", event.getUser().getId());
        data.put("user-name", event.getUser().getName());
        data.put("user-name-global", event.getUser().getGlobalName());
        data.put("reason", reason);
        data.put("type", EVENT_NAME);
        data.put("version", SeasonTracker.getCurrentSeasonVersion() + "." + SeasonTracker.getCurrentGenerationCount());
        data.put("season", SeasonTracker.getCurrentSeasonVersion());

        Member member = event.getMember();
        if (member != null) {
            data.put("roles", EventUtil.rolesToString(member.getRoles()));
        }
    }

    @Override
    public String getEventName() {
        return EVENT_NAME;
    }

    @Override
    public String getDistinctId() {
        return DistinctId.APP_NAME.getName();
    }

    @Override
    public JSONObject getData() {
        return data;
    }
}
