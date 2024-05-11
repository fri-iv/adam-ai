package com.github.novicezk.midjourney.bot.commands.util;

import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.model.GeneratedPromptData;
import com.github.novicezk.midjourney.bot.prompt.PromptGenerator;
import com.github.novicezk.midjourney.bot.queue.QueueManager;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.SeasonTracker;
import com.github.novicezk.midjourney.controller.SubmitController;
import com.github.novicezk.midjourney.dto.SubmitImagineDTO;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.List;

public class GeneratingRequestHandler {
    private final SubmitController submitController;

    public GeneratingRequestHandler(SubmitController submitController) {
        this.submitController = submitController;
    }

    public void doReRoll(ButtonInteractionEvent event) {
        if (!hasTesterOrAdminRole(event.getMember())) {
            OnErrorAction.onMissingTestersRoleMessage(event);
            return;
        }

        processInteraction(event);
    }

    public void doImagine(SlashCommandInteractionEvent event) {
        if (!hasTesterOrAdminRole(event.getMember())) {
            OnErrorAction.onMissingTestersRoleMessage(event);
            return;
        }

        processInteraction(event);
    }

    private void processInteraction(ButtonInteractionEvent event) {
        List<String> imageUrls = getUserImageUrls(event.getUser());
        String title = generateTitle(imageUrls.isEmpty(), "");

        if (imageUrls.isEmpty()) {
            String discordAvatarUrl = getImageUrlFromDiscordAvatar(event.getUser());
            if (discordAvatarUrl != null) {
                imageUrls.add(discordAvatarUrl);
            }
        }

        if (imageUrls.isEmpty()) {
            OnErrorAction.onImageErrorMessage(event);
            return;
        }

        if (QueueManager.reachLimitQueue(event.getUser().getId())) {
            OnErrorAction.onQueueFullMessage(event);
            return;
        }

        GeneratedPromptData promptData = new PromptGenerator().generatePrompt(imageUrls, event.getUser());
        processPromptData(promptData, title, event);
    }

    private void processInteraction(SlashCommandInteractionEvent event) {
        List<String> imageUrls = getUserImageUrls(event.getUser());
        String title = generateTitle(imageUrls.isEmpty(), "");

        if (imageUrls.isEmpty()) {
            String discordAvatarUrl = getImageUrlFromDiscordAvatar(event.getUser());
            if (discordAvatarUrl != null) {
                imageUrls.add(discordAvatarUrl);
            }
        }

        if (imageUrls.isEmpty()) {
            OnErrorAction.onImageErrorMessage(event);
            return;
        }

        if (QueueManager.reachLimitQueue(event.getUser().getId())) {
            OnErrorAction.onQueueFullMessage(event);
            return;
        }

        GeneratedPromptData promptData = new PromptGenerator().generatePrompt(imageUrls, event.getUser());
        processPromptData(promptData, title, event);
    }

    private boolean hasTesterOrAdminRole(Member member) {
        return member != null &&
                (hasRole(member, Config.getRoleTester()) || hasRole(member, Config.getAdminsRoleId()) || hasRole(member, Config.getGodfatherId()));
    }

    private boolean hasRole(Member member, String roleId) {
        return member.getRoles().stream().anyMatch(r -> r.getId().equals(roleId));
    }

    private List<String> getUserImageUrls(User user) {
        return CommandsUtil.getUserUrls(user.getId());
    }

    private String generateTitle(boolean isEmpty, String defaultTitle) {
        return isEmpty ? defaultTitle : "";
    }

    private String getImageUrlFromDiscordAvatar(User user) {
        return CommandsUtil.getImageUrlFromDiscordAvatar(user);
    }

    private void processPromptData(GeneratedPromptData promptData, String title, ButtonInteractionEvent event) {
        String postText = title + promptData.getMessage();
        SeasonTracker.incrementGenerationCount();

        SubmitImagineDTO imagineDTO = new SubmitImagineDTO();
        imagineDTO.setPrompt(promptData.getPrompt());
        SubmitResultVO result = submitController.imagine(imagineDTO);
        if (result != null) {
            CommandsUtil.handleCommandResponse(result, postText, promptData.getPrompt(), event);
        } else {
            OnErrorAction.onImageErrorMessage(event);
        }
    }

    private void processPromptData(GeneratedPromptData promptData, String title, SlashCommandInteractionEvent event) {
        String postText = title + promptData.getMessage();
        SeasonTracker.incrementGenerationCount();

        SubmitImagineDTO imagineDTO = new SubmitImagineDTO();
        imagineDTO.setPrompt(promptData.getPrompt());
        SubmitResultVO result = submitController.imagine(imagineDTO);
        if (result != null) {
            CommandsUtil.handleCommandResponse(result, postText, promptData.getPrompt(), event);
        } else {
            OnErrorAction.onImageErrorMessage(event);
        }
    }
}