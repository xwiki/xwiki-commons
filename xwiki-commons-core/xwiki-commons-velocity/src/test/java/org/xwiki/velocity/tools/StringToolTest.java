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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link StringTool}.
 *
 * @version $Id$
 */
class StringToolTest
{
    @Test
    void defaultString()
    {
        assertEquals("abc", StringTool.defaultString("abc", "xyz"));
        assertEquals("xyz", StringTool.defaultString(null, "xyz"));
    }

    @Test
    void appendIfMissing()
    {
        assertNull(StringTool.appendIfMissing(null, null));
        assertEquals("abc", StringTool.appendIfMissing("abc", null));
        assertEquals("xyz", StringTool.appendIfMissing("", "xyz"));
        assertEquals("abcxyz", StringTool.appendIfMissing("abc", "xyz"));
        assertEquals("abcxyz", StringTool.appendIfMissing("abcxyz", "xyz"));
        assertEquals("abcXYZxyz", StringTool.appendIfMissing("abcXYZ", "xyz"));
    }

    @Test
    void appendIfMissingIgnoreCase()
    {
        assertNull(StringTool.appendIfMissingIgnoreCase(null, null));
        assertEquals("abc", StringTool.appendIfMissingIgnoreCase("abc", null));
        assertEquals("xyz", StringTool.appendIfMissingIgnoreCase("", "xyz"));
        assertEquals("abcxyz", StringTool.appendIfMissingIgnoreCase("abc", "xyz"));
        assertEquals("abcxyz", StringTool.appendIfMissingIgnoreCase("abcxyz", "xyz"));
        assertEquals("abcXYZ", StringTool.appendIfMissingIgnoreCase("abcXYZ", "xyz"));
    }

    @Test
    void chomp()
    {
        assertNull(StringTool.chomp(null, "bar"));
        assertEquals("", StringTool.chomp("", "bar"));
        assertEquals("foo", StringTool.chomp("foobar", "bar"));
        assertEquals("foobar", StringTool.chomp("foobar", "baz"));
        assertEquals("", StringTool.chomp("foo", "foo"));
        assertEquals("foo ", StringTool.chomp("foo ", "foo"));
    }

    @Test
    void compare()
    {
        assertEquals(0, StringTool.compare(null, null));
        assertTrue(StringTool.compare(null, "a") < 0);
        assertTrue(StringTool.compare("a", null) > 0);
        assertEquals(0, StringTool.compare("abc", "abc"));
        assertTrue(StringTool.compare("a", "b") < 0);
        assertTrue(StringTool.compare("b", "a") > 0);
    }

    @Test
    void compareIgnoreCase()
    {
        assertEquals(0, StringTool.compareIgnoreCase(null, null));
        assertTrue(StringTool.compareIgnoreCase(null, "a") < 0);
        assertTrue(StringTool.compareIgnoreCase("a", null) > 0);
        assertEquals(0, StringTool.compareIgnoreCase("abc", "abc"));
        assertEquals(0, StringTool.compareIgnoreCase("abc", "ABC"));
        assertTrue(StringTool.compareIgnoreCase("a", "b") < 0);
    }

    @Test
    void contains()
    {
        assertFalse(StringTool.contains(null, "a"));
        assertFalse(StringTool.contains("abc", null));
        assertTrue(StringTool.contains("", ""));
        assertTrue(StringTool.contains("abc", ""));
        assertTrue(StringTool.contains("abc", "a"));
        assertFalse(StringTool.contains("abc", "z"));
    }

    @Test
    void containsAny()
    {
        assertFalse(StringTool.containsAny(null, "a"));
        assertFalse(StringTool.containsAny("", "a"));
        assertFalse(StringTool.containsAny("abc", (CharSequence[]) null));
        assertFalse(StringTool.containsAny("abc", new CharSequence[0]));
        assertTrue(StringTool.containsAny("abcd", "ab", null));
        assertTrue(StringTool.containsAny("abcd", "ab", "cd"));
        assertTrue(StringTool.containsAny("abc", "d", "abc"));
    }

