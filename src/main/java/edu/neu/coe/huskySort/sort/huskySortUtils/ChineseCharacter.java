package edu.neu.coe.huskySort.sort.huskySortUtils;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.regex.Pattern;

/**
 * Concrete implementation of UnicodeCharacter for Chinese characters.
 */
public class ChineseCharacter extends UnicodeCharacter {
    /**
     * Take the pinyin representation of a character and yield its husky code (614 bits).
     *
     * @return a long.
     */
    public long encode() {
        return pinyinCoder.huskyEncode(alt); // Note this assumes that alt is ready before encode() is called.
    }

    /**
     * Take the unicode representation of a character and yield its pinyin.
     * <p>
     * NOTE: if a character generates more than one pinyin string, only the first will be used.
     *
     * @return the String from pinyin4j.
     */
    public String alt() {
        final String[] pinyinStrings = PinyinHelper.toHanyuPinyinStringArray(unicode);
        if (pinyinStrings == null)
            return unicode + "";
        else if (pinyinStrings.length > 0) {
            // NOTE: there are a few superfluous (?) colons in the pinyin strings (e.g. "lu:") which need to be replaced.
            final String pinyin = colonPattern.matcher(pinyinStrings[0]).replaceAll(" ");
            final String tone = pinyin.substring(pinyin.length() - 1);
            final String py = pinyin.substring(0, pinyin.length() - 1);
            return py + " " + tone;
        } else throw new RuntimeException("no pinyin available for: " + unicode);
    }

    /**
     * Constructor which takes a (unicode) char as input.
     *
     * @param unicode the unicode character.
     */
    public ChineseCharacter(final char unicode) {
        super(unicode);
    }

    @Override
    public String toString() {
        return "ChineseCharacter{" + unicode + "(" + Long.toHexString(unicode) + "):" + alt + ":" + Long.toHexString(longCode) + "}";
    }

    public static String convertToPinyin(final String s) {
        final StringBuilder result = new StringBuilder();
        for (int i = 0; i < s.length(); i++) result.append(new ChineseCharacter(s.charAt(i)).alt);
        return result.toString();
    }

    private final static HuskyCoder<String> pinyinCoder = HuskyCoderFactory.englishCoder;

    private static final Pattern colonPattern = Pattern.compile(":");
}
