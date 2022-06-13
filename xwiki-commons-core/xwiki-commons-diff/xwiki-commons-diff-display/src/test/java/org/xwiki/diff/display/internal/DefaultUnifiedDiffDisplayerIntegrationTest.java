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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.diff.Chunk;
import org.xwiki.diff.Conflict;
import org.xwiki.diff.Delta;
import org.xwiki.diff.DiffConfiguration;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.MergeConfiguration;
import org.xwiki.diff.MergeResult;
import org.xwiki.diff.display.UnifiedDiffBlock;
import org.xwiki.diff.internal.ChangeDelta;
import org.xwiki.diff.internal.DefaultChunk;
import org.xwiki.diff.internal.DefaultConflict;
import org.xwiki.diff.internal.DefaultDiffManager;
import org.xwiki.diff.internal.DeleteDelta;
import org.xwiki.diff.internal.InsertDelta;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests on {@link DefaultUnifiedDiffDisplayer} that performs check by reading on files,
 * performs merge and diff using {@link DefaultDiffManager} and checks the result of the unified diff.
 *
 * @since 12.3RC1
 * @since 11.10.5
 * @version $Id$
 */
@ComponentTest
@ComponentList(DefaultDiffManager.class)
class DefaultUnifiedDiffDisplayerIntegrationTest
{
    @InjectMockComponents
    private DefaultUnifiedDiffDisplayer defaultUnifiedDiffDisplayer;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    private List<String> readLines(String path) throws IOException
    {
        InputStream stream = DefaultUnifiedDiffDisplayerTest.class.getResourceAsStream('/' + path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return IOUtils.readLines(reader);
    }
    
    private DiffManager getDiffManager() throws ComponentLookupException
    {
        return this.componentManager.getInstance(DiffManager.class);
    }

    @Test
    void display() throws Exception
    {
        String directory = "integration1";
        List<String> previous = readLines(String.format("%s/previous.txt", directory));
        List<String> current = readLines(String.format("%s/current.txt", directory));
        List<String> next = readLines(String.format("%s/next.txt", directory));

        DiffManager diffManager = this.componentManager.getInstance(DiffManager.class);
        MergeResult<String> mergeResult = diffManager.merge(previous, next, current, new MergeConfiguration<>());
        assertEquals(4, mergeResult.getConflicts().size());

        DiffResult<String> diffResult = diffManager.diff(current, mergeResult.getMerged(), new DiffConfiguration<>());
        List<UnifiedDiffBlock<String, Object>> diffBlocks =
            this.defaultUnifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(12, diffBlocks.size());

        diffBlocks =
            this.defaultUnifiedDiffDisplayer.display(diffResult, Collections.emptyList());
        assertEquals(4, diffBlocks.size());
    }

    private Delta<String> createExpectedDelta(Delta.Type type, int indexPrevious, List<String> previousLines,
        int indexNext, List<String> nextLines)
    {
        Chunk<String> previousChunk = new DefaultChunk<>(indexPrevious, previousLines);
        Chunk<String> nextChunk = new DefaultChunk<>(indexNext, nextLines);
        switch (type) {
            case CHANGE:
                return new ChangeDelta<>(previousChunk, nextChunk);

            case DELETE:
                return new DeleteDelta<>(previousChunk, nextChunk);

            case INSERT:
                return new InsertDelta<>(previousChunk, nextChunk);

            default:
                throw new IllegalArgumentException("Wrong type");
        }
    }

    private Conflict<String> createExpectedConflict(int index, Delta<String> deltaCurrent, Delta<String> deltaNext)
    {
        return new DefaultConflict<>(index, deltaCurrent, deltaNext);
    }

    @Test
    void displayWithConflicts1() throws Exception
    {
        String directory = "integration3";
        List<String> previous = readLines(String.format("%s/previous.txt", directory));
        List<String> current = readLines(String.format("%s/current.txt", directory));
        List<String> next = readLines(String.format("%s/next.txt", directory));
        MergeResult<String> mergeResult = getDiffManager().merge(previous, next, current, null);
        Delta<String> conflict1DeltaCurrent = createExpectedDelta(Delta.Type.CHANGE, 0, Arrays.asList(
            "Esse minim non amet enim mollit ipsum.",
            "Esse minim non amet enim mollit ipsum.",
            ""
        ), 0, Arrays.asList(
            "",
            "Esse minim non amet enim mollit ipsum.",
            "Est do dolore cupidatat elit irure, excepteur amet lorem lorem."
        ));

        Delta<String> conflict1DeltaNext = createExpectedDelta(Delta.Type.CHANGE, 0, Arrays.asList(
            "Esse minim non amet enim mollit ipsum.",
            "Esse minim non amet enim mollit ipsum.",
            ""
        ), 0, Arrays.asList(
            "Est do dolore cupidatat elit irure, excepteur amet lorem lorem.",
            "",
            "Commodo sunt consequat irure eiusmod nostrud."
        ));
        Conflict<String> expectedConflict1 = createExpectedConflict(0, conflict1DeltaCurrent, conflict1DeltaNext);

        Delta<String> conflict2DeltaCurrent = createExpectedDelta(Delta.Type.INSERT, 3, Collections.emptyList(), 3,
            Arrays.asList(
                "Est do dolore cupidatat elit irure, excepteur amet lorem lorem.",
                "Est do dolore cupidatat elit irure, excepteur amet lorem lorem.",
                "Commodo sunt consequat irure eiusmod nostrud.",
                "",
                "",
                "",
                "",
                "Esse minim non amet enim mollit ipsum."
        ));

        Delta<String> conflict2DeltaNext = createExpectedDelta(Delta.Type.INSERT, 3, Collections.emptyList(), 3,
            Arrays.asList(
                "Commodo sunt consequat irure eiusmod nostrud.",
                ""
            ));
        Conflict<String> expectedConflict2 = createExpectedConflict(3, conflict2DeltaCurrent, conflict2DeltaNext);

        Delta<String> conflict3DeltaCurrent = createExpectedDelta(Delta.Type.CHANGE, 5, Collections.singletonList(""),
            5, Arrays.asList(
                "",
                ""
            ));
        Delta<String> conflict3DeltaNext = createExpectedDelta(Delta.Type.DELETE, 5, Collections.singletonList(""),
            5, Collections.emptyList());
        Conflict<String> expectedConflict3 = createExpectedConflict(5, conflict3DeltaCurrent, conflict3DeltaNext);
        assertEquals(Arrays.asList(expectedConflict1, expectedConflict2, expectedConflict3),
            mergeResult.getConflicts());

        DiffResult<String> diffResult = getDiffManager().diff(current, mergeResult.getMerged(), null);
        List<UnifiedDiffBlock<String, Object>> unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult);
        assertEquals(1, unifiedDiffBlocks.size());

        unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(3, unifiedDiffBlocks.size());
    }