    @Test
    void containsAnyIgnoreCase()
    {
        assertFalse(StringTool.containsAnyIgnoreCase(null, "a"));
        assertFalse(StringTool.containsAnyIgnoreCase("", "a"));
        assertFalse(StringTool.containsAnyIgnoreCase("abc", (CharSequence[]) null));
        assertTrue(StringTool.containsAnyIgnoreCase("abcd", "AB", null));
        assertTrue(StringTool.containsAnyIgnoreCase("abc", "D", "ABC"));
        assertTrue(StringTool.containsAnyIgnoreCase("ABC", "d", "abc"));
    }

    @Test
    void containsIgnoreCase()
    {
        assertFalse(StringTool.containsIgnoreCase(null, "a"));
        assertFalse(StringTool.containsIgnoreCase("abc", null));
        assertTrue(StringTool.containsIgnoreCase("", ""));
        assertTrue(StringTool.containsIgnoreCase("abc", ""));
        assertTrue(StringTool.containsIgnoreCase("abc", "A"));
        assertFalse(StringTool.containsIgnoreCase("abc", "Z"));
    }

    @Test
    void endsWith()
    {
        assertTrue(StringTool.endsWith(null, null));
        assertFalse(StringTool.endsWith(null, "def"));
        assertFalse(StringTool.endsWith("abcdef", null));
        assertTrue(StringTool.endsWith("abcdef", "def"));
        assertFalse(StringTool.endsWith("ABCDEF", "def"));
        assertTrue(StringTool.endsWith("ABCDEF", ""));
    }

    @Test
    void endsWithAny()
    {
        assertFalse(StringTool.endsWithAny(null, null));
        assertFalse(StringTool.endsWithAny(null, new String[] {"abc"}));
        assertFalse(StringTool.endsWithAny("abcxyz", null));
        assertTrue(StringTool.endsWithAny("abcxyz", new String[] {""}));
        assertTrue(StringTool.endsWithAny("abcxyz", new String[] {"xyz"}));
        assertTrue(StringTool.endsWithAny("abcxyz", new String[] {null, "xyz", "abc"}));
    }

    @Test
    void endsWithIgnoreCase()
    {
        assertTrue(StringTool.endsWithIgnoreCase(null, null));
        assertFalse(StringTool.endsWithIgnoreCase(null, "def"));
        assertFalse(StringTool.endsWithIgnoreCase("abcdef", null));
        assertTrue(StringTool.endsWithIgnoreCase("abcdef", "def"));
        assertTrue(StringTool.endsWithIgnoreCase("ABCDEF", "def"));
        assertFalse(StringTool.endsWithIgnoreCase("ABCDEF", "cde"));
    }

    @Test
    void equals()
    {
        assertTrue(StringTool.equals(null, null));
        assertFalse(StringTool.equals(null, "abc"));
        assertFalse(StringTool.equals("abc", null));
        assertTrue(StringTool.equals("abc", "abc"));
        assertFalse(StringTool.equals("abc", "ABC"));
    }

    @Test
    void equalsAny()
    {
        assertFalse(StringTool.equalsAny(null, (CharSequence[]) null));
        assertTrue(StringTool.equalsAny(null, null, null));
        assertFalse(StringTool.equalsAny(null, "abc", "def"));
        assertFalse(StringTool.equalsAny("abc", null, "def"));
        assertTrue(StringTool.equalsAny("abc", "abc", "def"));
        assertFalse(StringTool.equalsAny("abc", "ABC", "DEF"));
    }

    @Test
    void equalsAnyIgnoreCase()
    {
        assertFalse(StringTool.equalsAnyIgnoreCase(null, (CharSequence[]) null));
        assertTrue(StringTool.equalsAnyIgnoreCase(null, null, null));
        assertFalse(StringTool.equalsAnyIgnoreCase(null, "abc", "def"));
        assertFalse(StringTool.equalsAnyIgnoreCase("abc", null, "def"));
        assertTrue(StringTool.equalsAnyIgnoreCase("abc", "abc", "def"));
        assertTrue(StringTool.equalsAnyIgnoreCase("abc", "ABC", "DEF"));
    }

