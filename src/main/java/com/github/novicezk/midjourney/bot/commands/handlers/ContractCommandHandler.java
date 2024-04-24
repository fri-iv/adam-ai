package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.model.CharacterStrength;
import com.github.novicezk.midjourney.bot.utils.ColorUtil;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.github.novicezk.midjourney.controller.SubmitController;
import com.github.novicezk.midjourney.dto.SubmitImagineDTO;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

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
        if (member == null || !isAuthorized(member)) {
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

    private boolean isAuthorized(Member member) {
        String adminsRoleId = Config.getAdminsRoleId();
        String godfatherId = Config.getGodfatherId();
        return member.getRoles().stream()
                .anyMatch(role -> role.getId().equals(adminsRoleId) || role.getId().equals(godfatherId));
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
        if ("test".equals(task)) {
            event.getHook().sendMessageEmbeds(List.of(EmbedUtil.createEmbed("test command"))).queue();
        } else if ("faq".equals(task) && event.getGuild() != null) {
            handleFaqCommand(event);
            event.getHook().sendMessageEmbeds(EmbedUtil.createEmbed("done")).queue();
        } else if ("rare".equals(task)) {
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
        } else {
            event.getHook().sendMessageEmbeds(List.of(EmbedUtil.createEmbed("Command not found"))).queue();
        }
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