package com.github.novicezk.midjourney.bot.commands.guild;

import com.github.novicezk.midjourney.bot.utils.Config;
import com.github.novicezk.midjourney.bot.utils.EmbedUtil;
import com.github.novicezk.midjourney.bot.utils.ImageDownloader;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrivateMessageSender {

    public void sendMessageToUser(User user, String text) {
        sendMessageToUser(user, text, new ArrayList<>(), false);
    }

    public void sendMessageEmbedToUser(User user, String text) {
        sendMessageToUser(user, text, new ArrayList<>(), true);
    }

    public void sendMessageToUser(User user, String text, List<FileUpload> files, boolean embeds) {
        user.openPrivateChannel().queue(privateChannel -> {
            if (embeds) {
                privateChannel.sendMessageEmbeds(EmbedUtil.createEmbedCute(text))
                        .addFiles(files)
                        .queue();
            } else {
                privateChannel.sendMessage(text)
                        .addFiles(files)
                        .queue();
            }
        });
    }

    public void sendMessageFromButtonClick(ButtonInteractionEvent event, String text) {
        Member member = event.getMember();
        if (member != null) {
            List<Message.Attachment> attachments = event.getMessage().getAttachments();
            List<FileUpload> files = getFilesFromAttachments(attachments);
            sendMessageToUser(member.getUser(), text, files, false);
        }
    }

    public void sendToContactManager(MessageReceivedEvent event, List<Message.Attachment> attachments) {
        List<FileUpload> files = getFilesFromAttachments(attachments);
        event.getJDA().retrieveUserById(Config.getContactManagerId()).queue(contactManager -> {
            if (contactManager != null) {
                contactManager.openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessage("Received private message from <@" + event.getAuthor().getId() + ">\n\n> "
                                    + event.getMessage().getContentRaw())
                            .addFiles(files)
                            .queue();
                    event.getAuthor().openPrivateChannel().queue(channel -> {
                        channel.sendMessageEmbeds(EmbedUtil.createEmbed("Your message has been sent to the team!"))
                                .queue();
                    });
                });
            }
        });
    }

    public void notifyContactManager(JDA jda, List<Message.Attachment> attachments, String title, String text) {
        List<FileUpload> files = getFilesFromAttachments(attachments);
        jda.retrieveUserById(Config.getContactManagerId()).queue(contactManager -> {
            if (contactManager != null) {
                contactManager.openPrivateChannel().queue(privateChannel ->
                        privateChannel.sendMessageEmbeds(EmbedUtil.createEmbed(title, text))
                                .addFiles(files)
                                .queue());
            }
        });
    }

    public void notifyMutedMember(Member member) {
        if (member != null) {
            User user = member.getUser();
            user.openPrivateChannel().queue(privateChannel ->
                    privateChannel.sendMessage(String.format(
                                    "You've been muted in the guild for violating our rules. To be unmuted, please contact <@%s>",
                                    Config.getContactManagerId()
                            ))
                            .queue());
        }
    }

    private List<FileUpload> getFilesFromAttachments(List<Message.Attachment> attachments) {
        List<FileUpload> files = new ArrayList<>();
        for (Message.Attachment attachment : attachments) {
            try {
                File imageFile = ImageDownloader.downloadImage(attachment.getUrl());
                files.add(FileUpload.fromData(imageFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return files;
    }
}
