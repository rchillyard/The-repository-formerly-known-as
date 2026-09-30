/*
 * Copyright (c) 2026. Phasmid Software
 */

package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.RecordComponent;
import java.time.LocalDate;
import java.util.function.Function;

/**
 * Derives a {@link CompositeHuskyCoder} from a record's component declarations.
 * <p>
 * This is stage two of TODO.md item 31, and the stage the item says matters. Stage one gives a
 * builder that folds declared fields and checks the budget; what it cannot do is guarantee that
 * the order fields are packed in is the order the type is compared in. That obligation is stated
 * in {@code HuskySortBenchmark.Tuple} as a comment from 2020 -- "the fields must be coded in the
 * same order of priority as the comparison" -- and left to whoever edits the class next.
 * <p>
 * A record can discharge it, because {@link Class#getRecordComponents()} returns its components in
 * <b>declaration order</b>, and that ordering is specified rather than incidental. Taking the
 * packing order from the declaration, and {@link CompositeHuskyCoder#comparator()} from the same
 * list, removes the possibility of disagreement instead of testing for it.
 *
 * <h2>What Java gives and what Scala gives</h2>
 * Robin's question, 2026-09-29, was whether records allow the enumeration a Scala case class does.
 * They do, and rather better for this purpose: {@code getRecordComponents} is specified to be in
 * declaration order, each component yields an accessor, and {@code isRecord()} makes the test
 * reliable. The asymmetry runs the other way. A Java record has <b>no {@code compareTo}</b>, so
 * there is no existing ordering to derive the packing from -- which is why this class offers the
 * comparator as well. Generating both from one list is a stronger guarantee than checking a
 * generated encoding against a hand-written comparison.
 *
 * <h2>Reflection happens once</h2>
 * Accessors are unreflected into {@link MethodHandle}s when the coder is built, never invoked
 * reflectively per element. That is not a micro-optimisation but the premise of the whole method:
 * husky coding exists to extract a sort key <i>once</i> per element instead of once per
 * comparison, and measured on this machine {@code Method.invoke} costs about 132 times a direct
 * call and a {@code MethodHandle} about 65. A coder that reflected per element would spend more
 * than the comparisons it saves.
 *
 * <h2>Annotations are optional</h2>
 * A component with no {@link HuskyField} is encoded as widely as its type allows. Since fields
 * over budget are truncated from the bottom rather than rejected, an unannotated record still
 * sorts correctly -- it simply loses its least significant fields and reports {@code perfect}
 * false. Annotation buys exactness, not correctness.
 */
public final class RecordHuskyCoder {

    /**
     * The default alphabet for an unannotated String component: printable ASCII in code-point
     * order, seven bits a character, so at most nine characters would fit the budget alone. It is
     * a fallback rather than a recommendation -- most real string fields draw on far less than
     * this, and saying so is worth several characters of precision.
     */
    public static final String DEFAULT_ALPHABET = buildDefaultAlphabet();

    /**
     * The epoch for an unannotated LocalDate component, with a span wide enough for any plausible
     * business date. Fifteen bits, which is a lot to spend unasked, hence the recommendation to
     * annotate.
     */
    public static final LocalDate DEFAULT_EPOCH = LocalDate.of(1970, 1, 1);
    private static final long DEFAULT_DAYS = 32_767L;

    /**
     * @param type a record class.
     * @param <R>  its type.
     * @return a coder packing its components in declaration order, most significant first.
     * @throws IllegalArgumentException if the class is not a record, has no components, or holds a
     *                                  component of a type this class cannot encode.
     */
    public static <R extends Record> CompositeHuskyCoder<R> of(final Class<R> type) {
        if (!type.isRecord()) throw new IllegalArgumentException(type.getName() + " is not a record");
        final RecordComponent[] components = type.getRecordComponents();
        if (components.length == 0) throw new IllegalArgumentException(type.getName() + " has no components");
        final CompositeHuskyCoder.Builder<R> builder = CompositeHuskyCoder.<R>builder().named(type.getSimpleName() + "Coder");
        for (final RecordComponent rc : components) addComponent(builder, rc, accessor(rc), rc.getName());
        return builder.build();
    }

