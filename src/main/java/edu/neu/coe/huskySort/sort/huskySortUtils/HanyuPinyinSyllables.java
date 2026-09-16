package edu.neu.coe.huskySort.sort.huskySortUtils;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * The complete alphabet of Hanyu Pinyin syllables (ignoring tone), in pinyin alphabetical
 * order, together with an ordinal lookup suitable for a radix/digit-per-syllable encoding of
 * Chinese names (see TODO.md item 4).
 * <p>
 * Source: the "Overall table" on
 * <a href="https://en.wikipedia.org/wiki/Pinyin_table">Wikipedia's Pinyin table</a> (fetched
 * and parsed 2026-07-23), which lists every valid initial+final combination in Standard
 * Chinese. Only entries marked as standard are included here (413 of them) -- entries the
 * page marks as nonstandard (regionalisms, neologisms, or Taiwan/PRC variant-only readings,
 * e.g. "biang" from biangbiang noodles) are excluded, since they are vanishingly unlikely to
 * appear in real personal names.
 * <p>
 * <b>Correction, 2026-08-05:</b> the original parse of the source table silently dropped the
 * entire bare "-a" final column (18 syllables: a, ba, ca, cha, da, fa, ga, ha, ka, la, ma, na,
 * pa, sa, sha, ta, za, zha -- all standard, none nonstandard), which is very likely the actual
 * explanation for the "~449 vs. 395" discrepancy this comment used to flag as unresolved: this
 * one column alone accounts for a third of that gap, and a systematic one-column parsing miss
 * (rather than scattered errors) is exactly the kind of thing more of the same bug elsewhere
 * could still explain the rest of it. Discovered indirectly: a new pinyin-aware sorter with no
 * cleanup-pass safety net (unlike {@code HuskyCoderChinesePinyin}, which always runs one)
 * depended on {@link #ordinalOf} being complete, and produced a visibly wrong order for real
 * corpus data as a result. Re-verified against the same Wikipedia source (and cross-checked
 * against a second pinyin chart) before adding the missing 18 back in. This did not affect the
 * final correctness of anything with a cleanup pass (radix's first pass over a name using one
 * of these syllables was doing more cleanup-pass work than necessary, not producing a wrong
 * final answer), but it does affect the honesty of this file's own claim to be "the complete
 * alphabet."
 * <p>
 * Ordering follows the rules at
 * <a href="https://en.wikipedia.org/wiki/Pinyin_alphabetical_order">Pinyin alphabetical
 * order</a>: plain letter-by-letter comparison, with one wrinkle for the two modified vowels
 * (ê, ü) -- the unmodified form sorts before the modified one (e.g. "lu" before "lü"). The
 * Wikipedia rule only gives minimal-pair examples and does not say how longer words extending
 * the unmodified letter relate to the bare modified form; this implementation takes the
 * position that the entire unmodified-letter subtree precedes the modified form, e.g.
 * "luan" &lt; "lun" &lt; "luo" &lt; "l&uuml;" &lt; "l&uuml;e" (not "lu" &lt; "l&uuml;" &lt;
 * "luan" &lt; ...). This matters for real names (呂/吕 L&uuml; is a common surname); it does
 * not meaningfully matter for &ecirc; (a rare standalone interjection syllable, essentially
 * never part of a real name).
 */
public final class HanyuPinyinSyllables {

    /**
     * Comparator implementing pinyin alphabetical order: plain letter-by-letter comparison,
     * with the unmodified-before-modified rule for ê/ü (see class javadoc for the "whole
     * subtree" interpretation used here).
     */
    public static final Comparator<String> ORDER = (a, b) -> {
        final int n = Math.min(a.length(), b.length());
        for (int i = 0; i < n; i++) {
            final int cf = compareChar(a.charAt(i), b.charAt(i));
            if (cf != 0) return cf;
        }
        return Integer.compare(a.length(), b.length());
    };

    /**
     * @param syllable a Hanyu Pinyin syllable spelling (no tone), e.g. "zhang".
     * @return its ordinal position (0-based) in {@link #SYLLABLES}, or -1 if it is not a
     * recognized standard syllable.
     */
    public static int ordinalOf(final String syllable) {
        final Integer ordinal = ORDINALS.get(syllable);
        return ordinal != null ? ordinal : -1;
    }

    /**
     * @return the number of distinct standard syllables in this alphabet.
     */
    public static int size() {
        return SYLLABLES.length;
    }

    private static int compareChar(final char x, final char y) {
        if (x == y) return 0;
        final char bx = baseLetter(x);
        final char by = baseLetter(y);
        if (bx != by) return Character.compare(bx, by);
        // same base letter, so exactly one of x, y is the modified form (ê or ü):
        // unmodified sorts first.
        return Boolean.compare(isModified(x), isModified(y));
    }

    private static char baseLetter(final char c) {
        if (c == 'ü') return 'u';
        if (c == 'ê') return 'e';
        return c;
    }

    private static boolean isModified(final char c) {
        return c == 'ü' || c == 'ê';
    }

    private static Map<String, Integer> buildOrdinals() {
        final Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < SYLLABLES.length; i++) map.put(SYLLABLES[i], i);
        return map;
    }

    private HanyuPinyinSyllables() {
    }

    // NOTE: SYLLABLES must be declared (and thus initialized) before ORDINALS, since static
    // field initializers run in textual order and buildOrdinals() reads SYLLABLES.
    public static final String[] SYLLABLES = {
            "a", "ai", "an", "ang", "ao", "ba", "bai", "ban", "bang", "bao", "bei", "ben", "beng", "bi",
            "bian", "biao", "bie", "bin", "bing", "bo", "bu", "ca", "cai", "can", "cang", "cao", "ce", "cen",
            "ceng", "cha", "chai", "chan", "chang", "chao", "che", "chen", "cheng", "chi", "chong", "chou",
            "chu", "chua", "chuai", "chuan", "chuang", "chui", "chun", "chuo", "ci", "cong", "cou", "cu",
            "cuan", "cui", "cun", "cuo", "da", "dai", "dan", "dang", "dao", "de", "dei", "den", "deng", "di",
            "dian", "diao", "die", "din", "ding", "diu", "dong", "dou", "du", "duan", "dui", "dun", "duo", "e",
            "ei", "en", "eng", "er", "ê", "fa", "fan", "fang", "fei", "fen", "feng", "fo", "fou", "fu", "ga",
            "gai", "gan", "gang", "gao", "ge", "gei", "gen", "geng", "gong", "gou", "gu", "gua", "guai",
            "guan", "guang", "gui", "gun", "guo", "ha", "hai", "han", "hang", "hao", "he", "hei", "hen",
            "heng", "hm", "hng", "hong", "hou", "hu", "hua", "huai", "huan", "huang", "hui", "hun", "huo",
            "ji", "jia", "jian", "jiang", "jiao", "jie", "jin", "jing", "jiong", "jiu", "ju", "juan", "jue",
            "jun", "ka", "kai", "kan", "kang", "kao", "ke", "ken", "keng", "kong", "kou", "ku", "kua", "kuai",
            "kuan", "kuang", "kui", "kun", "kuo", "la", "lai", "lan", "lang", "lao", "le", "lei", "leng", "li",
            "lian", "liang", "liao", "lie", "lin", "ling", "liu", "lo", "long", "lou", "lu", "luan", "lun",
            "luo", "lü", "lüe", "m", "ma", "mai", "man", "mang", "mao", "me", "mei", "men", "meng", "mi",
            "mian", "miao", "mie", "min", "ming", "miu", "mo", "mou", "mu", "n", "na", "nai", "nan", "nang",
            "nao", "ne", "nei", "nen", "neng", "ng", "ni", "nian", "niang", "niao", "nie", "nin", "ning",
            "niu", "nong", "nou", "nu", "nuan", "nun", "nuo", "nü", "nüe", "o", "ou", "pa", "pai", "pan",
            "pang", "pao", "pei", "pen", "peng", "pi", "pian", "piao", "pie", "pin", "ping", "po", "pou", "pu",
            "qi", "qia", "qian", "qiang", "qiao", "qie", "qin", "qing", "qiong", "qiu", "qu", "quan", "que",
            "qun", "ran", "rang", "rao", "re", "ren", "reng", "ri", "rong", "rou", "ru", "ruan", "rui", "run",
            "ruo", "sa", "sai", "san", "sang", "sao", "se", "sen", "seng", "sha", "shai", "shan", "shang",
            "shao", "she", "shei", "shen", "sheng", "shi", "shou", "shu", "shua", "shuai", "shuan", "shuang",
            "shui", "shun", "shuo", "si", "song", "sou", "su", "suan", "sui", "sun", "suo", "ta", "tai", "tan",
            "tang", "tao", "te", "teng", "ti", "tian", "tiao", "tie", "ting", "tong", "tou", "tu", "tuan",
            "tui", "tun", "tuo", "wa", "wai", "wan", "wang", "wei", "wen", "weng", "wo", "wu", "xi", "xia",
            "xian", "xiang", "xiao", "xie", "xin", "xing", "xiong", "xiu", "xu", "xuan", "xue", "xun", "ya",
            "yan", "yang", "yao", "ye", "yi", "yin", "ying", "yo", "yong", "you", "yu", "yuan", "yue", "yun",
            "za", "zai", "zan", "zang", "zao", "ze", "zei", "zen", "zeng", "zha", "zhai", "zhan", "zhang",
            "zhao", "zhe", "zhen", "zheng", "zhi", "zhong", "zhou", "zhu", "zhua", "zhuai", "zhuan", "zhuang",
            "zhui", "zhun", "zhuo", "zi", "zong", "zou", "zu", "zuan", "zui", "zun", "zuo"
    };

    private static final Map<String, Integer> ORDINALS = buildOrdinals();
}
