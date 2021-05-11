package me.novascomp.flat.sort;

import java.util.Comparator;
import me.novascomp.flat.model.Flat;

public class SortByIdentifier implements Comparator<Flat> {

    @Override
    public int compare(Flat a, Flat b) {
        return Integer.valueOf(a.getIdentifier()) - Integer.valueOf(b.getIdentifier());
    }
}
