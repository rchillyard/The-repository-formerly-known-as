package edu.neu.coe.huskySort.util;

import edu.neu.coe.huskySort.sort.BaseHelper;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@SuppressWarnings("SuspiciousMethodCalls")
public class Config {

    /**
     * Method to copy this Config, but setting sectionName.optionName to be value.
     *
     * @param sectionName the section name.
     * @param optionName  the option name.
     * @param value       the new value.
     * @return a new Config as described.
     */
    public Config copy(final String sectionName, final String optionName, final String value) {
        final Ini ini = new Ini();
        for (final Map.Entry<String, Profile.Section> entry : this.ini.entrySet())
            for (final Map.Entry<String, String> x : entry.getValue().entrySet())
                ini.put(entry.getKey(), x.getKey(), x.getValue());
        final Config result = new Config(ini);
        final Profile.Section section = result.ini.get(sectionName);
        section.replace(optionName, value);
        result.ini.replace(sectionName, section);
        return result;
    }

    public String get(final Object sectionName, final Object optionName, final String defaultValue) {
        return get(sectionName, optionName, String.class, defaultValue);
    }

    public String get(final Object sectionName, final Object optionName) {
        return get(sectionName, optionName, (String) null);
    }

    /**
     * Get a configured value of type T.
     * NOTE: using this method, it is not possible to retrieve an empty String as the result,
     * unless you specify an empty string as the default.
     *
     * @param sectionName  the section name.
     * @param optionName   the option name.
     * @param defaultValue the default value.
     * @param <T>          the type of the result.
     * @return the configured value as a T.
     */
    public <T> T get(final Object sectionName, final Object optionName, final Class<T> clazz, final T defaultValue) {
        T t = ini.get(sectionName, optionName, clazz);
        if (t == null || t.equals(""))
            t = defaultValue;
        final String sT = t != null ? t.toString() : "null";
        if (unLogged(sectionName + "." + optionName))
            logger.debug(() -> "Config.get(" + sectionName + ", " + optionName + ") = " + sT);
        return t;
    }

    public <T> T get(final Object sectionName, final Object optionName, final Class<T> clazz) {
        return get(sectionName, optionName, clazz, null);
    }

    public boolean getBoolean(final String sectionName, final String optionName) {
        return get(sectionName, optionName, boolean.class);
    }

    public Stream<Integer> getIntegerStream(final String sectionName, final String optionName) {
        final String[] split = get(sectionName, optionName, String.class).split(",");
        return Arrays.stream(split).map(Integer::parseInt);
    }

    /**
     * Method to get an Int.
     * This doesn't work quite like the String getters.
     * In this case, when the value is unset, the log message will not show the default value.
     *
     * @param sectionName  the section name.
     * @param optionName   the option name.
     * @param defaultValue the default value.
     * @return the configured value as an int.
     */
    public int getInt(final String sectionName, final String optionName, final int defaultValue) {
        final String s = get(sectionName, optionName);
        if (s == null || s.isEmpty()) return defaultValue;
        return Integer.parseInt(s);
    }

    /**
     * Method to get an Int.
     * This doesn't work quite like the String getters.
     * In this case, when the value is unset, the log message will not show the default value.
     *
     * @param sectionName  the section name.
     * @param optionName   the option name.
     * @param defaultValue the default value.
     * @return the configured value as an int.
     */
    public long getLong(final String sectionName, final String optionName, final long defaultValue) {
        final String s = get(sectionName, optionName);
        if (s == null || s.isEmpty()) return defaultValue;
        return Long.parseLong(s);
    }

    public double getDouble(final String sectionName, final String optionName, final double defaultValue) {
        final String s = get(sectionName, optionName);
        if (s == null || s.isEmpty()) return defaultValue;
        return Double.parseDouble(s);
    }

    /**
     * Method to get a String.
     * In this case, when the value is unset, the log message will show the default value, unless it perceives
     * the value as the empty string.
     *
     * @param sectionName  the section name.
     * @param optionName   the option name.
     * @param defaultValue the default value.
     * @return the configured value as an int.
     */
    public String getString(final String sectionName, final String optionName, final String defaultValue) {
        final String s = get(sectionName, optionName, defaultValue);
        if (s.isEmpty()) return defaultValue;
        return s;
    }

    public String getComment(final String key) {
        final String comment = ini.getComment(key);
        if (unLogged(key))
            logger.debug(() -> "Config.getComment(" + key + ") = " + comment);
        return comment;
    }

    public List<Profile.Section> getAll(final Object key) {
        return ini.getAll(key);
    }

    public Profile.Section get(final Object key) {
        return ini.get(key);
    }

    public Profile.Section get(final Object key, final int index) {
        return ini.get(key, index);
    }

    public Profile.Section getOrDefault(final Object key, final Profile.Section defaultValue) {
        return ini.getOrDefault(key, defaultValue);
    }

    public Config(final Ini ini) {
        this.ini = ini;
    }

    public Config(final Reader reader) throws IOException {
        this(new Ini(reader));
    }

    public Config(final InputStream stream) throws IOException {
        this(new Ini(stream));
    }

    public Config(final URL resource) throws IOException {
        this(new Ini(resource));
    }

    public Config(final File input) throws IOException {
        this(new Ini(input));
    }

    public Config(final String file) throws IOException {
        this(new File(file));
    }

    /**
     * Method to determine if this configuration has an instrumented helper.
     * NOTE: we would prefer to place this logic in the Helper class but we put it here for now.
     *
     * @return true if helper is instrument
     */
    public boolean isInstrumented() {
        return getBoolean(HELPER, INSTRUMENT);
    }

    // CONSIDER: sort these out.
    public static final String HELPER = "helper";
    public static final String INSTRUMENT = BaseHelper.INSTRUMENT;

    /**
     * Method to load the appropriate configuration.
     * <p>
     * If clazz is not null, then we look for config.ini relative to the given class.
     * If clazz is null, or if the resource cannot be found relative to the class,
     * then we look in the root directory.
     *
     * @param clazz the Class in which to look for the config.ini file (may be null).
     * @return a new Config.
     * @throws IOException if config.ini cannot be found using the locations described above.
     */
    public static Config load(final Class<?> clazz) throws IOException {
        final String name = "config.ini";
        URL resource = null;
        if (clazz != null) resource = clazz.getResource(name);
        if (resource == null)
            resource = Config.class.getResource("/" + name);
        if (resource != null) return new Config(resource);
        throw new IOException("resource " + name + " not found for " + clazz);
    }

    public static Config load() throws IOException {
        return load(null);
    }

    private static boolean unLogged(final String s) {
        final Boolean value = logged.get(s);
        if (value == null) {
            logged.put(s, true);
            return true;
        }
        return !value;
    }

    final static LazyLogger logger = new LazyLogger(Config.class);

    // NOTE this is static because, otherwise, we get too much logging when we copy a Config that hasn't had all enquiries made yet.
    private static final Map<String, Boolean> logged = new HashMap<>();

    private final Ini ini;
}