    @Test
    void equalsIgnoreCase()
    {
        assertTrue(StringTool.equalsIgnoreCase(null, null));
        assertFalse(StringTool.equalsIgnoreCase(null, "abc"));
        assertFalse(StringTool.equalsIgnoreCase("abc", null));
        assertTrue(StringTool.equalsIgnoreCase("abc", "abc"));
        assertTrue(StringTool.equalsIgnoreCase("abc", "ABC"));
    }

    @Test
    void indexOf()
    {
        assertEquals(-1, StringTool.indexOf(null, "a"));
        assertEquals(-1, StringTool.indexOf("abc", null));
        assertEquals(0, StringTool.indexOf("", ""));
        assertEquals(0, StringTool.indexOf("aabaabaa", "a"));
        assertEquals(2, StringTool.indexOf("aabaabaa", "b"));
        assertEquals(1, StringTool.indexOf("aabaabaa", "ab"));
    }

    @Test
    void indexOfWithStartPos()
    {
        assertEquals(-1, StringTool.indexOf(null, "a", 0));
        assertEquals(-1, StringTool.indexOf("abc", null, 0));
        assertEquals(0, StringTool.indexOf("", "", 0));
        assertEquals(2, StringTool.indexOf("aabaabaa", "b", 0));
        assertEquals(5, StringTool.indexOf("aabaabaa", "b", 3));
        assertEquals(2, StringTool.indexOf("aabaabaa", "b", -1));
    }

