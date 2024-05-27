package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.commands.guild.PrivateMessageSender;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.events.EventsManager;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;

public class MuteCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "mute";

    private final PrivateMessageSender privateMessageSender;

    public MuteCommandHandler(PrivateMessageSender privateMessageSender) {
        this.privateMessageSender = privateMessageSender;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member == null || !CommandsUtil.isUserAuthorized(member)) {
            OnErrorAction.onMissingRoleMessage(event);
            return;
        }

        OptionMapping userOption = event.getOption("user");
        OptionMapping reason = event.getOption("reason");
        Guild guild = event.getGuild();

        if (guild == null || userOption == null || reason == null) {
            OnErrorAction.onMissingFieldMessage(event);
            return;
        }

        Member mutedMember = userOption.getAsMember();
        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbedSuccess("done")).queue();
        Role muteRole = guild.getRoleById(Config.getRoleMuted());
        // Mute the user
        if (muteRole != null && mutedMember != null) {
            guild.addRoleToMember(mutedMember, muteRole).queue();

            privateMessageSender.notifyMutedMember(mutedMember);
            EventsManager.onMutedMember(event, mutedMember, reason.getAsString());
        }
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        OptionData user = new OptionData(OptionType.MENTIONABLE, "user", "Participants", true);
        OptionData reason = new OptionData(OptionType.STRING, "reason", "Mute reason", true);

        return Collections.singletonList(Commands
                .slash(COMMAND_NAME, "Admins only")
                .addOptions(user, reason));
    }
}
