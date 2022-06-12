package edu.neu.coe.huskySort.sort;

public class SortException extends RuntimeException {

    public SortException(final String message) {
        super(message);
    }

    public SortException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SortException(final Throwable cause) {
        super(cause);
    }

    public SortException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
