package me.novascomp.flat.sort;

import java.util.Comparator;
import me.novascomp.microservice.nvm.model.LightweightCategory;

public class SortByText implements Comparator<LightweightCategory> {

    @Override
    public int compare(LightweightCategory a, LightweightCategory b) {
        return Integer.valueOf(a.getText()) - Integer.valueOf(b.getText());
    }
}
