package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeleteMessageCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "delete-message";
    private static final Pattern MESSAGE_LINK_PATTERN = Pattern.compile("https://discord\\.com/channels/(\\d+)/(\\d+)/(\\d+).*");

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member == null || !CommandsUtil.isUserAuthorized(member)) {
            OnErrorAction.onMissingRoleMessage(event);
            return;
        }

        OptionMapping messageLink = event.getOption("message-link");
        Guild guild = event.getGuild();

        if (guild == null || messageLink == null) {
            OnErrorAction.onMissingFieldMessage(event);
            return;
        }

        Matcher matcher = MESSAGE_LINK_PATTERN.matcher(messageLink.getAsString());
        if (matcher.find()) {
            String channelId = matcher.group(2);
            guild.getTextChannelById(channelId).retrieveMessageById(matcher.group(3)).queue(
                    message -> message.delete().queue(
                            success -> event.getHook().sendMessageEmbeds(EmbedUtil.createEmbedSuccess("done")).queue(),
                            failure -> {
                                event.getHook()
                                        .sendMessageEmbeds(EmbedUtil.createEmbedWarning("Failed to delete message. Make sure you have permission to delete messages in that channel."))
                                        .queue();
                            }

                    ),
                    notFound -> event.getHook()
                            .sendMessageEmbeds(EmbedUtil.createEmbedWarning("Message not found. Make sure you've provided the correct message link."))
                            .queue()
            );
        } else {
            event.getHook()
                    .sendMessageEmbeds(EmbedUtil.createEmbedWarning("Invalid message link format. Please provide a valid Discord message link."))
                    .queue();
        }
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        OptionData link = new OptionData(OptionType.STRING, "message-link", "Copy message link to delete", true);

        return Collections.singletonList(Commands
                .slash(COMMAND_NAME, "Admins only")
                .addOptions(link));
    }
}
