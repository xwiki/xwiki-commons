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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.xwiki.velocity.tools.RegexTool.RegexResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RegexTool}.
 *
 * @version $Id$
 * @since 2.0RC1
 */
class RegexToolTest
{
    @Test
    void find()
    {
        RegexTool tool = new RegexTool();
        List<RegexResult> result =
            tool.find("<h1><span>header</span></h1> whatever", "<[hH][12].*?><span>(.*?)</span></[hH][12]>");

        assertEquals(2, result.size());
        assertEquals("<h1><span>header</span></h1>", result.get(0).getGroup());
        assertEquals(0, result.get(0).getStart());
        assertEquals(28, result.get(0).getEnd());
        assertEquals("header", result.get(1).getGroup());
        assertEquals(10, result.get(1).getStart());
        assertEquals(16, result.get(1).getEnd());
    }

    @Test
    void findWithoutMatches()
    {
        RegexTool tool = new RegexTool();
        List<RegexResult> result = tool.find("nothing here", "something");

        assertEquals(0, result.size());
    }

    @Test
    void findAll()
    {
        RegexTool tool = new RegexTool();
        List<List<RegexResult>> result =
            tool.findAll("one :two three (:four) five :six seven=:eight", ":(\\w+) (\\w+)");

        assertEquals(2, result.size());
        assertEquals(":two three", result.get(0).get(0).getGroup());
        assertEquals(":six seven", result.get(1).get(0).getGroup());

        assertEquals(3, result.get(0).size());
        assertEquals("two", result.get(0).get(1).getGroup());
        assertEquals("three", result.get(0).get(2).getGroup());

        assertEquals(3, result.get(1).size());
        assertEquals("six", result.get(1).get(1).getGroup());
        assertEquals("seven", result.get(1).get(2).getGroup());
    }

    @Test
    void findAllNoMatch()
    {
        RegexTool tool = new RegexTool();
        List<List<RegexResult>> result = tool.findAll("nothing here", "something");

        assertEquals(0, result.size());
    }

    /**
     * Compiling a valid regular expression should work.
     */
    @Test
    void testCompileValidRegex()
    {
        RegexTool tool = new RegexTool();
        Pattern p = tool.compile("ab?");
        assertNotNull(p);
        // Try to check that the right regular expression was parsed.
        Matcher m = p.matcher("xyz");
        assertFalse(m.matches());
        m = p.matcher("a");
        assertTrue(m.matches());
        m = p.matcher("aba");
        assertFalse(m.matches());
        m.reset();
        assertTrue(m.find() && m.find() && m.hitEnd());
    }

    /**
     * Compiling a valid regular expression with internal flags should work.
     */
    @Test
    void compileRegexWithFlags()
    {
        RegexTool tool = new RegexTool();
        Pattern p = tool.compile("(?im)^ab?$");
        assertNotNull(p);
        // Try to check that the right regular expression was parsed.
        Matcher m = p.matcher("xyz");
        assertFalse(m.matches());
        m = p.matcher("A");
        assertTrue(m.matches());
        m = p.matcher("ab\na");
        assertFalse(m.matches());
        m.reset();
        assertTrue(m.find() && m.find() && m.hitEnd());
    }

    /**
     * Compiling an invalid regular expression should return null, and not throw an exception.
     */
    @Test
    void compileInvalidRegex()
    {
        RegexTool tool = new RegexTool();
        Pattern p = tool.compile("*");
        assertNull(p);
    }

    /**
     * Escaping a string containing regex syntax characters.
     */
    @Test
    void quote()
    {
        RegexTool tool = new RegexTool();
        assertEquals(Pattern.quote("^(\\)[]"), tool.quote("^(\\)[]"));
    }

    @Test
    void quoteReplacement()
    {
        RegexTool tool = new RegexTool();
        assertEquals(Matcher.quoteReplacement("$1 \\$2"), tool.quoteReplacement("$1 \\$2"));
    }
}
