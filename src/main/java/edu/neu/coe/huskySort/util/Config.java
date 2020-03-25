package edu.neu.coe.huskySort.util;

import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.List;

@SuppressWarnings("SuspiciousMethodCalls")
public class Config {
    public String get(Object sectionName, Object optionName) {
        return get(sectionName, optionName, String.class);
    }

    public <T> T get(Object sectionName, Object optionName, Class<T> clazz) {
        final T t = ini.get(sectionName, optionName, clazz);
        logger.debug(() -> "Config.get(" + sectionName + ", " + optionName + ") = " + t);
        return t;
    }

    public String getComment(Object key) {
        final String comment = ini.getComment(key);
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

    public Config(Reader input) throws IOException {
        this(new Ini(input));
    }

    public Config(InputStream input) throws IOException {
        this(new Ini(input));
    }

    public Config(URL input) throws IOException {
        this(new Ini(input));
    }

    public Config(File input) throws IOException {
        this(new Ini(input));
    }

    public Config(String file) throws IOException {
        this(new File(file));
    }

    private final Ini ini;

    final static LazyLogger logger = new LazyLogger(Config.class);
}
