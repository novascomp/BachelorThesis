package me.novascomp.microservice.nvf.model;

import java.util.List;
import me.novascomp.messages.model.Category;

public class CategoryHierarchy {

    private List<Category> mainCategoriesId;
    private List<Category> secondaryCategoriesId;

    public List<Category> getMainCategories() {
        return mainCategoriesId;
    }

    public void setMainCategories(List<Category> mainCategoriesId) {
        this.mainCategoriesId = mainCategoriesId;
    }

    public List<Category> getSecondaryCategories() {
        return secondaryCategoriesId;
    }

    public void setSecondaryCategories(List<Category> secondaryCategoriesId) {
        this.secondaryCategoriesId = secondaryCategoriesId;
    }
}
