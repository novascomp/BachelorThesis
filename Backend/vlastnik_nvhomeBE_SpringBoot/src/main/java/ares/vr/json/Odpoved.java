package ares.vr.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Odpoved {

    @JsonProperty("Pocet_zaznamu")
    public String pocet_zaznamu;
    @JsonProperty("Pomocne_ID")
    public String pomocne_ID;
    @JsonProperty("Uvod")
    public Uvod uvod;
    @JsonProperty("Vypis_VR")
    public VypisVR vypis_VR;
    @JsonProperty("Vysledek_hledani")
    public VysledekHledani vysledek_hledani;
}
