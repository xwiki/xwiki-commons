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

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.velocity.tools.RegexTool.RegexResult;

/**
 * Unit tests for {@link RegexTool}.
 *
 * @version $Id$
 * @since 2.0RC1
 */
public class RegexToolTest
{
    @Test
    public void testFind()
    {
        RegexTool tool = new RegexTool();
        List<RegexResult> result =
            tool.find("<h1><span>header</span></h1> whatever", "<[hH][12].*?><span>(.*?)</span></[hH][12]>");

        Assert.assertEquals(2, result.size());
        Assert.assertEquals("<h1><span>header</span></h1>", result.get(0).getGroup());
        Assert.assertEquals(0, result.get(0).getStart());
        Assert.assertEquals(28, result.get(0).getEnd());
        Assert.assertEquals("header", result.get(1).getGroup());
        Assert.assertEquals(10, result.get(1).getStart());
        Assert.assertEquals(16, result.get(1).getEnd());
    }

    @Test
    public void testFindWithoutMatches()
    {
        RegexTool tool = new RegexTool();
        List<RegexResult> result = tool.find("nothing here", "something");

        Assert.assertEquals(0, result.size());
    }

    @Test
    public void findAll()
    {
        RegexTool tool = new RegexTool();
        List<List<RegexResult>> result =
            tool.findAll("one :two three (:four) five :six seven=:eight", ":(\\w+) (\\w+)");

        Assert.assertEquals(2, result.size());
        Assert.assertEquals(":two three", result.get(0).get(0).getGroup());
        Assert.assertEquals(":six seven", result.get(1).get(0).getGroup());

        Assert.assertEquals(3, result.get(0).size());
        Assert.assertEquals("two", result.get(0).get(1).getGroup());
        Assert.assertEquals("three", result.get(0).get(2).getGroup());

        Assert.assertEquals(3, result.get(1).size());
        Assert.assertEquals("six", result.get(1).get(1).getGroup());
        Assert.assertEquals("seven", result.get(1).get(2).getGroup());
    }

    @Test
    public void findAllNoMatch()
    {
        RegexTool tool = new RegexTool();
        List<List<RegexResult>> result = tool.findAll("nothing here", "something");

        Assert.assertEquals(0, result.size());
    }

    /**
     * Compiling a valid regular expression should work.
     */
    @Test
    public void testCompileValidRegex()
    {
        RegexTool tool = new RegexTool();
        Pattern p = tool.compile("ab?");
        Assert.assertNotNull(p);
        // Try to check that the right regular expression was parsed.
        Matcher m = p.matcher("xyz");
        Assert.assertFalse(m.matches());
        m = p.matcher("a");
        Assert.assertTrue(m.matches());
        m = p.matcher("aba");
        Assert.assertFalse(m.matches());
        m.reset();
        Assert.assertTrue(m.find() && m.find() && m.hitEnd());
    }

    /**
     * Compiling a valid regular expression with internal flags should work.
     */
    @Test
    public void testCompileRegexWithFlags()
    {
        RegexTool tool = new RegexTool();
        Pattern p = tool.compile("(?im)^ab?$");
        Assert.assertNotNull(p);
        // Try to check that the right regular expression was parsed.
        Matcher m = p.matcher("xyz");
        Assert.assertFalse(m.matches());
        m = p.matcher("A");
        Assert.assertTrue(m.matches());
        m = p.matcher("ab\na");
        Assert.assertFalse(m.matches());
        m.reset();
        Assert.assertTrue(m.find() && m.find() && m.hitEnd());
    }

    /**
     * Compiling an invalid regular expression should return null, and not throw an exception.
     */
    @Test
    public void testCompileInvalidRegex()
    {
        RegexTool tool = new RegexTool();
        Pattern p = tool.compile("*");
        Assert.assertNull(p);
    }

    /**
     * Escaping a string containing regex syntax characters.
     */
    @Test
    public void testQuote()
    {
        RegexTool tool = new RegexTool();
        Assert.assertEquals(Pattern.quote("^(\\)[]"), tool.quote("^(\\)[]"));
    }

    @Test
    public void testQuoteReplacement()
    {
        RegexTool tool = new RegexTool();
        Assert.assertEquals(Matcher.quoteReplacement("$1 \\$2"), tool.quoteReplacement("$1 \\$2"));
    }
}
