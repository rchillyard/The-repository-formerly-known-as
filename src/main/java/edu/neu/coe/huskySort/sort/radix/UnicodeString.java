package edu.neu.coe.huskySort.sort.radix;

import edu.neu.coe.huskySort.sort.huskySortUtils.UnicodeCharacter;

import java.util.Arrays;

/**
 * Inner instance class which represents a string of UnicodeCharacter instances.
 */
public class UnicodeString implements StringComparable<UnicodeString, UnicodeCharacter> {
    /**
     * Constructor which takes a String representing a "word" or name.
     * We expect each of the characters of word to be a unicode representation.
     *
     * @param word a sequence of unicode characters.
     */
    public UnicodeString(final CharacterMap characterMap, final String word) {
        this.word = word;
        this.unicodes = new UnicodeCharacter[word.length()];
        for (int i = 0; i < word.length(); i++) unicodes[i] = characterMap.get(word.charAt(i));
    }

    /**
     * Method to determine if the ith unicode character is valid, i.e. is i < the length of the string.
     *
     * @param i the index of the desired unicode character (equivalent to "d" in UnicodeMSDStringSort).
     * @return true or false.
     */
    public boolean valid(final int i) {
        assert (i >= 0) : "UnicodeString: negative index " + i;
        return i < unicodes.length;
    }

    /**
     * Method to get the character at position i.
     *
     * @param i the index of the desired unicode character (equivalent to "d" in UnicodeMSDStringSort).
     * @return a UnicodeCharacter or (if not a valid character) the Null character.
     */
    public UnicodeCharacter charAt(final int i) {
        if (valid(i)) return unicodes[i];
        // CONSIDER that only the Nth character should be null. Others should throw an exception.
        return UnicodeCharacter.NullChar;
    }

    /**
     * Method to compare this UnicodeString with other -- starting at the dth character, but if necessary continuing to later characters.
     * This is particularly used by the insertion sort mechanism.
     *
     * @param that another UnicodeString.
     * @param d    the offset of the first character to compare in each of the strings.
     * @return negative, zero, or positive according to this less than, = or greater than other.
     */
    public int compareFromD(final UnicodeString that, final int d) {
        return CharacterMap.compareUnicodeStringsFromD(this, that, d);
    }

    /**
     * Recover the original (unicode) String from this UnicodeString.
     *
     * @return the value of word.
     */
    public String recoverString() {
        return word;
    }

    @Override
    public String toString() {
        return "UnicodeString{" + "word='" + word + '\'' + ", unicodes=" + Arrays.toString(unicodes) + '}';
    }

    /**
     * The original representation of the String, before being converted to UnicodeCharacter form.
     */
    final String word;
    public final UnicodeCharacter[] unicodes;
}
