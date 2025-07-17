/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.velocity.tools;

import java.util.Objects;

import org.apache.commons.lang3.Strings;
import org.xwiki.text.StringUtils;

/**
 * Velocity Tool providing various helpers to manipulate strings.
 *
 * <p>This class is basically {@link org.apache.commons.lang3.StringUtils} but with fewer deprecated methods, to
 * control the deprecation in XWiki and to keep methods available in Velocity whose recommended alternative isn't
 * available in Velocity.</p>
 *
 * @version $Id$
 * @since 15.7RC1
 */
public class StringTool extends StringUtils
{
    /**
     * Returns either the given String, or if the String is null, nullDefault.
     *
     * @param str the String to check, may be null
     * @param nullDefault the default String to return if the input is {@code null}, may be null
     * @return the passed in String, or the default if it was {@code null}
     * @see org.apache.commons.lang3.StringUtils#defaultString(String, String)
     */
    @SuppressWarnings("deprecation")
    public static String defaultString(final String str, final String nullDefault)
    {
        return Objects.toString(str, nullDefault);
    }

    /**
     * Appends the suffix to the end of the string if the string does not already end with any of the suffixes.
     *
     * <pre>
     * StringTool.appendIfMissing(null, null)      = null
     * StringTool.appendIfMissing("abc", null)     = "abc"
     * StringTool.appendIfMissing("", "xyz"        = "xyz"
     * StringTool.appendIfMissing("abc", "xyz")    = "abcxyz"
     * StringTool.appendIfMissing("abcxyz", "xyz") = "abcxyz"
     * StringTool.appendIfMissing("abcXYZ", "xyz") = "abcXYZxyz"
     * </pre>
     * <p>
     * With additional suffixes,
     * </p>
     *
     * <pre>
     * StringTool.appendIfMissing(null, null, null)       = null
     * StringTool.appendIfMissing("abc", null, null)      = "abc"
     * StringTool.appendIfMissing("", "xyz", null)        = "xyz"
     * StringTool.appendIfMissing("abc", "xyz", new CharSequence[]{null}) = "abcxyz"
     * StringTool.appendIfMissing("abc", "xyz", "")       = "abc"
     * StringTool.appendIfMissing("abc", "xyz", "mno")    = "abcxyz"
     * StringTool.appendIfMissing("abcxyz", "xyz", "mno") = "abcxyz"
     * StringTool.appendIfMissing("abcmno", "xyz", "mno") = "abcmno"
     * StringTool.appendIfMissing("abcXYZ", "xyz", "mno") = "abcXYZxyz"
     * StringTool.appendIfMissing("abcMNO", "xyz", "mno") = "abcMNOxyz"
     * </pre>
     *
     * @param str      The string.
     * @param suffix   The suffix to append to the end of the string.
     * @param suffixes Additional suffixes that are valid terminators.
     * @return A new String if suffix was appended, the same string otherwise.
     */
    public static String appendIfMissing(final String str, final CharSequence suffix, final CharSequence... suffixes)
    {
        return Strings.CS.appendIfMissing(str, suffix, suffixes);
    }

    /**
     * Appends the suffix to the end of the string if the string does not
     * already end, case-insensitive, with any of the suffixes.
     *
     * <pre>
     * StringTool.appendIfMissingIgnoreCase(null, null)      = null
     * StringTool.appendIfMissingIgnoreCase("abc", null)     = "abc"
     * StringTool.appendIfMissingIgnoreCase("", "xyz")       = "xyz"
     * StringTool.appendIfMissingIgnoreCase("abc", "xyz")    = "abcxyz"
     * StringTool.appendIfMissingIgnoreCase("abcxyz", "xyz") = "abcxyz"
     * StringTool.appendIfMissingIgnoreCase("abcXYZ", "xyz") = "abcXYZ"
     * </pre>
     * <p>With additional suffixes,</p>
     * <pre>
     * StringTool.appendIfMissingIgnoreCase(null, null, null)       = null
     * StringTool.appendIfMissingIgnoreCase("abc", null, null)      = "abc"
     * StringTool.appendIfMissingIgnoreCase("", "xyz", null)        = "xyz"
     * StringTool.appendIfMissingIgnoreCase("abc", "xyz", new CharSequence[]{null}) = "abcxyz"
     * StringTool.appendIfMissingIgnoreCase("abc", "xyz", "")       = "abc"
     * StringTool.appendIfMissingIgnoreCase("abc", "xyz", "mno")    = "abcxyz"
     * StringTool.appendIfMissingIgnoreCase("abcxyz", "xyz", "mno") = "abcxyz"
     * StringTool.appendIfMissingIgnoreCase("abcmno", "xyz", "mno") = "abcmno"
     * StringTool.appendIfMissingIgnoreCase("abcXYZ", "xyz", "mno") = "abcXYZ"
     * StringTool.appendIfMissingIgnoreCase("abcMNO", "xyz", "mno") = "abcMNO"
     * </pre>
     *
     * @param str The string.
     * @param suffix The suffix to append to the end of the string.
     * @param suffixes Additional suffixes that are valid terminators.
     * @return A new String if suffix was appended, the same string otherwise.
     */
    public static String appendIfMissingIgnoreCase(final String str, final CharSequence suffix,
        final CharSequence... suffixes)
    {
        return Strings.CI.appendIfMissing(str, suffix, suffixes);
    }

    /**
     * Removes {@code separator} from the end of
     * {@code str} if it's there, otherwise leave it alone.
     *
     * <pre>
     * StringTool.chomp(null, *)         = null
     * StringTool.chomp("", *)           = ""
     * StringTool.chomp("foobar", "bar") = "foo"
     * StringTool.chomp("foobar", "baz") = "foobar"
     * StringTool.chomp("foo", "foo")    = ""
     * StringTool.chomp("foo ", "foo")   = "foo "
     * StringTool.chomp(" foo", "foo")   = " "
     * StringTool.chomp("foo", "foooo")  = "foo"
     * StringTool.chomp("foo", "")       = "foo"
     * StringTool.chomp("foo", null)     = "foo"
     * </pre>
     *
     * @param str  the String to chomp from, may be null
     * @param separator  separator String, may be null
     * @return String without trailing separator, {@code null} if null String input
     */
    public static String chomp(final String str, final String separator)
    {
        return Strings.CS.removeEnd(str, separator);
    }

