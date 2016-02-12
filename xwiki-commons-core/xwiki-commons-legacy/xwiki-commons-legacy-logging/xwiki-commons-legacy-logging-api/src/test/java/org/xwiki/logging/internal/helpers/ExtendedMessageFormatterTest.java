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
package org.xwiki.logging.internal.helpers;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

/**
 * Validate {@link ExtendedMessageFormatter}.
 *
 * @version $Id$
 */
public class ExtendedMessageFormatterTest
{
    @Test
    public void testNullMessage()
    {
        Assert.assertNull(ExtendedMessageFormatter.parseMessage(null, new Object[] {}));
    }

    @Test
    public void testEmptyMessage()
    {
        Assert.assertEquals(Arrays.asList(""), ExtendedMessageFormatter.parseMessage("", new Object[] {}));
    }

    @Test
    public void testNullArguments()
    {
        Assert.assertEquals(Arrays.asList("message"), ExtendedMessageFormatter.parseMessage("message", null));
    }

    @Test
    public void testEmptyArguments()
    {
        Assert
            .assertEquals(Arrays.asList("message"), ExtendedMessageFormatter.parseMessage("message", new Object[] {}));
    }

    @Test
    public void testPlain()
    {
        Assert.assertEquals(Arrays.asList("message"),
            ExtendedMessageFormatter.parseMessage("message", new Object[] { "0" }));
    }

    @Test
    public void testWithArguments()
    {
        Assert.assertEquals(Arrays.asList("hello ", " world"),
            ExtendedMessageFormatter.parseMessage("hello {} world", new Object[] { "0" }));
    }

    @Test
    public void testWithoutArguments()
    {
        Assert.assertEquals(Arrays.asList("hello {} world"),
            ExtendedMessageFormatter.parseMessage("hello {} world", new Object[] {}));
    }

    @Test
    public void testWithEndingArgument()
    {
        Assert.assertEquals(Arrays.asList("hello ", ""),
            ExtendedMessageFormatter.parseMessage("hello {}", new Object[] { "0" }));
    }

    @Test
    public void testWithoutTooMuchArguments()
    {
        Assert.assertEquals(Arrays.asList("hello ", " world"),
            ExtendedMessageFormatter.parseMessage("hello {} world", new Object[] { "0", "1" }));
    }

    @Test
    public void testWithOnlyArguments()
    {
        Assert.assertEquals(Arrays.asList("", "", ""),
            ExtendedMessageFormatter.parseMessage("{}{}", new Object[] { "0", "1" }));
    }
}
