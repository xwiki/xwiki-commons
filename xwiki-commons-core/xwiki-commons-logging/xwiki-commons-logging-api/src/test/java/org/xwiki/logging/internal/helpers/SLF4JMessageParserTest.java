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
import org.xwiki.logging.internal.helpers.AbstractMessageParser.MessageIndex;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Validate {@link SLF4JMessageParser}.
 *
 * @version $Id$
 */
class SLF4JMessageParserTest
{
    @Test
    void plain()
    {
        AbstractMessageParser parser = new SLF4JMessageParser("plain text");

        assertEquals("plain text", parser.next().getString());
        assertNull(parser.next());
    }

    @Test
    void logWithTwoArguments()
    {
        AbstractMessageParser parser = new SLF4JMessageParser("hello {} world {}");

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
        AbstractMessageParser parser;

        parser = new SLF4JMessageParser("one \\ two");

        assertEquals("one \\ two", parser.next().getString());
        assertNull(parser.next());

        parser = new SLF4JMessageParser("one \\{} two {}");

        assertEquals("one {} two ", parser.next().getString());
        assertEquals("{}", parser.next().getString());
        assertEquals(0, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        assertNull(parser.next());

        parser = new SLF4JMessageParser("one \\{} two \\\\{} three \\\\\\{}");

        assertEquals("one {} two \\", parser.next().getString());
        assertEquals("{}", parser.next().getString());
        assertEquals(0, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        assertEquals(" three \\\\", parser.next().getString());
        assertEquals("{}", parser.next().getString());
        assertEquals(1, ((MessageIndex) parser.getCurrentMessageElement()).getIndex());
        assertNull(parser.next());
    }
}
