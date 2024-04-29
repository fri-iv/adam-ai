package com.github.novicezk.midjourney.bot.commands;

import com.github.novicezk.midjourney.bot.commands.guild.*;
import com.github.novicezk.midjourney.bot.commands.handlers.*;
import com.github.novicezk.midjourney.bot.events.EventsManager;
import com.github.novicezk.midjourney.bot.queue.QueueManager;
import com.github.novicezk.midjourney.bot.user.UserJoinTimeManager;
import com.github.novicezk.midjourney.controller.SubmitController;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CommandsManager extends ListenerAdapter {
    private final ButtonInteractionHandler buttonInteractionHandler;
    private final GuildMemberJoinHandler guildMemberJoinHandler;
    private final GuildMemberLeaveHandler guildMemberLeaveHandler;
    private final MessageReceivedHandler messageReceivedHandler;
    private final List<CommandHandler> commandHandlers;

    public CommandsManager(SubmitController submitController) {
        PrivateMessageSender privateMessageSender = new PrivateMessageSender();

        this.buttonInteractionHandler = new ButtonInteractionHandler(privateMessageSender);
        this.messageReceivedHandler = new MessageReceivedHandler(privateMessageSender);
        this.guildMemberJoinHandler = new GuildMemberJoinHandler(submitController);
        this.commandHandlers = initializeCommandHandlers(submitController);
        this.guildMemberLeaveHandler = new GuildMemberLeaveHandler();
    }

    private List<CommandHandler> initializeCommandHandlers(SubmitController submitController) {
        List<CommandHandler> handlers = new ArrayList<>();
        handlers.add(new ContractCommandHandler(submitController));
        handlers.add(new GenerateCommandHandler(submitController));
        handlers.add(new UploadImageCommandHandler());
        handlers.add(new GetImagesCommandHandler());
        handlers.add(new AnalyticsCommandHandler());
        handlers.add(new GetLogCommandHandler());
        handlers.add(new QueueCommandHandler());
        handlers.add(new EmbedCommandHandler());
        handlers.add(new PingCommandHandler());
        handlers.add(new HelpCommandHandler());
        return handlers;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        messageReceivedHandler.onMessageReceived(event);
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        EventsManager.onCommand(event);
        for (CommandHandler handler : commandHandlers) {
            if (handler.supports(event.getName())) {
                handler.handle(event);
                return;
            }
        }
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commandData = CommandInitializer.initializeCommands();
        event.getGuild().updateCommands().addCommands(commandData).queue();

        // clear queue on start
        QueueManager.clearQueue(event.getGuild());
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        buttonInteractionHandler.handleButtonInteraction(event);
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        UserJoinTimeManager.addUserJoinTime(event.getUser().getId());
        guildMemberJoinHandler.handleGuildMemberJoin(event);
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        guildMemberLeaveHandler.onGuildMemberRemove(event);
    }
}
