package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Root {
    @JsonProperty("version")
    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    String version;

    @JsonProperty("$schema")
    public String get$schema() {
        return this.$schema;
    }

    public void set$schema(String $schema) {
        this.$schema = $schema;
    }

    String $schema;

    @JsonProperty("runs")
    public ArrayList<Run> getRuns() {
        return this.runs;
    }

    public void setRuns(ArrayList<Run> runs) {
        this.runs = runs;
    }

    ArrayList<Run> runs;
}
