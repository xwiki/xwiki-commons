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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xwiki.diff.Chunk;
import org.xwiki.diff.Conflict;
import org.xwiki.diff.ConflictDecision;
import org.xwiki.diff.Delta;
import org.xwiki.diff.Delta.Type;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.MergeConfiguration;
import org.xwiki.diff.MergeResult;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ComponentTest
public class DefaultDiffManagerTest
{
    @InjectMockComponents
    private DefaultDiffManager diffManager;

    // We want to execute some tests with all the possible fallback configurations.
    public static MergeConfiguration[] createConfigurations() {
        MergeConfiguration defaultConfig = new MergeConfiguration();
        MergeConfiguration previousConfig = new MergeConfiguration();
        previousConfig.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        MergeConfiguration nextConfig = new MergeConfiguration();
        nextConfig.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        MergeConfiguration currentConfig = new MergeConfiguration();
        currentConfig.setFallbackOnConflict(MergeConfiguration.Version.CURRENT);

        // Default is currently current, but I find it safer to keep both the default and the explicit current config
        // in case we change that in the future. Null value should be also fallback to the default config.
        return new MergeConfiguration[] { null, defaultConfig, previousConfig, nextConfig, currentConfig };
    }

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

    @ParameterizedTest
    @MethodSource("createConfigurations")
    public void mergeStringList(MergeConfiguration<String> mergeConfiguration) throws Exception
    {
        MergeResult<String> result;

        // Only new

        result = this.diffManager.merge(
            Arrays.asList("some content"), Arrays.asList("some new content"), Arrays.asList("some content"),
            mergeConfiguration);
        assertTrue(result.getConflicts().isEmpty());
        assertEquals(Arrays.asList("some new content"), result.getMerged());

        // Only current

        result = this.diffManager.merge(
            Arrays.asList("some content"), Arrays.asList("some content"), Arrays.asList("some current content"),
            mergeConfiguration);
        assertTrue(result.getConflicts().isEmpty());
        assertEquals(Arrays.asList("some current content"), result.getMerged());

        // New after

        result = this.diffManager.merge(
            Arrays.asList("some content"), Arrays.asList("some content", "after"), Arrays.asList("some content"),
            mergeConfiguration);
        assertTrue(result.getConflicts().isEmpty());
        assertEquals(Arrays.asList("some content", "after"), result.getMerged());

        // Before and after

        result = this.diffManager.merge(Arrays.asList("some content"),
            Arrays.asList("before", "some content"), Arrays.asList("some content", "after"), mergeConfiguration);
        assertTrue(result.getConflicts().isEmpty());
        assertEquals(Arrays.asList("before", "some content", "after"), result.getMerged());

        // After and before

        result = this.diffManager.merge(Arrays.asList("some content"),
            Arrays.asList("some content", "after"), Arrays.asList("before", "some content"), mergeConfiguration);
        assertTrue(result.getConflicts().isEmpty());
        assertEquals(Arrays.asList("before", "some content", "after"), result.getMerged());

        // Same current and next

        result = this.diffManager.merge(Arrays.asList("some content"),
            Arrays.asList("some new content"), Arrays.asList("some new content"), mergeConfiguration);
        assertTrue(result.getConflicts().isEmpty());
        assertEquals(Arrays.asList("some new content"), result.getMerged());

    }

    @ParameterizedTest
    @MethodSource("createConfigurations")
    public void mergeCharListNoConflict(MergeConfiguration<Character> mergeConfiguration) throws Exception
    {
        MergeResult<Character> result;

        // New before

        result = this.diffManager
            .merge(toCharacters("bc"), toCharacters("abc"), toCharacters("bc"), mergeConfiguration);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abc"), result.getMerged());

        // New after

        result = this.diffManager
            .merge(toCharacters("ab"), toCharacters("abc"), toCharacters("ab"), mergeConfiguration);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abc"), result.getMerged());

        // New middle

        result = this.diffManager
            .merge(toCharacters("ac"), toCharacters("abc"), toCharacters("ac"), mergeConfiguration);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abc"), result.getMerged());

        // Before and after

        result =
            this.diffManager.merge(toCharacters("b"), toCharacters("ab"), toCharacters("bc"), mergeConfiguration);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abc"), result.getMerged());

        // After and before

        result =
            this.diffManager.merge(toCharacters("b"), toCharacters("bc"), toCharacters("ab"), mergeConfiguration);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abc"), result.getMerged());

        // Insert current and next

        result = this.diffManager
            .merge(toCharacters("abc"), toCharacters("aibc"), toCharacters("abcj"), mergeConfiguration);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("aibcj"), result.getMerged());

