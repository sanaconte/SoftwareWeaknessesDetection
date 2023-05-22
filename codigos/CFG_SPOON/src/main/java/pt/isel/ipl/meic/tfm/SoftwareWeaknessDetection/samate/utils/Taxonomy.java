package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Taxonomy {
    @JsonProperty("name")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;

    @JsonProperty("version")
    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    String version;

    @JsonProperty("informationUri")
    public String getInformationUri() {
        return this.informationUri;
    }

    public void setInformationUri(String informationUri) {
        this.informationUri = informationUri;
    }

    String informationUri;

    @JsonProperty("downloadUri")
    public String getDownloadUri() {
        return this.downloadUri;
    }

    public void setDownloadUri(String downloadUri) {
        this.downloadUri = downloadUri;
    }

    String downloadUri;

    @JsonProperty("organization")
    public String getOrganization() {
        return this.organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    String organization;

    @JsonProperty("shortDescription")
    public ShortDescription getShortDescription() {
        return this.shortDescription;
    }

    public void setShortDescription(ShortDescription shortDescription) {
        this.shortDescription = shortDescription;
    }

    ShortDescription shortDescription;

    @JsonProperty("isComprehensive")
    public boolean getIsComprehensive() {
        return this.isComprehensive;
    }

    public void setIsComprehensive(boolean isComprehensive) {
        this.isComprehensive = isComprehensive;
    }

    boolean isComprehensive;

    @JsonProperty("taxa")
    public ArrayList<Taxa> getTaxa() {
        return this.taxa;
    }

    public void setTaxa(ArrayList<Taxa> taxa) {
        this.taxa = taxa;
    }

    ArrayList<Taxa> taxa;
}
