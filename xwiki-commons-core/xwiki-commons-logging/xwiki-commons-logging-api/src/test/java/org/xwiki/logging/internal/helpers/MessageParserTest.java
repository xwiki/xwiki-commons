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

import org.junit.Assert;
import org.junit.Test;
import org.xwiki.logging.internal.helpers.MessageParser.MessageIndex;

/**
 * Validate {@link MessageParser}.
 * 
 * @version $Id$
 */
public class MessageParserTest
{
    @Test
    public void testPlain()
    {
        MessageParser parser = new MessageParser("plain text", false);

        Assert.assertEquals("plain text", parser.next().getString());
        Assert.assertNull(parser.next());

        parser = new MessageParser("plain text", true);

        Assert.assertEquals("plain text", parser.next().getString());
        Assert.assertNull(parser.next());
    }

    @Test
    public void testLogWithTwoArguments()
    {
        MessageParser parser = new MessageParser("hello {} world {}", false);

        Assert.assertEquals("hello ", parser.next().getString());
        Assert.assertEquals("{}", parser.next().getString());
        Assert.assertEquals(0, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        Assert.assertEquals(" world ", parser.next().getString());
        Assert.assertEquals("{}", parser.next().getString());
        Assert.assertEquals(1, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        Assert.assertNull(parser.next());

        parser = new MessageParser("hello {} world {}", true);

        Assert.assertEquals("hello ", parser.next().getString());
        Assert.assertEquals("{}", parser.next().getString());
        Assert.assertEquals(0, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        Assert.assertEquals(" world ", parser.next().getString());
        Assert.assertEquals("{}", parser.next().getString());
        Assert.assertEquals(1, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        Assert.assertNull(parser.next());
    }

    @Test
    public void testLogWithEscaping()
    {
        MessageParser parser = new MessageParser("one \\{} two \\\\{} three \\\\\\{}", false);

        Assert.assertEquals("one {} two \\", parser.next().getString());
        Assert.assertEquals("\\{}", parser.next().getString());
        Assert.assertEquals(0, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        Assert.assertEquals(" three \\\\", parser.next().getString());
        Assert.assertEquals("\\{}", parser.next().getString());
        Assert.assertEquals(1, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        Assert.assertNull(parser.next());

        parser = new MessageParser("one \\{} two \\\\{} three \\\\\\{}", true);

        Assert.assertEquals("one {} two \\", parser.next().getString());
        Assert.assertEquals("\\{}", parser.next().getString());
        Assert.assertEquals(0, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        Assert.assertEquals(" three \\\\", parser.next().getString());
        Assert.assertEquals("\\{}", parser.next().getString());
        Assert.assertEquals(1, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        Assert.assertNull(parser.next());
    }

    @Test
    public void testLogWithIndices()
    {
        MessageParser parser;

        parser = new MessageParser("one {1} zero {0} two {}", false);

        Assert.assertEquals("one {1} zero {0} two ", parser.next().getString());
        Assert.assertEquals("{}", parser.next().getString());
        Assert.assertEquals(0, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        Assert.assertNull(parser.next());

        parser = new MessageParser("one {1} zero {0} two {}", true);

        Assert.assertEquals("one ", parser.next().getString());
        Assert.assertEquals("{1}", parser.next().getString());
        Assert.assertEquals(1, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        Assert.assertEquals(" zero ", parser.next().getString());
        Assert.assertEquals("{0}", parser.next().getString());
        Assert.assertEquals(0, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        Assert.assertEquals(" two ", parser.next().getString());
        Assert.assertEquals("{}", parser.next().getString());
        Assert.assertEquals(2, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        Assert.assertNull(parser.next());
    }
}
