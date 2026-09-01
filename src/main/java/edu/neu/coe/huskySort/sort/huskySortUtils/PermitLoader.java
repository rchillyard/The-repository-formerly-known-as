/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the San Francisco building permit corpus.
 * <p>
 * The resource holds the block, lot and filing date of every permit in the city's published record
 * for 2013-01-02 to 2018-02-23 -- 198,900 of them, none missing any of the three fields. It is the
 * three sorted-on columns of San Francisco's own dataset, published by DataSF, reduced from the
 * original forty-three columns and 75MB so that it can live in the repository.
 * <p>
 * This is real municipal data rather than a generated file, which is the point of using it. The
 * evidence is in the dates: no permit was ever filed on a Saturday or a Sunday, only 68.5% of the
 * calendar days in the span appear at all, and Mondays run a fifth below Tuesdays. Generated dates do
 * not look like that.
 * <p>
 * NOTE reads through the classloader as a stream, so it works from an exploded classpath and from
 * inside a shaded benchmarks jar alike.
 */
public class PermitLoader {

    public static final String RESOURCE = "sf-building-permits.csv";

    /**
     * @return every permit in the corpus, in the order published, which is not sorted.
     */
    public static Permit[] getPermits() {
        return getPermits(RESOURCE);
    }

    /**
     * @param resource the name of a classpath resource in the corpus's format.
     * @return the permits it holds.
     */
    public static Permit[] getPermits(final String resource) {
        try (final InputStream is = PermitLoader.class.getClassLoader().getResourceAsStream(resource)) {
            if (is == null) throw new FileNotFoundException(resource);
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                final List<Permit> result = new ArrayList<>();
                reader.readLine();  // the header
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    if (line.isEmpty()) continue;
                    final String[] fields = line.split(",", -1);
                    if (fields.length != 3)
                        throw new IOException("expected three fields in " + resource + ", found "
                                + fields.length + ": " + line);
                    result.add(new Permit(fields[0], fields[1], LocalDate.parse(fields[2])));
                }
                return result.toArray(new Permit[0]);
            }
        } catch (final IOException e) {
            throw new RuntimeException("cannot read the permit corpus: " + resource, e);
        }
    }
}
