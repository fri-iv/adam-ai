package com.github.novicezk.midjourney.bot.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    /**
     * @param extension .txt .json format
     */
    public static FileUpload getFileFromString(String content, String filename, String extension) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());

        File file = new File(filename + "." + timeStamp + extension);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.close();

            return FileUpload.fromData(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String formatJson(String body) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
        try {
            Object json = mapper.readValue(body, Object.class);
            return writer.writeValueAsString(json);
        } catch (IOException e) {
            // Handle error
            e.printStackTrace();
            return body; // return original body in case of error
        }
    }

    public static List<FileUpload> getFilesFromAttachments(List<Message.Attachment> attachments) {
        return getFilesFromAttachments(attachments, "image.jpg");
    }

    public static List<FileUpload> getFilesFromAttachments(List<Message.Attachment> attachments, String filename) {
        List<FileUpload> files = new ArrayList<>();
        for (Message.Attachment attachment : attachments) {
            try {
                File imageFile = ImageDownloader.downloadImage(attachment.getUrl(), filename);
                files.add(FileUpload.fromData(imageFile));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return files;
    }
}
