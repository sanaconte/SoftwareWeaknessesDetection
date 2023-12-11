package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PhysicalLocation {
    @JsonProperty("artifactLocation")
    public ArtifactLocation getArtifactLocation() {
        return this.artifactLocation;
    }

    public void setArtifactLocation(ArtifactLocation artifactLocation) {
        this.artifactLocation = artifactLocation;
    }

    ArtifactLocation artifactLocation;

    @JsonProperty("region")
    public Region getRegion() {
        return this.region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    Region region;
}