    @Test
    void displayWithConflicts2() throws Exception
    {
        String directory = "integration4";
        List<String> previous = readLines(String.format("%s/previous.txt", directory));
        List<String> current = readLines(String.format("%s/current.txt", directory));
        List<String> next = readLines(String.format("%s/next.txt", directory));
        MergeResult<String> mergeResult = getDiffManager().merge(previous, next, current, null);
        assertEquals(3, mergeResult.getConflicts().size());

        DiffResult<String> diffResult = getDiffManager().diff(current, mergeResult.getMerged(), null);
        List<UnifiedDiffBlock<String, Object>> unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult);
        assertEquals(1, unifiedDiffBlocks.size());

        unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(3, unifiedDiffBlocks.size());
    }

    @Test
    void displayWithConflicts3() throws Exception
    {
        String directory = "integration5";
        List<String> previous = readLines(String.format("%s/previous.txt", directory));
        List<String> current = readLines(String.format("%s/current.txt", directory));
        List<String> next = readLines(String.format("%s/next.txt", directory));
        MergeResult<String> mergeResult = getDiffManager().merge(previous, next, current, null);
        assertEquals(3, mergeResult.getConflicts().size());

        DiffResult<String> diffResult = getDiffManager().diff(current, mergeResult.getMerged(), null);
        List<UnifiedDiffBlock<String, Object>> unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult);
        assertEquals(1, unifiedDiffBlocks.size());

        unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(2, unifiedDiffBlocks.size());
    }

    @Test
    void displayWithConflicts4() throws Exception
    {
        String directory = "integration6";
        List<String> previous = readLines(String.format("%s/previous.txt", directory));
        List<String> current = readLines(String.format("%s/current.txt", directory));
        List<String> next = readLines(String.format("%s/next.txt", directory));
        MergeResult<String> mergeResult = getDiffManager().merge(previous, next, current, null);
        assertEquals(5, mergeResult.getConflicts().size());

        DiffResult<String> diffResult = getDiffManager().diff(current, mergeResult.getMerged(), null);
        List<UnifiedDiffBlock<String, Object>> unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult);
        assertEquals(1, unifiedDiffBlocks.size());

        unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(3, unifiedDiffBlocks.size());
    }

    @Test
    void displayWithConflicts5() throws Exception
    {
        String directory = "integration7";
        List<String> previous = readLines(String.format("%s/previous.txt", directory));
        List<String> current = readLines(String.format("%s/current.txt", directory));
        List<String> next = readLines(String.format("%s/next.txt", directory));
        MergeResult<String> mergeResult = getDiffManager().merge(previous, next, current, null);
        assertEquals(3, mergeResult.getConflicts().size());

        DiffResult<String> diffResult = getDiffManager().diff(current, mergeResult.getMerged(), null);
        List<UnifiedDiffBlock<String, Object>> unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult);
        assertEquals(0, unifiedDiffBlocks.size());

        unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(0, unifiedDiffBlocks.size());

        diffResult = getDiffManager().diff(previous, mergeResult.getMerged(), null);
        unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult);
        assertEquals(1, unifiedDiffBlocks.size());

        unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(7, unifiedDiffBlocks.size());
    }

    @Test
    void displayWithConflicts6() throws Exception
    {
        String directory = "integration8";
        List<String> previous = readLines(String.format("%s/previous.txt", directory));
        List<String> current = readLines(String.format("%s/current.txt", directory));
        List<String> next = readLines(String.format("%s/next.txt", directory));
        MergeResult<String> mergeResult = getDiffManager().merge(previous, next, current, null);
        assertEquals(6, mergeResult.getConflicts().size());

        DiffResult<String> diffResult = getDiffManager().diff(current, mergeResult.getMerged(), null);
        List<UnifiedDiffBlock<String, Object>> unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult);
        assertEquals(0, unifiedDiffBlocks.size());

        unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(0, unifiedDiffBlocks.size());

        diffResult = getDiffManager().diff(previous, mergeResult.getMerged(), null);
        unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult);
        assertEquals(1, unifiedDiffBlocks.size());

        unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(13, unifiedDiffBlocks.size());
    }

    @Test
    void displayWithConflicts7() throws Exception
    {
        String directory = "integration9";
        List<String> previous = readLines(String.format("%s/previous.txt", directory));
        List<String> current = readLines(String.format("%s/current.txt", directory));
        List<String> next = readLines(String.format("%s/next.txt", directory));
        MergeResult<String> mergeResult = getDiffManager().merge(previous, next, current, null);
        assertEquals(5, mergeResult.getConflicts().size());

        DiffResult<String> diffResult = getDiffManager().diff(current, mergeResult.getMerged(), null);
        List<UnifiedDiffBlock<String, Object>> unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult);
        assertEquals(1, unifiedDiffBlocks.size());

        unifiedDiffBlocks = this.defaultUnifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(6, unifiedDiffBlocks.size());
    }
}