    /**
     * Compare two Strings lexicographically, as per {@link String#compareTo(String)}, returning :
     * <ul>
     *  <li>{@code int = 0}, if {@code str1} is equal to {@code str2} (or both {@code null})</li>
     *  <li>{@code int < 0}, if {@code str1} is less than {@code str2}</li>
     *  <li>{@code int > 0}, if {@code str1} is greater than {@code str2}</li>
     * </ul>
     *
     * <p>This is a {@code null} safe version of :</p>
     * <blockquote><pre>str1.compareTo(str2)</pre></blockquote>
     *
     * <p>{@code null} value is considered less than non-{@code null} value.
     * Two {@code null} references are considered equal.</p>
     *
     * <pre>{@code
     * StringTool.compare(null, null)   = 0
     * StringTool.compare(null , "a")   < 0
     * StringTool.compare("a", null)   > 0
     * StringTool.compare("abc", "abc") = 0
     * StringTool.compare("a", "b")     < 0
     * StringTool.compare("b", "a")     > 0
     * StringTool.compare("a", "B")     > 0
     * StringTool.compare("ab", "abc")  < 0
     * }</pre>
     *
     * @see #compare(String, String, boolean)
     * @see String#compareTo(String)
     * @param str1  the String to compare from
     * @param str2  the String to compare to
     * @return &lt; 0, 0, &gt; 0, if {@code str1} is respectively less, equal or greater than {@code str2}
     */
    public static int compare(final String str1, final String str2)
    {
        return Strings.CS.compare(str1, str2);
    }

    /**
     * Compare two Strings lexicographically, ignoring case differences,
     * as per {@link String#compareToIgnoreCase(String)}, returning :
     * <ul>
     *  <li>{@code int = 0}, if {@code str1} is equal to {@code str2} (or both {@code null})</li>
     *  <li>{@code int < 0}, if {@code str1} is less than {@code str2}</li>
     *  <li>{@code int > 0}, if {@code str1} is greater than {@code str2}</li>
     * </ul>
     *
     * <p>This is a {@code null} safe version of :</p>
     * <blockquote><pre>str1.compareToIgnoreCase(str2)</pre></blockquote>
     *
     * <p>{@code null} value is considered less than non-{@code null} value.
     * Two {@code null} references are considered equal.
     * Comparison is case insensitive.</p>
     *
     * <pre>{@code
     * StringTool.compareIgnoreCase(null, null)   = 0
     * StringTool.compareIgnoreCase(null , "a")   < 0
     * StringTool.compareIgnoreCase("a", null)    > 0
     * StringTool.compareIgnoreCase("abc", "abc") = 0
     * StringTool.compareIgnoreCase("abc", "ABC") = 0
     * StringTool.compareIgnoreCase("a", "b")     < 0
     * StringTool.compareIgnoreCase("b", "a")     > 0
     * StringTool.compareIgnoreCase("a", "B")     < 0
     * StringTool.compareIgnoreCase("A", "b")     < 0
     * StringTool.compareIgnoreCase("ab", "ABC")  < 0
     * }</pre>
     *
     * @see #compareIgnoreCase(String, String, boolean)
     * @see String#compareToIgnoreCase(String)
     * @param str1  the String to compare from
     * @param str2  the String to compare to
     * @return &lt; 0, 0, &gt; 0, if {@code str1} is respectively less, equal ou greater than {@code str2},
     *          ignoring case differences.
     */
    public static int compareIgnoreCase(final String str1, final String str2)
    {
        return Strings.CI.compare(str1, str2);
    }

    /**
     * Tests if CharSequence contains a search CharSequence, handling {@code null}.
     * This method uses {@link String#indexOf(String)} if possible.
     *
     * <p>A {@code null} CharSequence will return {@code false}.</p>
     *
     * <pre>
     * StringTool.contains(null, *)     = false
     * StringTool.contains(*, null)     = false
     * StringTool.contains("", "")      = true
     * StringTool.contains("abc", "")   = true
     * StringTool.contains("abc", "a")  = true
     * StringTool.contains("abc", "z")  = false
     * </pre>
     *
     * @param seq  the CharSequence to check, may be null
     * @param searchSeq  the CharSequence to find, may be null
     * @return true if the CharSequence contains the search CharSequence,
     *  false if not or {@code null} string input
     */
    public static boolean contains(final CharSequence seq, final CharSequence searchSeq)
    {
        return Strings.CS.contains(seq, searchSeq);
    }

    /**
     * Tests if the CharSequence contains any of the CharSequences in the given array.
     *
     * <p>
     * A {@code null} {@code cs} CharSequence will return {@code false}. A {@code null} or zero length search array will
     * return {@code false}.
     * </p>
     *
     * <pre>
     * StringTool.containsAny(null, *)            = false
     * StringTool.containsAny("", *)              = false
     * StringTool.containsAny(*, null)            = false
     * StringTool.containsAny(*, [])              = false
     * StringTool.containsAny("abcd", "ab", null) = true
     * StringTool.containsAny("abcd", "ab", "cd") = true
     * StringTool.containsAny("abc", "d", "abc")  = true
     * </pre>
     *
     * @param cs The CharSequence to check, may be null
     * @param searchCharSequences The array of CharSequences to search for, may be null. Individual CharSequences may be
     *        null as well.
     * @return {@code true} if any of the search CharSequences are found, {@code false} otherwise
     */
    public static boolean containsAny(final CharSequence cs, final CharSequence... searchCharSequences)
    {
        return Strings.CS.containsAny(cs, searchCharSequences);
    }

    /**
     * Tests if the CharSequence contains any of the CharSequences in the given array, ignoring case.
     *
     * <p>
     * A {@code null} {@code cs} CharSequence will return {@code false}. A {@code null} or zero length search array will
     * return {@code false}.
     * </p>
     *
     * <pre>
     * StringTool.containsAny(null, *)            = false
     * StringTool.containsAny("", *)              = false
     * StringTool.containsAny(*, null)            = false
     * StringTool.containsAny(*, [])              = false
     * StringTool.containsAny("abcd", "ab", null) = true
     * StringTool.containsAny("abcd", "ab", "cd") = true
     * StringTool.containsAny("abc", "d", "abc")  = true
     * StringTool.containsAny("abc", "D", "ABC")  = true
     * StringTool.containsAny("ABC", "d", "abc")  = true
     * </pre>
     *
     * @param cs The CharSequence to check, may be null
     * @param searchCharSequences The array of CharSequences to search for, may be null. Individual CharSequences may be
     *        null as well.
     * @return {@code true} if any of the search CharSequences are found, {@code false} otherwise
     */
    public static boolean containsAnyIgnoreCase(final CharSequence cs, final CharSequence... searchCharSequences)
    {
        return Strings.CI.containsAny(cs, searchCharSequences);
    }

    /**
     * Tests if CharSequence contains a search CharSequence irrespective of case,
     * handling {@code null}. Case-insensitivity is defined as by
     * {@link String#equalsIgnoreCase(String)}.
     *
     * <p>A {@code null} CharSequence will return {@code false}.
     *
     * <pre>
     * StringTool.containsIgnoreCase(null, *)    = false
     * StringTool.containsIgnoreCase(*, null)    = false
     * StringTool.containsIgnoreCase("", "")     = true
     * StringTool.containsIgnoreCase("abc", "")  = true
     * StringTool.containsIgnoreCase("abc", "a") = true
     * StringTool.containsIgnoreCase("abc", "z") = false
     * StringTool.containsIgnoreCase("abc", "A") = true
     * StringTool.containsIgnoreCase("abc", "Z") = false
     * </pre>
     *
     * @param str  the CharSequence to check, may be null
     * @param searchStr  the CharSequence to find, may be null
     * @return true if the CharSequence contains the search CharSequence irrespective of
     * case or false if not or {@code null} string input
     */
    public static boolean containsIgnoreCase(final CharSequence str, final CharSequence searchStr)
    {
        return Strings.CI.contains(str, searchStr);
    }

