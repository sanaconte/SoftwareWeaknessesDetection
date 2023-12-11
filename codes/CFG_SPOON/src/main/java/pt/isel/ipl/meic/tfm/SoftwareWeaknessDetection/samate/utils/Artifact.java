package pt.isel.ipl.meic.tfm.SoftwareWeaknessDetection.samate.utils;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
Root root = om.readValue(myJsonString, Root.class); */
public class Artifact{
    @JsonProperty("location")
    public Location getLocation() {
        return this.location; }
    public void setLocation(Location location) {
        this.location = location; }
    Location location;
    @JsonProperty("length")
    public int getLength() {
        return this.length; }
    public void setLength(int length) {
        this.length = length; }
    int length;
    @JsonProperty("sourceLanguage")
    public String getSourceLanguage() {
        return this.sourceLanguage; }
    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage; }
    String sourceLanguage;
    @JsonProperty("hashes")
    public Hashes getHashes() {
        return this.hashes; }
    public void setHashes(Hashes hashes) {
        this.hashes = hashes; }
    Hashes hashes;
}


