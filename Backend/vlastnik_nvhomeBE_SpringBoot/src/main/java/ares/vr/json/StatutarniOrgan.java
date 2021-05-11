package ares.vr.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class StatutarniOrgan {

    @JsonProperty("Clen")
    public List<Clen> clen;
    @JsonProperty("Nazev")
    public String nazev;
    @JsonProperty("PocetClenu")
    public PocetClenu pocetClenu;
    @JsonProperty("ZpusobJednani")
    public ZpusobJednani zpusobJednani;
}
