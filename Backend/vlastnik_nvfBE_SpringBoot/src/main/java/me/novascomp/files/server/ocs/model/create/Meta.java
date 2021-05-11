
package me.novascomp.files.server.ocs.model.create;

import java.util.HashMap;
import java.util.Map;

public class Meta {

    private String status;
    private Integer statuscode;
    private Object message;
    private String totalitems;
    private String itemsperpage;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getStatuscode() {
        return statuscode;
    }

    public void setStatuscode(Integer statuscode) {
        this.statuscode = statuscode;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public String getTotalitems() {
        return totalitems;
    }

    public void setTotalitems(String totalitems) {
        this.totalitems = totalitems;
    }

    public String getItemsperpage() {
        return itemsperpage;
    }

    public void setItemsperpage(String itemsperpage) {
        this.itemsperpage = itemsperpage;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
