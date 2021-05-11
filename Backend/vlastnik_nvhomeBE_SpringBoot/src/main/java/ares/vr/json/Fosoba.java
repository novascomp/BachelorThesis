package ares.vr.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

public class Fosoba {

    public Adresa adresa;
    public String datumNarozeni;
    public String jmeno;
    public String prijmeni;
    public String titulPred;

    @Override
    public String toString() {
        return "Fosoba{" + "adresa=" + adresa.toString() + ", datumNarozeni=" + datumNarozeni + ", jmeno=" + jmeno + ", prijmeni=" + prijmeni + ", titulPred=" + titulPred + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.datumNarozeni);
        hash = 17 * hash + Objects.hashCode(this.jmeno);
        hash = 17 * hash + Objects.hashCode(this.prijmeni);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Fosoba other = (Fosoba) obj;
        if (!Objects.equals(this.datumNarozeni.toUpperCase(), other.datumNarozeni.toUpperCase())) {
            return false;
        }
        if (!Objects.equals(this.jmeno.toUpperCase(), other.jmeno.toUpperCase())) {
            return false;
        }
        if (!Objects.equals(this.prijmeni.toUpperCase(), other.prijmeni.toUpperCase())) {
            return false;
        }
        return true;
    }

}