    /**
     * Tests if a CharSequence ends with a specified suffix.
     *
     * <p>{@code null}s are handled without exceptions. Two {@code null}
     * references are considered to be equal. The comparison is case-sensitive.</p>
     *
     * <pre>
     * StringTool.endsWith(null, null)      = true
     * StringTool.endsWith(null, "def")     = false
     * StringTool.endsWith("abcdef", null)  = false
     * StringTool.endsWith("abcdef", "def") = true
     * StringTool.endsWith("ABCDEF", "def") = false
     * StringTool.endsWith("ABCDEF", "cde") = false
     * StringTool.endsWith("ABCDEF", "")    = true
     * </pre>
     *
     * @see String#endsWith(String)
     * @param str  the CharSequence to check, may be null
     * @param suffix the suffix to find, may be null
     * @return {@code true} if the CharSequence ends with the suffix, case-sensitive, or
     *  both {@code null}
     */
    public static boolean endsWith(final CharSequence str, final CharSequence suffix)
    {
        return Strings.CS.endsWith(str, suffix);
    }

    /**
     * Tests if a CharSequence ends with any of the provided case-sensitive suffixes.
     *
     * <pre>
     * StringTool.endsWithAny(null, null)                  = false
     * StringTool.endsWithAny(null, new String[] {"abc"})  = false
     * StringTool.endsWithAny("abcxyz", null)              = false
     * StringTool.endsWithAny("abcxyz", new String[] {""}) = true
     * StringTool.endsWithAny("abcxyz", new String[] {"xyz"}) = true
     * StringTool.endsWithAny("abcxyz", new String[] {null, "xyz", "abc"}) = true
     * StringTool.endsWithAny("abcXYZ", "def", "XYZ")      = true
     * StringTool.endsWithAny("abcXYZ", "def", "xyz")      = false
     * </pre>
     *
     * @param sequence  the CharSequence to check, may be null
     * @param searchStrings the case-sensitive CharSequences to find, may be empty or contain {@code null}
     * @see #endsWith(CharSequence, CharSequence)
     * @return {@code true} if the input {@code sequence} is {@code null} AND no {@code searchStrings} are provided, or
     *   the input {@code sequence} ends in any of the provided case-sensitive {@code searchStrings}.
     */
    public static boolean endsWithAny(final CharSequence sequence, final CharSequence... searchStrings)
    {
        return Strings.CS.endsWithAny(sequence, searchStrings);
    }

    /**
     * Case-insensitive check if a CharSequence ends with a specified suffix.
     *
     * <p>{@code null}s are handled without exceptions. Two {@code null}
     * references are considered to be equal. The comparison is case insensitive.</p>
     *
     * <pre>
     * StringTool.endsWithIgnoreCase(null, null)      = true
     * StringTool.endsWithIgnoreCase(null, "def")     = false
     * StringTool.endsWithIgnoreCase("abcdef", null)  = false
     * StringTool.endsWithIgnoreCase("abcdef", "def") = true
     * StringTool.endsWithIgnoreCase("ABCDEF", "def") = true
     * StringTool.endsWithIgnoreCase("ABCDEF", "cde") = false
     * </pre>
     *
     * @see String#endsWith(String)
     * @param str  the CharSequence to check, may be null
     * @param suffix the suffix to find, may be null
     * @return {@code true} if the CharSequence ends with the suffix, case-insensitive, or
     *  both {@code null}
     */
    public static boolean endsWithIgnoreCase(final CharSequence str, final CharSequence suffix)
    {
        return Strings.CI.endsWith(str, suffix);
    }

    /**
     * Compares two CharSequences, returning {@code true} if they represent
     * equal sequences of characters.
     *
     * <p>{@code null}s are handled without exceptions. Two {@code null}
     * references are considered to be equal. The comparison is <strong>case-sensitive</strong>.</p>
     *
     * <pre>
     * StringTool.equals(null, null)   = true
     * StringTool.equals(null, "abc")  = false
     * StringTool.equals("abc", null)  = false
     * StringTool.equals("abc", "abc") = true
     * StringTool.equals("abc", "ABC") = false
     * </pre>
     *
     * @param cs1  the first CharSequence, may be {@code null}
     * @param cs2  the second CharSequence, may be {@code null}
     * @return {@code true} if the CharSequences are equal (case-sensitive), or both {@code null}
     * @see Object#equals(Object)
     * @see #equalsIgnoreCase(CharSequence, CharSequence)
     */
    public static boolean equals(final CharSequence cs1, final CharSequence cs2)
    {
        return Strings.CS.equals(cs1, cs2);
    }

    /**
     * Compares given {@code string} to a CharSequences vararg of {@code searchStrings},
     * returning {@code true} if the {@code string} is equal to any of the {@code searchStrings}.
     *
     * <pre>
     * StringTool.equalsAny(null, (CharSequence[]) null) = false
     * StringTool.equalsAny(null, null, null)    = true
     * StringTool.equalsAny(null, "abc", "def")  = false
     * StringTool.equalsAny("abc", null, "def")  = false
     * StringTool.equalsAny("abc", "abc", "def") = true
     * StringTool.equalsAny("abc", "ABC", "DEF") = false
     * </pre>
     *
     * @param string to compare, may be {@code null}.
     * @param searchStrings a vararg of strings, may be {@code null}.
     * @return {@code true} if the string is equal (case-sensitive) to any other element of {@code searchStrings};
     * {@code false} if {@code searchStrings} is null or contains no matches.
     */
    public static boolean equalsAny(final CharSequence string, final CharSequence... searchStrings)
    {
        return Strings.CS.equalsAny(string, searchStrings);
    }

    /**
     * Compares given {@code string} to a CharSequences vararg of {@code searchStrings},
     * returning {@code true} if the {@code string} is equal to any of the {@code searchStrings}, ignoring case.
     *
     * <pre>
     * StringTool.equalsAnyIgnoreCase(null, (CharSequence[]) null) = false
     * StringTool.equalsAnyIgnoreCase(null, null, null)    = true
     * StringTool.equalsAnyIgnoreCase(null, "abc", "def")  = false
     * StringTool.equalsAnyIgnoreCase("abc", null, "def")  = false
     * StringTool.equalsAnyIgnoreCase("abc", "abc", "def") = true
     * StringTool.equalsAnyIgnoreCase("abc", "ABC", "DEF") = true
     * </pre>
     *
     * @param string to compare, may be {@code null}.
     * @param searchStrings a vararg of strings, may be {@code null}.
     * @return {@code true} if the string is equal (case-insensitive) to any other element of {@code searchStrings};
     * {@code false} if {@code searchStrings} is null or contains no matches.
     */
    public static boolean equalsAnyIgnoreCase(final CharSequence string, final CharSequence... searchStrings)
    {
        return Strings.CI.equalsAny(string, searchStrings);
    }

