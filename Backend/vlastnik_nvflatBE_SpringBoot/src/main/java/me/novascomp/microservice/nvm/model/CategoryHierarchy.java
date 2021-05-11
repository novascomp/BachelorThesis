package me.novascomp.microservice.nvm.model;

import java.util.List;

public class CategoryHierarchy {

    private List<LightweightCategory> mainCategoriesId;
    private List<LightweightCategory> secondaryCategoriesId;

    public CategoryHierarchy(List<LightweightCategory> mainCategoriesId, List<LightweightCategory> secondaryCategoriesId) {
        this.mainCategoriesId = mainCategoriesId;
        this.secondaryCategoriesId = secondaryCategoriesId;
    }

    public List<LightweightCategory> getMainCategories() {
        return mainCategoriesId;
    }

    public void setMainCategories(List<LightweightCategory> mainCategoriesId) {
        this.mainCategoriesId = mainCategoriesId;
    }

    public List<LightweightCategory> getSecondaryCategories() {
        return secondaryCategoriesId;
    }

    public void setSecondaryCategories(List<LightweightCategory> secondaryCategoriesId) {
        this.secondaryCategoriesId = secondaryCategoriesId;
    }
}
