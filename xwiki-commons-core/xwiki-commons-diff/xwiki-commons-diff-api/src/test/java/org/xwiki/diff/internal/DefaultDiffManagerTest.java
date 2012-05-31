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

import java.util.Arrays;
import java.util.Collections;

import junit.framework.Assert;

import org.junit.Test;
import org.xwiki.diff.Delta.Type;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.MergeResult;
import org.xwiki.test.AbstractComponentTestCase;

public class DefaultDiffManagerTest extends AbstractComponentTestCase
{
    private DiffManager diffManager;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        this.diffManager = getComponentManager().getInstance(DiffManager.class);
    }

    @Test
    public void testDiffStringList() throws DiffException
    {
        // Null

        DiffResult<String> result = this.diffManager.diff(null, null, null);

        Assert.assertTrue(result.getPatch().isEmpty());
        Assert.assertTrue(result.getUnifiedDiff().isEmpty());

        // Empty

        result = this.diffManager.diff(Collections.<String> emptyList(), Collections.<String> emptyList(), null);

        Assert.assertTrue(result.getPatch().isEmpty());
        Assert.assertTrue(result.getUnifiedDiff().isEmpty());

        // Equals

        result = this.diffManager.diff(Arrays.asList("equals"), Arrays.asList("equals"), null);

        Assert.assertTrue(result.getPatch().isEmpty());
        Assert.assertEquals(1, result.getUnifiedDiff().size());

        // Previous empty

        result = this.diffManager.diff(Collections.<String> emptyList(), Arrays.asList("next"), null);

        Assert.assertEquals(1, result.getPatch().size());
        Assert.assertEquals(Type.INSERT, result.getPatch().get(0).getType());
        Assert.assertEquals(Arrays.asList("next"), result.getPatch().get(0).getNext().getElements());
        Assert.assertEquals(0, result.getPatch().get(0).getNext().getIndex());
        Assert.assertEquals(1, result.getUnifiedDiff().size());

        // Next empty

        result = this.diffManager.diff(Arrays.asList("previous"), Collections.<String> emptyList(), null);

        Assert.assertEquals(1, result.getPatch().size());
        Assert.assertEquals(Type.DELETE, result.getPatch().get(0).getType());
        Assert.assertEquals(Arrays.asList("previous"), result.getPatch().get(0).getPrevious().getElements());
        Assert.assertEquals(0, result.getPatch().get(0).getPrevious().getIndex());
        Assert.assertEquals(1, result.getUnifiedDiff().size());
    }

    @Test
    public void testDiffCharList() throws DiffException
    {
        // Equals

        DiffResult<Character> result = this.diffManager.diff(Arrays.asList('a'), Arrays.asList('a'), null);

        Assert.assertTrue(result.getPatch().isEmpty());

        // Changed

        result = this.diffManager.diff(Arrays.asList('a'), Arrays.asList('b'), null);

        Assert.assertEquals(1, result.getPatch().size());
        Assert.assertEquals(Type.CHANGE, result.getPatch().get(0).getType());
    }

    @Test
    public void testMergeStringList() throws DiffException
    {
        // Only new

        MergeResult<String> result =
            this.diffManager.merge(Arrays.asList("some content"), Arrays.asList("some new content"),
                Arrays.asList("some content"), null);

        Assert.assertEquals(Arrays.asList("some new content"), result.getMerged());

        // New after

        result =
            this.diffManager.merge(Arrays.asList("some content"), Arrays.asList("some content", "after"),
                Arrays.asList("some content"), null);

        Assert.assertEquals(Arrays.asList("some content", "after"), result.getMerged());

        // Before and after

        result =
            this.diffManager.merge(Arrays.asList("some content"), Arrays.asList("before", "some content"),
                Arrays.asList("some content", "after"), null);

        Assert.assertEquals(Arrays.asList("before", "some content", "after"), result.getMerged());

        // After and before

        result =
            this.diffManager.merge(Arrays.asList("some content"), Arrays.asList("some content", "after"),
                Arrays.asList("before", "some content"), null);

        Assert.assertEquals(Arrays.asList("before", "some content", "after"), result.getMerged());
    }

    @Test
    public void testMergeCharList() throws DiffException
    {
        // New before

        MergeResult<Character> result =
            this.diffManager
                .merge(Arrays.asList('b', 'c'), Arrays.asList('a', 'b', 'c'), Arrays.asList('b', 'c'), null);

        Assert.assertEquals(Arrays.asList('a', 'b', 'c'), result.getMerged());

        // New after

        result =
            this.diffManager
                .merge(Arrays.asList('a', 'b'), Arrays.asList('a', 'b', 'c'), Arrays.asList('a', 'b'), null);

        Assert.assertEquals(Arrays.asList('a', 'b', 'c'), result.getMerged());

        // New middle

        result =
            this.diffManager
                .merge(Arrays.asList('a', 'c'), Arrays.asList('a', 'b', 'c'), Arrays.asList('a', 'c'), null);

        Assert.assertEquals(Arrays.asList('a', 'b', 'c'), result.getMerged());

        // Before and after

        result = this.diffManager.merge(Arrays.asList('b'), Arrays.asList('a', 'b'), Arrays.asList('b', 'c'), null);

        Assert.assertEquals(Arrays.asList('a', 'b', 'c'), result.getMerged());

        // After and before

        result = this.diffManager.merge(Arrays.asList('b'), Arrays.asList('b', 'c'), Arrays.asList('a', 'b'), null);

        Assert.assertEquals(Arrays.asList('a', 'b', 'c'), result.getMerged());
    }
}
