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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        List<FileUpload> files = new ArrayList<>();
        Set<String> fileNames = new HashSet<>();

        for (Message.Attachment attachment : attachments) {
            String originalFileName = attachment.getFileName();
            String uniqueFileName = getUniqueFileName(fileNames, originalFileName);

            files.addAll(getFileFromAttachment(attachment, uniqueFileName));
        }
        return files;
    }

    public static List<FileUpload> getFilesFromAttachment(Message.Attachment attachment, String filename) {
        return getFileFromAttachment(attachment, filename);
    }

    private static List<FileUpload> getFileFromAttachment(
            Message.Attachment attachment,
            String filename
    ) {
        List<FileUpload> files = new ArrayList<>();
        try {
            File imageFile = ImageDownloader.downloadImage(attachment.getUrl(), filename);
            files.add(FileUpload.fromData(imageFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return files;
    }

    private static String getUniqueFileName(Set<String> existingNames, String fileName) {
        String name = fileName;
        String extension = "";

        // Check if the file has an extension
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            name = fileName.substring(0, dotIndex);
            extension = fileName.substring(dotIndex); // including the dot
        }

        // If a file with the same name already exists, add a numeric suffix
        int counter = 1;
        String uniqueFileName = fileName;
        while (existingNames.contains(uniqueFileName)) {
            uniqueFileName = name + "_" + counter + extension;
            counter++;
        }

        // Add a unique name to the list of existing names
        existingNames.add(uniqueFileName);

        return uniqueFileName;
    }

}
