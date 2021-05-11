package me.novascomp.microservice.nvm.model;

public class LightweightCategory extends LightweightComponent {

    private String categoryId;

    public LightweightCategory() {
    }

    public LightweightCategory(String componentId) {
        this.categoryId = componentId;
    }

    public LightweightCategory(String text, String creatorKey) {
        super(text, creatorKey);
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    
}
