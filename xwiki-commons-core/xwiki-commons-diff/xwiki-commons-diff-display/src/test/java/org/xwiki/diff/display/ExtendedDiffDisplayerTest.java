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
package org.xwiki.diff.display;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.display.InlineDiffWord.WordType;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * Unit tests for {@link ExtendedDiffDisplayer}.
 * 
 * @version $Id$
 * @since 4.1M2
 */
public class ExtendedDiffDisplayerTest extends AbstractComponentTestCase
{
    @Test
    public void testLineAdded() throws Exception
    {
        execute("one\nthree", "one\ntwo\nthree", "@@ -1,2 +1,3 @@\n one\n+two\n three\n");
    }

    @Test
    public void testLineRemoved() throws Exception
    {
        execute("one\ntwo\nthree", "one\nthree", "@@ -1,3 +1,2 @@\n one\n-two\n three\n");
    }

    @Test
    public void testLineChanged() throws Exception
    {
        execute("one\ntwo\nthree", "one\ntWo\nthree", "@@ -1,3 +1,3 @@\n one\n-t-w-o\n+t+W+o\n three\n");
    }

    @Test
    public void testLineReplaced() throws Exception
    {
        execute("one\ntwo\nthree", "one\ntWo\nextra\nthree", "@@ -1,3 +1,4 @@\n one\n-two\n+tWo\n+extra\n three\n");
    }

    /**
     * Generates the extended diff between the given versions and asserts if it meets the expectation.
     * 
     * @param original the original version
     * @param revised the revised version
     * @param expected the expected extended diff
     * @throws Exception if creating the diff fails
     */
    @SuppressWarnings("unchecked")
    private void execute(String original, String revised, String expected) throws Exception
    {
        ParameterizedType lineSplitterType =
            new DefaultParameterizedType(null, Splitter.class, String.class, String.class);
        Splitter<String, String> lineSplitter = getComponentManager().getInstance(lineSplitterType, "line");
        List<String> originalLines = lineSplitter.split(original);
        List<String> revisedLines = lineSplitter.split(revised);

        Map<WordType, String> separators = new HashMap<WordType, String>();
        separators.put(WordType.ADDED, "+");
        separators.put(WordType.DELETED, "-");
        separators.put(WordType.CONTEXT, "");

        ParameterizedType charSplitterType =
            new DefaultParameterizedType(null, Splitter.class, String.class, Character.class);
        Splitter<String, Character> charSplitter = getComponentManager().getInstance(charSplitterType, "char");

        DiffManager diffManager = getComponentManager().getInstance(DiffManager.class);
        DiffResult<String> diffResult = diffManager.diff(originalLines, revisedLines, null);
        ExtendedDiffDisplayer<String, Character> builder =
            new ExtendedDiffDisplayer<String, Character>(diffManager, charSplitter);

        StringBuilder actual = new StringBuilder();
        for (UnifiedDiffBlock<String> block : builder.display(diffResult)) {
            actual.append(String.format("@@ -%s,%s +%s,%s @@\n", block.getOriginalStart() + 1, block.getOriginalSize(),
                block.getRevisedStart() + 1, block.getRevisedSize()));
            for (UnifiedDiffLine<String> line : block) {
                if (line instanceof ExtendedDiffLine) {
                    actual.append(line.getType().getSymbol());
                    for (InlineDiffWord<Character> word : ((ExtendedDiffLine<String, Character>) line).getWords()) {
                        String separator = separators.get(word.getType());
                        actual.append(separator).append(word).append(separator);
                    }
                    actual.append('\n');
                } else {
                    actual.append(line);
                }
            }
        }
        Assert.assertEquals(expected, actual.toString());
    }
}
