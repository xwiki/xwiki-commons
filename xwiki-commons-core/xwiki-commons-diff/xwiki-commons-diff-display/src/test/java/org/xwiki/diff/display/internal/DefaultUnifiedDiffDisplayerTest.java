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

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.display.UnifiedDiffBlock;
import org.xwiki.diff.display.UnifiedDiffDisplayer;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * Unit tests for {@link DefaultUnifiedDiffDisplayer}.
 * 
 * @version $Id$
 * @since 4.1M2
 */
@RunWith(Parameterized.class)
public class DefaultUnifiedDiffDisplayerTest extends AbstractComponentTestCase
{
    /**
     * The previous version.
     */
    private final List<String> previous;

    /**
     * The next version.
     */
    private final List<String> next;

    /**
     * The expected unified diff.
     */
    private final String expected;

    /**
     * Creates a new test with the given input and the specified expected output.
     * 
     * @param previous the previous version
     * @param next the next version
     * @param expected the expected unified diff
     */
    public DefaultUnifiedDiffDisplayerTest(List<String> previous, List<String> next, String expected)
    {
        this.previous = previous;
        this.next = next;
        this.expected = expected;
    }

    /**
     * The actual test.
     */
    @Test
    public void execute() throws Exception
    {
        DiffManager diffManager = getComponentManager().getInstance(DiffManager.class);
        UnifiedDiffDisplayer unifiedDiffDisplayer = getComponentManager().getInstance(UnifiedDiffDisplayer.class);
        List<UnifiedDiffBlock<String, Object>> blocks =
            unifiedDiffDisplayer.display(diffManager.diff(previous, next, null));

        StringBuilder actual = new StringBuilder();
        for (UnifiedDiffBlock<String, ? > block : blocks) {
            actual.append(block);
        }
        Assert.assertEquals(expected, actual.toString());
    }

    /**
     * @return the collection of test parameters
     */
    @Parameters
    public static Collection<Object[]> data() throws IOException
    {
        Collection<Object[]> data = new ArrayList<Object[]>();

        //
        // Add special tests.
        //

        // Both previous and next are empty.
        data.add(new Object[] {Collections.<String> emptyList(), Collections.<String> emptyList(), ""});

        // Previous and next are equal.
        List<String> lines = Arrays.asList("one", "two", "three");
        data.add(new Object[] {lines, lines, ""});

        // Previous is empty.
        data.add(new Object[] {Collections.<String> emptyList(), lines, "@@ -1,0 +1,3 @@\n+one\n+two\n+three\n"});

        // Next is empty.
        data.add(new Object[] {lines, Collections.<String> emptyList(), "@@ -1,3 +1,0 @@\n-one\n-two\n-three\n"});

        // Line removed.
        data.add(new Object[] {lines, Arrays.asList(lines.get(0), lines.get(2)),
        "@@ -1,3 +1,2 @@\n one\n-two\n three\n"});

        // Line added.
        data.add(new Object[] {lines, Arrays.asList(lines.get(0), lines.get(1), "between", lines.get(2)),
        "@@ -1,3 +1,4 @@\n one\n two\n+between\n three\n"});

        // Line changed.
        data.add(new Object[] {lines, Arrays.asList(lines.get(0), "Two", lines.get(2)),
        "@@ -1,3 +1,3 @@\n one\n-two\n+Two\n three\n"});

        //
        // Add tests from files.
        //

        List<String> previous = readLines("previous.txt");
        String[] testNames = new String[] {"twoContexts", "sharedContext"};
        for (String testName : testNames) {
            data.add(new Object[] {previous, readLines(testName + ".txt"), readContent(testName + ".diff")});
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
