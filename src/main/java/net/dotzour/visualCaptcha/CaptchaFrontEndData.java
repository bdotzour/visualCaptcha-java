package net.dotzour.visualCaptcha;

import java.util.ArrayList;
import java.util.List;

public class CaptchaFrontEndData
{
    private String imageName;
    private String imageFieldName;
    private List<String> values;
    private String audioFieldName;

    public CaptchaFrontEndData(String imageName, String imageFieldName, List<String> imageOptions, String audioFieldName) {
        this.imageName = imageName;
        this.imageFieldName = imageFieldName;
        this.values = new ArrayList<>(imageOptions);
        this.audioFieldName = audioFieldName;
    }

    public String getImageName() {
        return imageName;
    }

    public String getImageFieldName() {
        return imageFieldName;
    }

    public List<String> getValues() {
        return values;
    }

    public String getAudioFieldName() {
        return audioFieldName;
    }

}