package com.github.novicezk.midjourney.bot.utils;

import com.github.novicezk.midjourney.bot.model.images.ImageComposed;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

@Slf4j
public class ImageComposer {
    private static final String BACKGROUNDS_DIR = "adam-ai/welcome_images";

    public static Color getAverageColor(File imageFile) throws Exception {
        // Read the image file
        BufferedImage image = ImageIO.read(imageFile);

        // Initialize variables for sum of RGB values
        int totalRed = 0, totalGreen = 0, totalBlue = 0;

        // Iterate through each pixel and sum up RGB values
        int pixelCount = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color color = new Color(image.getRGB(x, y));
                totalRed += color.getRed();
                totalGreen += color.getGreen();
                totalBlue += color.getBlue();
                pixelCount++;
            }
        }

        // Calculate average RGB values
        int averageRed = totalRed / pixelCount;
        int averageGreen = totalGreen / pixelCount;
        int averageBlue = totalBlue / pixelCount;

        return new Color(averageRed, averageGreen, averageBlue);
    }

    public static ImageComposed composeImage(String userImageUrl) throws Exception {
        // Download the user's image
        BufferedImage userImage = downloadImage(userImageUrl);

        // Select a random background image
        File[] backgroundFiles = new File(BACKGROUNDS_DIR).listFiles();
        int randomIndex = (int) (Math.random() * backgroundFiles.length);
        File backgroundFile = backgroundFiles[randomIndex];
        BufferedImage backgroundImage = ImageIO.read(backgroundFile);
        Color averrageColor = getAverageColor(backgroundFile);

        // Determine the appropriate position to center the user's image
        int backgroundWidth = backgroundImage.getWidth();
        int backgroundHeight = backgroundImage.getHeight();
        int userImageWidth = (int) (userImage.getWidth() * 2.8);
        int userImageHeight = (int) (userImage.getHeight() * 2.8);

        int xPosition = (backgroundWidth - userImageWidth) / 2;
        int yPosition = (backgroundHeight - userImageHeight) / 2;

        // Create a new combined image
        BufferedImage combinedImage = new BufferedImage(backgroundWidth, backgroundHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = combinedImage.createGraphics();

        // Draw the background image
        g2d.drawImage(backgroundImage, 0, 0, null);

        // Draw the user's image centered on the background
        int strokeWidth = 10; // Ширина обводки
        g2d.setStroke(new BasicStroke(strokeWidth));
        g2d.setColor(Color.WHITE); // Цвет обводки
        int borderSize = userImageWidth + strokeWidth * 2; // Размер обводки
        g2d.draw(new Ellipse2D.Double(xPosition - strokeWidth, yPosition - strokeWidth, borderSize, borderSize));
        g2d.setClip(new Ellipse2D.Float(xPosition, yPosition, userImageWidth, userImageHeight));

        // Draw the user's image centered on the background
        g2d.drawImage(userImage, xPosition, yPosition, userImageWidth, userImageHeight, null);

        // Reset clip
        g2d.setClip(null);

        g2d.dispose();

        // Generate a unique filename for the combined image
        String filename = String.format("welcome_%s.png", randomIndex + 1);

        // Save the combined image to a temporary file
        File outputFile = new File(filename);
        ImageIO.write(combinedImage, "png", outputFile);

        return new ImageComposed(averrageColor, outputFile, "attachment://" + outputFile.getPath());
    }

    private static BufferedImage downloadImage(String imageUrl) throws IOException {
        URL url = new URL(imageUrl);
        return ImageIO.read(url);
    }
}
