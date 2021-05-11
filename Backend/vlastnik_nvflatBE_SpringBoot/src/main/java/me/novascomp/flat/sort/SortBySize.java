package me.novascomp.flat.sort;

import java.util.Comparator;
import me.novascomp.flat.model.Flat;

public class SortBySize implements Comparator<Flat> {

    @Override
    public int compare(Flat a, Flat b) {
        return Integer.valueOf(a.getDetailList().get(0).getSize().split(" ")[0]) - Integer.valueOf(b.getDetailList().get(0).getSize().split(" ")[0]);
    }
}
