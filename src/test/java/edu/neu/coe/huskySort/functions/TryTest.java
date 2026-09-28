package edu.neu.coe.huskySort.functions;

import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link Try#toTry(Optional)}, the bridge from {@link Optional} into {@link Try}.
 * <p>
 * The two differ in what they carry when there is nothing: an empty Optional says only "absent",
 * while a Try must name a cause. {@code toTry} supplies {@link NoSuchElementException} for that,
 * which is the same exception {@code Optional.get} would have thrown, so the conversion loses
 * nothing a caller did not already have.
 */
public class TryTest {

    @Test
    public void aPresentOptionalBecomesASuccessCarryingTheSameValue() {
        final Try<String> result = Try.toTry(Optional.of("Hello"));
        assertTrue(result.isSuccess());
        assertFalse(result.isFailure());
        assertEquals("Hello", result.get());
    }

    /**
     * NOTE {@code Failure} stores {@code new RuntimeException(t)} rather than {@code t} itself, so
     * the exception handed to {@code failure} comes back as the <i>cause</i> of what
     * {@code getMessage} returns, not as the thing itself. Worth asserting at both levels, since
     * the wrapping is easy to overlook and a caller testing {@code instanceof} on the outer object
     * would silently never match.
     */
    @Test
    public void anEmptyOptionalBecomesAFailureCarryingNoSuchElement() {
        final Try<String> result = Try.toTry(Optional.empty());
        assertFalse(result.isSuccess());
        assertTrue(result.isFailure());
        final Throwable outer = result.getMessage();
        assertTrue("Failure wraps whatever it is given in a RuntimeException",
                outer instanceof RuntimeException);
        assertTrue("and the original is its cause, as Optional.get would have thrown",
                outer.getCause() instanceof NoSuchElementException);
    }

    /**
     * The wrapping compounds: mapping a failure calls {@code failure} again on the already-wrapped
     * exception, so each map adds a layer. Recorded rather than asserted as desirable -- it means
     * the cause chain of a long map pipeline is deeper than a reader would expect.
     */
    @Test
    public void mappingAFailureAddsAnotherLayerOfWrapping() {
        final Throwable once = Try.toTry(Optional.<String>empty()).getMessage();
        final Throwable twice = Try.toTry(Optional.<String>empty()).map(String::length).getMessage();
        assertTrue(once.getCause() instanceof NoSuchElementException);
        assertTrue("one more RuntimeException than before", twice.getCause() instanceof RuntimeException);
        assertTrue(twice.getCause().getCause() instanceof NoSuchElementException);
    }

    /**
     * A failure must stay a failure under {@code get}, rather than returning null -- that is the
     * whole difference between Try and a nullable result.
     */
    @Test
    public void gettingTheValueOfAFailureThrows() {
        try {
            Try.toTry(Optional.empty()).get();
            fail("get() on a failure should throw rather than return null");
        } catch (final RuntimeException e) {
            // expected
        }
    }

    /**
     * The conversion composes: mapping over a converted Optional behaves the same way as mapping
     * over the Optional and then converting.
     */
    @Test
    public void mappingComposesWithTheConversion() {
        assertEquals(Try.toTry(Optional.of("Hello")).map(String::length).get(),
                Try.toTry(Optional.of("Hello").map(String::length)).get());
        assertTrue(Try.toTry(Optional.<String>empty()).map(String::length).isFailure());
    }

    /**
     * {@code Optional.of(null)} is not expressible, but {@code ofNullable} is, and it must land on
     * the failure side rather than producing a success wrapping null.
     */
    @Test
    public void aNullableOptionalOfNullBecomesAFailure() {
        assertTrue(Try.toTry(Optional.ofNullable(null)).isFailure());
    }
}
