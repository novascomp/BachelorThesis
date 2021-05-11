package me.novascomp.microservice.nvm.model;

import java.util.Objects;

public class LightweightComponent {

    private String componentId;
    private String text;
    private String creatorKey;

    public LightweightComponent() {
    }

    public LightweightComponent(String componentId) {
        this.componentId = componentId;
    }

    public LightweightComponent(String text, String creatorKey) {
        this.text = text;
        this.creatorKey = creatorKey;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreatorKey() {
        return creatorKey;
    }

    public void setCreatorKey(String creatorKey) {
        this.creatorKey = creatorKey;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.text);
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

        final LightweightComponent other = (LightweightComponent) obj;
        if (!Objects.equals(this.text, other.text)) {
            return false;
        }
        return true;
    }

}
