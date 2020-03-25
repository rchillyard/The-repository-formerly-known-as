package edu.neu.coe.huskySort.sort;

public abstract class SortWithHelper<X extends Comparable<X>> implements Sort<X> {


    public SortWithHelper(Helper<X> helper) {
        this.helper = helper;
    }

    public SortWithHelper(String description, int N, boolean instrumenting) {
        this(HelperFactory.create(description, N, instrumenting));
        closeHelper = true;
    }

    public Helper<X> getHelper() {
        return helper;
    }

    @Override
    public String toString() {
        return helper.toString();
    }

    public void close() {
        if (closeHelper) helper.close();
    }

    private final Helper<X> helper;
    private boolean closeHelper = false;

}
