package com.github.novicezk.midjourney.bot.commands.guild;

import com.github.novicezk.midjourney.ReturnCode;
import com.github.novicezk.midjourney.bot.commands.CommandsUtil;
import com.github.novicezk.midjourney.bot.error.ErrorMessageHandler;
import com.github.novicezk.midjourney.bot.events.EventsManager;
import com.github.novicezk.midjourney.bot.model.GeneratedPromptData;
import com.github.novicezk.midjourney.bot.prompt.PromptGenerator;
import com.github.novicezk.midjourney.bot.queue.QueueManager;
import com.github.novicezk.midjourney.bot.user.UserJoinTimeManager;
import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.SeasonTracker;
import com.github.novicezk.midjourney.bot.utils.WelcomeMessageTracker;
import com.github.novicezk.midjourney.controller.SubmitController;
import com.github.novicezk.midjourney.dto.SubmitImagineDTO;
import com.github.novicezk.midjourney.result.SubmitResultVO;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.List;

@Slf4j
public class GuildMemberJoinHandler {

    private final SubmitController submitController;

    public GuildMemberJoinHandler(SubmitController submitController) {
        this.submitController = submitController;
    }

    public void handleGuildMemberJoin(GuildMemberJoinEvent event) {
        Member member = event.getMember();
        User user = member.getUser();

        handleWelcomeMessage(user);
        handleGenerateWelcomeArt(user, event);
        cacheAllMembers(event.getGuild().getMembers());
    }

    private void cacheAllMembers(List<Member> members) {
        if (!members.isEmpty()) {
            for (Member member : members) {
                String userId = member.getId();
                UserJoinTimeManager.addUserJoinTime(userId);
            }
        }
    }

    // send welcome message to DMs
    private void handleWelcomeMessage(User user) {
        String welcomeMessage = String.format("""
               Hey there! I'm Adam, the **Avatar Portal** guild's bot.
                               \s
               Want to create your own avatar? Send me your ideas or reach out to our manager <@%s>
               Click the button and we'll get in touch ASAP. Enjoy your stay!
               """,
                Config.getContactManagerId()
        );

        Button createButton = Button.success("wel:create-avatar", "Create avatar \uD83D\uDCAB");
        user.openPrivateChannel().queue(privateChannel -> privateChannel
                .sendMessage(welcomeMessage)
                .addActionRow(createButton)
                .queue());
    }

    private void handleGenerateWelcomeArt(User user, GuildMemberJoinEvent event) {
        if (user.getAvatarUrl() != null && !WelcomeMessageTracker.hasBeenWelcomed(user.getId())) {
            String discordAvatarUrl = CommandsUtil.getImageUrlFromDiscordAvatar(user);
            GeneratedPromptData promptData = new PromptGenerator().generatePrompt(discordAvatarUrl, user);

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
