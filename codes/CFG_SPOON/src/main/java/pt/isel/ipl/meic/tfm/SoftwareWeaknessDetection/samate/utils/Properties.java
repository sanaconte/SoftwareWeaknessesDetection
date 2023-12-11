package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Properties {
    @JsonProperty("id")
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    int id;

    String application;

    @JsonProperty("application")
    public String getApplication() {
        return application;
    }

    @JsonProperty("dependencies")
    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }

    List<String> dependencies;

    public void setApplication(String application) {
        this.application = application;
    }

    @JsonProperty("author")
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    String author;

    @JsonProperty("version")
    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    String version;

    @JsonProperty("type")
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    String type;

    @JsonProperty("status")
    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    String status;

    @JsonProperty("submissionDate")
    public String getSubmissionDate() {
        return this.submissionDate;
    }

    public void setSubmissionDate(String submissionDate) {
        this.submissionDate = submissionDate;
    }

    String submissionDate;

    @JsonProperty("language")
    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    String language;

    @JsonProperty("description")
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    String description;

    @JsonProperty("state")
    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    String state;
}
