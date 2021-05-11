
package me.novascomp.files.server.ocs.model.delete;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "status",
    "statuscode",
    "message",
    "totalitems",
    "itemsperpage"
})
public class Meta {

    @JsonProperty("status")
    private String status;
    @JsonProperty("statuscode")
    private Integer statuscode;
    @JsonProperty("message")
    private Object message;
    @JsonProperty("totalitems")
    private String totalitems;
    @JsonProperty("itemsperpage")
    private String itemsperpage;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("statuscode")
    public Integer getStatuscode() {
        return statuscode;
    }

    @JsonProperty("statuscode")
    public void setStatuscode(Integer statuscode) {
        this.statuscode = statuscode;
    }

    @JsonProperty("message")
    public Object getMessage() {
        return message;
    }

    @JsonProperty("message")
    public void setMessage(Object message) {
        this.message = message;
    }

    @JsonProperty("totalitems")
    public String getTotalitems() {
        return totalitems;
    }

    @JsonProperty("totalitems")
    public void setTotalitems(String totalitems) {
        this.totalitems = totalitems;
    }

    @JsonProperty("itemsperpage")
    public String getItemsperpage() {
        return itemsperpage;
    }

    @JsonProperty("itemsperpage")
    public void setItemsperpage(String itemsperpage) {
        this.itemsperpage = itemsperpage;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
