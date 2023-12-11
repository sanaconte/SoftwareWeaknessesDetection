package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Run {
    @JsonProperty("properties")
    public Properties getProperties() {
        return this.properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    Properties properties;

    @JsonProperty("tool")
    public Tool getTool() {
        return this.tool;
    }

    public void setTool(Tool tool) {
        this.tool = tool;
    }

    Tool tool;

    @JsonProperty("artifacts")
    public ArrayList<Artifact> getArtifacts() {
        return this.artifacts;
    }

    public void setArtifacts(ArrayList<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    ArrayList<Artifact> artifacts;

    @JsonProperty("taxonomies")
    public ArrayList<Taxonomy> getTaxonomies() {
        return this.taxonomies;
    }

    public void setTaxonomies(ArrayList<Taxonomy> taxonomies) {
        this.taxonomies = taxonomies;
    }

    ArrayList<Taxonomy> taxonomies;

    @JsonProperty("results")
    public ArrayList<Result> getResults() {
        return this.results;
    }

    public void setResults(ArrayList<Result> results) {
        this.results = results;
    }

    ArrayList<Result> results;
}
