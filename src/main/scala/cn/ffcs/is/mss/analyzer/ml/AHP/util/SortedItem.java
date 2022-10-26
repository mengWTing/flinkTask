package cn.ffcs.is.mss.analyzer.ml.AHP.util;

/**
 * @author hanyu
 * @ClassName AbstractMethod
 * @date 2022/3/21 11:31
 * @description
 * @update [no][date YYYY-MM-DD][name][description]
 **/
import java.util.Arrays;

public class SortedItem implements Comparable {
    int index;
    double value;

    public SortedItem(int index, double value) {
        this.index = index;
        this.value = value;
    }

    public int compareTo(Object o) {
        return Double.compare(((SortedItem)o).value, this.value);
    }

    public int getIndex() {
        return this.index;
    }

    public double getValue() {
        return this.value;
    }

    public static SortedItem[] sort(double[] values) {
        SortedItem[] items = new SortedItem[values.length];

        for(int i = 0; i < values.length; ++i) {
            items[i] = new SortedItem(i, values[i]);
        }

        Arrays.sort(items);
        return items;
    }
}