    /**
     * Compares two CharSequences, returning {@code true} if they represent
     * equal sequences of characters, ignoring case.
     *
     * <p>{@code null}s are handled without exceptions. Two {@code null}
     * references are considered equal. The comparison is <strong>case insensitive</strong>.</p>
     *
     * <pre>
     * StringTool.equalsIgnoreCase(null, null)   = true
     * StringTool.equalsIgnoreCase(null, "abc")  = false
     * StringTool.equalsIgnoreCase("abc", null)  = false
     * StringTool.equalsIgnoreCase("abc", "abc") = true
     * StringTool.equalsIgnoreCase("abc", "ABC") = true
     * </pre>
     *
     * @param cs1  the first CharSequence, may be {@code null}
     * @param cs2  the second CharSequence, may be {@code null}
     * @return {@code true} if the CharSequences are equal (case-insensitive), or both {@code null}
     * @see #equals(CharSequence, CharSequence)
     */
    public static boolean equalsIgnoreCase(final CharSequence cs1, final CharSequence cs2)
    {
        return Strings.CI.equals(cs1, cs2);
    }

    /**
     * Finds the first index within a CharSequence, handling {@code null}.
     * This method uses {@link String#indexOf(String, int)} if possible.
     *
     * <p>A {@code null} CharSequence will return {@code -1}.</p>
     *
     * <pre>
     * StringTool.indexOf(null, *)          = -1
     * StringTool.indexOf(*, null)          = -1
     * StringTool.indexOf("", "")           = 0
     * StringTool.indexOf("", *)            = -1 (except when * = "")
     * StringTool.indexOf("aabaabaa", "a")  = 0
     * StringTool.indexOf("aabaabaa", "b")  = 2
     * StringTool.indexOf("aabaabaa", "ab") = 1
     * StringTool.indexOf("aabaabaa", "")   = 0
     * </pre>
     *
     * @param seq  the CharSequence to check, may be null
     * @param searchSeq  the CharSequence to find, may be null
     * @return the first index of the search CharSequence,
     *  -1 if no match or {@code null} string input
     */
    public static int indexOf(final CharSequence seq, final CharSequence searchSeq)
    {
        return Strings.CS.indexOf(seq, searchSeq);
    }

    /**
     * Finds the first index within a CharSequence, handling {@code null}.
     * This method uses {@link String#indexOf(String, int)} if possible.
     *
     * <p>A {@code null} CharSequence will return {@code -1}.
     * A negative start position is treated as zero.
     * An empty ("") search CharSequence always matches.
     * A start position greater than the string length only matches
     * an empty search CharSequence.</p>
     *
     * <pre>
     * StringTool.indexOf(null, *, *)          = -1
     * StringTool.indexOf(*, null, *)          = -1
     * StringTool.indexOf("", "", 0)           = 0
     * StringTool.indexOf("", *, 0)            = -1 (except when * = "")
     * StringTool.indexOf("aabaabaa", "a", 0)  = 0
     * StringTool.indexOf("aabaabaa", "b", 0)  = 2
     * StringTool.indexOf("aabaabaa", "ab", 0) = 1
     * StringTool.indexOf("aabaabaa", "b", 3)  = 5
     * StringTool.indexOf("aabaabaa", "b", 9)  = -1
     * StringTool.indexOf("aabaabaa", "b", -1) = 2
     * StringTool.indexOf("aabaabaa", "", 2)   = 2
     * StringTool.indexOf("abc", "", 9)        = 3
     * </pre>
     *
     * @param seq  the CharSequence to check, may be null
     * @param searchSeq  the CharSequence to find, may be null
     * @param startPos  the start position, negative treated as zero
     * @return the first index of the search CharSequence (always &ge; startPos),
     *  -1 if no match or {@code null} string input
     */
    public static int indexOf(final CharSequence seq, final CharSequence searchSeq, final int startPos)
    {
        return Strings.CS.indexOf(seq, searchSeq, startPos);
    }

    /**
     * Case in-sensitive find of the first index within a CharSequence.
     *
     * <p>A {@code null} CharSequence will return {@code -1}.
     * A negative start position is treated as zero.
     * An empty ("") search CharSequence always matches.
     * A start position greater than the string length only matches
     * an empty search CharSequence.</p>
     *
     * <pre>
     * StringTool.indexOfIgnoreCase(null, *)          = -1
     * StringTool.indexOfIgnoreCase(*, null)          = -1
     * StringTool.indexOfIgnoreCase("", "")           = 0
     * StringTool.indexOfIgnoreCase(" ", " ")         = 0
     * StringTool.indexOfIgnoreCase("aabaabaa", "a")  = 0
     * StringTool.indexOfIgnoreCase("aabaabaa", "b")  = 2
     * StringTool.indexOfIgnoreCase("aabaabaa", "ab") = 1
     * </pre>
     *
     * @param str  the CharSequence to check, may be null
     * @param searchStr  the CharSequence to find, may be null
     * @return the first index of the search CharSequence,
     *  -1 if no match or {@code null} string input
     */
    public static int indexOfIgnoreCase(final CharSequence str, final CharSequence searchStr)
    {
        return Strings.CI.indexOf(str, searchStr);
    }

    /**
     * Case in-sensitive find of the first index within a CharSequence
     * from the specified position.
     *
     * <p>A {@code null} CharSequence will return {@code -1}.
     * A negative start position is treated as zero.
     * An empty ("") search CharSequence always matches.
     * A start position greater than the string length only matches
     * an empty search CharSequence.</p>
     *
     * <pre>
     * StringTool.indexOfIgnoreCase(null, *, *)          = -1
     * StringTool.indexOfIgnoreCase(*, null, *)          = -1
     * StringTool.indexOfIgnoreCase("", "", 0)           = 0
     * StringTool.indexOfIgnoreCase("aabaabaa", "A", 0)  = 0
     * StringTool.indexOfIgnoreCase("aabaabaa", "B", 0)  = 2
     * StringTool.indexOfIgnoreCase("aabaabaa", "AB", 0) = 1
     * StringTool.indexOfIgnoreCase("aabaabaa", "B", 3)  = 5
     * StringTool.indexOfIgnoreCase("aabaabaa", "B", 9)  = -1
     * StringTool.indexOfIgnoreCase("aabaabaa", "B", -1) = 2
     * StringTool.indexOfIgnoreCase("aabaabaa", "", 2)   = 2
     * StringTool.indexOfIgnoreCase("abc", "", 9)        = -1
     * </pre>
     *
     * @param str  the CharSequence to check, may be null
     * @param searchStr  the CharSequence to find, may be null
     * @param startPos  the start position, negative treated as zero
     * @return the first index of the search CharSequence (always &ge; startPos),
     *  -1 if no match or {@code null} string input
     */
    public static int indexOfIgnoreCase(final CharSequence str, final CharSequence searchStr, final int startPos)
    {
        return Strings.CI.indexOf(str, searchStr, startPos);
    }

