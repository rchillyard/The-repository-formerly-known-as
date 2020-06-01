package edu.neu.coe.huskySort.util;

import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("SuspiciousMethodCalls")
public class Config {
    public String get(Object sectionName, Object optionName) {
        return get(sectionName, optionName, String.class);
    }

    public <T> T get(Object sectionName, Object optionName, Class<T> clazz) {
        final T t = ini.get(sectionName, optionName, clazz);
        if (!getSetLogged(sectionName + "." + optionName))
            logger.debug(() -> "Config.get(" + sectionName + ", " + optionName + ") = " + t);
        return t;
    }

    public Boolean getBoolean(String sectionName, String optionName) {
        return get(sectionName, optionName, boolean.class);
    }

    public int getInt(final String sectionName, final String optionName, final int defaultValue) {
        final String s = get(sectionName, optionName);
        if (s.isEmpty()) return defaultValue;
        return Integer.parseInt(s);
    }

    public long getLong(final String sectionName, final String optionName, final long defaultValue) {
        final String s = get(sectionName, optionName);
        if (s.isEmpty()) return defaultValue;
        return Long.parseLong(s);
    }

    public String getComment(String key) {
        final String comment = ini.getComment(key);
        if (!getSetLogged(key))
            logger.debug(() -> "Config.getComment(" + key + ") = " + comment);
        return comment;
    }

    public List<Profile.Section> getAll(Object key) {
        return ini.getAll(key);
    }

    public Profile.Section get(Object key) {
        return ini.get(key);
    }

    public Profile.Section get(Object key, int index) {
        return ini.get(key, index);
    }

    public Profile.Section getOrDefault(Object key, Profile.Section defaultValue) {
        return ini.getOrDefault(key, defaultValue);
    }

    public Config(Ini ini) {
        this.ini = ini;
    }

    public Config(Reader reader) throws IOException {
        this(new Ini(reader));
    }

    public Config(InputStream stream) throws IOException {
        this(new Ini(stream));
    }

    public Config(URL resource) throws IOException {
        this(new Ini(resource));
    }

    public Config(File input) throws IOException {
        this(new Ini(input));
    }

    public Config(String file) throws IOException {
        this(new File(file));
    }

    /**
     * Method to load the appropriate configuration.
     * <p>
     * If clazz is not null, then we look for config.ini relative to the given class.
     * If clazz is null, or if the resource cannot be found relative to the class,
     * then we look in the root directory.
     *
     * @param clazz the Class in which to look for the config.ini file.
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

    private boolean getSetLogged(String s) {
        Boolean value = logged.get(s);
        if (value == null) {
            logged.put(s, true);
            return false;
        }
        return value;
    }

    final static LazyLogger logger = new LazyLogger(Config.class);

    private final Map<String, Boolean> logged = new HashMap<>();
    private final Ini ini;
}
