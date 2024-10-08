package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.commands.guild.PrivateMessageSender;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class AddProjectMemberCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "add-project-member";

    private final PrivateMessageSender privateMessageSender;

    public AddProjectMemberCommandHandler(PrivateMessageSender privateMessageSender) {
        this.privateMessageSender = privateMessageSender;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member == null || !CommandsUtil.isUserAuthorized(event, member)) {
            OnErrorAction.onMissingRoleMessage(event);
            return;
        }

        if (!Config.getProjectsCategory().equals(event.getChannel().asTextChannel().getParentCategoryId())) {
            OnErrorAction.sendMessage(event, "This command can't be used in this category", false);
            return;
        }

        OptionMapping memberOption = event.getOption("member");
        OptionMapping roleOption = event.getOption("role");
        OptionMapping customRoleOption = event.getOption("custom-role");

        if (memberOption == null || roleOption == null) {
            OnErrorAction.onMissingFieldMessage(event);
            return;
        }

        Member selectedMember = memberOption.getAsMember();
        if (selectedMember == null) {
            event.getHook().sendMessage("Unable to find the selected member.").setEphemeral(true).queue();
            OnErrorAction.sendMessage(event, "Unable to find the selected member.", true);
            return;
        }

        String selectedRole = roleOption.getAsString();
        if (selectedRole.equals("custom") && customRoleOption == null) {
            selectedRole = "guest";
        }
        String roleDescription = switch (selectedRole) {
            case "artist" -> "an **Artist**";
            case "manager" -> "a **Manager**";
            case "custom" -> customRoleOption.getAsString();
            default -> "a **Guest**";
        };

        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbedSuccess("done")).queue();

        TextChannel channel = event.getChannel().asTextChannel();
        channel.getManager().putMemberPermissionOverride(
                        selectedMember.getIdLong(),
                        EnumSet.of(Permission.VIEW_CHANNEL), null)
                .queue();

        channel.sendMessageEmbeds(EmbedUtil.createEmbedSuccess(
                        "New Member Added",
                        String.format("<@%s> has joined as %s", selectedMember.getId(), roleDescription)))
                .queue();

        String channelLink = "<#" + channel.getId() + ">";
        sendPrivateMessageToMember(selectedMember.getUser(), channelLink, roleDescription);
    }

    private void sendPrivateMessageToMember(User user, String channelLink, String roleDescription) {
        privateMessageSender.sendMessageEmbedToUser(user, String.format("""
                            %s
                            You’ve been added to this project as %s
                            """,
                channelLink,
                roleDescription
        ));
    }


    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        OptionData member = new OptionData(OptionType.USER, "member", "Select the new project member", true);

        OptionData role = new OptionData(OptionType.STRING, "role", "Assign a role to the member", true)
                .addChoice("Artist", "artist")
                .addChoice("Guest", "guest")
                .addChoice("Manager", "manager")
                .addChoice("Custom Role (enter manually)", "custom");

        OptionData customRole = new OptionData(OptionType.STRING, "custom-role", "ex.: a Guest", false);

        return Collections.singletonList(Commands
                .slash(COMMAND_NAME, "Add new member to the project")
                .addOptions(member, role, customRole));
    }
}
