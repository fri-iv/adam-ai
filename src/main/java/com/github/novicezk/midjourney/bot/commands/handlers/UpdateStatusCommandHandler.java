package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.utils.ChannelUtil;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.github.novicezk.midjourney.bot.webhook.TrelloManager;
import com.julienvey.trello.domain.TList;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.util.Collections;
import java.util.List;

public class UpdateStatusCommandHandler implements CommandSelectHandler {
    public static final String COMMAND_NAME = "update-status";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member == null || !CommandsUtil.isUserAuthorized(event, member)) {
            OnErrorAction.onMissingRoleMessage(event);
            return;
        }


        List<TList> trelloColumns = TrelloManager.getBoardColumns();
        if (trelloColumns.isEmpty()) {
            event.getHook().sendMessage("No Trello columns found!").setEphemeral(true).queue();
            return;
        }

        StringSelectMenu.Builder menu = StringSelectMenu.create("trello-column-select")
                .setPlaceholder("Select a status")
                .setRequiredRange(1, 1); // Choose only one option

        // Add each column as an element of the list
        for (TList column : trelloColumns) {
            menu.addOption(column.getName(), column.getId());
        }

        event.getHook().sendMessage("Update the Project Status:")
                .addActionRow(menu.build())
                .setEphemeral(true)
                .queue();

        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbedSuccess("done")).setEphemeral(true).queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        return Collections.singletonList(Commands.slash(COMMAND_NAME, "Admins only"));
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("trello-column-select")) {

            event.deferReply().setEphemeral(true).queue();

            String selectedColumnId = event.getValues().get(0);
            List<TList> trelloColumns = TrelloManager.getBoardColumns();
            String selectedColumnName = trelloColumns.stream()
                    .filter(column -> column.getId().equals(selectedColumnId))
                    .findFirst()
                    .map(TList::getName)
                    .orElse("Unknown");

            event.getHook().sendMessageEmbeds(EmbedUtil.createEmbedSuccess(
                            String.format("Project status updated: **%s**", selectedColumnName)))
                    .setEphemeral(true).queue();

            ChannelUtil.updateChannelTopic(event.getChannel().asTextChannel(), selectedColumnName);
        }
    }
}
