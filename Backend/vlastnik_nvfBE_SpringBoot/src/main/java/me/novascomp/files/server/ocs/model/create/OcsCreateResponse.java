
package me.novascomp.files.server.ocs.model.create;

import java.util.HashMap;
import java.util.Map;

public class OcsCreateResponse {

    private Ocs ocs;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Ocs getOcs() {
        return ocs;
    }

    public void setOcs(Ocs ocs) {
        this.ocs = ocs;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
