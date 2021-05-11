package ares.vr.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ZakladniUdaje {

    @JsonProperty("DatumAktualizace")
    public String datumAktualizace;
    @JsonProperty("DatumZapisu")
    public String datumZapisu;
    @JsonProperty("Ico")
    public Ico ico;
    @JsonProperty("ObchodniFirma")
    public ObchodniFirma obchodniFirma;
    @JsonProperty("PravniForma")
    public PravniForma pravniForma;
    @JsonProperty("Rejstrik")
    public String rejstrik;
    @JsonProperty("Sidlo")
    public Sidlo sidlo;
    @JsonProperty("SpisovaZnacka")
    public SpisovaZnacka spisovaZnacka;
    @JsonProperty("StavSubjektu")
    public StavSubjektu stavSubjektu;
}
