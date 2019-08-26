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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.MergeResult;
import org.xwiki.diff.display.UnifiedDiffBlock;
import org.xwiki.diff.display.UnifiedDiffConflictElement;
import org.xwiki.diff.display.UnifiedDiffElement;
import org.xwiki.diff.internal.DefaultDiffManager;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.text.XWikiToStringBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link DefaultUnifiedDiffDisplayer}.
 *
 * @version $Id$
 * @since 4.1M2
 */
@ComponentList({
    DefaultDiffManager.class
})
@ComponentTest
public class DefaultUnifiedDiffDisplayerTest
{
    @InjectMockComponents
    private DefaultUnifiedDiffDisplayer unifiedDiffDisplayer;

    @InjectComponentManager
    private ComponentManager componentManager;

    static class TestData {
        List<String> previous;
        List<String> next;
        String expected;

        TestData(List<String> previous, List<String> next, String expected)
        {
            this.previous = previous;
            this.next = next;
            this.expected = expected;
        }

        @Override
        public String toString()
        {
            return new XWikiToStringBuilder(this)
                .append("previous", previous)
                .append("next", next)
                .append("expected", expected)
                .build();
        }
    }

    private DiffManager getDiffManager() throws ComponentLookupException
    {
        return this.componentManager.getInstance(DiffManager.class);
    }

    /**
     * The actual test.
     */
    @ParameterizedTest
    @MethodSource("parametersData")
    public void display(TestData data) throws Exception
    {
        List<UnifiedDiffBlock<String, Object>> blocks =
            unifiedDiffDisplayer.display(getDiffManager().diff(data.previous, data.next, null));

        StringBuilder actual = new StringBuilder();
        for (UnifiedDiffBlock<String, ?> block : blocks) {
            actual.append(block);
        }
        assertEquals(data.expected, actual.toString());
    }

    @Test
    public void displayWithConflicts() throws Exception
    {
        List<String> previous = readLines("previous.txt");
        List<String> current = readLines("twoContexts.txt");
        List<String> next = readLines("twoContexts_next.txt");

        MergeResult<String> mergeResult = getDiffManager().merge(previous, next, current, null);
        assertFalse(mergeResult.getConflicts().isEmpty());

        DiffResult<String> diffResult = getDiffManager().diff(previous, next, null);
        List<UnifiedDiffBlock<String, Object>> unifiedDiffBlocks = unifiedDiffDisplayer.display(diffResult);

        assertEquals(2, unifiedDiffBlocks.size());
        assertFalse(unifiedDiffBlocks.get(1).isConflicting());

        unifiedDiffBlocks = unifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());

        assertEquals(2, unifiedDiffBlocks.size());
        assertTrue(unifiedDiffBlocks.get(1).isConflicting());

        assertEquals(8, unifiedDiffBlocks.get(1).size());
        UnifiedDiffConflictElement<String> unifiedDiffConflictElement = unifiedDiffBlocks.get(1).get(3).getConflict();
        assertEquals("    //   If k > 2 then fib(k) = fib(k-1) + fib(k-2).",
            unifiedDiffConflictElement.getPreviousElement());
        assertEquals("    //   if k > 2 then fib(k) = fib(k-1) + fib(k-2)",
            unifiedDiffConflictElement.getCurrentElement());
        assertEquals("    //   if k > 2 then fib(k) = fib(k-1) + fib(k-2)?",
            unifiedDiffConflictElement.getNextElement());

        unifiedDiffConflictElement = unifiedDiffBlocks.get(1).get(4).getConflict();
        assertEquals("    //   If k > 2 then fib(k) = fib(k-1) + fib(k-2).",
            unifiedDiffConflictElement.getPreviousElement());
        assertEquals("    //   if k > 2 then fib(k) = fib(k-1) + fib(k-2)",
            unifiedDiffConflictElement.getCurrentElement());
        assertEquals("    //   if k > 2 then fib(k) = fib(k-1) + fib(k-2)?",
            unifiedDiffConflictElement.getNextElement());
    }

    /**
     * @return the collection of test parameters
     */
    static Collection<TestData> parametersData() throws IOException
    {
        Collection<TestData> data = new ArrayList<TestData>();

        //
        // Add special tests.
        //

        // Both previous and next are empty.
        data.add(new TestData(Collections.<String>emptyList(), Collections.<String>emptyList(), ""));

        // Previous and next are equal.
        List<String> lines = Arrays.asList("one", "two", "three");
        data.add(new TestData(lines, lines, "" ));

        // Previous is empty.
        data.add(new TestData(Collections.<String>emptyList(), lines, "@@ -1,0 +1,3 @@\n+one\n+two\n+three\n"));

        // Next is empty.
        data.add(new TestData(lines, Collections.<String>emptyList(), "@@ -1,3 +1,0 @@\n-one\n-two\n-three\n" ));

        // Line removed.
        data.add(new TestData(lines, Arrays.asList(lines.get(0), lines.get(2)),
        "@@ -1,3 +1,2 @@\n one\n-two\n three\n" ));

        // Line added.
        data.add(new TestData(lines, Arrays.asList(lines.get(0), lines.get(1), "between", lines.get(2)),
        "@@ -1,3 +1,4 @@\n one\n two\n+between\n three\n"));

        // Line changed.
        data.add(new TestData(lines, Arrays.asList(lines.get(0), "Two", lines.get(2)),
        "@@ -1,3 +1,3 @@\n one\n-two\n+Two\n three\n"));

        //
        // Add tests from files.
        //

        List<String> previous = readLines("previous.txt");
        String[] testNames = new String[] { "twoContexts", "sharedContext" };
        for (String testName : testNames) {
            data.add(new TestData(previous, readLines(testName + ".txt"), readContent(testName + ".diff")));
        }

        return data;
    }

    /**
     * Reads the lines from the specified file.
     *
     * @param fileName the file name
     * @return the lines from the specified file
     * @throws IOException if reading the file fails
     */
    private static List<String> readLines(String fileName) throws IOException
    {
        InputStream stream = DefaultUnifiedDiffDisplayerTest.class.getResourceAsStream('/' + fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return IOUtils.readLines(reader);
    }

    /**
     * Reads the content of the specified file.
     *
     * @param fileName the file name
     * @return the content of the specified file
     * @throws IOException if reading the fail fails
     */
    private static String readContent(String fileName) throws IOException
    {
        InputStream stream = DefaultUnifiedDiffDisplayerTest.class.getResourceAsStream('/' + fileName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return IOUtils.toString(reader);
    }
}
