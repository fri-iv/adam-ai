package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.github.novicezk.midjourney.bot.utils.RoleUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DevTeamCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "dev-team";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member == null || !CommandsUtil.isUserAuthorized(event, member)) {
            OnErrorAction.onMissingRoleMessage(event);
            return;
        }

        String channelId = Config.getDevTeamChannel();
        Guild guild = event.getGuild();

        if (guild == null || guild.getTextChannelById(channelId) == null) {
            OnErrorAction.onMissingFieldMessage(event);
            return;
        }

        updateRolesMessage(guild, guild.getTextChannelById(channelId));

        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbedSuccess("Done")).setEphemeral(true).queue();
    }

    public void updateRolesMessage(Guild guild, TextChannel channel) {
        channel.getHistory().retrievePast(1)
                .queue(messages -> {
                    if (!messages.isEmpty()) {
                        Message lastMessage = messages.get(0);
                        lastMessage.delete().queue();
                    }
                    sendRolesMessage(guild, channel);

                });
    }


    private void sendRolesMessage(Guild guild, TextChannel channel) {
        guild.loadMembers().onSuccess(members -> {
            List<Member> managers = RoleUtil.getMembersWithRole(guild, members, Config.getDevRolesManager());
            List<Member> mentors = RoleUtil.getMembersWithRole(guild, members, Config.getDevRolesMentor());
            List<Member> artists = RoleUtil.getMembersWithRole(guild, members, Config.getDevRolesArtist());
            List<Member> talents = RoleUtil.getMembersWithRole(guild, members, Config.getDevRolesTalent());

            channel.sendMessageEmbeds(EmbedUtil.createEmbedCute(
                    "Roles in Our Team",
                    String.format("""
                        **Role:** <@&%s>
                        **Members:** %s
                        
                        - Handles payments
                        - Has access to all projects
                        - Assigns tasks
                        
                        **Role:** <@&%s>
                        **Members:** %s
                        
                        - Administers the artists' guild
                        - Reviews team working conditions
                        - Receives a share of the income
                        
                        **Role:** <@&%s>
                        **Members:** %s
                        
                        - Accesses monthly budget reports
                        - Creates content
                        - Moderates the clients' guild
                        
                        **Role:** <@&%s>
                        **Members:** %s
                        
                        - Can review the studio's projects
                        
                        """,
                            Config.getDevRolesManager(),
                            formatMembersList(managers),
                            Config.getDevRolesMentor(),
                            formatMembersList(mentors),
                            Config.getDevRolesArtist(),
                            formatMembersList(artists),
                            Config.getDevRolesTalent(),
                            formatMembersList(talents)
                    )
            )).queue();
        });
    }

    public static String formatMembersList(List<Member> members) {
        return members.stream()
                .map(member -> "<@" + member.getId() + ">")
                .collect(Collectors.joining(", "));
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        return Collections.singletonList(Commands.slash(COMMAND_NAME, "Admins only"));
    }
}
