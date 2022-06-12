/*
 * Copyright (c) 2017. Phasmid Software
 */

package edu.neu.coe.huskySort.bqs;

import java.util.Arrays;
import java.util.Iterator;

public class Bag_Array<Item> implements Bag<Item> {

    /**
     * Add the given item to this Bag.
     * If the Bag is full, then it will be grown (by doubling) to accommodate the new element.
     *
     * @param item the item to add.
     */
    public void add(final Item item) {
        assert items != null;
        if (full())
            grow(items, 2 * capacity());
        items[count++] = item;
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public int size() {
        return count;
    }

    public void clear() {
        count = 0;
    }

    public boolean contains(final Item item) {
        for (final Item i : items) {
            if (i != null && i.equals(item))
                return true;
        }
        return false;
    }

    public int multiplicity(final Item item) {
        int result = 0;
        if (isEmpty()) return 0;
        for (final Item i : items) {
            if (i != null && i.equals(item))
                result++;
        }
        return result;
    }

    public Iterator<Item> iterator() {
        assert items != null; // Should be not-null any time after construction.
        // NOTE: there is no Java-defined array iterator.
        return Arrays.asList(asArray()).iterator();
    }

    /**
     * @return the contents of this Bag as an array of items
     */
    public Item[] asArray() {
        return Arrays.copyOf(items, count);
    }

    @Override
    public String toString() {
        return "Bag_Array{" +
                "items=" + Arrays.toString(asArray()) +
                ", count=" + count +
                '}';
    }

    /**
     * Construct a new, empty, Bag_Array.
     */
    public Bag_Array() {
        //noinspection unchecked
        grow((Item[]) new Object[0], 32);
    }

    /**
     * This fairly primitive grow method takes a T array called "from",
     * instantiates a new array of the given size,
     * copies all the elements of from into the start of the resulting array,
     * then returns the result.
     *
     * @param from the source array
     * @param size the size of the new array
     */
    private static <T> T[] growFrom(final T[] from, final int size) {
        // NOTE that we cannot use Arrays.copyOf here because we are extending the length of the array.
        @SuppressWarnings("unchecked") final T[] result = (T[]) new Object[size];
        System.arraycopy(from, 0, result, 0, from.length);
        return result;
    }

    private void grow(final Item[] source, final int size) {
        items = growFrom(source, size);
    }

    private int capacity() {
        assert items != null; // Should be not-null any time after construction.
        return items.length;
    }

    private boolean full() {
        return size() == capacity();
    }

    private Item[] items = null;
    private int count = 0;

}