    /**
     * Finds the last index within a CharSequence, handling {@code null}.
     * This method uses {@link String#lastIndexOf(String)} if possible.
     *
     * <p>A {@code null} CharSequence will return {@code -1}.</p>
     *
     * <pre>
     * StringTool.lastIndexOf(null, *)          = -1
     * StringTool.lastIndexOf(*, null)          = -1
     * StringTool.lastIndexOf("", "")           = 0
     * StringTool.lastIndexOf("aabaabaa", "a")  = 7
     * StringTool.lastIndexOf("aabaabaa", "b")  = 5
     * StringTool.lastIndexOf("aabaabaa", "ab") = 4
     * StringTool.lastIndexOf("aabaabaa", "")   = 8
     * </pre>
     *
     * @param seq  the CharSequence to check, may be null
     * @param searchSeq  the CharSequence to find, may be null
     * @return the last index of the search String,
     *  -1 if no match or {@code null} string input
     */
    public static int lastIndexOf(final CharSequence seq, final CharSequence searchSeq)
    {
        return Strings.CS.lastIndexOf(seq, searchSeq);
    }

    /**
     * Finds the last index within a CharSequence, handling {@code null}.
     * This method uses {@link String#lastIndexOf(String, int)} if possible.
     *
     * <p>A {@code null} CharSequence will return {@code -1}.
     * A negative start position returns {@code -1}.
     * An empty ("") search CharSequence always matches unless the start position is negative.
     * A start position greater than the string length searches the whole string.
     * The search starts at the startPos and works backwards; matches starting after the start
     * position are ignored.
     * </p>
     *
     * <pre>
     * StringTool.lastIndexOf(null, *, *)          = -1
     * StringTool.lastIndexOf(*, null, *)          = -1
     * StringTool.lastIndexOf("aabaabaa", "a", 8)  = 7
     * StringTool.lastIndexOf("aabaabaa", "b", 8)  = 5
     * StringTool.lastIndexOf("aabaabaa", "ab", 8) = 4
     * StringTool.lastIndexOf("aabaabaa", "b", 9)  = 5
     * StringTool.lastIndexOf("aabaabaa", "b", -1) = -1
     * StringTool.lastIndexOf("aabaabaa", "a", 0)  = 0
     * StringTool.lastIndexOf("aabaabaa", "b", 0)  = -1
     * StringTool.lastIndexOf("aabaabaa", "b", 1)  = -1
     * StringTool.lastIndexOf("aabaabaa", "b", 2)  = 2
     * StringTool.lastIndexOf("aabaabaa", "ba", 2)  = 2
     * </pre>
     *
     * @param seq  the CharSequence to check, may be null
     * @param searchSeq  the CharSequence to find, may be null
     * @param startPos  the start position, negative treated as zero
     * @return the last index of the search CharSequence (always &le; startPos),
     *  -1 if no match or {@code null} string input
     */
    public static int lastIndexOf(final CharSequence seq, final CharSequence searchSeq, final int startPos)
    {
        return Strings.CS.lastIndexOf(seq, searchSeq, startPos);
    }

    /**
     * Case in-sensitive find of the last index within a CharSequence.
     *
     * <p>A {@code null} CharSequence will return {@code -1}.
     * A negative start position returns {@code -1}.
     * An empty ("") search CharSequence always matches unless the start position is negative.
     * A start position greater than the string length searches the whole string.</p>
     *
     * <pre>
     * StringTool.lastIndexOfIgnoreCase(null, *)          = -1
     * StringTool.lastIndexOfIgnoreCase(*, null)          = -1
     * StringTool.lastIndexOfIgnoreCase("aabaabaa", "A")  = 7
     * StringTool.lastIndexOfIgnoreCase("aabaabaa", "B")  = 5
     * StringTool.lastIndexOfIgnoreCase("aabaabaa", "AB") = 4
     * </pre>
     *
     * @param str  the CharSequence to check, may be null
     * @param searchStr  the CharSequence to find, may be null
     * @return the first index of the search CharSequence,
     *  -1 if no match or {@code null} string input
     */
    public static int lastIndexOfIgnoreCase(final CharSequence str, final CharSequence searchStr)
    {
        return Strings.CI.lastIndexOf(str, searchStr);
    }

    /**
     * Case in-sensitive find of the last index within a CharSequence
     * from the specified position.
     *
     * <p>A {@code null} CharSequence will return {@code -1}.
     * A negative start position returns {@code -1}.
     * An empty ("") search CharSequence always matches unless the start position is negative.
     * A start position greater than the string length searches the whole string.
     * The search starts at the startPos and works backwards; matches starting after the start
     * position are ignored.
     * </p>
     *
     * <pre>
     * StringTool.lastIndexOfIgnoreCase(null, *, *)          = -1
     * StringTool.lastIndexOfIgnoreCase(*, null, *)          = -1
     * StringTool.lastIndexOfIgnoreCase("aabaabaa", "A", 8)  = 7
     * StringTool.lastIndexOfIgnoreCase("aabaabaa", "B", 8)  = 5
     * StringTool.lastIndexOfIgnoreCase("aabaabaa", "AB", 8) = 4
     * StringTool.lastIndexOfIgnoreCase("aabaabaa", "B", 9)  = 5
     * StringTool.lastIndexOfIgnoreCase("aabaabaa", "B", -1) = -1
     * StringTool.lastIndexOfIgnoreCase("aabaabaa", "A", 0)  = 0
     * StringTool.lastIndexOfIgnoreCase("aabaabaa", "B", 0)  = -1
     * </pre>
     *
     * @param str  the CharSequence to check, may be null
     * @param searchStr  the CharSequence to find, may be null
     * @param startPos  the start position
     * @return the last index of the search CharSequence (always &le; startPos),
     *  -1 if no match or {@code null} input
     */
    public static int lastIndexOfIgnoreCase(final CharSequence str, final CharSequence searchStr, final int startPos)
    {
        return Strings.CI.lastIndexOf(str, searchStr, startPos);
    }

    /**
     * Prepends the prefix to the start of the string if the string does not already start with any of the prefixes.
     *
     * <pre>
     * StringTool.prependIfMissing(null, null) = null
     * StringTool.prependIfMissing("abc", null) = "abc"
     * StringTool.prependIfMissing("", "xyz") = "xyz"
     * StringTool.prependIfMissing("abc", "xyz") = "xyzabc"
     * StringTool.prependIfMissing("xyzabc", "xyz") = "xyzabc"
     * StringTool.prependIfMissing("XYZabc", "xyz") = "xyzXYZabc"
     * </pre>
     * <p>
     * With additional prefixes,
     * </p>
     *
     * <pre>
     * StringTool.prependIfMissing(null, null, null) = null
     * StringTool.prependIfMissing("abc", null, null) = "abc"
     * StringTool.prependIfMissing("", "xyz", null) = "xyz"
     * StringTool.prependIfMissing("abc", "xyz", new CharSequence[]{null}) = "xyzabc"
     * StringTool.prependIfMissing("abc", "xyz", "") = "abc"
     * StringTool.prependIfMissing("abc", "xyz", "mno") = "xyzabc"
     * StringTool.prependIfMissing("xyzabc", "xyz", "mno") = "xyzabc"
     * StringTool.prependIfMissing("mnoabc", "xyz", "mno") = "mnoabc"
     * StringTool.prependIfMissing("XYZabc", "xyz", "mno") = "xyzXYZabc"
     * StringTool.prependIfMissing("MNOabc", "xyz", "mno") = "xyzMNOabc"
     * </pre>
     *
     * @param str      The string.
     * @param prefix   The prefix to prepend to the start of the string.
     * @param prefixes Additional prefixes that are valid.
     * @return A new String if prefix was prepended, the same string otherwise.
     */
    public static String prependIfMissing(final String str, final CharSequence prefix, final CharSequence... prefixes)
    {
        return Strings.CS.prependIfMissing(str, prefix, prefixes);
    }

