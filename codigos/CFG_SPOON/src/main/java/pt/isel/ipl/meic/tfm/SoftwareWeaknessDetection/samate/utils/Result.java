package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Result {
    @JsonProperty("ruleId")
    public String getRuleId() {
        return this.ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    String ruleId;

    @JsonProperty("message")
    public Message getMessage() {
        return this.message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    Message message;

    @JsonProperty("locations")
    public ArrayList<Location2> getLocations() {
        return this.locations;
    }

    public void setLocations(ArrayList<Location2> locations) {
        this.locations = locations;
    }

    ArrayList<Location2> locations;

    @JsonProperty("taxa")
    public ArrayList<Taxa> getTaxa() {
        return this.taxa;
    }

    public void setTaxa(ArrayList<Taxa> taxa) {
        this.taxa = taxa;
    }

    ArrayList<Taxa> taxa;
}
