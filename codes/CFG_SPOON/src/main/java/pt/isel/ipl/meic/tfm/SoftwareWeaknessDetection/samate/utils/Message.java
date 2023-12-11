package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {
    @JsonProperty("text")
    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    String text;
}
