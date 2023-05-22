package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Location2 {
    @JsonProperty("physicalLocation")
    public PhysicalLocation getPhysicalLocation() {
        return this.physicalLocation;
    }

    public void setPhysicalLocation(PhysicalLocation physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    PhysicalLocation physicalLocation;
}
