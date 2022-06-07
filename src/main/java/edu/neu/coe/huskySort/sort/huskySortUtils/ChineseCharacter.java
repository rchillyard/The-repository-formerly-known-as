package edu.neu.coe.huskySort.sort.huskySortUtils;

import net.sourceforge.pinyin4j.PinyinHelper;

public class ChineseCharacter extends UnicodeCharacter {
    @Override
    public long encode(final String x) {
        return pinyinCoder.huskyEncode(x);
    }

    @Override
    public String alt(final char x) {
        final String[] pinyinStrings = PinyinHelper.toHanyuPinyinStringArray(x);
        if (pinyinStrings.length > 0) return pinyinStrings[0];
        else throw new RuntimeException("no pinyin available for: " + x);
    }

    public ChineseCharacter(final char unicode) {
        super(unicode);
    }

    @Override
    public String toString() {
        return "ChineseCharacter{" + unicode + "(" + Long.toHexString(unicode) + "):" + alt + ":" + Long.toHexString(longCode) + "}";
    }

    private final static HuskyCoder<String> pinyinCoder = HuskyCoderFactory.englishCoder;

}
