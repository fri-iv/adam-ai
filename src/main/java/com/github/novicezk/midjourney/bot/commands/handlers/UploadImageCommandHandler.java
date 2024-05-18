package com.github.novicezk.midjourney.bot.commands.handlers;

import com.github.novicezk.midjourney.bot.error.OnErrorAction;
import com.github.novicezk.midjourney.bot.images.ImageBBUploader;
import com.github.novicezk.midjourney.bot.images.ImageStorage;
import com.github.novicezk.midjourney.bot.model.images.ImageResponse;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UploadImageCommandHandler implements CommandHandler {
    public static final String COMMAND_NAME = "upload-image";

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        // Defer reply to avoid timeout
        event.deferReply().setEphemeral(true).queue();

        OptionMapping mainImageOption = event.getOption("main_image");
        if (mainImageOption != null && mainImageOption.getAsAttachment().isImage()) {
            List<String> imageUrls = extractImageUrls(event);
            if (!imageUrls.isEmpty()) {
                ImageStorage.addImageUrl(event.getUser().getId(), imageUrls);
                event.getHook().sendMessageEmbeds(
                                EmbedUtil.createEmbedSuccess("Your images are in! Now you can use `/generate` to start generating characters or try `/get-images` to see what you've uploaded."))
                        .setEphemeral(true)
                        .queue();
            } else {
                OnErrorAction.onImageValidateErrorMessage(event);
            }
        } else {
            OnErrorAction.onImageValidateErrorMessage(event);
        }
    }

    private List<String> extractImageUrls(SlashCommandInteractionEvent event) {
        List<String> imageUrls = new ArrayList<>();
        OptionMapping mainImageOption = event.getOption("main_image");
        Message.Attachment mainImage = mainImageOption.getAsAttachment();

        ImageResponse uploadedImageResponse = ImageBBUploader.uploadImageNew(mainImage.getUrl());
        if (uploadedImageResponse != null && uploadedImageResponse.getData().getUrl() != null) {
            imageUrls.add(uploadedImageResponse.getData().getUrl());
        }

        for (int i = 2; i <= 4; i++) {
            OptionMapping imageOption = event.getOption("image" + i);
            if (imageOption != null && imageOption.getAsAttachment().isImage()) {
                Message.Attachment attachment = imageOption.getAsAttachment();
                ImageResponse response = ImageBBUploader.uploadImageNew(attachment.getUrl());
                if (response != null && response.getData().getUrl() != null) {
                    imageUrls.add(response.getData().getUrl());
                }
            }
        }
        return imageUrls;
    }

    @Override
    public boolean supports(String eventName) {
        return COMMAND_NAME.equals(eventName);
    }

    @Override
    public List<CommandData> getCommandData() {
        OptionData attachment = new OptionData(OptionType.ATTACHMENT, "main_image", "Choose your image", true);
        OptionData attachment2 = new OptionData(OptionType.ATTACHMENT, "image2", "Optional image", false);
        OptionData attachment3 = new OptionData(OptionType.ATTACHMENT, "image3", "Optional image", false);
        OptionData attachment4 = new OptionData(OptionType.ATTACHMENT, "image4", "Optional image", false);
        return Collections.singletonList(Commands
                .slash(UploadImageCommandHandler.COMMAND_NAME, "Upload your image to generate something amazing!")
                .addOptions(attachment, attachment2, attachment3, attachment4));
    }
}
