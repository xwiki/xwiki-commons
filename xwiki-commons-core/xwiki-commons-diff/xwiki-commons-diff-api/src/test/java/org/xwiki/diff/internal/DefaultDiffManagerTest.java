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
package org.xwiki.diff.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.diff.Delta.Type;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.MergeResult;
import org.xwiki.logging.LogLevel;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

public class DefaultDiffManagerTest
{
    @Rule
    public final MockitoComponentMockingRule<DiffManager> mocker = new MockitoComponentMockingRule<DiffManager>(
        DefaultDiffManager.class);

    /**
     * @param str the single line text
     * @return the lines
     */
    private static List<Character> toCharacters(String str)
    {
        List<Character> characters;

        if (str != null) {
            characters = new ArrayList<Character>(str.length());

            for (char c : str.toCharArray()) {
                characters.add(c);
            }
        } else {
            characters = Collections.emptyList();
        }

        return characters;
    }

    private static String toString(List<Character> characters)
    {
        return StringUtils.join(characters, null);
    }

    // Tests

    @Test
    public void testDiffStringList() throws Exception
    {
        // Null

        DiffResult<String> result = this.mocker.getComponentUnderTest().diff(null, null, null);

        Assert.assertTrue(result.getPatch().isEmpty());

        // Empty

        result = this.mocker.getComponentUnderTest().diff(Collections.<String>emptyList(),
            Collections.<String>emptyList(), null);

        Assert.assertTrue(result.getPatch().isEmpty());

        // Equals

        result = this.mocker.getComponentUnderTest().diff(Arrays.asList("equals"), Arrays.asList("equals"), null);

        Assert.assertTrue(result.getPatch().isEmpty());

        // Previous empty

        result = this.mocker.getComponentUnderTest().diff(Collections.<String>emptyList(), Arrays.asList("next"), null);

        Assert.assertEquals(1, result.getPatch().size());
        Assert.assertEquals(Type.INSERT, result.getPatch().get(0).getType());
        Assert.assertEquals(Arrays.asList("next"), result.getPatch().get(0).getNext().getElements());
        Assert.assertEquals(0, result.getPatch().get(0).getNext().getIndex());

        // Next empty

        result =
            this.mocker.getComponentUnderTest().diff(Arrays.asList("previous"), Collections.<String>emptyList(), null);

        Assert.assertEquals(1, result.getPatch().size());
        Assert.assertEquals(Type.DELETE, result.getPatch().get(0).getType());
        Assert.assertEquals(Arrays.asList("previous"), result.getPatch().get(0).getPrevious().getElements());
        Assert.assertEquals(0, result.getPatch().get(0).getPrevious().getIndex());
    }

    @Test
    public void testDiffCharList() throws Exception
    {
        // Equals

        DiffResult<Character> result =
            this.mocker.getComponentUnderTest().diff(Arrays.asList('a'), Arrays.asList('a'), null);

        Assert.assertTrue(result.getPatch().isEmpty());

        // Changed

        result = this.mocker.getComponentUnderTest().diff(Arrays.asList('a'), Arrays.asList('b'), null);

        Assert.assertEquals(1, result.getPatch().size());
        Assert.assertEquals(Type.CHANGE, result.getPatch().get(0).getType());
    }

    @Test
    public void testMergeStringList() throws Exception
    {
        MergeResult<String> result;

        // Only new

        result = this.mocker.getComponentUnderTest().merge(
            Arrays.asList("some content"), Arrays.asList("some new content"), Arrays.asList("some content"), null);

        Assert.assertEquals(Arrays.asList("some new content"), result.getMerged());

        // Only current

        result = this.mocker.getComponentUnderTest().merge(
            Arrays.asList("some content"), Arrays.asList("some content"), Arrays.asList("some current content"), null);

        Assert.assertEquals(Arrays.asList("some current content"), result.getMerged());

        // New after

        result = this.mocker.getComponentUnderTest().merge(
            Arrays.asList("some content"), Arrays.asList("some content", "after"), Arrays.asList("some content"), null);

        Assert.assertEquals(Arrays.asList("some content", "after"), result.getMerged());

        // Before and after

        result = this.mocker.getComponentUnderTest().merge(Arrays.asList("some content"),
            Arrays.asList("before", "some content"), Arrays.asList("some content", "after"), null);

        Assert.assertEquals(Arrays.asList("before", "some content", "after"), result.getMerged());

        // After and before

        result = this.mocker.getComponentUnderTest().merge(Arrays.asList("some content"),
            Arrays.asList("some content", "after"), Arrays.asList("before", "some content"), null);

        Assert.assertEquals(Arrays.asList("before", "some content", "after"), result.getMerged());

        // Same current and next

        result = this.mocker.getComponentUnderTest().merge(Arrays.asList("some content"),
            Arrays.asList("some new content"), Arrays.asList("some new content"), null);

        Assert.assertEquals(Arrays.asList("some new content"), result.getMerged());

    }

    @Test
    public void testMergeCharList() throws Exception
    {
        MergeResult<Character> result;

        // New empty

        result = this.mocker.getComponentUnderTest()
            .merge(toCharacters("a"), toCharacters(""), toCharacters("b"), null);

        Assert.assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("b"), result.getMerged());

        // New before

        result = this.mocker.getComponentUnderTest()
            .merge(toCharacters("bc"), toCharacters("abc"), toCharacters("bc"), null);

