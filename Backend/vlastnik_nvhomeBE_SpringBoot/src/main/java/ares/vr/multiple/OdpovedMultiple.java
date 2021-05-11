package ares.vr.multiple;

import ares.vr.json.VypisVR;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class OdpovedMultiple {

    @JsonProperty("Vypis_VR")
    public List<VypisVR> vypis_VR;
}
