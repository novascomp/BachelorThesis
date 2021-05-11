package ares.vr.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class Uvod {

    @JsonProperty("Aktualizace_DB")
    public String aktualizace_DB;
    @JsonProperty("Cas_vypisu")
    public String cas_vypisu;
    @JsonProperty("Datum_vypisu")
    public String datum_vypisu;
    @JsonProperty("Nadpis")
    public String nadpis;
    @JsonProperty("Typ_odkazu")
    public String typ_odkazu;
    @JsonProperty("Typ_vypisu")
    public String typ_vypisu;
}
