package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Hashes {
    @JsonProperty("sha-256")
    public String getSha256() {
        return this.sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    String sha256;
}
