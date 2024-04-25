package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.ErrorMessageHandler;
import com.github.novicezk.midjourney.bot.events.EventsManager;
import com.github.novicezk.midjourney.bot.model.GeneratedPromptData;
import com.github.novicezk.midjourney.bot.prompt.PromptGenerator;
import com.github.novicezk.midjourney.bot.queue.QueueManager;
import com.github.novicezk.midjourney.bot.utils.SeasonTracker;
import com.github.novicezk.midjourney.bot.utils.WelcomeMessageTracker;
import com.github.novicezk.midjourney.controller.SubmitController;
import com.github.novicezk.midjourney.dto.SubmitImagineDTO;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;

@Slf4j
public class GuildMemberJoinHandler {

    private SubmitController submitController;

    public GuildMemberJoinHandler(SubmitController submitController) {
        this.submitController = submitController;
    }

    public void handleGuildMemberJoin(GuildMemberJoinEvent event) {
        Member member = event.getMember();
        User user = member.getUser();

        log.debug("onGuildMemberJoin");
        if (user.getAvatarUrl() != null && !WelcomeMessageTracker.hasBeenWelcomed(user.getId())) {
            log.debug("onGuildMemberJoin hasBeenWelcomed");

            String discordAvatarUrl = CommandsUtil.getImageUrlFromDiscordAvatar(event.getUser());
            GeneratedPromptData promptData = new PromptGenerator().generatePrompt(discordAvatarUrl, event.getUser());

            SubmitImagineDTO imagineDTO = new SubmitImagineDTO();
            imagineDTO.setPrompt(promptData.getPrompt());
            processPromptData(promptData, event);
        }
    }

    private void processPromptData(GeneratedPromptData promptData, GuildMemberJoinEvent event) {
        String postText = promptData.getMessage();
        SeasonTracker.incrementGenerationCount();
        WelcomeMessageTracker.markAsWelcomed(event.getUser().getId());

        SubmitImagineDTO imagineDTO = new SubmitImagineDTO();
        imagineDTO.setPrompt(promptData.getPrompt());
        SubmitResultVO result = submitController.imagine(imagineDTO);
        if (result != null) {
            handleCommandResponse(result, postText, promptData.getPrompt(), event);
        }
    }

    private void handleCommandResponse(
            SubmitResultVO result,
            String postText,
            String prompt,
            GuildMemberJoinEvent event
    ) {
        if (result.getCode() == ReturnCode.SUCCESS || result.getCode() == ReturnCode.IN_QUEUE) {
            QueueManager.addToQueue(event.getGuild(), prompt, event.getUser().getId(), result.getResult(), postText);
            EventsManager.onWelcomeGenerate(event);
        } else {
            ErrorMessageHandler.sendMessage(
                    event.getGuild(),
                    event.getUser().getId(),
                    "Critical miss! \uD83C\uDFB2\uD83E\uDD26 \nTry again or upload new image!",
                    result.getCode() + " " + result.getDescription()
            );
            log.error("{}: {}", result.getCode(), result.getDescription());
        }
    }
}
