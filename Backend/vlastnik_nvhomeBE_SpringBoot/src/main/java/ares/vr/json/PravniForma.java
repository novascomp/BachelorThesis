package ares.vr.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PravniForma {

    @JsonProperty("KodPravniForma")
    public String kodPravniForma;
    @JsonProperty("NazevPravniForma")
    public String nazevPravniForma;
}
