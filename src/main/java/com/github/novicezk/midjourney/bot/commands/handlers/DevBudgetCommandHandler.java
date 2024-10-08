package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.github.novicezk.midjourney.bot.utils.FileUtil;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;

public class DevBudgetCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "budget-report";


    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member == null || !CommandsUtil.isUserAuthorized(event, member)) {
            OnErrorAction.onMissingRoleMessage(event);
            return;
        }

        String channelId = Config.getDevBudgetChannel();
        Guild guild = event.getGuild();
        OptionMapping monthMapping = event.getOption("month");
        OptionMapping fileMapping = event.getOption("file");

        if (guild == null
                || monthMapping == null
                || fileMapping == null
                || guild.getTextChannelById(channelId) == null
        ) {
            OnErrorAction.onMissingFieldMessage(event);
            return;
        }

        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbedSuccess("Done")).setEphemeral(true).queue();
        guild.getTextChannelById(channelId).sendMessageEmbeds(EmbedUtil.createEmbedSuccess(
                monthMapping.getAsString() + " Report",
                "To access the report go to https://zenmoney.ru/a/#import and upload the file",
                "prepared by " + member.getEffectiveName()
        )).addFiles(FileUtil.getFilesFromAttachment(fileMapping.getAsAttachment(), "avihero.csv")).queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        OptionData month = new OptionData(OptionType.STRING, "month", "Select the month", true)
                .addChoices(
                        new Command.Choice("January", "January"),
                        new Command.Choice("February", "February"),
                        new Command.Choice("March", "March"),
                        new Command.Choice("April", "April"),
                        new Command.Choice("May", "May"),
                        new Command.Choice("June", "June"),
                        new Command.Choice("July", "July"),
                        new Command.Choice("August", "August"),
                        new Command.Choice("September", "September"),
                        new Command.Choice("October", "October"),
                        new Command.Choice("November", "November"),
                        new Command.Choice("December", "December")
                );

        OptionData file = new OptionData(OptionType.ATTACHMENT, "file", "Import the file", true);

        return Collections.singletonList(Commands
                .slash(COMMAND_NAME, "Admins only")
                .addOptions(month, file));
    }
}
