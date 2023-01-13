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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link UnifiedDiffBlock}
 *
 * @version $Id$
 */
class UnifiedDiffBlockTest
{
    private List<Character> stringToListChar(String string)
    {
        return Arrays.asList(ArrayUtils.toObject(string.toCharArray()));
    }

    private UnifiedDiffBlock<String, Character> getUnidifiedDiffBlock()
    {
        UnifiedDiffBlock<String, Character> block = new UnifiedDiffBlock<>();
        UnifiedDiffElement<String, Character> element;
        InlineDiffChunk<Character> chunk;
        List<InlineDiffChunk<Character>> chunks;

        element = new UnifiedDiffElement<>(14, UnifiedDiffElement.Type.CONTEXT, "Lorem ipsum dolor sit amet, "
            + "consectetur");
        block.add(element);

        element = new UnifiedDiffElement<>(15, UnifiedDiffElement.Type.CONTEXT, "");
        block.add(element);

        element = new UnifiedDiffElement<>(16, UnifiedDiffElement.Type.DELETED, "== Sub-paragraph ==");
        chunks = new ArrayList<>();
        chunk = new InlineDiffChunk<>(InlineDiffChunk.Type.DELETED, this.stringToListChar("== "));
        chunks.add(chunk);
        chunk = new InlineDiffChunk<>(InlineDiffChunk.Type.UNMODIFIED, this.stringToListChar("S"));
        chunks.add(chunk);
        chunk = new InlineDiffChunk<>(InlineDiffChunk.Type.DELETED, this.stringToListChar("ub-"));
        chunks.add(chunk);
        chunk = new InlineDiffChunk<>(InlineDiffChunk.Type.UNMODIFIED, this.stringToListChar("paragraph"));
        chunks.add(chunk);
        chunk = new InlineDiffChunk<>(InlineDiffChunk.Type.DELETED, this.stringToListChar(" =="));
        chunks.add(chunk);
        element.setChunks(chunks);
        block.add(element);

        element = new UnifiedDiffElement<>(18, UnifiedDiffElement.Type.ADDED, "Some changes in this paragraph.");
        chunks = new ArrayList<>();
        chunk = new InlineDiffChunk<>(InlineDiffChunk.Type.UNMODIFIED, this.stringToListChar("S"));
        chunks.add(chunk);
        chunk = new InlineDiffChunk<>(InlineDiffChunk.Type.ADDED, this.stringToListChar("ome changes in this "));
        chunks.add(chunk);
        chunk = new InlineDiffChunk<>(InlineDiffChunk.Type.UNMODIFIED, this.stringToListChar("paragraph"));
        chunks.add(chunk);
        chunk = new InlineDiffChunk<>(InlineDiffChunk.Type.ADDED, this.stringToListChar("."));
        chunks.add(chunk);
        element.setChunks(chunks);
        block.add(element);

        element = new UnifiedDiffElement<>(17, UnifiedDiffElement.Type.CONTEXT, "");
        block.add(element);

        element = new UnifiedDiffElement<>(18, UnifiedDiffElement.Type.DELETED,
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit");
        block.add(element);

        element = new UnifiedDiffElement<>(19, UnifiedDiffElement.Type.DELETED, "");
        block.add(element);

        return block;
    }

    @Test
    void equals()
    {
        assertEquals(getUnidifiedDiffBlock(), getUnidifiedDiffBlock());
    }

    @Test
    void hashcode()
    {
        assertEquals(getUnidifiedDiffBlock().hashCode(), getUnidifiedDiffBlock().hashCode());
    }

    @Test
    void getPreviousStart()
    {
        assertEquals(14, getUnidifiedDiffBlock().getPreviousStart());
    }

    @Test
    void getPreviousSize()
    {
        assertEquals(6, getUnidifiedDiffBlock().getPreviousSize());
    }

    @Test
    void getNextStart()
    {
        assertEquals(14, getUnidifiedDiffBlock().getNextStart());
    }

    @Test
    void getNextSize()
    {
        assertEquals(4, getUnidifiedDiffBlock().getNextSize());
    }
}