        result = this.diffManager
            .merge(toCharacters("abc"), toCharacters("abcj"), toCharacters("aibc"), mergeConfiguration);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("aibcj"), result.getMerged());

        result = this.diffManager
            .merge(toCharacters("abc"), toCharacters("ajbc"), toCharacters("aibc"), mergeConfiguration);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("ajibc"), result.getMerged());

        result = this.diffManager
            .merge(toCharacters("ab"), toCharacters("aijb"), toCharacters("aib"), mergeConfiguration);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("aijb"), result.getMerged());

        result = this.diffManager
            .merge(toCharacters("ab"), toCharacters("ajb"), toCharacters("aijb"), mergeConfiguration);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("aijb"), result.getMerged());

        result =
            this.diffManager.merge(toCharacters("d"), toCharacters("ab d"), toCharacters("abc d"), mergeConfiguration);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abc d"), result.getMerged());

        result =
            this.diffManager.merge(toCharacters("d"), toCharacters("abc d"), toCharacters("ab d"), mergeConfiguration);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abc d"), result.getMerged());

        result =
            this.diffManager.merge(toCharacters("d"), toCharacters("bcd"), toCharacters("abcd"), mergeConfiguration);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abcd"), result.getMerged());

        result =
            this.diffManager.merge(toCharacters("d"), toCharacters("abcd"), toCharacters("bcd"), mergeConfiguration);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("abcd"), result.getMerged());

        // Misc

        result = this.diffManager.merge(toCharacters("Alice Macro"),
            toCharacters("Alice Wiki Macro (upgraded)"), toCharacters("Alice Extension"), mergeConfiguration);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(toCharacters("Alice Wiki Extension (upgraded)"), result.getMerged());

        result = this.diffManager
            .merge(toCharacters("$a(b)"), toCharacters("$c(d)e"), toCharacters("$c(d)e"), mergeConfiguration);

        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals("$c(d)e", toString(result.getMerged()));
    }

    @Test
    public void mergeCharOnConflicts() throws Exception
    {
        MergeResult<Character> result;
        Conflict<Character> conflict;
        MergeConfiguration<Character> mergeConfiguration;

        List<Character> emptyList = Collections.emptyList();
        List<Character> onlyA = toCharacters("a");
        List<Character> onlyB = toCharacters("b");
        List<Character> onlyC = toCharacters("c");

        // New empty
        mergeConfiguration = null;
        result = this.diffManager.merge(onlyA, emptyList, onlyB, mergeConfiguration);
        conflict = createConflict(0,
            Type.CHANGE, 0, 0, onlyA, onlyB,
            Type.CHANGE, 0, 0, onlyA, emptyList);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(onlyB, result.getMerged());

        mergeConfiguration = new MergeConfiguration<>();
        result = this.diffManager.merge(onlyA, emptyList, onlyB, mergeConfiguration);
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(onlyB, result.getMerged());

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager.merge(onlyA, emptyList, onlyB, mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(onlyA, result.getMerged());

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        result = this.diffManager.merge(onlyA, emptyList, onlyB, mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(emptyList, result.getMerged());

        // Current and new at the same place
        mergeConfiguration = null;
        result = this.diffManager.merge(onlyA, onlyB, onlyC, mergeConfiguration);
        conflict = createConflict(0, 'a', 'c', 'b');
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(onlyC, result.getMerged());

        mergeConfiguration = new MergeConfiguration<>();
        result = this.diffManager.merge(onlyA, onlyB, onlyC, mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(onlyC, result.getMerged());

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager.merge(onlyA, onlyB, onlyC, mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(onlyA, result.getMerged());

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        result = this.diffManager.merge(onlyA, onlyB, onlyC, mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(onlyB, result.getMerged());

        // Current and new in conflict at different indices
        mergeConfiguration = null;
        conflict = createConflict(0,
            Type.CHANGE, 0, 0, Arrays.asList('a', 'b'), Arrays.asList('a', 'z'),
            Type.CHANGE, 0, 0, Arrays.asList('a', 'b'), Arrays.asList('y', 'y'));
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("yycd"), toCharacters("azzd"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("azzd", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("yycd"), toCharacters("azzd"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("azzd", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("yycd"), toCharacters("azzd"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("abcd", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("yycd"), toCharacters("azzd"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("yycd", toString(result.getMerged()));

        // Current and new in conflict at different indices
        conflict = createConflict(0,
            Type.CHANGE, 0, 0, Arrays.asList('a', 'b'), Arrays.asList('y', 'y'),
            Type.CHANGE, 0, 0, Arrays.asList('a', 'b'), Arrays.asList('a', 'z'));
        mergeConfiguration = null;
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("azzd"), toCharacters("yycd"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("yycd", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("azzd"), toCharacters("yycd"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("yycd", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("azzd"), toCharacters("yycd"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("abcd", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("azzd"), toCharacters("yycd"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("azzd", toString(result.getMerged()));

        // New overlapping current
        conflict = createConflict(0,
            Type.CHANGE, 0, 0, Arrays.asList('a', 'a', 'b', 'b'), Arrays.asList('d', 'd', 'd', 'd'),
            Type.CHANGE, 0, 0, Arrays.asList('a', 'a', 'b', 'b'), Arrays.asList('a', 'r', 'r', 'b'));
        mergeConfiguration = null;
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("arrbcc"), toCharacters("ddddcc"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("ddddcc", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("arrbcc"), toCharacters("ddddcc"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("ddddcc", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("arrbcc"), toCharacters("ddddcc"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("aabbcc", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("arrbcc"), toCharacters("ddddcc"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("arrbcc", toString(result.getMerged()));

        // current overlapping new
        conflict = createConflict(0,
            Type.CHANGE, 0, 0, Arrays.asList('a', 'a', 'b', 'b'), Arrays.asList('a', 'r', 'r', 'b'),
            Type.CHANGE, 0, 0, Arrays.asList('a', 'a', 'b', 'b'), Arrays.asList('d', 'd', 'd', 'd'));
        mergeConfiguration = null;
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("ddddcc"), toCharacters("arrbcc"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("arrbcc", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("ddddcc"), toCharacters("arrbcc"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("arrbcc", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("ddddcc"), toCharacters("arrbcc"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("aabbcc", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("ddddcc"), toCharacters("arrbcc"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("ddddcc", toString(result.getMerged()));

        // Multiple conflicts
        conflict = createConflict(0,
            Type.CHANGE, 0, 0, Arrays.asList('a', 'a', 'b', 'b'), Arrays.asList('a', 'r', 'r', 'b'),
            Type.CHANGE, 0, 0, Arrays.asList('a', 'a', 'b', 'b'), Arrays.asList('d', 'd', 'd', 'd'));
        Conflict<Character> conflict1 = createConflict(7,
            Type.CHANGE, 7, 7, Arrays.asList('a', 'b'), Arrays.asList('y','y'),
            Type.CHANGE, 7, 7, Arrays.asList('a', 'b'), Arrays.asList('a', 'z'));
        mergeConfiguration = null;
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(2, result.getLog().getLogs(LogLevel.ERROR).size());
        List<LogEvent> logConflicts = new ArrayList<>(result.getLog().getLogs(LogLevel.ERROR));
        assertEquals(2, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(conflict1, result.getConflicts().get(1));
        assertEquals("arrbcc yycd", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(2, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(logConflicts, result.getLog().getLogs(LogLevel.ERROR));
        assertEquals(2, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(conflict1, result.getConflicts().get(1));
        assertEquals("arrbcc yycd", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(2, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(logConflicts, result.getLog().getLogs(LogLevel.ERROR));
        assertEquals(2, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(conflict1, result.getConflicts().get(1));
        assertEquals("aabbcc abcd", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(2, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(logConflicts, result.getLog().getLogs(LogLevel.ERROR));
        assertEquals(2, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(conflict1, result.getConflicts().get(1));
        assertEquals("ddddcc azzd", toString(result.getMerged()));

        // Another multiple conflicts
        conflict = createConflict(2,
            Type.CHANGE, 2, 2, Arrays.asList('c'), Arrays.asList('c'),
            Type.CHANGE, 2, 2, Arrays.asList('c'), Arrays.asList('d'));
        conflict1 = createConflict(4,
            Type.CHANGE, 4, 4, Arrays.asList('f'), Collections.emptyList(),
            Type.CHANGE, 4, 4, Arrays.asList('f'), Arrays.asList('f'));
        mergeConfiguration = null;
        result = this.diffManager
            .merge(toCharacters("abcefhik"), toCharacters("abdefgijk"), toCharacters("abcdehijk"), mergeConfiguration);
        assertEquals(2, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(2, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(conflict1, result.getConflicts().get(1));
        assertEquals("abcdehijk", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        result = this.diffManager
            .merge(toCharacters("abcefhik"), toCharacters("abdefgijk"), toCharacters("abcdehijk"), mergeConfiguration);
        assertEquals(2, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(2, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(conflict1, result.getConflicts().get(1));
        assertEquals("abcdehijk", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("abcefhik"), toCharacters("abdefgijk"), toCharacters("abcdehijk"), mergeConfiguration);
        assertEquals(2, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(2, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(conflict1, result.getConflicts().get(1));
        assertEquals("abcefhijk", toString(result.getMerged()));

        mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        result = this.diffManager
            .merge(toCharacters("abcefhik"), toCharacters("abdefgijk"), toCharacters("abcdehijk"), mergeConfiguration);
        assertEquals(2, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(2, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(conflict1, result.getConflicts().get(1));
        assertEquals("abdefgijk", toString(result.getMerged()));
    }

    @Test
    public void applyDecisionsOnMergeCharConflicts() throws Exception
    {
        MergeResult<Character> result;
        MergeConfiguration<Character> mergeConfiguration;
        Conflict<Character> conflict;
        ConflictDecision<Character> conflictDecision;
        List<ConflictDecision<Character>> allConflictDecisions;

        List<Character> emptyList = Collections.emptyList();
        List<Character> onlyA = toCharacters("a");
        List<Character> onlyB = toCharacters("b");
        List<Character> onlyC = toCharacters("c");

        // New empty
        conflict = createConflict(0,
            Type.CHANGE, 0, 0, onlyA, onlyB,
            Type.CHANGE, 0, 0, onlyA, emptyList);
        conflictDecision = new DefaultConflictDecision<>(conflict);
        conflictDecision.setType(ConflictDecision.DecisionType.UNDECIDED);
        allConflictDecisions = Collections.singletonList(conflictDecision);
        mergeConfiguration = new MergeConfiguration<>(allConflictDecisions);
        result = this.diffManager.merge(onlyA, emptyList, onlyB, mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(onlyB, result.getMerged());

        conflictDecision.setType(ConflictDecision.DecisionType.CURRENT);
        result = this.diffManager.merge(onlyA, emptyList, onlyB, mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals(onlyB, result.getMerged());

        conflictDecision.setType(ConflictDecision.DecisionType.PREVIOUS);
        result = this.diffManager.merge(onlyA, emptyList, onlyB, mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals(onlyA, result.getMerged());

        conflictDecision.setType(ConflictDecision.DecisionType.NEXT);
        result = this.diffManager.merge(onlyA, emptyList, onlyB, mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals(emptyList, result.getMerged());

        conflictDecision.setCustom(Arrays.asList('x', 'y'));
        result = this.diffManager.merge(onlyA, emptyList, onlyB, mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals(Arrays.asList('x', 'y'), result.getMerged());

        // Current and new at the same place
        conflict = createConflict(0, 'a', 'c', 'b');
        conflictDecision = new DefaultConflictDecision<>(conflict);
        allConflictDecisions = Collections.singletonList(conflictDecision);
        mergeConfiguration = new MergeConfiguration<>(allConflictDecisions);
        conflictDecision.setType(ConflictDecision.DecisionType.UNDECIDED);
        result = this.diffManager.merge(onlyA, onlyB, onlyC, mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(onlyC, result.getMerged());

        conflictDecision.setType(ConflictDecision.DecisionType.CURRENT);
        result = this.diffManager.merge(onlyA, onlyB, onlyC, mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals(onlyC, result.getMerged());

        conflictDecision.setType(ConflictDecision.DecisionType.PREVIOUS);
        result = this.diffManager.merge(onlyA, onlyB, onlyC, mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals(onlyA, result.getMerged());

        conflictDecision.setType(ConflictDecision.DecisionType.NEXT);
        result = this.diffManager.merge(onlyA, onlyB, onlyC, mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals(onlyB, result.getMerged());

        conflictDecision.setCustom(Collections.emptyList());
        result = this.diffManager.merge(onlyA, onlyB, onlyC, mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals(Collections.emptyList(), result.getMerged());

        // Current and new in conflict at different indices
        conflict = createConflict(0,
            Type.CHANGE, 0, 0, Arrays.asList('a', 'b'), Arrays.asList('a', 'z'),
            Type.CHANGE, 0, 0, Arrays.asList('a', 'b'), Arrays.asList('y', 'y'));
        conflictDecision = new DefaultConflictDecision<>(conflict);
        allConflictDecisions = Collections.singletonList(conflictDecision);
        mergeConfiguration = new MergeConfiguration<>(allConflictDecisions);
        conflictDecision.setType(ConflictDecision.DecisionType.UNDECIDED);
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("yycd"), toCharacters("azzd"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("azzd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.CURRENT);
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("yycd"), toCharacters("azzd"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("azzd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("yycd"), toCharacters("azzd"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("abcd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.NEXT);
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("yycd"), toCharacters("azzd"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("yycd", toString(result.getMerged()));

        conflictDecision.setCustom(toCharacters("Something else"));
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("yycd"), toCharacters("azzd"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("Something elsed", toString(result.getMerged()));

        // Current and new in conflict at different indices
        conflict = createConflict(0,
            Type.CHANGE, 0, 0, Arrays.asList('a', 'b'), Arrays.asList('y', 'y'),
            Type.CHANGE, 0, 0, Arrays.asList('a', 'b'), Arrays.asList('a', 'z'));
        conflictDecision = new DefaultConflictDecision<>(conflict);
        allConflictDecisions = Collections.singletonList(conflictDecision);
        mergeConfiguration = new MergeConfiguration<>(allConflictDecisions);
        conflictDecision.setType(ConflictDecision.DecisionType.UNDECIDED);
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("azzd"), toCharacters("yycd"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("yycd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.CURRENT);
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("azzd"), toCharacters("yycd"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("yycd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("azzd"), toCharacters("yycd"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("abcd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.NEXT);
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("azzd"), toCharacters("yycd"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("azzd", toString(result.getMerged()));

        conflictDecision.setCustom(Collections.singletonList('x'));
        result = this.diffManager
            .merge(toCharacters("abcd"), toCharacters("azzd"), toCharacters("yycd"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("xd", toString(result.getMerged()));

        // New overlapping current
        conflict = createConflict(0,
            Type.CHANGE, 0, 0, Arrays.asList('a', 'a', 'b', 'b'), Arrays.asList('d', 'd', 'd', 'd'),
            Type.CHANGE, 0, 0, Arrays.asList('a', 'a', 'b', 'b'), Arrays.asList('a', 'r', 'r', 'b'));
        conflictDecision = new DefaultConflictDecision<>(conflict);
        allConflictDecisions = Collections.singletonList(conflictDecision);
        mergeConfiguration = new MergeConfiguration<>(allConflictDecisions);
        conflictDecision.setType(ConflictDecision.DecisionType.UNDECIDED);
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("arrbcc"), toCharacters("ddddcc"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("ddddcc", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.CURRENT);
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("arrbcc"), toCharacters("ddddcc"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("ddddcc", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("arrbcc"), toCharacters("ddddcc"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("aabbcc", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.NEXT);
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("arrbcc"), toCharacters("ddddcc"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("arrbcc", toString(result.getMerged()));

        conflictDecision.setCustom(toCharacters("anything"));
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("arrbcc"), toCharacters("ddddcc"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("anythingcc", toString(result.getMerged()));

        // current overlapping new
        conflict = createConflict(0,
            Type.CHANGE, 0, 0, Arrays.asList('a', 'a', 'b', 'b'), Arrays.asList('a', 'r', 'r', 'b'),
            Type.CHANGE, 0, 0, Arrays.asList('a', 'a', 'b', 'b'), Arrays.asList('d', 'd', 'd', 'd'));
        conflictDecision = new DefaultConflictDecision<>(conflict);
        allConflictDecisions = Collections.singletonList(conflictDecision);
        mergeConfiguration = new MergeConfiguration<>(allConflictDecisions);
        conflictDecision.setType(ConflictDecision.DecisionType.UNDECIDED);
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("ddddcc"), toCharacters("arrbcc"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("arrbcc", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.CURRENT);
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("ddddcc"), toCharacters("arrbcc"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("arrbcc", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("ddddcc"), toCharacters("arrbcc"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("aabbcc", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.NEXT);
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("ddddcc"), toCharacters("arrbcc"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("ddddcc", toString(result.getMerged()));

        conflictDecision.setCustom(Collections.emptyList());
        result = this.diffManager
            .merge(toCharacters("aabbcc"), toCharacters("ddddcc"), toCharacters("arrbcc"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("cc", toString(result.getMerged()));

        // Multiple conflicts
        conflict = createConflict(0,
            Type.CHANGE, 0, 0, Arrays.asList('a', 'a', 'b', 'b'), Arrays.asList('a', 'r', 'r', 'b'),
            Type.CHANGE, 0, 0, Arrays.asList('a', 'a', 'b', 'b'), Arrays.asList('d', 'd', 'd', 'd'));
        Conflict<Character> conflict1 = createConflict(7,
            Type.CHANGE, 7, 7, Arrays.asList('a', 'b'), Arrays.asList('y','y'),
            Type.CHANGE, 7, 7, Arrays.asList('a', 'b'), Arrays.asList('a', 'z'));
        allConflictDecisions = new ArrayList<>();
        conflictDecision = new DefaultConflictDecision<>(conflict);
        allConflictDecisions.add(conflictDecision);
        ConflictDecision<Character> conflictDecision1 = new DefaultConflictDecision<>(conflict1);
        allConflictDecisions.add(conflictDecision1);
        conflictDecision.setType(ConflictDecision.DecisionType.UNDECIDED);
        conflictDecision1.setType(ConflictDecision.DecisionType.UNDECIDED);
        mergeConfiguration = new MergeConfiguration<>(allConflictDecisions);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(2, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(2, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(conflict1, result.getConflicts().get(1));
        assertEquals("arrbcc yycd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.CURRENT);
        conflictDecision1.setType(ConflictDecision.DecisionType.CURRENT);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("arrbcc yycd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.PREVIOUS);
        conflictDecision1.setType(ConflictDecision.DecisionType.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("aabbcc abcd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.NEXT);
        conflictDecision1.setType(ConflictDecision.DecisionType.NEXT);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("ddddcc azzd", toString(result.getMerged()));

        conflictDecision.setCustom(toCharacters("A first change"));
        conflictDecision1.setCustom(toCharacters("and another one"));
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("A first changecc and another oned", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.CURRENT);
        conflictDecision1.setType(ConflictDecision.DecisionType.NEXT);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("arrbcc azzd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.NEXT);
        conflictDecision1.setType(ConflictDecision.DecisionType.CURRENT);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("ddddcc yycd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.CURRENT);
        conflictDecision1.setType(ConflictDecision.DecisionType.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("arrbcc abcd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.PREVIOUS);
        conflictDecision1.setType(ConflictDecision.DecisionType.CURRENT);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("aabbcc yycd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.NEXT);
        conflictDecision1.setType(ConflictDecision.DecisionType.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("ddddcc abcd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.PREVIOUS);
        conflictDecision1.setType(ConflictDecision.DecisionType.NEXT);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("aabbcc azzd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.UNDECIDED);
        conflictDecision1.setType(ConflictDecision.DecisionType.CURRENT);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("arrbcc yycd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.CURRENT);
        conflictDecision1.setType(ConflictDecision.DecisionType.UNDECIDED);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict1, result.getConflicts().get(0));
        assertEquals("arrbcc yycd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.UNDECIDED);
        conflictDecision1.setType(ConflictDecision.DecisionType.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("arrbcc abcd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.PREVIOUS);
        conflictDecision1.setType(ConflictDecision.DecisionType.UNDECIDED);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict1, result.getConflicts().get(0));
        assertEquals("aabbcc yycd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.UNDECIDED);
        conflictDecision1.setType(ConflictDecision.DecisionType.NEXT);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("arrbcc azzd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.NEXT);
        conflictDecision1.setType(ConflictDecision.DecisionType.UNDECIDED);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict1, result.getConflicts().get(0));
        assertEquals("ddddcc yycd", toString(result.getMerged()));

        conflictDecision.setCustom(toCharacters("bla"));
        conflictDecision1.setType(ConflictDecision.DecisionType.UNDECIDED);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict1, result.getConflicts().get(0));
        assertEquals("blacc yycd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.UNDECIDED);
        conflictDecision1.setCustom(toCharacters("foo"));
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals("arrbcc food", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.CUSTOM);
        conflictDecision1.setType(ConflictDecision.DecisionType.NEXT);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("cc azzd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.NEXT);
        conflictDecision1.setCustom(toCharacters("foobar"));
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("ddddcc foobard", toString(result.getMerged()));

        conflictDecision.setCustom(toCharacters("Something like that"));
        conflictDecision1.setType(ConflictDecision.DecisionType.PREVIOUS);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("Something like thatcc abcd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.PREVIOUS);
        conflictDecision1.setCustom(toCharacters("dddddd"));
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("aabbcc ddddddd", toString(result.getMerged()));

        conflictDecision.setCustom(toCharacters("abc"));
        conflictDecision1.setType(ConflictDecision.DecisionType.CURRENT);
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("abccc yycd", toString(result.getMerged()));

        conflictDecision.setType(ConflictDecision.DecisionType.CURRENT);
        conflictDecision1.setCustom(toCharacters("azzd"));
        result = this.diffManager
            .merge(toCharacters("aabbcc abcd"), toCharacters("ddddcc azzd"), toCharacters("arrbcc yycd"),
                mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals("arrbcc azzdd", toString(result.getMerged()));
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
        assertEquals(1, result.getConflicts().size());

        Conflict<String> conflict = createConflict(0,
            Type.CHANGE, 0, 0, Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("New content", "That is completely different"),
            Type.CHANGE, 0, 0, Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3"));
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(Arrays.asList("New content", "That is completely different"), result.getMerged());

        MergeConfiguration<String> mergeConfiguration = new MergeConfiguration<>();
        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"),
            Arrays.asList("New content", "That is completely different"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(Arrays.asList("Line 1", "Line 2", "Line 3"), result.getMerged());

        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"),
            Arrays.asList("New content", "That is completely different"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"), result.getMerged());

        // Test 2: All content has been deleted between previous and current
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
                Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"),
                Collections.emptyList(), null);
        conflict = createConflict(0,
            Type.CHANGE, 0, 0, Arrays.asList("Line 1", "Line 2", "Line 3"), Collections.emptyList(),
            Type.CHANGE, 0, 0, Arrays.asList("Line 1", "Line 2", "Line 3"), Arrays.asList("Line 1", "Line 2 modified",
                "Line 3"));
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(Collections.emptyList(), result.getMerged());

        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"),
            Collections.emptyList(), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(Arrays.asList("Line 1", "Line 2", "Line 3"), result.getMerged());

        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"),
            Collections.emptyList(), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"), result.getMerged());

        // Test 3: All content has been deleted between previous and next
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Collections.emptyList(),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"), null);
        conflict = createConflict(0,
            Type.CHANGE, 0, 0, Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3"),
            Type.CHANGE, 0, 0, Arrays.asList("Line 1", "Line 2", "Line 3"), Collections.emptyList());
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"), result.getMerged());

        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Collections.emptyList(),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(Arrays.asList("Line 1", "Line 2", "Line 3"), result.getMerged());

        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.NEXT);
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Collections.emptyList(),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3", "Line 4 Added"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).get(0).toString()
            .contains("Conflict between"));
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(Collections.emptyList(), result.getMerged());

        // Test 4: All content has changed between previous and current, and current and next are identical
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("New content", "That is completely different"),
            Arrays.asList("New content", "That is completely different"), null);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals(Arrays.asList("New content", "That is completely different"), result.getMerged());

        mergeConfiguration.setFallbackOnConflict(MergeConfiguration.Version.PREVIOUS);
        result = this.diffManager.merge(Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("New content", "That is completely different"),
            Arrays.asList("New content", "That is completely different"), mergeConfiguration);
        assertEquals(0, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(0, result.getConflicts().size());
        assertEquals(Arrays.asList("New content", "That is completely different"), result.getMerged());

        mergeConfiguration = null;
        conflict = this.createConflict(2,
            Type.CHANGE, 2, 2, Arrays.asList("started to walk"), Arrays.asList("started to walk"),
            Type.CHANGE, 2, 2, Arrays.asList("started to walk"), Arrays.asList("dressed in black"));
        result = this.diffManager
            .merge(
                Arrays.asList(
                    "Once upon a time",
                    "a wolf",
                    "started to walk",
                    "in the forest"
                ),
                Arrays.asList(
                    "Once upon a time",
                    "a wolf",
                    "dressed in black",
                    "in the forest"
                ),
                Arrays.asList(
                    "Once upon a time",
                    "a wolf",
                    "started to walk",
                    "dressed in black",
                    "in the forest"
                ), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(
            Arrays.asList("Once upon a time", "a wolf", "started to walk", "dressed in black", "in the forest"),
            result.getMerged());

        // Test 6: We change everything but our first changes are the same at the beginning of current and next:
        // it still must led to a conflict, since we don't know if we should have "Another line" or not.
        // Now the conflict created is questionable: should it be a change conflict or an insert/delete conflict?
        mergeConfiguration = null;
        conflict = createConflict(0,
            Type.CHANGE, 0, 0, Arrays.asList("A first edit from a tab."),
            Arrays.asList("A second edit from another tab."),
            Type.CHANGE, 0, 0, Arrays.asList("A first edit from a tab."),
            Arrays.asList("A second edit from another tab."));
        result = this.diffManager.merge(
            Arrays.asList("A first edit from a tab."),
            Arrays.asList("A second edit from another tab."),
            Arrays.asList("A second edit from another tab.", "Another line"),
            mergeConfiguration);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(Arrays.asList("A second edit from another tab.", "Another line"), result.getMerged());

        // Test 7: Change 3 lines between previous and next, and only 2 between previous and current:
        // only 1 conflicts should occur, concerning the whole list of changes.
        conflict = this.createConflict(0,
            Type.CHANGE, 0, 0,
            Arrays.asList(
                "A fifth edit from another tab.",
                "Another line.",
                "Yet another line with other few changes."
            ),
            Arrays.asList(
                "A sixth edit from the first tab.",
                "Another line.",
                "Yet another line edited from the first tab."
            ),
            Type.CHANGE, 0, 0, Arrays.asList(
                "A fifth edit from another tab.",
                "Another line.",
                "Yet another line with other few changes."
            ),
            Arrays.asList(
                "A sixth edit from the second tab.",
                "Another line with small changes.",
                "Another edit from the second tab."
            ));
        result = this.diffManager.merge(
            Arrays.asList(
                "A fifth edit from another tab.",
                "Another line.",
                "Yet another line with other few changes."
            ),
            Arrays.asList(
                "A sixth edit from the second tab.",
                "Another line with small changes.",
                "Another edit from the second tab."
            ),
            Arrays.asList(
                "A sixth edit from the first tab.",
                "Another line.",
                "Yet another line edited from the first tab."
            ), mergeConfiguration);
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));

        // Test 7: Delete lines between previous and next, and only 1 change between previous and current:
        // only 1 conflicts should occur, concerning the whole list of changes.
        conflict = this.createConflict(0,
            Type.CHANGE, 0, 0,
            Arrays.asList(
                "A fifth edit from another tab.",
                "Another line.",
                "Yet another line with other few changes."
            ),
            Arrays.asList(
                "A sixth edit from the first tab.",
                "Another line.",
                "Yet another line edited from the first tab."
            ),
            Type.CHANGE, 0, 0, Arrays.asList(
                "A fifth edit from another tab.",
                "Another line.",
                "Yet another line with other few changes."
            ),
            Collections.emptyList());
        result = this.diffManager.merge(
            Arrays.asList(
                "A fifth edit from another tab.",
                "Another line.",
                "Yet another line with other few changes."
            ),
            Collections.emptyList(),
            Arrays.asList(
                "A sixth edit from the first tab.",
                "Another line.",
                "Yet another line edited from the first tab."
            ), mergeConfiguration);
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));

        // Test 8: Conflicts with empty lines
        conflict = this.createConflict(0,
            Type.CHANGE, 0, 0, Arrays.asList("Some content."), Arrays.asList("Another content"),
            Type.CHANGE, 0, 0, Arrays.asList("Some content."), Arrays.asList("Some content."));
        result = this.diffManager.merge(
            Arrays.asList(
                "Some content."
            ),
            Arrays.asList(
                "Some content.",
                "",
                "And now if I try",
                "to put something",
                "",
                "with empty lines",
                "like this"
            ),
            Arrays.asList(
                "Another content",
                "On multiple lines."
            ), mergeConfiguration);
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));

        // Test 9: Multiple conflicts and insertion of conflicting data
        // Test3: multiple conflicts
        conflict = this.createConflict(1,
            Type.CHANGE, 1, 1, Arrays.asList("Second line.", "Third line."), Arrays.asList("Second line.", "Line N°4"),
            Type.CHANGE, 1, 1, Arrays.asList("Second line.", "Third line."), Arrays.asList("Line N°2", "Third line."));

        Conflict<String> conflict2 = this.createConflict(5,
            Type.CHANGE, 5, 5, Collections.emptyList(), Arrays.asList("6th line."),
            Type.CHANGE, 5, 5, Collections.emptyList(), Arrays.asList("Sixth line."));
        result = this.diffManager.merge(
            Arrays.asList(
                "First line.",
                "Second line.",
                "Third line.",
                "Fourth line.",
                "Fifth line.",
                "Seventh line."
            ), Arrays.asList(
                "First line.",
                "Line N°2",
                "Third line.",
                "Fifth line.",
                "Sixth line.",
                "Seventh line."
            ), Arrays.asList(
                "First line.",
                "Second line.",
                "Line N°4",
                "Fifth line.",
                "6th line.",
                "Seventh line."
        ), mergeConfiguration);
        assertEquals(2, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(conflict2, result.getConflicts().get(1));

        // Test 10: conflict should only concern the minimum needed
        conflict = this.createConflict(0,
            Type.CHANGE, 0, 0, Arrays.asList("A second edit from another tab."),
            Arrays.asList("A fourth edit from second tab."),
            Type.CHANGE, 0, 0, Arrays.asList("A second edit from another tab."),
            Arrays.asList("A third edit from another tab."));
        result = this.diffManager.merge(
            Arrays.asList("A second edit from another tab.", "A new line."),
            Arrays.asList("A third edit from another tab.", "Another line.", "Yet another line."),
            Arrays.asList("A fourth edit from second tab.", "Another line."),
            mergeConfiguration);
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
    }

    private <E> Delta<E> createDelta(Type type, Chunk<E> previous, Chunk<E> next)
    {
        switch (type) {
            case CHANGE:
                return new ChangeDelta<E>(previous, next);

            case INSERT:
                return new InsertDelta<E>(previous, next);

            case DELETE:
                return new DeleteDelta<E>(previous, next);

            default:
                throw new UnsupportedOperationException();
        }
    }

    private <E> Conflict<E> createConflict(int conflictIndex,
        Type currentType, int previousCurrentIndex, int currentIndex, List<E> previousCurrent, List<E> nextCurrent,
        Type nextType, int previousNextIndex, int nextIndex, List<E> previousNext, List<E> nextNext)
    {
        DefaultChunk<E> previousCurrentChunk = new DefaultChunk<>(previousCurrentIndex, previousCurrent);
        DefaultChunk<E> currentChunk = new DefaultChunk<>(currentIndex, nextCurrent);

        DefaultChunk<E> previousNextChunk = new DefaultChunk<>(previousNextIndex, previousNext);
        DefaultChunk<E> nextChunk = new DefaultChunk<>(nextIndex, nextNext);

        Delta<E> currentDelta;
        return new DefaultConflict<>(conflictIndex,
            createDelta(currentType, previousCurrentChunk, currentChunk),
            createDelta(nextType, previousNextChunk, nextChunk));
    }

    private <E> Conflict<E> createConflict(int index, E previous, E current, E next)
    {
        return createConflict(index,
            Type.CHANGE, index, index, Collections.singletonList(previous), Collections.singletonList(current),
            Type.CHANGE, index, index, Collections.singletonList(previous), Collections.singletonList(next));
    }

    @Test
    public void applyDecisionsOnLinesConflicts() throws Exception
    {
        MergeResult<String> result;
        MergeConfiguration<String> mergeConfiguration;

        // Test 1: All content has changed between previous and current
        result = diffManager.merge(
            Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3"),
            Arrays.asList("Line 1", "Line 2 another change", "Line 3"), null);

        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());

        Conflict<String> conflict = createConflict(1, "Line 2", "Line 2 another change", "Line 2 modified");
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(Arrays.asList("Line 1", "Line 2 another change", "Line 3"), result.getMerged());

        ConflictDecision<String> conflictDecision = new DefaultConflictDecision<>(conflict);
        conflictDecision.setType(ConflictDecision.DecisionType.PREVIOUS);

        List<ConflictDecision<String>> conflictDecisionList = new ArrayList<>();
        conflictDecisionList.add(conflictDecision);
        mergeConfiguration = new MergeConfiguration<>(conflictDecisionList);
        result = diffManager.merge(
            Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3"),
            Arrays.asList("Line 1", "Line 2 another change", "Line 3"), mergeConfiguration);
        assertTrue(result.getConflicts().isEmpty());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).isEmpty());
        assertEquals(Arrays.asList("Line 1", "Line 2", "Line 3"), result.getMerged());

        conflictDecision.setType(ConflictDecision.DecisionType.NEXT);
        result = diffManager.merge(
            Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3"),
            Arrays.asList("Line 1", "Line 2 another change", "Line 3"), mergeConfiguration);
        assertTrue(result.getConflicts().isEmpty());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).isEmpty());
        assertEquals(Arrays.asList("Line 1", "Line 2 modified", "Line 3"), result.getMerged());

        conflictDecision.setType(ConflictDecision.DecisionType.CURRENT);
        result = diffManager.merge(
            Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3"),
            Arrays.asList("Line 1", "Line 2 another change", "Line 3"), mergeConfiguration);
        assertTrue(result.getConflicts().isEmpty());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).isEmpty());
        assertEquals(Arrays.asList("Line 1", "Line 2 another change", "Line 3"), result.getMerged());

        conflictDecision.setType(ConflictDecision.DecisionType.UNDECIDED);
        result = diffManager.merge(
            Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3"),
            Arrays.asList("Line 1", "Line 2 another change", "Line 3"), mergeConfiguration);
        assertEquals(1, result.getLog().getLogs(LogLevel.ERROR).size());
        assertEquals(1, result.getConflicts().size());
        assertEquals(conflict, result.getConflicts().get(0));
        assertEquals(Arrays.asList("Line 1", "Line 2 another change", "Line 3"), result.getMerged());

        conflictDecision.setCustom(Arrays.asList("Another complete custom change", "and a line"));
        result = diffManager.merge(
            Arrays.asList("Line 1", "Line 2", "Line 3"),
            Arrays.asList("Line 1", "Line 2 modified", "Line 3"),
            Arrays.asList("Line 1", "Line 2 another change", "Line 3"), mergeConfiguration);
        assertTrue(result.getConflicts().isEmpty());
        assertTrue(result.getLog().getLogs(LogLevel.ERROR).isEmpty());
        assertEquals(Arrays.asList("Line 1", "Another complete custom change", "and a line", "Line 3"),
            result.getMerged());
    }
}
