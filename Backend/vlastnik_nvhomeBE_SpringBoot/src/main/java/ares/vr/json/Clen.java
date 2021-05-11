package ares.vr.json;

import java.util.Objects;

public class Clen {

    public Clenstvi clenstvi;
    public Fosoba fosoba;
    public Funkce funkce;

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.fosoba);
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
        final Clen other = (Clen) obj;
        if (!Objects.equals(this.fosoba, other.fosoba)) {
            return false;
        }
        return true;
    }

}
