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
@ComponentTest
// @formatter:off
@ComponentList({
    DefaultDiffManager.class
})
// @formatter:on
class DefaultUnifiedDiffDisplayerTest
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
    void display(TestData data) throws Exception
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
    void displayWithConflicts() throws Exception
    {
        List<String> previous = readLines("previous.txt");
        List<String> current = readLines("twoContexts.txt");
        List<String> next = readLines("twoContexts_next.txt");

        MergeResult<String> mergeResult = getDiffManager().merge(previous, next, current, null);
        assertEquals(1, mergeResult.getConflicts().size());

        DiffResult<String> diffResult = getDiffManager().diff(previous, next, null);
        List<UnifiedDiffBlock<String, Object>> unifiedDiffBlocks = unifiedDiffDisplayer.display(diffResult);

        assertEquals(2, unifiedDiffBlocks.size());
        assertFalse(unifiedDiffBlocks.get(0).isConflicting());
        assertFalse(unifiedDiffBlocks.get(1).isConflicting());

        unifiedDiffBlocks = unifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());

        // 4 blocks:
        // - first one if the first changes without conflict
        // - second one is a context block
        // - third one is the conflict itself
        // - fourth one is another context block
        // We don't want to put the context inside the conflict to avoid any side effect when using it.
        assertEquals(4, unifiedDiffBlocks.size());
        assertFalse(unifiedDiffBlocks.get(0).isConflicting());
        assertFalse(unifiedDiffBlocks.get(1).isConflicting());
        assertTrue(unifiedDiffBlocks.get(2).isConflicting());
        assertFalse(unifiedDiffBlocks.get(3).isConflicting());

        assertEquals(3, unifiedDiffBlocks.get(1).size());
        assertEquals(2, unifiedDiffBlocks.get(2).size());
        UnifiedDiffConflictElement<String> unifiedDiffConflictElement = unifiedDiffBlocks.get(2).getConflict();
        assertEquals(Collections.singletonList("    //   If k > 2 then fib(k) = fib(k-1) + fib(k-2)."),
            unifiedDiffConflictElement.getPreviousElement());
        assertEquals(Collections.singletonList("    //   if k > 2 then fib(k) = fib(k-1) + fib(k-2)"),
            unifiedDiffConflictElement.getCurrentElement());
        assertEquals(Collections.singletonList("    //   if k > 2 then fib(k) = fib(k-1) + fib(k-2)?"),
            unifiedDiffConflictElement.getNextElement());
    }

    @Test
    void displayWithConflictsMultipleLines() throws Exception
    {
        //  Test1: whole blocks are in conflicts
        List<String> previous = Arrays.asList(
            "A fifth edit from another tab.",
            "Another line.",
            "Yet another line with other few changes."
        );

        List<String> next = Arrays.asList(
            "A sixth edit from the second tab.",
            "Another line with small changes.",
            "Another edit from the second tab."
        );

        List<String> current = Arrays.asList(
            "A sixth edit from the first tab.",
            "Another line.",
            "Yet another line edited from the first tab."
        );

        MergeResult<String> mergeResult = getDiffManager().merge(previous, next, current, null);
        assertEquals(1, mergeResult.getConflicts().size());

        DiffResult<String> diffResult = getDiffManager().diff(next, current, null);
        List<UnifiedDiffBlock<String, Object>> unifiedDiffBlocks = unifiedDiffDisplayer.display(diffResult);
        assertEquals(1, unifiedDiffBlocks.size());
        assertFalse(unifiedDiffBlocks.get(0).isConflicting());

        List<UnifiedDiffBlock<String, Object>> unifiedDiffBlocksWithConflicts =
            unifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(1, unifiedDiffBlocksWithConflicts.size());
        assertTrue(unifiedDiffBlocksWithConflicts.get(0).isConflicting());
        UnifiedDiffConflictElement<String> conflict = unifiedDiffBlocksWithConflicts.get(0).getConflict();
        assertEquals(previous, conflict.getPreviousElement());
        assertEquals(next, conflict.getNextElement());
        assertEquals(current, conflict.getCurrentElement());

        // Test2: conflict only on the first line, so we should display 2 blocks
        previous = Arrays.asList(
            "Some content."
        );

        next = Arrays.asList(
            "Some content.",
            "",
            "And now if I try",
            "to put something",
            "",
            "with empty lines",
            "like this"
        );

        current = Arrays.asList(
            "Another content",
            "On multiple lines."
        );
        mergeResult = getDiffManager().merge(previous, next, current, null);
        assertEquals(1, mergeResult.getConflicts().size());

        // when we don't pass the conflict we only display one block for the diff
        diffResult = getDiffManager().diff(next, current, null);
        unifiedDiffBlocks = unifiedDiffDisplayer.display(diffResult);
        assertEquals(1, unifiedDiffBlocks.size());
        assertFalse(unifiedDiffBlocks.get(0).isConflicting());

        // with the conflict we should still display 1 block, since the conflict concerns the whole changes
        unifiedDiffBlocksWithConflicts =
            unifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(1, unifiedDiffBlocksWithConflicts.size());
        assertTrue(unifiedDiffBlocksWithConflicts.get(0).isConflicting());

        assertEquals(9, unifiedDiffBlocksWithConflicts.get(0).size());

        assertEquals(previous,
            unifiedDiffBlocksWithConflicts.get(0).getConflict().getPreviousElement());
        assertEquals(next,
            unifiedDiffBlocksWithConflicts.get(0).getConflict().getNextElement());
        assertEquals(current,
            unifiedDiffBlocksWithConflicts.get(0).getConflict().getCurrentElement());

        // Test4: Same as before but with next / current inverted
        previous = Arrays.asList(
            "Some content.",
            "Anything else"
        );

        next = Arrays.asList(
            "Some content.",
            "On multiple lines."
        );

        current = Arrays.asList(
            "Some content.",
            "",
            "And now if I try",
            "to put something",
            "",
            "with empty lines",
            "like this"
        );

        mergeResult = getDiffManager().merge(previous, next, current, null);
        assertEquals(1, mergeResult.getConflicts().size());

        // when we don't pass the conflict we only display one block for the diff
        diffResult = getDiffManager().diff(next, current, null);
        unifiedDiffBlocks = unifiedDiffDisplayer.display(diffResult);
        assertEquals(1, unifiedDiffBlocks.size());
        assertFalse(unifiedDiffBlocks.get(0).isConflicting());

        // but with the conflict we should display 3 blocks
        unifiedDiffBlocksWithConflicts =
            unifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(2, unifiedDiffBlocksWithConflicts.size());
        assertFalse(unifiedDiffBlocksWithConflicts.get(0).isConflicting());
        assertTrue(unifiedDiffBlocksWithConflicts.get(1).isConflicting());

        assertEquals(1, unifiedDiffBlocksWithConflicts.get(0).size());
        assertEquals(" Some content.\n", unifiedDiffBlocksWithConflicts.get(0).get(0).toString());

        assertEquals(7, unifiedDiffBlocksWithConflicts.get(1).size());
        assertEquals("-On multiple lines.\n", unifiedDiffBlocksWithConflicts.get(1).get(0).toString());
        assertEquals("+\n", unifiedDiffBlocksWithConflicts.get(1).get(1).toString());
        assertEquals("+And now if I try\n", unifiedDiffBlocksWithConflicts.get(1).get(2).toString());
        assertEquals("+to put something\n", unifiedDiffBlocksWithConflicts.get(1).get(3).toString());
        assertEquals("+\n", unifiedDiffBlocksWithConflicts.get(1).get(4).toString());
        assertEquals("+with empty lines\n", unifiedDiffBlocksWithConflicts.get(1).get(5).toString());
        assertEquals("+like this\n", unifiedDiffBlocksWithConflicts.get(1).get(6).toString());

        // Test3: conflict in the middle, so we should display 3 blocks
        previous = Arrays.asList(
            "Some content.",
            "Anything else"
        );

        next = Arrays.asList(
            "Some content.",
            "",
            "And now if I try",
            "to put something",
            "",
            "with empty lines",
            "like this"
        );

        current = Arrays.asList(
            "Some content.",
            "On multiple lines."
        );
        mergeResult = getDiffManager().merge(previous, next, current, null);
        assertEquals(1, mergeResult.getConflicts().size());

        // when we don't pass the conflict we only display one block for the diff
        diffResult = getDiffManager().diff(next, current, null);
        unifiedDiffBlocks = unifiedDiffDisplayer.display(diffResult);
        assertEquals(1, unifiedDiffBlocks.size());
        assertFalse(unifiedDiffBlocks.get(0).isConflicting());

        // but with the conflict we should display 3 blocks
        unifiedDiffBlocksWithConflicts =
            unifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(2, unifiedDiffBlocksWithConflicts.size());
        assertFalse(unifiedDiffBlocksWithConflicts.get(0).isConflicting());
        assertTrue(unifiedDiffBlocksWithConflicts.get(1).isConflicting());

        assertEquals(1, unifiedDiffBlocksWithConflicts.get(0).size());
        assertEquals(" Some content.\n", unifiedDiffBlocksWithConflicts.get(0).get(0).toString());

        assertEquals(7, unifiedDiffBlocksWithConflicts.get(1).size());
        assertEquals("-\n", unifiedDiffBlocksWithConflicts.get(1).get(0).toString());
        assertEquals("-And now if I try\n", unifiedDiffBlocksWithConflicts.get(1).get(1).toString());
        assertEquals("-to put something\n", unifiedDiffBlocksWithConflicts.get(1).get(2).toString());
        assertEquals("-\n", unifiedDiffBlocksWithConflicts.get(1).get(3).toString());
        assertEquals("-with empty lines\n", unifiedDiffBlocksWithConflicts.get(1).get(4).toString());
        assertEquals("-like this\n", unifiedDiffBlocksWithConflicts.get(1).get(5).toString());
        assertEquals("+On multiple lines.\n", unifiedDiffBlocksWithConflicts.get(1).get(6).toString());


        // Test5: multiple conflicts
        previous = Arrays.asList(
            "First line.",
            "Second line.",
            "Third line.",
            "Fourth line.",
            "Fifth line.",
            "Seventh line."
        );

        next = Arrays.asList(
            "First line.",
            "Line N°2",
            "Third line.",
            "Fifth line.",
            "Sixth line.",
            "Seventh line."
        );

        current = Arrays.asList(
            "First line.",
            "Second line.",
            "Line N°4",
            "Fifth line.",
            "6th line.",
            "Seventh line."
        );

        mergeResult = getDiffManager().merge(previous, next, current, null);
        assertEquals(2, mergeResult.getConflicts().size());

        // when we don't pass the conflict we only display two blocks for the diff
        diffResult = getDiffManager().diff(next, current, null);
        unifiedDiffBlocks = unifiedDiffDisplayer.display(diffResult);
        assertEquals(1, unifiedDiffBlocks.size());
        assertFalse(unifiedDiffBlocks.get(0).isConflicting());

        // but with the conflict we should display 3 blocks
        unifiedDiffBlocksWithConflicts =
            unifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(5, unifiedDiffBlocksWithConflicts.size());
        assertFalse(unifiedDiffBlocksWithConflicts.get(0).isConflicting());
        assertTrue(unifiedDiffBlocksWithConflicts.get(1).isConflicting());
        assertFalse(unifiedDiffBlocksWithConflicts.get(2).isConflicting());
        assertTrue(unifiedDiffBlocksWithConflicts.get(3).isConflicting());
        assertFalse(unifiedDiffBlocksWithConflicts.get(4).isConflicting());

        assertEquals(1, unifiedDiffBlocksWithConflicts.get(0).size());
        assertEquals(" First line.\n", unifiedDiffBlocksWithConflicts.get(0).get(0).toString());

        assertEquals(4, unifiedDiffBlocksWithConflicts.get(1).size());
        assertEquals("-Line N°2\n", unifiedDiffBlocksWithConflicts.get(1).get(0).toString());
        assertEquals("-Third line.\n", unifiedDiffBlocksWithConflicts.get(1).get(1).toString());
        assertEquals("+Second line.\n", unifiedDiffBlocksWithConflicts.get(1).get(2).toString());
        assertEquals("+Line N°4\n", unifiedDiffBlocksWithConflicts.get(1).get(3).toString());

        assertEquals(1, unifiedDiffBlocksWithConflicts.get(2).size());
        assertEquals(" Fifth line.\n", unifiedDiffBlocksWithConflicts.get(2).get(0).toString());

        assertEquals(2, unifiedDiffBlocksWithConflicts.get(3).size());
        assertEquals("-Sixth line.\n", unifiedDiffBlocksWithConflicts.get(3).get(0).toString());
        assertEquals("+6th line.\n", unifiedDiffBlocksWithConflicts.get(3).get(1).toString());

        assertEquals(1, unifiedDiffBlocksWithConflicts.get(4).size());
        assertEquals(" Seventh line.\n", unifiedDiffBlocksWithConflicts.get(4).get(0).toString());

        // Test6: test conflict subset.
        previous = Arrays.asList("Foo", "Bar", "Baz");
        current = Arrays.asList("Some", "Other", "Thing");
        next = Arrays.asList("Any", "Other", "Thing");
        mergeResult = getDiffManager().merge(previous, next, current, null);
        assertEquals(1, mergeResult.getConflicts().size());

        diffResult = getDiffManager().diff(previous, next, null);
        unifiedDiffBlocks = unifiedDiffDisplayer.display(diffResult);
        assertEquals(1, unifiedDiffBlocks.size());

        unifiedDiffBlocksWithConflicts = unifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(2, unifiedDiffBlocksWithConflicts.size());
        assertTrue(unifiedDiffBlocksWithConflicts.get(0).isConflicting());
        assertFalse(unifiedDiffBlocksWithConflicts.get(1).isConflicting());

        assertEquals(2, unifiedDiffBlocksWithConflicts.get(0).size());
        assertEquals(4, unifiedDiffBlocksWithConflicts.get(1).size());

        assertEquals("-Foo\n", unifiedDiffBlocksWithConflicts.get(0).get(0).toString());
        assertEquals("+Any\n", unifiedDiffBlocksWithConflicts.get(0).get(1).toString());

        assertEquals("-Bar\n", unifiedDiffBlocksWithConflicts.get(1).get(0).toString());
        assertEquals("-Baz\n", unifiedDiffBlocksWithConflicts.get(1).get(1).toString());
        assertEquals("+Other\n", unifiedDiffBlocksWithConflicts.get(1).get(2).toString());
        assertEquals("+Thing\n", unifiedDiffBlocksWithConflicts.get(1).get(3).toString());

        previous = Arrays.asList("Foo", "Bar", "Baz");
        current = Arrays.asList("Other");
        next = Arrays.asList("Other", "Thing");
        mergeResult = getDiffManager().merge(previous, next, current, null);
        assertEquals(1, mergeResult.getConflicts().size());

        diffResult = getDiffManager().diff(previous, next, null);
        unifiedDiffBlocks = unifiedDiffDisplayer.display(diffResult);
        assertEquals(1, unifiedDiffBlocks.size());

        unifiedDiffBlocksWithConflicts = unifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(2, unifiedDiffBlocksWithConflicts.size());
        assertTrue(unifiedDiffBlocksWithConflicts.get(0).isConflicting());
        assertFalse(unifiedDiffBlocksWithConflicts.get(1).isConflicting());

        assertEquals(4, unifiedDiffBlocksWithConflicts.get(0).size());
        assertEquals(1, unifiedDiffBlocksWithConflicts.get(1).size());

        assertEquals("-Foo\n", unifiedDiffBlocksWithConflicts.get(0).get(0).toString());
        assertEquals("-Bar\n", unifiedDiffBlocksWithConflicts.get(0).get(1).toString());
        assertEquals("+Other\n", unifiedDiffBlocksWithConflicts.get(0).get(2).toString());
        assertEquals("+Thing\n", unifiedDiffBlocksWithConflicts.get(0).get(3).toString());

        assertEquals("-Baz\n", unifiedDiffBlocksWithConflicts.get(1).get(0).toString());
    }

    /**
     * @return the collection of test parameters
     */
    static Collection<TestData> parametersData() throws IOException
    {
        Collection<TestData> data = new ArrayList<>();

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
