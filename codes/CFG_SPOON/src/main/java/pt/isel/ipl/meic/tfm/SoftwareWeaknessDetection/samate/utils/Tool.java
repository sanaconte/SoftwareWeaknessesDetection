package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tool {
    @JsonProperty("driver")
    public Driver getDriver() {
        return this.driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    Driver driver;
}
