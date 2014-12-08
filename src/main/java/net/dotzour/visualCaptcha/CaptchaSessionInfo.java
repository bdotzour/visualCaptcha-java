package net.dotzour.visualCaptcha;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class CaptchaSessionInfo implements Serializable
{
    private List<CaptchaAnswer> choices;
    private String validChoice;
    private String fieldName;
    private String audioFieldName;
    private CaptchaAnswer audioAnswer;

    public CaptchaSessionInfo(String fieldName, String validChoice, String audioFieldName, CaptchaAnswer audioAnswer, List<CaptchaAnswer> choices){
        this.fieldName = fieldName;
        this.validChoice = validChoice;
        this.audioFieldName = audioFieldName;
        this.audioAnswer = audioAnswer;
        this.choices = new ArrayList<>(choices);
    }

    public CaptchaSessionInfo(String fieldName, String validChoice, String audioFieldName, CaptchaAnswer audioAnswer, CaptchaAnswer... choices){
        this.fieldName = fieldName;
        this.validChoice = validChoice;
        this.audioFieldName = audioFieldName;
        this.audioAnswer = audioAnswer;
        this.choices = Arrays.asList(choices);
    }

    public List<CaptchaAnswer> getChoices() {
        return choices;
    }

    public String getValidChoice() {
        return validChoice;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getAudioFieldName() {
        return audioFieldName;
    }

    public CaptchaAnswer getAudioAnswer() {
        return audioAnswer;
    }
}