        Assert.assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("abc"), result.getMerged());

        // New after

        result = this.mocker.getComponentUnderTest()
            .merge(toCharacters("ab"), toCharacters("abc"), toCharacters("ab"), null);

        Assert.assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("abc"), result.getMerged());

        // New middle

        result = this.mocker.getComponentUnderTest()
            .merge(toCharacters("ac"), toCharacters("abc"), toCharacters("ac"), null);

        Assert.assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("abc"), result.getMerged());

        // Before and after

        result =
            this.mocker.getComponentUnderTest().merge(toCharacters("b"), toCharacters("ab"), toCharacters("bc"), null);

        Assert.assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("abc"), result.getMerged());

        // After and before

        result =
            this.mocker.getComponentUnderTest().merge(toCharacters("b"), toCharacters("bc"), toCharacters("ab"), null);

        Assert.assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("abc"), result.getMerged());

        // Insert current and next

        result = this.mocker.getComponentUnderTest()
            .merge(toCharacters("abc"), toCharacters("aibc"), toCharacters("abcj"), null);

        Assert.assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("aibcj"), result.getMerged());

        result = this.mocker.getComponentUnderTest()
            .merge(toCharacters("abc"), toCharacters("abcj"), toCharacters("aibc"), null);

        Assert.assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("aibcj"), result.getMerged());

        result = this.mocker.getComponentUnderTest()
            .merge(toCharacters("abc"), toCharacters("ajbc"), toCharacters("aibc"), null);

        Assert.assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("ajibc"), result.getMerged());

        result = this.mocker.getComponentUnderTest()
            .merge(toCharacters("ab"), toCharacters("aijb"), toCharacters("aib"), null);

        Assert.assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("aijb"), result.getMerged());

        result = this.mocker.getComponentUnderTest()
            .merge(toCharacters("ab"), toCharacters("ajb"), toCharacters("aijb"), null);

        Assert.assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("aijb"), result.getMerged());

        result =
            this.mocker.getComponentUnderTest().merge(toCharacters("d"), toCharacters("ab d"), toCharacters("abc d"), null);

        Assert.assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("abc d"), result.getMerged());

        result =
            this.mocker.getComponentUnderTest().merge(toCharacters("d"), toCharacters("abc d"), toCharacters("ab d"), null);

        Assert.assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("abc d"), result.getMerged());

        result =
            this.mocker.getComponentUnderTest().merge(toCharacters("d"), toCharacters("bcd"), toCharacters("abcd"), null);

        Assert.assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("abcd"), result.getMerged());

        result =
            this.mocker.getComponentUnderTest().merge(toCharacters("d"), toCharacters("abcd"), toCharacters("bcd"), null);

        Assert.assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("abcd"), result.getMerged());

        // Misc

        result = this.mocker.getComponentUnderTest().merge(toCharacters("Alice Macro"),
            toCharacters("Alice Wiki Macro (upgraded)"), toCharacters("Alice Extension"), null);

        Assert.assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("Alice Wiki Extension (upgraded)"), result.getMerged());

        result = this.mocker.getComponentUnderTest()
            .merge(toCharacters("$a(b)"), toCharacters("$c(d)e"), toCharacters("$c(d)e"), null);

        Assert.assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals("$c(d)e", toString(result.getMerged()));
    }

    @Test
    public void testMergeCharOnConflicts() throws Exception
    {
        MergeResult<Character> result;

        // Current and new at the same place
        result =
            this.mocker.getComponentUnderTest().merge(toCharacters("a"), toCharacters("b"), toCharacters("c"), null);

        Assert.assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("c"), result.getMerged());

        // Current and new in conflict at different indices
        result = this.mocker.getComponentUnderTest()
            .merge(toCharacters("abcd"), toCharacters("yycd"), toCharacters("azzd"), null);

        Assert.assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals("yycd", toString(result.getMerged()));

        // Current and new in conflict at different indices
        result = this.mocker.getComponentUnderTest()
            .merge(toCharacters("abcd"), toCharacters("azzd"), toCharacters("yycd"), null);

        Assert.assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals("yycd", toString(result.getMerged()));

        // New overlapping current
        result = this.mocker.getComponentUnderTest()
            .merge(toCharacters("aabbcc"), toCharacters("arrbcc"), toCharacters("ddddcc"), null);

        Assert.assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals("ddddcc", toString(result.getMerged()));

        // current overlapping new
        result = this.mocker.getComponentUnderTest()
            .merge(toCharacters("aabbcc"), toCharacters("ddddcc"), toCharacters("arrbcc"), null);

        Assert.assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals("ddddcc", toString(result.getMerged()));

        result = this.mocker.getComponentUnderTest()
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"), null);

        Assert.assertEquals(2, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals("ddddcc azzd", toString(result.getMerged()));
    }

    @Test
    public void testMergeWithEmptyNew() throws Exception
    {
        MergeResult<Character> result;

        result =
            this.mocker.getComponentUnderTest().merge(toCharacters("a"), toCharacters(""), toCharacters("b"), null);

        Assert.assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        Assert.assertEquals(toCharacters("b"), result.getMerged());
    }

    @Test
    public void testMergeWhenUserHasChangedAllContent() throws Exception
    {
        MergeResult<String> result;

        // Test 1: All content has changed between previous and current
        result = mocker.getComponentUnderTest().merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
                Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"),
                Arrays.asList("New content", "That is completely different"), null);

        Assert.assertEquals(Arrays.asList("New content", "That is completely different"), result.getMerged());

        // Test 2: All content has been deleted between previous and current
        result = mocker.getComponentUnderTest().merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
                Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"),
                Collections.emptyList(), null);

        Assert.assertEquals(Collections.emptyList(), result.getMerged());
    }
}