    /**
     * Adds one component, or, for a component which is itself a record, the components of that
     * record in its own declaration order.
     * <p>
     * Flattening a nested record rather than requiring a coder for it keeps the rule simple: the
     * fields of the composite are the leaves of the declaration tree, read left to right, which is
     * the order a lexicographic comparison of the nested type would visit them in.
     */
    private static <R> void addComponent(final CompositeHuskyCoder.Builder<R> builder, final RecordComponent rc,
                                         final Function<R, Object> read, final String path) {
        final Class<?> t = rc.getType();
        final HuskyField spec = rc.getAnnotation(HuskyField.class);
        if (t.isRecord()) {
            reject(spec, path, "a nested record takes its width from its own components");
            for (final RecordComponent inner : t.getRecordComponents()) {
                final Function<Object, Object> readInner = accessor(inner);
                addComponent(builder, inner, x -> readInner.apply(read.apply(x)), path + "." + inner.getName());
            }
        } else {
            builder.add(read, coderFor(t, spec, path));
        }
    }

    /**
     * @param t    the component's type.
     * @param spec its annotation, or null.
     * @param path its name, qualified if nested, for messages.
     * @return the field coder to use for it.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static HuskyFieldCoder<Object> coderFor(final Class<?> t, final HuskyField spec, final String path) {
        if (t == boolean.class || t == Boolean.class) {
            reject(spec, path, "a boolean is one bit whatever you say");
            return (HuskyFieldCoder) HuskyFieldCoder.ofBoolean(path);
        }
        if (t.isEnum()) {
            reject(spec, path, "an enum takes its width from its constant count");
            return (HuskyFieldCoder) HuskyFieldCoder.ofEnum(path, (Class) t);
        }
        if (t == String.class) {
            final int chars = spec == null || spec.chars() == 0 ? 9 : spec.chars();
            final String alphabet = spec == null || spec.alphabet().isEmpty() ? DEFAULT_ALPHABET : spec.alphabet();
            requireUnset(spec, path, t);
            return (HuskyFieldCoder) HuskyFieldCoder.ofString(path, chars, alphabet);
        }
        if (t == LocalDate.class) {
            final LocalDate epoch = spec == null || spec.epoch().isEmpty() ? DEFAULT_EPOCH : LocalDate.parse(spec.epoch());
            final long days = spec == null || spec.days() == 0 ? DEFAULT_DAYS : spec.days();
            requireUnset(spec, path, t);
            return (HuskyFieldCoder) HuskyFieldCoder.ofDate(path, epoch, days);
        }
        if (t == long.class || t == Long.class) {
            final boolean noMin = spec == null || spec.min() == Long.MIN_VALUE;
            final boolean noMax = spec == null || spec.max() == Long.MAX_VALUE;
            // Both ends or neither. Neither takes the full 64-bit width, which is the whole point
            // of ofLong. One end alone cannot be completed: defaulting the other to the type's own
            // extreme gives a span wider than Long.MAX_VALUE, which ofRange rightly refuses, and
            // guessing anything narrower would be a width declaration the caller never made --
            // exactly the silent guess this annotation exists to replace.
            if (noMin && noMax) {
                requireUnset(spec, path, t);
                return (HuskyFieldCoder) HuskyFieldCoder.ofLong(path);
            }
            if (noMin || noMax)
                throw new IllegalArgumentException(path + " is a long, so @HuskyField must declare both min and"
                        + " max or neither: one end alone leaves a range wider than Long.MAX_VALUE. Omit the"
                        + " annotation for the full 64-bit width.");
        }
        final long[] bounds = integralBounds(t, path);
        final long min = spec == null || spec.min() == Long.MIN_VALUE ? bounds[0] : spec.min();
        final long max = spec == null || spec.max() == Long.MAX_VALUE ? bounds[1] : spec.max();
        requireUnset(spec, path, t);
        final HuskyFieldCoder<Long> longs = HuskyFieldCoder.ofRange(path, min, max);
        // Boxed components arrive as Byte, Short, Integer, Character or Long; normalise to long.
        return new HuskyFieldCoder<>() {
            public int bits() {
                return longs.bits();
            }

            public long encode(final Object value) {
                return longs.encode(asLong(value));
            }

            public boolean exact(final Object value) {
                return longs.exact(asLong(value));
            }

            public String name() {
                return path;
            }
        };
    }

    /**
     * @return the full range of an integral component type, which is what an unannotated component
     * of that type is taken to span.
     * @throws IllegalArgumentException for a type this class cannot encode, naming it -- better
     *                                  than silently omitting a field, which would produce a coder
     *                                  that ignores part of the key.
     */
    private static long[] integralBounds(final Class<?> t, final String path) {
        if (t == byte.class || t == Byte.class) return new long[]{Byte.MIN_VALUE, Byte.MAX_VALUE};
        if (t == short.class || t == Short.class) return new long[]{Short.MIN_VALUE, Short.MAX_VALUE};
        if (t == char.class || t == Character.class) return new long[]{Character.MIN_VALUE, Character.MAX_VALUE};
        if (t == int.class || t == Integer.class) return new long[]{Integer.MIN_VALUE, Integer.MAX_VALUE};
        if (t == long.class || t == Long.class)
            // Reached only when a range was declared; an undeclared long takes the full 64-bit
            // width through HuskyFieldCoder.ofLong, which the caller handles before arriving here.
            return new long[]{Long.MIN_VALUE, Long.MAX_VALUE};
        throw new IllegalArgumentException("component " + path + " has type " + t.getName()
                + ", which RecordHuskyCoder cannot encode. Supported: boolean, enum, byte, short, char,"
                + " int, long, String, LocalDate, and records of those. Encode it by hand with"
                + " CompositeHuskyCoder.builder() and a HuskyFieldCoder of your own.");
    }

