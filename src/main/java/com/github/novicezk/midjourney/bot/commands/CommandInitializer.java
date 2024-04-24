package com.github.novicezk.midjourney.bot.commands;

import com.github.novicezk.midjourney.bot.commands.handlers.*;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

public class CommandInitializer {

    public static List<CommandData> initializeCommands() {
        List<CommandData> commandDataList = new ArrayList<>();

        // upload-image command
        OptionData attachment = new OptionData(OptionType.ATTACHMENT, "main_image", "Choose your image", true);
        OptionData attachment2 = new OptionData(OptionType.ATTACHMENT, "image2", "Optional image", false);
        OptionData attachment3 = new OptionData(OptionType.ATTACHMENT, "image3", "Optional image", false);
        OptionData attachment4 = new OptionData(OptionType.ATTACHMENT, "image4", "Optional image", false);
        commandDataList.add(Commands.slash(UploadImageCommandHandler.COMMAND_NAME, "Upload your image to generate something amazing!")
                .addOptions(attachment, attachment2, attachment3, attachment4));

        // contract command
        OptionData promptContract = new OptionData(OptionType.STRING, "prompt", "Prompt to use the contract command");
        OptionData idContract = new OptionData(OptionType.STRING, "task", "Task id");
        commandDataList.add(Commands.slash(ContractCommandHandler.COMMAND_NAME, "Admins only").addOptions(promptContract, idContract));

        // embed command
        OptionData embedDescription = new OptionData(OptionType.STRING, "description", "String", true);
        OptionData embedChannel = new OptionData(OptionType.CHANNEL, "channel", "Name", true);
        OptionData embedTitle = new OptionData(OptionType.STRING, "title", "String");
        OptionData embedFooter = new OptionData(OptionType.STRING, "footer", "String");
        OptionData embedColor = new OptionData(OptionType.STRING, "color", "hex #000000");
        commandDataList.add(Commands.slash(EmbedCommandHandler.COMMAND_NAME, "Admins only")
                .addOptions(embedChannel, embedDescription, embedTitle, embedFooter, embedColor));

        // other commands
        commandDataList.add(Commands.slash(GetImagesCommandHandler.COMMAND_NAME, "Get your currently uploaded images."));
        commandDataList.add(Commands.slash(GenerateCommandHandler.COMMAND_NAME, "Need some inspiration? Use this command to generate images!"));
        commandDataList.add(Commands.slash(GetLogCommandHandler.COMMAND_NAME, "Logs file"));
        commandDataList.add(Commands.slash(PingCommandHandler.COMMAND_NAME, "Default ping command(or?)"));
        commandDataList.add(Commands.slash(QueueCommandHandler.COMMAND_NAME_GET, "Check the current queue status."));
        commandDataList.add(Commands.slash(QueueCommandHandler.COMMAND_NAME_CLEAR, "Admins only"));
        commandDataList.add(Commands.slash(HelpCommandHandler.COMMAND_NAME, "View all available bot commands"));

        return commandDataList;
    }
}
