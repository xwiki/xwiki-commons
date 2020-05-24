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
package org.xwiki.diff.display.internal;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.display.InlineDiffChunk;
import org.xwiki.diff.display.InlineDiffChunk.Type;
import org.xwiki.diff.display.Splitter;
import org.xwiki.diff.display.UnifiedDiffBlock;
import org.xwiki.diff.display.UnifiedDiffConfiguration;
import org.xwiki.diff.display.UnifiedDiffDisplayer;
import org.xwiki.diff.display.UnifiedDiffElement;
import org.xwiki.diff.internal.DefaultDiffManager;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests how the unified and inline diff displayers can be mixed to generate a diff both at line (unified) and character
 * (inline) level.
 *
 * @version $Id$
 * @since 4.1M2
 */
@ComponentTest
// @formatter:off
@ComponentList({
    LineSplitter.class,
    CharSplitter.class,
    DefaultDiffManager.class,
    DefaultUnifiedDiffDisplayer.class,
    DefaultInlineDiffDisplayer.class
})
// @formatter:on
class ExtendedDiffDisplayerTest
{
    @InjectComponentManager
    private ComponentManager componentManager;

    @Test
    void displayLineAdded() throws Exception
    {
        execute("one\nthree", "one\ntwo\nthree", "@@ -1,2 +1,3 @@\n one\n+two\n three\n");
    }

    @Test
    void displayLineRemoved() throws Exception
    {
        execute("one\ntwo\nthree", "one\nthree", "@@ -1,3 +1,2 @@\n one\n-two\n three\n");
    }

    @Test
    void displayLineChanged() throws Exception
    {
        execute("one\ntwo\nthree", "one\ntWo\nthree", "@@ -1,3 +1,3 @@\n one\n-t-w-o\n+t+W+o\n three\n");
    }

    @Test
    void displayLineReplaced() throws Exception
    {
        execute("one\ntwo\nthree", "one\ntWo\nextra\nthree", "@@ -1,3 +1,4 @@\n one\n-two\n+tWo\n+extra\n three\n");
    }

    @Test
    void displayLinesChanges() throws Exception
    {
        execute("one\ntwo\nthree\nfour", "one\ntWo\nthrEE\nfour",
            "@@ -1,4 +1,4 @@\n one\n-t-w-o\n-thr-ee-\n+t+W+o\n+thr+EE+\n four\n");
        execute("one\ntwoo\nthre\nfour", "one\ntwo\nthree\nfour",
            "@@ -1,4 +1,4 @@\n one\n-two-o-\n-thre\n+two\n+thre+e+\n four\n");
    }

    @Test
    void displayNullInput() throws Exception
    {
        execute(null, null, "");
    }

    /**
     * Generates the extended diff between the given versions and asserts if it meets the expectation.
     *
     * @param previous the previous version
     * @param next the next version
     * @param expected the expected extended diff
     * @throws Exception if creating the diff fails
     */
    private void execute(String previous, String next, String expected) throws Exception
    {
        ParameterizedType lineSplitterType =
            new DefaultParameterizedType(null, Splitter.class, String.class, String.class);
        Splitter<String, String> lineSplitter = this.componentManager.getInstance(lineSplitterType, "line");
        List<String> previousLines = lineSplitter.split(previous);
        List<String> nextLines = lineSplitter.split(next);

        DiffManager diffManager = this.componentManager.getInstance(DiffManager.class);
        DiffResult<String> diffResult = diffManager.diff(previousLines, nextLines, null);

        ParameterizedType charSplitterType =
            new DefaultParameterizedType(null, Splitter.class, String.class, Character.class);
        Splitter<String, Character> charSplitter = this.componentManager.getInstance(charSplitterType);

        UnifiedDiffDisplayer unifiedDiffDisplayer = this.componentManager.getInstance(UnifiedDiffDisplayer.class);
        UnifiedDiffConfiguration<String, Character> config = unifiedDiffDisplayer.getDefaultConfiguration();
        config.setSplitter(charSplitter);

        Map<Type, String> separators = new HashMap<>();
        separators.put(Type.ADDED, "+");
        separators.put(Type.DELETED, "-");
        separators.put(Type.UNMODIFIED, "");

        StringBuilder actual = new StringBuilder();
        for (UnifiedDiffBlock<String, Character> block : unifiedDiffDisplayer.display(diffResult, config)) {
            actual.append(String.format("@@ -%s,%s +%s,%s @@\n", block.getPreviousStart() + 1, block.getPreviousSize(),
                block.getNextStart() + 1, block.getNextSize()));
            for (UnifiedDiffElement<String, Character> line : block) {
                if (line.getChunks() != null) {
                    actual.append(line.getType().getSymbol());
                    for (InlineDiffChunk<Character> chunk : line.getChunks()) {
                        String separator = separators.get(chunk.getType());
                        actual.append(separator).append(chunk).append(separator);
                    }
                    actual.append('\n');
                } else {
                    actual.append(line);
                }
            }
        }
        assertEquals(expected, actual.toString());
    }
}
