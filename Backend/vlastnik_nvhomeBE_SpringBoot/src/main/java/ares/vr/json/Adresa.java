package ares.vr.json;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Adresa {

    @JsonProperty("CisloDomu")
    public String cisloDomu;
    @JsonProperty("CisloOr")
    public String cisloOr;
    @JsonProperty("IdAdresaZdroje")
    public String idAdresaZdroje;
    @JsonProperty("KodStatu")
    public String kodStatu;
    @JsonProperty("NazevCastob")
    public String nazevCastob;
    @JsonProperty("NazevObce")
    public String nazevObce;
    @JsonProperty("NazevOkresu")
    public String nazevOkresu;
    @JsonProperty("NazevStatu")
    public String nazevStatu;
    @JsonProperty("NazevUvp")
    public String nazevUvp;
    @JsonProperty("Psc")
    public String psc;
    @JsonProperty("TypCisDom")
    public String typCisDom;

    @Override
    public String toString() {
        return "Adresa{" + "cisloDomu=" + cisloDomu + ", cisloOr=" + cisloOr + ", idAdresaZdroje=" + idAdresaZdroje + ", kodStatu=" + kodStatu + ", nazevCastob=" + nazevCastob + ", nazevObce=" + nazevObce + ", nazevOkresu=" + nazevOkresu + ", nazevStatu=" + nazevStatu + ", nazevUvp=" + nazevUvp + ", psc=" + psc + ", typCisDom=" + typCisDom + '}';
    }

}
