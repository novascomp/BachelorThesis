package ares.vr.fe;

public class AresVrForFEPruposes {

    private String obchodniFirmaValue;

    public AresVrForFEPruposes() {
    }

    public AresVrForFEPruposes(String obchodniFirmaValue) {
        this.obchodniFirmaValue = obchodniFirmaValue;
    }

    public String getObchodniFirmaValue() {
        return obchodniFirmaValue;
    }

    public void setObchodniFirmaValue(String obchodniFirmaValue) {
        this.obchodniFirmaValue = obchodniFirmaValue;
    }

    @Override
    public String toString() {
        return "AresVrForFEPruposes{" + "obchodniFirmaValue=" + obchodniFirmaValue + '}';
    }

}
