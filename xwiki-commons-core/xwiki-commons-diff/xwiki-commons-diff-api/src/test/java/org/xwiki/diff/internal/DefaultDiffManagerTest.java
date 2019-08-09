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
import org.junit.jupiter.api.Test;
import org.xwiki.diff.Delta.Type;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.MergeConfiguration;
import org.xwiki.diff.MergeResult;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ComponentTest
public class DefaultDiffManagerTest
{
    @InjectMockComponents
    private DefaultDiffManager diffManager;

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
    public void diffStringList() throws Exception
    {
        // Null

        DiffResult<String> result = this.diffManager.diff(null, null, null);

        assertTrue(result.getPatch().isEmpty());

        // Empty

        result = this.diffManager.diff(Collections.<String>emptyList(),
            Collections.<String>emptyList(), null);

        assertTrue(result.getPatch().isEmpty());

        // Equals

        result = this.diffManager.diff(Arrays.asList("equals"), Arrays.asList("equals"), null);

        assertTrue(result.getPatch().isEmpty());

        // Previous empty

        result = this.diffManager.diff(Collections.<String>emptyList(), Arrays.asList("next"), null);

        assertEquals(1, result.getPatch().size());
        assertEquals(Type.INSERT, result.getPatch().get(0).getType());
        assertEquals(Arrays.asList("next"), result.getPatch().get(0).getNext().getElements());
        assertEquals(0, result.getPatch().get(0).getNext().getIndex());

        // Next empty

        result =
            this.diffManager.diff(Arrays.asList("previous"), Collections.<String>emptyList(), null);

        assertEquals(1, result.getPatch().size());
        assertEquals(Type.DELETE, result.getPatch().get(0).getType());
        assertEquals(Arrays.asList("previous"), result.getPatch().get(0).getPrevious().getElements());
        assertEquals(0, result.getPatch().get(0).getPrevious().getIndex());
    }

    @Test
    public void diffCharList() throws Exception
    {
        // Equals

        DiffResult<Character> result =
            this.diffManager.diff(Arrays.asList('a'), Arrays.asList('a'), null);

        assertTrue(result.getPatch().isEmpty());

        // Changed

        result = this.diffManager.diff(Arrays.asList('a'), Arrays.asList('b'), null);

        assertEquals(1, result.getPatch().size());
        assertEquals(Type.CHANGE, result.getPatch().get(0).getType());
    }

    @Test
    public void mergeStringList() throws Exception
    {
        MergeResult<String> result;

        // Only new

        result = this.diffManager.merge(
            Arrays.asList("some content"), Arrays.asList("some new content"), Arrays.asList("some content"), null);

        assertEquals(Arrays.asList("some new content"), result.getMerged());

        // Only current

        result = this.diffManager.merge(
            Arrays.asList("some content"), Arrays.asList("some content"), Arrays.asList("some current content"), null);

        assertEquals(Arrays.asList("some current content"), result.getMerged());

        // New after

        result = this.diffManager.merge(
            Arrays.asList("some content"), Arrays.asList("some content", "after"), Arrays.asList("some content"), null);

        assertEquals(Arrays.asList("some content", "after"), result.getMerged());

        // Before and after

        result = this.diffManager.merge(Arrays.asList("some content"),
            Arrays.asList("before", "some content"), Arrays.asList("some content", "after"), null);

        assertEquals(Arrays.asList("before", "some content", "after"), result.getMerged());

        // After and before

        result = this.diffManager.merge(Arrays.asList("some content"),
            Arrays.asList("some content", "after"), Arrays.asList("before", "some content"), null);

        assertEquals(Arrays.asList("before", "some content", "after"), result.getMerged());

        // Same current and next

        result = this.diffManager.merge(Arrays.asList("some content"),
            Arrays.asList("some new content"), Arrays.asList("some new content"), null);

        assertEquals(Arrays.asList("some new content"), result.getMerged());

    }

    @Test
    public void mergeCharList() throws Exception
    {
        MergeResult<Character> result;

        // New empty

        result = this.diffManager
            .merge(toCharacters("a"), toCharacters(""), toCharacters("b"), null);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("b"), result.getMerged());

        // New before

        result = this.diffManager
            .merge(toCharacters("bc"), toCharacters("abc"), toCharacters("bc"), null);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abc"), result.getMerged());

        // New after

        result = this.diffManager
            .merge(toCharacters("ab"), toCharacters("abc"), toCharacters("ab"), null);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abc"), result.getMerged());

        // New middle

        result = this.diffManager
            .merge(toCharacters("ac"), toCharacters("abc"), toCharacters("ac"), null);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abc"), result.getMerged());

        // Before and after

        result =
            this.diffManager.merge(toCharacters("b"), toCharacters("ab"), toCharacters("bc"), null);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abc"), result.getMerged());

        // After and before

        result =
            this.diffManager.merge(toCharacters("b"), toCharacters("bc"), toCharacters("ab"), null);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abc"), result.getMerged());

        // Insert current and next

