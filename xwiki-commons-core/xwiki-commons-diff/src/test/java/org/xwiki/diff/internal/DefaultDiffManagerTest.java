package org.xwiki.diff.internal;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.xwiki.diff.Delta.TYPE;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.DiffResult;
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

        // Empty

        result = this.diffManager.diff(Arrays.asList(""), Arrays.asList(""), null);

        Assert.assertTrue(result.getPatch().isEmpty());

        // Equals

        result = this.diffManager.diff(Arrays.asList("equals"), Arrays.asList("equals"), null);

        Assert.assertTrue(result.getPatch().isEmpty());

        // Previous empty

        result = this.diffManager.diff(Arrays.asList(""), Arrays.asList("next"), null);

        Assert.assertEquals(1, result.getPatch().size());
        Assert.assertEquals(TYPE.INSERT, result.getPatch().get(0).getType());

        // Next empty

        result = this.diffManager.diff(Arrays.asList("previous"), Arrays.asList(""), null);

        Assert.assertEquals(1, result.getPatch().size());
        Assert.assertEquals(TYPE.DELETE, result.getPatch().get(0).getType());
    }

    @Test
    public void testDiffCharList() throws DiffException
    {
        DiffResult<String> result = this.diffManager.diff(Arrays.asList(""), Arrays.asList(""), null);

        Assert.assertTrue(result.getPatch().isEmpty());
    }

    @Test
    public void testMergeStringList() throws DiffException
    {
        DiffResult<String> result = this.diffManager.diff(Arrays.asList(""), Arrays.asList(""), null);

        Assert.assertTrue(result.getPatch().isEmpty());
    }

    @Test
    public void testMergeCharList() throws DiffException
    {
        DiffResult<String> result = this.diffManager.diff(Arrays.asList(""), Arrays.asList(""), null);

        Assert.assertTrue(result.getPatch().isEmpty());
    }
}