    /**
     * Prepends the prefix to the start of the string if the string does not
     * already start, case-insensitive, with any of the prefixes.
     *
     * <pre>
     * StringTool.prependIfMissingIgnoreCase(null, null) = null
     * StringTool.prependIfMissingIgnoreCase("abc", null) = "abc"
     * StringTool.prependIfMissingIgnoreCase("", "xyz") = "xyz"
     * StringTool.prependIfMissingIgnoreCase("abc", "xyz") = "xyzabc"
     * StringTool.prependIfMissingIgnoreCase("xyzabc", "xyz") = "xyzabc"
     * StringTool.prependIfMissingIgnoreCase("XYZabc", "xyz") = "XYZabc"
     * </pre>
     * <p>With additional prefixes,</p>
     * <pre>
     * StringTool.prependIfMissingIgnoreCase(null, null, null) = null
     * StringTool.prependIfMissingIgnoreCase("abc", null, null) = "abc"
     * StringTool.prependIfMissingIgnoreCase("", "xyz", null) = "xyz"
     * StringTool.prependIfMissingIgnoreCase("abc", "xyz", new CharSequence[]{null}) = "xyzabc"
     * StringTool.prependIfMissingIgnoreCase("abc", "xyz", "") = "abc"
     * StringTool.prependIfMissingIgnoreCase("abc", "xyz", "mno") = "xyzabc"
     * StringTool.prependIfMissingIgnoreCase("xyzabc", "xyz", "mno") = "xyzabc"
     * StringTool.prependIfMissingIgnoreCase("mnoabc", "xyz", "mno") = "mnoabc"
     * StringTool.prependIfMissingIgnoreCase("XYZabc", "xyz", "mno") = "XYZabc"
     * StringTool.prependIfMissingIgnoreCase("MNOabc", "xyz", "mno") = "MNOabc"
     * </pre>
     *
     * @param str The string.
     * @param prefix The prefix to prepend to the start of the string.
     * @param prefixes Additional prefixes that are valid (optional).
     * @return A new String if prefix was prepended, the same string otherwise.
     */
    public static String prependIfMissingIgnoreCase(final String str, final CharSequence prefix,
        final CharSequence... prefixes)
    {
        return Strings.CI.prependIfMissing(str, prefix, prefixes);
    }

    /**
     * Removes all occurrences of a substring from within the source string.
     *
     * <p>A {@code null} source string will return {@code null}.
     * An empty ("") source string will return the empty string.
     * A {@code null} remove string will return the source string.
     * An empty ("") remove string will return the source string.</p>
     *
     * <pre>
     * StringTool.remove(null, *)        = null
     * StringTool.remove("", *)          = ""
     * StringTool.remove(*, null)        = *
     * StringTool.remove(*, "")          = *
     * StringTool.remove("queued", "ue") = "qd"
     * StringTool.remove("queued", "zz") = "queued"
     * </pre>
     *
     * @param str  the source String to search, may be null
     * @param remove  the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     *  {@code null} if null String input
     */
    public static String remove(final String str, final String remove)
    {
        return Strings.CS.remove(str, remove);
    }

    /**
     * Removes a substring only if it is at the end of a source string,
     * otherwise returns the source string.
     *
     * <p>A {@code null} source string will return {@code null}.
     * An empty ("") source string will return the empty string.
     * A {@code null} search string will return the source string.</p>
     *
     * <pre>
     * StringTool.removeEnd(null, *)      = null
     * StringTool.removeEnd("", *)        = ""
     * StringTool.removeEnd(*, null)      = *
     * StringTool.removeEnd("www.domain.com", ".com.")  = "www.domain.com"
     * StringTool.removeEnd("www.domain.com", ".com")   = "www.domain"
     * StringTool.removeEnd("www.domain.com", "domain") = "www.domain.com"
     * StringTool.removeEnd("abc", "")    = "abc"
     * </pre>
     *
     * @param str  the source String to search, may be null
     * @param remove  the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     *  {@code null} if null String input
     */
    public static String removeEnd(final String str, final String remove)
    {
        return Strings.CS.removeEnd(str, remove);
    }

    /**
     * Case-insensitive removal of a substring if it is at the end of a source string,
     * otherwise returns the source string.
     *
     * <p>A {@code null} source string will return {@code null}.
     * An empty ("") source string will return the empty string.
     * A {@code null} search string will return the source string.</p>
     *
     * <pre>
     * StringTool.removeEndIgnoreCase(null, *)      = null
     * StringTool.removeEndIgnoreCase("", *)        = ""
     * StringTool.removeEndIgnoreCase(*, null)      = *
     * StringTool.removeEndIgnoreCase("www.domain.com", ".com.")  = "www.domain.com"
     * StringTool.removeEndIgnoreCase("www.domain.com", ".com")   = "www.domain"
     * StringTool.removeEndIgnoreCase("www.domain.com", "domain") = "www.domain.com"
     * StringTool.removeEndIgnoreCase("abc", "")    = "abc"
     * StringTool.removeEndIgnoreCase("www.domain.com", ".COM") = "www.domain")
     * StringTool.removeEndIgnoreCase("www.domain.COM", ".com") = "www.domain")
     * </pre>
     *
     * @param str  the source String to search, may be null
     * @param remove  the String to search for (case-insensitive) and remove, may be null
     * @return the substring with the string removed if found,
     *  {@code null} if null String input
     */
    public static String removeEndIgnoreCase(final String str, final String remove)
    {
        return Strings.CI.removeEnd(str, remove);
    }

    /**
     * Case-insensitive removal of all occurrences of a substring from within
     * the source string.
     *
     * <p>
     * A {@code null} source string will return {@code null}. An empty ("")
     * source string will return the empty string. A {@code null} remove string
     * will return the source string. An empty ("") remove string will return
     * the source string.
     * </p>
     *
     * <pre>
     * StringTool.removeIgnoreCase(null, *)        = null
     * StringTool.removeIgnoreCase("", *)          = ""
     * StringTool.removeIgnoreCase(*, null)        = *
     * StringTool.removeIgnoreCase(*, "")          = *
     * StringTool.removeIgnoreCase("queued", "ue") = "qd"
     * StringTool.removeIgnoreCase("queued", "zz") = "queued"
     * StringTool.removeIgnoreCase("quEUed", "UE") = "qd"
     * StringTool.removeIgnoreCase("queued", "zZ") = "queued"
     * </pre>
     *
     * @param str
     *            the source String to search, may be null
     * @param remove
     *            the String to search for (case-insensitive) and remove, may be
     *            null
     * @return the substring with the string removed if found, {@code null} if
     *         null String input
     */
    public static String removeIgnoreCase(final String str, final String remove)
    {
        return Strings.CI.remove(str, remove);
    }