        result = this.diffManager
            .merge(toCharacters("abc"), toCharacters("aibc"), toCharacters("abcj"), null);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("aibcj"), result.getMerged());

        result = this.diffManager
            .merge(toCharacters("abc"), toCharacters("abcj"), toCharacters("aibc"), null);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("aibcj"), result.getMerged());

        result = this.diffManager
            .merge(toCharacters("abc"), toCharacters("ajbc"), toCharacters("aibc"), null);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("ajibc"), result.getMerged());

        result = this.diffManager
            .merge(toCharacters("ab"), toCharacters("aijb"), toCharacters("aib"), null);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("aijb"), result.getMerged());

        result = this.diffManager
            .merge(toCharacters("ab"), toCharacters("ajb"), toCharacters("aijb"), null);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("aijb"), result.getMerged());

        result =
            this.diffManager.merge(toCharacters("d"), toCharacters("ab d"), toCharacters("abc d"), null);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abc d"), result.getMerged());

        result =
            this.diffManager.merge(toCharacters("d"), toCharacters("abc d"), toCharacters("ab d"), null);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abc d"), result.getMerged());

        result =
            this.diffManager.merge(toCharacters("d"), toCharacters("bcd"), toCharacters("abcd"), null);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abcd"), result.getMerged());

        result =
            this.diffManager.merge(toCharacters("d"), toCharacters("abcd"), toCharacters("bcd"), null);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abcd"), result.getMerged());

        // Misc

        result = this.diffManager.merge(toCharacters("Alice Macro"),
            toCharacters("Alice Wiki Macro (upgraded)"), toCharacters("Alice Extension"), null);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("Alice Wiki Extension (upgraded)"), result.getMerged());

        result = this.diffManager
            .merge(toCharacters("$a(b)"), toCharacters("$c(d)e"), toCharacters("$c(d)e"), null);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals("$c(d)e", toString(result.getMerged()));
    }

    @Test
    public void mergeCharOnConflicts() throws Exception
    {
        MergeResult<Character> result;
        MergeConfiguration<Character> mergeConfiguration;

        // Current and new at the same place
        result =
            this.diffManager.merge(toCharacters("a"), toCharacters("b"), toCharacters("c"), null);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("c"), result.getMerged());

        // Current and new in conflict at different indices
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("yycd"), toCharacters("azzd"), null);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals("azzd", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("yycd"), toCharacters("azzd"), mergeConfiguration);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals("abcd", toString(result.getMerged()));

        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("yycd"), toCharacters("azzd"), mergeConfiguration);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals("yycd", toString(result.getMerged()));

        // Current and new in conflict at different indices
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("azzd"), toCharacters("yycd"), null);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals("yycd", toString(result.getMerged()));

        // New overlapping current
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("arrbcc"), toCharacters("ddddcc"), null);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals("ddddcc", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("arrbcc"), toCharacters("ddddcc"), mergeConfiguration);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals("aabbcc", toString(result.getMerged()));

        // current overlapping new
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("ddddcc"), toCharacters("arrbcc"), null);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals("arrbcc", toString(result.getMerged()));

        mergeConfiguration = null;
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(2, result.getLog().getLogs(LogLevel.ERROR).size());
        List<LogEvent> logConflicts = new ArrayList<>(result.getLog().getLogs(LogLevel.ERROR));
        assertEquals("arrbcc yycd", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(2, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(logConflicts, result.getLog().getLogs(LogLevel.ERROR));
        assertEquals("aabbcc abcd", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(2, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(logConflicts, result.getLog().getLogs(LogLevel.ERROR));
        assertEquals("ddddcc azzd", toString(result.getMerged()));
    }

    @Test
    public void mergeWithEmptyNew() throws Exception
    {
        MergeResult<Character> result;

        result =
            this.diffManager.merge(toCharacters("a"), toCharacters(""), toCharacters("b"), null);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("b"), result.getMerged());
    }

    @Test
    public void mergeWhenUserHasChangedAllContent() throws Exception
    {
        MergeResult<String> result;

        // Test 1: All content has changed between previous and current
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
                Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"),
                Arrays.asList("New content", "That is completely different"), null);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(Arrays.asList("New content", "That is completely different"), result.getMerged());

        MergeConfiguration<String> mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"),
            Arrays.asList("New content", "That is completely different"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(Arrays.asList("Line 1", "Line 2", "Line 3"), result.getMerged());

        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"),
            Arrays.asList("New content", "That is completely different"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"), result.getMerged());

        // Test 2: All content has been deleted between previous and current
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
                Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"),
                Collections.emptyList(), null);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(Collections.emptyList(), result.getMerged());

        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"),
            Collections.emptyList(), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(Arrays.asList("Line 1", "Line 2", "Line 3"), result.getMerged());

        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"),
            Collections.emptyList(), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"), result.getMerged());

        // Test 3: All content has been deleted between previous and next
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Collections.emptyList(),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"), null);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"), result.getMerged());

        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Collections.emptyList(),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(Arrays.asList("Line 1", "Line 2", "Line 3"), result.getMerged());

        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Collections.emptyList(),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(Collections.emptyList(), result.getMerged());

        // Test 4: All content has changed between previous and current, and current and next are identical
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("New content", "That is completely different"),
            Arrays.asList("New content", "That is completely different"), null);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(Arrays.asList("New content", "That is completely different"), result.getMerged());

        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("New content", "That is completely different"),
            Arrays.asList("New content", "That is completely different"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(Arrays.asList("New content", "That is completely different"), result.getMerged());
    }
}
