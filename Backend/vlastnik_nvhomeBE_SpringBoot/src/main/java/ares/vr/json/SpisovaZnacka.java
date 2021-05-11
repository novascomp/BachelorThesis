package ares.vr.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SpisovaZnacka {

    @JsonProperty("OddilVlozka")
    public String oddilVlozka;
    @JsonProperty("Soud")
    public Soud soud;
}