    /**
     * Removes a substring only if it is at the beginning of a source string,
     * otherwise returns the source string.
     *
     * <p>A {@code null} source string will return {@code null}.
     * An empty ("") source string will return the empty string.
     * A {@code null} search string will return the source string.</p>
     *
     * <pre>
     * StringTool.removeStart(null, *)      = null
     * StringTool.removeStart("", *)        = ""
     * StringTool.removeStart(*, null)      = *
     * StringTool.removeStart("www.domain.com", "www.")   = "domain.com"
     * StringTool.removeStart("domain.com", "www.")       = "domain.com"
     * StringTool.removeStart("www.domain.com", "domain") = "www.domain.com"
     * StringTool.removeStart("abc", "")    = "abc"
     * </pre>
     *
     * @param str  the source String to search, may be null
     * @param remove  the String to search for and remove, may be null
     * @return the substring with the string removed if found,
     *  {@code null} if null String input
     */
    public static String removeStart(final String str, final String remove)
    {
        return Strings.CS.removeStart(str, remove);
    }

    /**
     * Case-insensitive removal of a substring if it is at the beginning of a source string,
     * otherwise returns the source string.
     *
     * <p>A {@code null} source string will return {@code null}.
     * An empty ("") source string will return the empty string.
     * A {@code null} search string will return the source string.</p>
     *
     * <pre>
     * StringTool.removeStartIgnoreCase(null, *)      = null
     * StringTool.removeStartIgnoreCase("", *)        = ""
     * StringTool.removeStartIgnoreCase(*, null)      = *
     * StringTool.removeStartIgnoreCase("www.domain.com", "www.")   = "domain.com"
     * StringTool.removeStartIgnoreCase("www.domain.com", "WWW.")   = "domain.com"
     * StringTool.removeStartIgnoreCase("domain.com", "www.")       = "domain.com"
     * StringTool.removeStartIgnoreCase("www.domain.com", "domain") = "www.domain.com"
     * StringTool.removeStartIgnoreCase("abc", "")    = "abc"
     * </pre>
     *
     * @param str  the source String to search, may be null
     * @param remove  the String to search for (case-insensitive) and remove, may be null
     * @return the substring with the string removed if found,
     *  {@code null} if null String input
     */
    public static String removeStartIgnoreCase(final String str, final String remove)
    {
        return Strings.CI.removeStart(str, remove);
    }

    /**
     * Replaces all occurrences of a String within another String.
     *
     * <p>A {@code null} reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringTool.replace(null, *, *)        = null
     * StringTool.replace("", *, *)          = ""
     * StringTool.replace("any", null, *)    = "any"
     * StringTool.replace("any", *, null)    = "any"
     * StringTool.replace("any", "", *)      = "any"
     * StringTool.replace("aba", "a", null)  = "aba"
     * StringTool.replace("aba", "a", "")    = "b"
     * StringTool.replace("aba", "a", "z")   = "zbz"
     * </pre>
     *
     * @see #replace(String text, String searchString, String replacement, int max)
     * @param text  text to search and replace in, may be null
     * @param searchString  the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @return the text with any replacements processed,
     *  {@code null} if null String input
     */
    public static String replace(final String text, final String searchString, final String replacement)
    {
        return Strings.CS.replace(text, searchString, replacement);
    }

    /**
     * Replaces a String with another String inside a larger String,
     * for the first {@code max} values of the search String.
     *
     * <p>A {@code null} reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringTool.replace(null, *, *, *)         = null
     * StringTool.replace("", *, *, *)           = ""
     * StringTool.replace("any", null, *, *)     = "any"
     * StringTool.replace("any", *, null, *)     = "any"
     * StringTool.replace("any", "", *, *)       = "any"
     * StringTool.replace("any", *, *, 0)        = "any"
     * StringTool.replace("abaa", "a", null, -1) = "abaa"
     * StringTool.replace("abaa", "a", "", -1)   = "b"
     * StringTool.replace("abaa", "a", "z", 0)   = "abaa"
     * StringTool.replace("abaa", "a", "z", 1)   = "zbaa"
     * StringTool.replace("abaa", "a", "z", 2)   = "zbza"
     * StringTool.replace("abaa", "a", "z", -1)  = "zbzz"
     * </pre>
     *
     * @param text  text to search and replace in, may be null
     * @param searchString  the String to search for, may be null
     * @param replacement  the String to replace it with, may be null
     * @param max  maximum number of values to replace, or {@code -1} if no maximum
     * @return the text with any replacements processed,
     *  {@code null} if null String input
     */
    public static String replace(final String text, final String searchString, final String replacement, final int max)
    {
        return Strings.CS.replace(text, searchString, replacement, max);
    }

    /**
     * Case insensitively replaces all occurrences of a String within another String.
     *
     * <p>A {@code null} reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringTool.replaceIgnoreCase(null, *, *)        = null
     * StringTool.replaceIgnoreCase("", *, *)          = ""
     * StringTool.replaceIgnoreCase("any", null, *)    = "any"
     * StringTool.replaceIgnoreCase("any", *, null)    = "any"
     * StringTool.replaceIgnoreCase("any", "", *)      = "any"
     * StringTool.replaceIgnoreCase("aba", "a", null)  = "aba"
     * StringTool.replaceIgnoreCase("abA", "A", "")    = "b"
     * StringTool.replaceIgnoreCase("aba", "A", "z")   = "zbz"
     * </pre>
     *
     * @see #replaceIgnoreCase(String text, String searchString, String replacement, int max)
     * @param text  text to search and replace in, may be null
     * @param searchString  the String to search for (case-insensitive), may be null
     * @param replacement  the String to replace it with, may be null
     * @return the text with any replacements processed,
     *  {@code null} if null String input
     */
    public static String replaceIgnoreCase(final String text, final String searchString, final String replacement)
    {
        return Strings.CI.replace(text, searchString, replacement);
    }

