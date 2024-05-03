package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.model.CharacterStrength;
import com.github.novicezk.midjourney.bot.model.images.ImageComposed;
import com.github.novicezk.midjourney.bot.utils.ColorUtil;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.github.novicezk.midjourney.bot.utils.ImageComposer;
import com.github.novicezk.midjourney.controller.SubmitController;
import com.github.novicezk.midjourney.dto.SubmitImagineDTO;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;

import java.util.List;

@Slf4j
public class ContractCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "contract";

    private final SubmitController submitController;

    public ContractCommandHandler(SubmitController submitController) {
        this.submitController = submitController;
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();

        Member member = event.getMember();
        if (member == null || !CommandsUtil.isUserAuthorized(member)) {
            OnErrorAction.onMissingRoleMessage(event);
            return;
        }

        OptionMapping promptMapping = event.getOption("prompt");
        OptionMapping taskMapping = event.getOption("task");
        if (promptMapping != null && !promptMapping.getAsString().isEmpty()) {
            handlePrompt(event, promptMapping.getAsString());
        } else if (taskMapping != null && !taskMapping.getAsString().isEmpty()) {
            handleTask(event, taskMapping.getAsString());
        } else {
            OnErrorAction.onMissingFieldMessage(event);
        }
    }

    private void handlePrompt(SlashCommandInteractionEvent event, String prompt) {
        SubmitImagineDTO imagineDTO = new SubmitImagineDTO();
        imagineDTO.setPrompt(prompt);
        SubmitResultVO result = submitController.imagine(imagineDTO);
        if (result != null) {
            CommandsUtil.handleCommandResponse(result, "`" + prompt + "`", prompt, event);
        } else {
            OnErrorAction.onImageErrorMessage(event);
        }
    }

    private void handleTask(SlashCommandInteractionEvent event, String task) {
        switch (task) {
            case "test":
                event.getHook().sendMessageEmbeds(List.of(EmbedUtil.createEmbed("test command"))).queue();
                break;
            case "faq-ai":
                handleFaqCommand(event);
                event.getHook().sendMessageEmbeds(EmbedUtil.createEmbedSuccess("done")).queue();
                break;
            case "rare":
                handleRareCommand(event);
                break;
            case "create":
                handleCreateAvatarCommand(event);
                break;
            case "faq-avatar":
                handleFaqAvatarCommand(event);
                break;
            case "welcome-msg":
                handleWelcomeMessageCommand(event);
                break;
            default:
                event.getHook().sendMessageEmbeds(List.of(EmbedUtil.createEmbed("Command not found"))).queue();
                break;
        }
    }

    private void handleWelcomeMessageCommand(SlashCommandInteractionEvent event) {
        String channelId = Config.getDebugChannel();
//        String channelId = Config.getWelcomeChannel();

        String avatarUrl = event.getUser().getAvatarUrl() != null ? event.getUser().getAvatarUrl() : event.getUser().getDefaultAvatarUrl();
        if (event.getGuild() == null || event.getGuild().getTextChannelById(channelId) == null || avatarUrl == null) {
            OnErrorAction.onDefaultMessage(event);
            return;
        }
        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbedSuccess("Done")).queue();

        Button createButton = Button.success("welch:create-avatar", "Create avatar \uD83D\uDCAB");
        TextChannel channel = event.getGuild().getTextChannelById(channelId);
        try {
            ImageComposed composeImage = ImageComposer.composeImage(avatarUrl);
            channel.sendMessage(String.format("Hello <@%s>", event.getUser().getId()))
                    .addEmbeds(EmbedUtil.createEmbed(
                            ":wave:  **Welcome to the server!**", String.format("""
                                     :large_blue_diamond: How do I begin?
                                     :small_blue_diamond: <#%s>
                                     :small_blue_diamond: See our showcases for model quality
                                      \s
                                     :large_orange_diamond: What's next?
                                     :small_orange_diamond: <#%s>
                                     :small_orange_diamond: Stay tuned for updates and projects
                                      \s
                                     :point_right: <#%s>
                                     :small_orange_diamond: Please go through and read rules
                                     :small_orange_diamond: After that check roles freely
                                      \s
                                     :large_blue_diamond: Ready for your own model?
                                     :small_blue_diamond: Click the button and we'll reach out!
                                      \s
                                     Welcome and enjoy!
                                    """,
                                    Config.getShowcasesChannel(),
                                    Config.getUpdatesChannel(),
                                    Config.getRulesChannel()
                            ), "Do it! Click it! Really!", composeImage.getAvverageColor(), composeImage.getFileUrl()
                    ))
                    .addFiles(FileUpload.fromData(composeImage.getImageFile()))
                    .addActionRow(createButton)
                    .queue();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void handleFaqAvatarCommand(SlashCommandInteractionEvent event) {
        String channelId = Config.getDebugChannel();
//        String channelId = "1091727782498816010";
        if (event.getGuild() == null || event.getGuild().getTextChannelById(channelId) == null) {
            OnErrorAction.onDefaultMessage(event);
            return;
        }
        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbedSuccess("Done")).queue();

        Button createButton = Button.success("faq:create-avatar", "Create avatar \uD83D\uDCAB");
        TextChannel channel = event.getGuild().getTextChannelById(channelId);
        channel.sendMessageEmbeds(EmbedUtil.createEmbed(
                        "Frequently Asked Questions",
                        """
                                **Q: How do I get started on my project?**
                                 A: To begin your project <#1092429060270985247> provide us with any reference images and information you have. We'll then give you a quote and timeline for the project.
                                 
                                 **Q: How much does a custom avatar cost?**
                                 A: The cost varies based on factors like design complexity, revisions needed and additional features. We provide personalized quotes for each project considering your requirements and budget.
                                 
                                 **Q: When do I need to pay for the avatar?**
                                 A: We require a 50% deposit upfront to start with the remaining balance due upon completion.
                                 
                                 **Q: How will I receive the avatar?**
                                 A: Once complete you can choose to receive Unity materials and a tutorial for self-uploading or have us upload it to a private world. Just let us know your preference!
                                 
                                 **Q: What payment options are available?**
                                 A: We accept PayPal, ko-fi and cryptocurrency. For PayPal we'll provide necessary details. For cryptocurrency specify your preference and we'll supply the corresponding wallet address.
                                 
                                 **Q: How long will my project take?**
                                 A: The timeline depends on factors like project complexity and workload. We'll provide an estimated timeline at the project's start and keep you informed of any changes.
                               \s""",
                        null,
                        ColorUtil.getWarningColor()))
                .addActionRow(createButton)
                .queue();
    }

    private void handleCreateAvatarCommand(SlashCommandInteractionEvent event) {
        String channelId = Config.getDebugChannel();
//        String channelId = "1092429060270985247";
        if (event.getGuild() == null || event.getGuild().getTextChannelById(channelId) == null) {
            OnErrorAction.onDefaultMessage(event);
            return;
        }
        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbedSuccess("Done")).queue();

        Button createButton = Button.success("ch:create-avatar", "Create avatar \uD83D\uDCAB");
        TextChannel channel = event.getGuild().getTextChannelById(channelId);
        channel.sendMessage(String.format(
                        "Hey there! Interested in creating your own avatar? Share your ideas with <@%s> in DMs or click this button and we'll reach out to you shortly!",
                        Config.getContactManagerId()
                ))
                .addActionRow(createButton)
                .queue();
    }

    private void handleRareCommand(SlashCommandInteractionEvent event) {
        int[] counts = new int[CharacterStrength.values().length];

        for (int i = 0; i < 100; i++) {
            CharacterStrength strength = CharacterStrength.getRandomStrength();
            counts[strength.ordinal()]++;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (CharacterStrength strength : CharacterStrength.values()) {
            stringBuilder.append(strength.getStrengthName()).append(": ").append(counts[strength.ordinal()]).append(", ");
        }
        stringBuilder.setLength(stringBuilder.length() - 2);
        event.getHook().sendMessageEmbeds(EmbedUtil.createEmbed("Character Strength Counts", stringBuilder.toString())).queue();
    }

    private void handleFaqCommand(SlashCommandInteractionEvent event) {
        Button roleButton = Button.primary("testers", "Join Testers");
        TextChannel channel = event.getGuild().getTextChannelById(Config.getFaqChannel());
        channel.sendMessageEmbeds(EmbedUtil.createEmbed(
                        "Welcome!",
                        "**1. What does our bot do?**\n" +
                                "Our AI image generator is here to inspire your avatar ideas! Our bot welcomes new members with a generated image that might suit you but if you have your own idea for your avatar feel free to share it with us!\n\n" +
                                "**2. How does it work?**\n" +
                                "Simply use the command `/generate` anytime.\n" +
                                "For all commands just type `/help`.\n\n" +
                                "**3. Who can use it?**\n" +
                                "Currently only <@&" + Config.getRoleVanguard() + "> and <@&" + Config.getRoleVerifiedClient() + "> have access.\n" +
                                "\n" +
                                "To gain access click the button and get the <@&" + Config.getRoleTester() + "> role.",
                        null,
                        ColorUtil.getWarningColor())
                ).addActionRow(roleButton)
                .queue();
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }
}