package ares.vr.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class VypisVR {

   // @JsonProperty("OstatniSkutecnosti")
   // public OstatniSkutecnosti ostatniSkutecnosti;
    @JsonProperty("StatutarniOrgan")
    public StatutarniOrgan statutarniOrgan;
    @JsonProperty("ZakladniUdaje")
    public ZakladniUdaje zakladniUdaje;
}
