/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A San Francisco building permit, reduced to the three fields of its natural ordering:
 * the Assessor's block and lot, which together identify a parcel, and the date the permit was filed.
 * <p>
 * This exists as a husky-encoding case study on real data. The type is favourable on all three of the
 * counts that decide the mechanism's advantage: the natural comparison is composite and therefore
 * expensive, comparing two Strings and a date with short-circuiting; the encoding is exact, so no
 * cleanup pass is needed; and no sort specialised to municipal permit records exists, so the
 * alternative is a comparator-driven sort or a radix sort hand-written for this one type.
 * <p>
 * Sorting permits by parcel and then by date is what the records are actually browsed by -- every
 * permit on this parcel, in the order it was filed -- rather than an ordering invented for a
 * benchmark.
 *
 * @see PermitCoder for the encoding and its bit budget.
 */
public class Permit implements HuskySortable<Permit> {

    /**
     * @param block     the Assessor's block, up to five characters.
     * @param lot       the Assessor's lot within the block, up to four characters.
     * @param filedDate the date the permit was filed.
     */
    public Permit(final String block, final String lot, final LocalDate filedDate) {
        this.block = block;
        this.lot = lot;
        this.filedDate = filedDate;
    }

    /**
     * Parcel first, then chronology. Block and lot are compared as Strings, which is how they read:
     * they are identifiers rather than numbers, and some lots carry a letter suffix.
     *
     * @param that the permit to compare with.
     * @return negative, zero or positive as this precedes, equals or follows that.
     */
    public int compareTo(final Permit that) {
        final int cf1 = block.compareTo(that.block);
        if (cf1 != 0) return cf1;
        final int cf2 = lot.compareTo(that.lot);
        if (cf2 != 0) return cf2;
        return filedDate.compareTo(that.filedDate);
    }

    /**
     * @return the husky code, which for this type is exact -- see {@link PermitCoder}.
     */
    public long huskyCode() {
        return PermitCoder.INSTANCE.huskyEncode(this);
    }

    public String getBlock() {
        return block;
    }

    public String getLot() {
        return lot;
    }

    public LocalDate getFiledDate() {
        return filedDate;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Permit)) return false;
        final Permit permit = (Permit) o;
        return block.equals(permit.block) && lot.equals(permit.lot) && filedDate.equals(permit.filedDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(block, lot, filedDate);
    }

    @Override
    public String toString() {
        return "Permit{" + block + "/" + lot + " filed " + filedDate + '}';
    }

    private final String block;
    private final String lot;
    private final LocalDate filedDate;
}
