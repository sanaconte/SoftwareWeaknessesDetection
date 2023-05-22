package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Taxa {
    @JsonProperty("id")
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String id;

    @JsonProperty("name")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;

    @JsonProperty("toolComponent")
    public ToolComponent getToolComponent() {
        return this.toolComponent;
    }

    public void setToolComponent(ToolComponent toolComponent) {
        this.toolComponent = toolComponent;
    }

    ToolComponent toolComponent;

    @JsonProperty("index")
    public int getIndex() {
        return this.index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    int index;
}