    @Test
    void indexOfIgnoreCase()
    {
        assertEquals(-1, StringTool.indexOfIgnoreCase(null, "a"));
        assertEquals(-1, StringTool.indexOfIgnoreCase("abc", null));
        assertEquals(0, StringTool.indexOfIgnoreCase("", ""));
        assertEquals(0, StringTool.indexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(2, StringTool.indexOfIgnoreCase("aabaabaa", "B"));
        assertEquals(1, StringTool.indexOfIgnoreCase("aabaabaa", "AB"));
    }

    @Test
    void indexOfIgnoreCaseWithStartPos()
    {
        assertEquals(-1, StringTool.indexOfIgnoreCase(null, "a", 0));
        assertEquals(-1, StringTool.indexOfIgnoreCase("abc", null, 0));
        assertEquals(0, StringTool.indexOfIgnoreCase("", "", 0));
        assertEquals(0, StringTool.indexOfIgnoreCase("aabaabaa", "A", 0));
        assertEquals(2, StringTool.indexOfIgnoreCase("aabaabaa", "B", 0));
        assertEquals(5, StringTool.indexOfIgnoreCase("aabaabaa", "B", 3));
    }

    @Test
    void lastIndexOf()
    {
        assertEquals(-1, StringTool.lastIndexOf(null, "a"));
        assertEquals(-1, StringTool.lastIndexOf("abc", null));
        assertEquals(0, StringTool.lastIndexOf("", ""));
        assertEquals(7, StringTool.lastIndexOf("aabaabaa", "a"));
        assertEquals(5, StringTool.lastIndexOf("aabaabaa", "b"));
        assertEquals(4, StringTool.lastIndexOf("aabaabaa", "ab"));
    }

    @Test
    void lastIndexOfWithStartPos()
    {
        assertEquals(-1, StringTool.lastIndexOf(null, "a", 0));
        assertEquals(-1, StringTool.lastIndexOf("abc", null, 0));
        assertEquals(7, StringTool.lastIndexOf("aabaabaa", "a", 8));
        assertEquals(5, StringTool.lastIndexOf("aabaabaa", "b", 8));
        assertEquals(4, StringTool.lastIndexOf("aabaabaa", "ab", 8));
        assertEquals(0, StringTool.lastIndexOf("aabaabaa", "a", 0));
    }

    @Test
    void lastIndexOfIgnoreCase()
    {
        assertEquals(-1, StringTool.lastIndexOfIgnoreCase(null, "a"));
        assertEquals(-1, StringTool.lastIndexOfIgnoreCase("abc", null));
        assertEquals(7, StringTool.lastIndexOfIgnoreCase("aabaabaa", "A"));
        assertEquals(5, StringTool.lastIndexOfIgnoreCase("aabaabaa", "B"));
        assertEquals(4, StringTool.lastIndexOfIgnoreCase("aabaabaa", "AB"));
    }

    @Test
    void lastIndexOfIgnoreCaseWithStartPos()
    {
        assertEquals(-1, StringTool.lastIndexOfIgnoreCase(null, "a", 0));
        assertEquals(-1, StringTool.lastIndexOfIgnoreCase("abc", null, 0));
        assertEquals(7, StringTool.lastIndexOfIgnoreCase("aabaabaa", "A", 8));
        assertEquals(5, StringTool.lastIndexOfIgnoreCase("aabaabaa", "B", 8));
        assertEquals(4, StringTool.lastIndexOfIgnoreCase("aabaabaa", "AB", 8));
        assertEquals(-1, StringTool.lastIndexOfIgnoreCase("aabaabaa", "B", -1));
    }

    @Test
    void prependIfMissing()
    {
        assertNull(StringTool.prependIfMissing(null, null));
        assertEquals("abc", StringTool.prependIfMissing("abc", null));
        assertEquals("xyz", StringTool.prependIfMissing("", "xyz"));
        assertEquals("xyzabc", StringTool.prependIfMissing("abc", "xyz"));
        assertEquals("xyzabc", StringTool.prependIfMissing("xyzabc", "xyz"));
        assertEquals("xyzXYZabc", StringTool.prependIfMissing("XYZabc", "xyz"));
    }

    @Test
    void prependIfMissingIgnoreCase()
    {
        assertNull(StringTool.prependIfMissingIgnoreCase(null, null));
        assertEquals("abc", StringTool.prependIfMissingIgnoreCase("abc", null));
        assertEquals("xyz", StringTool.prependIfMissingIgnoreCase("", "xyz"));
        assertEquals("xyzabc", StringTool.prependIfMissingIgnoreCase("abc", "xyz"));
        assertEquals("xyzabc", StringTool.prependIfMissingIgnoreCase("xyzabc", "xyz"));
        assertEquals("XYZabc", StringTool.prependIfMissingIgnoreCase("XYZabc", "xyz"));
    }

    @Test
    void remove()
    {
        assertNull(StringTool.remove(null, "ue"));
        assertEquals("", StringTool.remove("", "ue"));
        assertEquals("queued", StringTool.remove("queued", null));
        assertEquals("queued", StringTool.remove("queued", ""));
        assertEquals("qd", StringTool.remove("queued", "ue"));
        assertEquals("queued", StringTool.remove("queued", "zz"));
    }

    @Test
    void removeEnd()
    {
        assertNull(StringTool.removeEnd(null, ".com"));
        assertEquals("", StringTool.removeEnd("", ".com"));
        assertEquals("www.domain.com", StringTool.removeEnd("www.domain.com", null));
        assertEquals("www.domain", StringTool.removeEnd("www.domain.com", ".com"));
        assertEquals("www.domain.com", StringTool.removeEnd("www.domain.com", "domain"));
        assertEquals("abc", StringTool.removeEnd("abc", ""));
    }

    @Test
    void removeEndIgnoreCase()
    {
        assertNull(StringTool.removeEndIgnoreCase(null, ".com"));
        assertEquals("", StringTool.removeEndIgnoreCase("", ".com"));
        assertEquals("www.domain.com", StringTool.removeEndIgnoreCase("www.domain.com", null));
        assertEquals("www.domain", StringTool.removeEndIgnoreCase("www.domain.com", ".COM"));
        assertEquals("www.domain", StringTool.removeEndIgnoreCase("www.domain.COM", ".com"));
        assertEquals("abc", StringTool.removeEndIgnoreCase("abc", ""));
    }

    @Test
    void removeIgnoreCase()
    {
        assertNull(StringTool.removeIgnoreCase(null, "ue"));
        assertEquals("", StringTool.removeIgnoreCase("", "ue"));
        assertEquals("queued", StringTool.removeIgnoreCase("queued", null));
        assertEquals("queued", StringTool.removeIgnoreCase("queued", ""));
        assertEquals("qd", StringTool.removeIgnoreCase("queued", "ue"));
        assertEquals("qd", StringTool.removeIgnoreCase("quEUed", "UE"));
        assertEquals("queued", StringTool.removeIgnoreCase("queued", "zz"));
    }

    @Test
    void removeStart()
    {
        assertNull(StringTool.removeStart(null, "www."));
        assertEquals("", StringTool.removeStart("", "www."));
        assertEquals("www.domain.com", StringTool.removeStart("www.domain.com", null));
        assertEquals("domain.com", StringTool.removeStart("www.domain.com", "www."));
        assertEquals("domain.com", StringTool.removeStart("domain.com", "www."));
        assertEquals("www.domain.com", StringTool.removeStart("www.domain.com", "domain"));
        assertEquals("abc", StringTool.removeStart("abc", ""));
    }

    @Test
    void removeStartIgnoreCase()
    {
        assertNull(StringTool.removeStartIgnoreCase(null, "www."));
        assertEquals("", StringTool.removeStartIgnoreCase("", "www."));
        assertEquals("www.domain.com", StringTool.removeStartIgnoreCase("www.domain.com", null));
        assertEquals("domain.com", StringTool.removeStartIgnoreCase("www.domain.com", "www."));
        assertEquals("domain.com", StringTool.removeStartIgnoreCase("www.domain.com", "WWW."));
        assertEquals("domain.com", StringTool.removeStartIgnoreCase("domain.com", "www."));
        assertEquals("abc", StringTool.removeStartIgnoreCase("abc", ""));
    }

    @Test
    void replace()
    {
        assertNull(StringTool.replace(null, "a", "z"));
        assertEquals("", StringTool.replace("", "a", "z"));
        assertEquals("any", StringTool.replace("any", null, "z"));
        assertEquals("any", StringTool.replace("any", "a", null));
        assertEquals("any", StringTool.replace("any", "", "z"));
        assertEquals("b", StringTool.replace("aba", "a", ""));
        assertEquals("zbz", StringTool.replace("aba", "a", "z"));
    }

    @Test
    void replaceWithMax()
    {
        assertNull(StringTool.replace(null, "a", "z", 1));
        assertEquals("", StringTool.replace("", "a", "z", 1));
        assertEquals("any", StringTool.replace("any", null, "z", 1));
        assertEquals("any", StringTool.replace("any", "a", null, 1));
        assertEquals("any", StringTool.replace("any", "", "z", 1));
        assertEquals("abaa", StringTool.replace("abaa", "a", "z", 0));
        assertEquals("zbaa", StringTool.replace("abaa", "a", "z", 1));
        assertEquals("zbza", StringTool.replace("abaa", "a", "z", 2));
    }

    @Test
    void replaceIgnoreCase()
    {
        assertNull(StringTool.replaceIgnoreCase(null, "a", "z"));
        assertEquals("", StringTool.replaceIgnoreCase("", "a", "z"));
        assertEquals("any", StringTool.replaceIgnoreCase("any", null, "z"));
        assertEquals("any", StringTool.replaceIgnoreCase("any", "a", null));
        assertEquals("any", StringTool.replaceIgnoreCase("any", "", "z"));
        assertEquals("b", StringTool.replaceIgnoreCase("abA", "A", ""));
        assertEquals("zbz", StringTool.replaceIgnoreCase("aba", "A", "z"));
    }

    @Test
    void replaceIgnoreCaseWithMax()
    {
        assertNull(StringTool.replaceIgnoreCase(null, "a", "z", 1));
        assertEquals("", StringTool.replaceIgnoreCase("", "a", "z", 1));
        assertEquals("any", StringTool.replaceIgnoreCase("any", null, "z", 1));
        assertEquals("any", StringTool.replaceIgnoreCase("any", "a", null, 1));
        assertEquals("any", StringTool.replaceIgnoreCase("any", "", "z", 1));
        assertEquals("abaa", StringTool.replaceIgnoreCase("abaa", "a", "z", 0));
        assertEquals("zbaa", StringTool.replaceIgnoreCase("abaa", "A", "z", 1));
        assertEquals("zbza", StringTool.replaceIgnoreCase("abAa", "a", "z", 2));
    }

    @Test
    void replaceOnce()
    {
        assertNull(StringTool.replaceOnce(null, "a", "z"));
        assertEquals("", StringTool.replaceOnce("", "a", "z"));
        assertEquals("any", StringTool.replaceOnce("any", null, "z"));
        assertEquals("any", StringTool.replaceOnce("any", "a", null));
        assertEquals("any", StringTool.replaceOnce("any", "", "z"));
        assertEquals("ba", StringTool.replaceOnce("aba", "a", ""));
        assertEquals("zba", StringTool.replaceOnce("aba", "a", "z"));
    }

    @Test
    void replaceOnceIgnoreCase()
    {
        assertNull(StringTool.replaceOnceIgnoreCase(null, "a", "z"));
        assertEquals("", StringTool.replaceOnceIgnoreCase("", "a", "z"));
        assertEquals("any", StringTool.replaceOnceIgnoreCase("any", null, "z"));
        assertEquals("any", StringTool.replaceOnceIgnoreCase("any", "a", null));
        assertEquals("any", StringTool.replaceOnceIgnoreCase("any", "", "z"));
        assertEquals("ba", StringTool.replaceOnceIgnoreCase("aba", "a", ""));
        assertEquals("zba", StringTool.replaceOnceIgnoreCase("aba", "a", "z"));
        assertEquals("Foofoo", StringTool.replaceOnceIgnoreCase("FoOFoofoo", "foo", ""));
    }

    @Test
    void startsWith()
    {
        assertTrue(StringTool.startsWith(null, null));
        assertFalse(StringTool.startsWith(null, "abc"));
        assertFalse(StringTool.startsWith("abcdef", null));
        assertTrue(StringTool.startsWith("abcdef", "abc"));
        assertFalse(StringTool.startsWith("ABCDEF", "abc"));
    }

    @Test
    void startsWithAny()
    {
        assertFalse(StringTool.startsWithAny(null, null));
        assertFalse(StringTool.startsWithAny(null, new String[] {"abc"}));
        assertFalse(StringTool.startsWithAny("abcxyz", null));
        assertTrue(StringTool.startsWithAny("abcxyz", new String[] {""}));
        assertTrue(StringTool.startsWithAny("abcxyz", new String[] {"abc"}));
        assertTrue(StringTool.startsWithAny("abcxyz", new String[] {null, "xyz", "abc"}));
        assertFalse(StringTool.startsWithAny("abcxyz", null, "xyz", "ABCX"));
    }

    @Test
    void startsWithIgnoreCase()
    {
        assertTrue(StringTool.startsWithIgnoreCase(null, null));
        assertFalse(StringTool.startsWithIgnoreCase(null, "abc"));
        assertFalse(StringTool.startsWithIgnoreCase("abcdef", null));
        assertTrue(StringTool.startsWithIgnoreCase("abcdef", "abc"));
        assertTrue(StringTool.startsWithIgnoreCase("ABCDEF", "abc"));
    }
}