    private static long asLong(final Object value) {
        return value instanceof Character c ? c : ((Number) value).longValue();
    }

    /**
     * Unreflects a component's accessor once, so that reading it later costs a MethodHandle
     * invocation rather than a reflective one.
     */
    @SuppressWarnings("unchecked")
    private static <R, T> Function<R, T> accessor(final RecordComponent rc) {
        try {
            final MethodHandle handle = MethodHandles.publicLookup().unreflect(rc.getAccessor());
            return x -> {
                try {
                    return (T) handle.invoke(x);
                } catch (final Throwable e) {
                    throw new IllegalStateException("could not read component " + rc.getName(), e);
                }
            };
        } catch (final IllegalAccessException e) {
            throw new IllegalArgumentException("component " + rc.getName() + " of "
                    + rc.getDeclaringRecord().getName() + " is not accessible; the record and its accessors"
                    + " must be public for the coder to read them", e);
        }
    }

    /**
     * An attribute given for a type that does not use it is an error rather than being ignored: a
     * width declaration that silently does nothing is the failure this whole item exists to
     * prevent.
     */
    private static void requireUnset(final HuskyField spec, final String path, final Class<?> t) {
        if (spec == null) return;
        final boolean integral = t != String.class && t != LocalDate.class;
        final boolean textual = t == String.class;
        if (integral && (spec.chars() != 0 || !spec.alphabet().isEmpty()))
            throw new IllegalArgumentException(path + " is " + t.getSimpleName() + ", so chars and alphabet do not apply");
        if (integral && (!spec.epoch().isEmpty() || spec.days() != 0))
            throw new IllegalArgumentException(path + " is " + t.getSimpleName() + ", so epoch and days do not apply");
        if (!integral && (spec.min() != Long.MIN_VALUE || spec.max() != Long.MAX_VALUE))
            throw new IllegalArgumentException(path + " is " + t.getSimpleName() + ", so min and max do not apply");
        if (textual && (!spec.epoch().isEmpty() || spec.days() != 0))
            throw new IllegalArgumentException(path + " is a String, so epoch and days do not apply");
        if (!textual && !integral && (spec.chars() != 0 || !spec.alphabet().isEmpty()))
            throw new IllegalArgumentException(path + " is a LocalDate, so chars and alphabet do not apply");
    }

    private static void reject(final HuskyField spec, final String path, final String why) {
        if (spec != null) throw new IllegalArgumentException(path + " is annotated, but " + why);
    }

    private static String buildDefaultAlphabet() {
        final StringBuilder sb = new StringBuilder();
        for (char c = ' '; c <= '~'; c++) sb.append(c);
        return sb.toString();
    }

    private RecordHuskyCoder() {
    }
}