    /**
     * Case insensitively replaces a String with another String inside a larger String,
     * for the first {@code max} values of the search String.
     *
     * <p>A {@code null} reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringTool.replaceIgnoreCase(null, *, *, *)         = null
     * StringTool.replaceIgnoreCase("", *, *, *)           = ""
     * StringTool.replaceIgnoreCase("any", null, *, *)     = "any"
     * StringTool.replaceIgnoreCase("any", *, null, *)     = "any"
     * StringTool.replaceIgnoreCase("any", "", *, *)       = "any"
     * StringTool.replaceIgnoreCase("any", *, *, 0)        = "any"
     * StringTool.replaceIgnoreCase("abaa", "a", null, -1) = "abaa"
     * StringTool.replaceIgnoreCase("abaa", "a", "", -1)   = "b"
     * StringTool.replaceIgnoreCase("abaa", "a", "z", 0)   = "abaa"
     * StringTool.replaceIgnoreCase("abaa", "A", "z", 1)   = "zbaa"
     * StringTool.replaceIgnoreCase("abAa", "a", "z", 2)   = "zbza"
     * StringTool.replaceIgnoreCase("abAa", "a", "z", -1)  = "zbzz"
     * </pre>
     *
     * @param text  text to search and replace in, may be null
     * @param searchString  the String to search for (case-insensitive), may be null
     * @param replacement  the String to replace it with, may be null
     * @param max  maximum number of values to replace, or {@code -1} if no maximum
     * @return the text with any replacements processed,
     *  {@code null} if null String input
     */
    public static String replaceIgnoreCase(final String text, final String searchString, final String replacement,
        final int max)
    {
        return Strings.CI.replace(text, searchString, replacement, max);
    }

    /**
     * Replaces a String with another String inside a larger String, once.
     *
     * <p>A {@code null} reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringTool.replaceOnce(null, *, *)        = null
     * StringTool.replaceOnce("", *, *)          = ""
     * StringTool.replaceOnce("any", null, *)    = "any"
     * StringTool.replaceOnce("any", *, null)    = "any"
     * StringTool.replaceOnce("any", "", *)      = "any"
     * StringTool.replaceOnce("aba", "a", null)  = "aba"
     * StringTool.replaceOnce("aba", "a", "")    = "ba"
     * StringTool.replaceOnce("aba", "a", "z")   = "zba"
     * </pre>
     *
     * @see #replace(String text, String searchString, String replacement, int max)
     * @param text  text to search and replace in, may be null
     * @param searchString  the String to search for, may be null
     * @param replacement  the String to replace with, may be null
     * @return the text with any replacements processed,
     *  {@code null} if null String input
     */
    public static String replaceOnce(final String text, final String searchString, final String replacement)
    {
        return Strings.CS.replaceOnce(text, searchString, replacement);
    }

    /**
     * Case insensitively replaces a String with another String inside a larger String, once.
     *
     * <p>A {@code null} reference passed to this method is a no-op.</p>
     *
     * <pre>
     * StringTool.replaceOnceIgnoreCase(null, *, *)        = null
     * StringTool.replaceOnceIgnoreCase("", *, *)          = ""
     * StringTool.replaceOnceIgnoreCase("any", null, *)    = "any"
     * StringTool.replaceOnceIgnoreCase("any", *, null)    = "any"
     * StringTool.replaceOnceIgnoreCase("any", "", *)      = "any"
     * StringTool.replaceOnceIgnoreCase("aba", "a", null)  = "aba"
     * StringTool.replaceOnceIgnoreCase("aba", "a", "")    = "ba"
     * StringTool.replaceOnceIgnoreCase("aba", "a", "z")   = "zba"
     * StringTool.replaceOnceIgnoreCase("FoOFoofoo", "foo", "") = "Foofoo"
     * </pre>
     *
     * @see #replaceIgnoreCase(String text, String searchString, String replacement, int max)
     * @param text  text to search and replace in, may be null
     * @param searchString  the String to search for (case-insensitive), may be null
     * @param replacement  the String to replace with, may be null
     * @return the text with any replacements processed,
     *  {@code null} if null String input
     */
    public static String replaceOnceIgnoreCase(final String text, final String searchString, final String replacement)
    {
        return Strings.CI.replaceOnce(text, searchString, replacement);
    }

    /**
     * Tests if a CharSequence starts with a specified prefix.
     *
     * <p>{@code null}s are handled without exceptions. Two {@code null}
     * references are considered to be equal. The comparison is case-sensitive.</p>
     *
     * <pre>
     * StringTool.startsWith(null, null)      = true
     * StringTool.startsWith(null, "abc")     = false
     * StringTool.startsWith("abcdef", null)  = false
     * StringTool.startsWith("abcdef", "abc") = true
     * StringTool.startsWith("ABCDEF", "abc") = false
     * </pre>
     *
     * @see String#startsWith(String)
     * @param str  the CharSequence to check, may be null
     * @param prefix the prefix to find, may be null
     * @return {@code true} if the CharSequence starts with the prefix, case-sensitive, or
     *  both {@code null}
     */
    public static boolean startsWith(final CharSequence str, final CharSequence prefix)
    {
        return Strings.CS.startsWith(str, prefix);
    }

    /**
     * Tests if a CharSequence starts with any of the provided case-sensitive prefixes.
     *
     * <pre>
     * StringTool.startsWithAny(null, null)      = false
     * StringTool.startsWithAny(null, new String[] {"abc"})  = false
     * StringTool.startsWithAny("abcxyz", null)     = false
     * StringTool.startsWithAny("abcxyz", new String[] {""}) = true
     * StringTool.startsWithAny("abcxyz", new String[] {"abc"}) = true
     * StringTool.startsWithAny("abcxyz", new String[] {null, "xyz", "abc"}) = true
     * StringTool.startsWithAny("abcxyz", null, "xyz", "ABCX") = false
     * StringTool.startsWithAny("ABCXYZ", null, "xyz", "abc") = false
     * </pre>
     *
     * @param sequence the CharSequence to check, may be null
     * @param searchStrings the case-sensitive CharSequence prefixes, may be empty or contain {@code null}
     * @see #startsWith(CharSequence, CharSequence)
     * @return {@code true} if the input {@code sequence} is {@code null} AND no {@code searchStrings} are provided, or
     *   the input {@code sequence} begins with any of the provided case-sensitive {@code searchStrings}.
     */
    public static boolean startsWithAny(final CharSequence sequence, final CharSequence... searchStrings)
    {
        return Strings.CS.startsWithAny(sequence, searchStrings);
    }

    /**
     * Case-insensitive check if a CharSequence starts with a specified prefix.
     *
     * <p>{@code null}s are handled without exceptions. Two {@code null}
     * references are considered to be equal. The comparison is case insensitive.</p>
     *
     * <pre>
     * StringTool.startsWithIgnoreCase(null, null)      = true
     * StringTool.startsWithIgnoreCase(null, "abc")     = false
     * StringTool.startsWithIgnoreCase("abcdef", null)  = false
     * StringTool.startsWithIgnoreCase("abcdef", "abc") = true
     * StringTool.startsWithIgnoreCase("ABCDEF", "abc") = true
     * </pre>
     *
     * @see String#startsWith(String)
     * @param str  the CharSequence to check, may be null
     * @param prefix the prefix to find, may be null
     * @return {@code true} if the CharSequence starts with the prefix, case-insensitive, or
     *  both {@code null}
     */
    public static boolean startsWithIgnoreCase(final CharSequence str, final CharSequence prefix)
    {
        return Strings.CI.startsWith(str, prefix);
    }
}
