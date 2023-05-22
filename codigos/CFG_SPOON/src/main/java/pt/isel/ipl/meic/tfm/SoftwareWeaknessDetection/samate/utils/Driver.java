package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Driver {
    @JsonProperty("name")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;

    @JsonProperty("fullName")
    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    String fullName;

    @JsonProperty("informationUri")
    public String getInformationUri() {
        return this.informationUri;
    }

    public void setInformationUri(String informationUri) {
        this.informationUri = informationUri;
    }

    String informationUri;

    @JsonProperty("version")
    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    String version;

    @JsonProperty("organization")
    public String getOrganization() {
        return this.organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    String organization;

    @JsonProperty("supportedTaxonomies")
    public ArrayList<SupportedTaxonomy> getSupportedTaxonomies() {
        return this.supportedTaxonomies;
    }

    public void setSupportedTaxonomies(ArrayList<SupportedTaxonomy> supportedTaxonomies) {
        this.supportedTaxonomies = supportedTaxonomies;
    }

    ArrayList<SupportedTaxonomy> supportedTaxonomies;
}
