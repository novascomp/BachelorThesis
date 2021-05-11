package me.novascomp.flat.sort;

import java.util.Comparator;
import me.novascomp.flat.model.Flat;

public class SortByCommonShareSize implements Comparator<Flat> {

    @Override
    public int compare(Flat a, Flat b) {
        double shareSizeA = (Double.valueOf(a.getDetailList().get(0).getCommonShareSize().split("/")[0]) / Double.valueOf(a.getDetailList().get(0).getCommonShareSize().split("/")[1]));
        double shareSizeB = (Double.valueOf(b.getDetailList().get(0).getCommonShareSize().split("/")[0]) / Double.valueOf(b.getDetailList().get(0).getCommonShareSize().split("/")[1]));
        if (shareSizeA < shareSizeB) {
            return -1;
        }
        if (shareSizeA > shareSizeB) {
            return 1;
        }
        return 0;
    }
}
