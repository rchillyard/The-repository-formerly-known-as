package edu.neu.coe.huskySort.sort;

public abstract class SortWithHelper<X extends Comparable<X>> implements Sort<X> {

    public SortWithHelper(Helper<X> helper) {
        this.helper = helper;
    }

    public Helper<X> getHelper() {
        return helper;
    }

    @Override
    public String toString() {
        return helper.toString();
    }

    private final Helper<X> helper;
}
