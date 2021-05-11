package ares.vr.json;

import java.util.Optional;

public class AresResponseChecker {

    public static boolean checkAresOdpovedVypisVr(VypisVR vypisVR) {
        return Optional.ofNullable(vypisVR).isPresent();
    }

    public static boolean checkStatutarniOrgan(VypisVR vypisVR) {
        if (checkAresOdpovedVypisVr(vypisVR)) {
            return Optional.ofNullable(vypisVR.statutarniOrgan).isPresent();
        }
        return false;
    }

    public static boolean checkStatutarniZpusobJednani(VypisVR vypisVR) {
        if (checkStatutarniOrgan(vypisVR)) {
            return Optional.ofNullable(vypisVR.statutarniOrgan.zpusobJednani).isPresent();
        }
        return false;
    }

    public static boolean checkStatutarniOrganClen(VypisVR vypisVR) {
        if (checkStatutarniOrgan(vypisVR)) {
            return Optional.ofNullable(vypisVR.statutarniOrgan.clen).isPresent();
        }
        return false;
    }

    public static boolean checkStatutarniOrganClen(Clen clen) {
        return Optional.ofNullable(clen).isPresent();
    }

    public static boolean checkStatutarniOrganClenFunkce(Clen clen) {
        if (checkStatutarniOrganClen(clen)) {
            return Optional.ofNullable(clen.funkce).isPresent();
        }
        return false;
    }

    public static boolean checkStatutarniOrganClenFunkceNazev(Clen clen) {
        if (checkStatutarniOrganClenFunkce(clen)) {
            return Optional.ofNullable(clen.funkce.nazev).isPresent();
        }
        return false;
    }

    public static boolean checkStatutarniOrganClenFosoba(Clen clen) {
        if (checkStatutarniOrganClen(clen)) {
            return Optional.ofNullable(clen.fosoba).isPresent();
        }
        return false;
    }

    public static boolean checkZakladniUdaje(VypisVR vypisVR) {
        if (checkAresOdpovedVypisVr(vypisVR)) {
            return Optional.ofNullable(vypisVR.zakladniUdaje).isPresent();
        }
        return false;
    }

    public static boolean checkZakladniUdajeRejstrik(VypisVR vypisVR) {
        if (checkZakladniUdaje(vypisVR)) {
            return Optional.ofNullable(vypisVR.zakladniUdaje.rejstrik).isPresent();
        }
        return false;
    }

    public static boolean checkZakladniUdajeSidlo(VypisVR vypisVR) {
        if (checkZakladniUdaje(vypisVR)) {
            return Optional.ofNullable(vypisVR.zakladniUdaje.sidlo).isPresent();
        }
        return false;
    }

    public static boolean checkZakladniUdajeSidloAdresa(VypisVR vypisVR) {
        if (checkZakladniUdajeSidlo(vypisVR)) {
            return Optional.ofNullable(vypisVR.zakladniUdaje.sidlo.adresa).isPresent();
        }
        return false;
    }

    public static boolean checkZakladniUdajeObchodniFirma(VypisVR vypisVR) {
        if (checkZakladniUdaje(vypisVR)) {
            return Optional.ofNullable(vypisVR.zakladniUdaje.obchodniFirma).isPresent();
        }
        return false;
    }

    public static boolean checkS(String string) {
        return Optional.ofNullable(string).isPresent();
    }
}
