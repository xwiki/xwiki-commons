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

import org.junit.jupiter.api.Test;
import org.xwiki.logging.internal.helpers.MessageParser.MessageIndex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Validate {@link MessageParser}.
 *
 * @version $Id$
 */
public class MessageParserTest
{
    @Test
    void plain()
    {
        MessageParser parser = new MessageParser("plain text", false);

        assertEquals("plain text", parser.next().getString());
        assertNull(parser.next());

        parser = new MessageParser("plain text", true);

        assertEquals("plain text", parser.next().getString());
        assertNull(parser.next());
    }

    @Test
    void logWithTwoArguments()
    {
        MessageParser parser = new MessageParser("hello {} world {}", false);

        assertEquals("hello ", parser.next().getString());
        assertEquals("{}", parser.next().getString());
        assertEquals(0, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        assertEquals(" world ", parser.next().getString());
        assertEquals("{}", parser.next().getString());
        assertEquals(1, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        assertNull(parser.next());

        parser = new MessageParser("hello {} world {}", true);

        assertEquals("hello ", parser.next().getString());
        assertEquals("{}", parser.next().getString());
        assertEquals(0, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        assertEquals(" world ", parser.next().getString());
        assertEquals("{}", parser.next().getString());
        assertEquals(1, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        assertNull(parser.next());
    }

    @Test
    void logWithEscaping()
    {
        MessageParser parser = new MessageParser("one \\{} two \\\\{} three \\\\\\{}", false);

        assertEquals("one {} two \\", parser.next().getString());
        assertEquals("\\{}", parser.next().getString());
        assertEquals(0, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        assertEquals(" three \\\\", parser.next().getString());
        assertEquals("\\{}", parser.next().getString());
        assertEquals(1, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        assertNull(parser.next());

        parser = new MessageParser("one \\{} two \\\\{} three \\\\\\{}", true);

        assertEquals("one {} two \\", parser.next().getString());
        assertEquals("\\{}", parser.next().getString());
        assertEquals(0, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        assertEquals(" three \\\\", parser.next().getString());
        assertEquals("\\{}", parser.next().getString());
        assertEquals(1, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        assertNull(parser.next());
    }

    @Test
    void logWithIndices()
    {
        MessageParser parser;

        parser = new MessageParser("one {1} zero {0} two {}", false);

        assertEquals("one {1} zero {0} two ", parser.next().getString());
        assertEquals("{}", parser.next().getString());
        assertEquals(0, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        assertNull(parser.next());

        parser = new MessageParser("one {1} zero {0} two {}", true);

        assertEquals("one ", parser.next().getString());
        assertEquals("{1}", parser.next().getString());
        assertEquals(1, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        assertEquals(" zero ", parser.next().getString());
        assertEquals("{0}", parser.next().getString());
        assertEquals(0, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        assertEquals(" two ", parser.next().getString());
        assertEquals("{}", parser.next().getString());
        assertEquals(2, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        assertNull(parser.next());
    }
}
