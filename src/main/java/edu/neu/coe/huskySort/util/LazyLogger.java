package edu.neu.coe.huskySort.util;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggerFactory;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;

import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.function.Supplier;

@SuppressWarnings("rawtypes")
public class LazyLogger extends Logger {

    public LazyLogger(final Class<?> clazz) {
        super("LazyLogger");
        logger = Logger.getLogger(clazz);
    }

    public void trace(final Supplier<String> fMessage) {
        if (logger.isTraceEnabled())
            logger.trace(fMessage.get());
    }

    public void trace(final Supplier<String> fMessage, final Throwable t) {
        if (logger.isTraceEnabled())
            logger.trace(fMessage.get(), t);
    }

    public void debug(final Supplier<String> fMessage) {
        if (logger.isDebugEnabled())
            logger.debug(fMessage.get());
    }

    public void debug(final Supplier<String> fMessage, final Throwable t) {
        if (logger.isDebugEnabled())
            logger.debug(fMessage.get(), t);
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void trace(final Object message) {
        logger.trace("NOT lazy: " + message);
    }

    @Override
    public void trace(final Object message, final Throwable t) {
        logger.trace("NOT lazy: " + message, t);
    }

    @Override
    public void debug(final Object message) {
        logger.debug("NOT lazy: " + message);
    }

    @Override
    public void debug(final Object message, final Throwable t) {
        logger.debug("NOT lazy: " + message, t);
    }

    public static Logger getLogger(final String name) {
        return Logger.getLogger(name);
    }

    public static Logger getLogger(final Class clazz) {
        return Logger.getLogger(clazz);
    }

    public static Logger getRootLogger() {
        return Logger.getRootLogger();
    }

    public static Logger getLogger(final String name, final LoggerFactory factory) {
        return Logger.getLogger(name, factory);
    }

    @Override
    public void addAppender(final Appender newAppender) {
        logger.addAppender(newAppender);
    }

    @Override
    public void assertLog(final boolean assertion, final String msg) {
        logger.assertLog(assertion, msg);
    }

    @Override
    public void callAppenders(final LoggingEvent event) {
        logger.callAppenders(event);
    }

    @Override
    public void error(final Object message) {
        logger.error(message);
    }

    @Override
    public void error(final Object message, final Throwable t) {
        logger.error(message, t);
    }

    @Override
    public void fatal(final Object message) {
        logger.fatal(message);
    }

    @Override
    public void fatal(final Object message, final Throwable t) {
        logger.fatal(message, t);
    }

    @Override
    public boolean getAdditivity() {
        return logger.getAdditivity();
    }

    @Override
    public Enumeration getAllAppenders() {
        return logger.getAllAppenders();
    }

    @Override
    public Appender getAppender(final String name) {
        return logger.getAppender(name);
    }

    @Override
    public Level getEffectiveLevel() {
        return logger.getEffectiveLevel();
    }

    @Override
    public LoggerRepository getLoggerRepository() {
        return logger.getLoggerRepository();
    }

    @Override
    public ResourceBundle getResourceBundle() {
        return logger.getResourceBundle();
    }

    @Override
    public void info(final Object message) {
        logger.info(message);
    }

    @Override
    public void info(final Object message, final Throwable t) {
        logger.info(message, t);
    }

    @Override
    public boolean isAttached(final Appender appender) {
        return logger.isAttached(appender);
    }

    @Override
    public boolean isEnabledFor(final Priority level) {
        return logger.isEnabledFor(level);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void l7dlog(final Priority priority, final String key, final Throwable t) {
        logger.l7dlog(priority, key, t);
    }

    @Override
    public void l7dlog(final Priority priority, final String key, final Object[] params, final Throwable t) {
        logger.l7dlog(priority, key, params, t);
    }

    @Override
    public void log(final Priority priority, final Object message, final Throwable t) {
        logger.log(priority, message, t);
    }

    @Override
    public void log(final Priority priority, final Object message) {
        logger.log(priority, message);
    }

    @Override
    public void log(final String callerFQCN, final Priority level, final Object message, final Throwable t) {
        logger.log(callerFQCN, level, message, t);
    }

    @Override
    public void removeAllAppenders() {
        logger.removeAllAppenders();
    }

    @Override
    public void removeAppender(final Appender appender) {
        logger.removeAppender(appender);
    }

    @Override
    public void removeAppender(final String name) {
        logger.removeAppender(name);
    }

    @Override
    public void setAdditivity(final boolean additive) {
        logger.setAdditivity(additive);
    }

    @Override
    public void setLevel(final Level level) {
        logger.setLevel(level);
    }

    @Override
    public void setResourceBundle(final ResourceBundle bundle) {
        logger.setResourceBundle(bundle);
    }

    @Override
    public void warn(final Object message) {
        logger.warn(message);
    }

    @Override
    public void warn(final Object message, final Throwable t) {
        logger.warn(message, t);
    }

    private final Logger logger;
}
