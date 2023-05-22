package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Region {
    @JsonProperty("startLine")
    public int getStartLine() {
        return this.startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    int startLine;
}
