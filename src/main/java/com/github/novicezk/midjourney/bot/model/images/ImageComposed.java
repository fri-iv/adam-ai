package com.github.novicezk.midjourney.bot.model.images;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.io.File;

@Getter
@Setter
public class ImageComposed {
    private Color avverageColor;
    private File imageFile;
    private String fileUrl;

    public ImageComposed(Color avverageColor, File imageFile, String fileUrl) {
        this.avverageColor = avverageColor;
        this.imageFile = imageFile;
        this.fileUrl = fileUrl;
    }
}
