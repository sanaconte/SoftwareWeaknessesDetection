package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ToolComponent {
    @JsonProperty("name")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;

    @JsonProperty("index")
    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    int index;
}
