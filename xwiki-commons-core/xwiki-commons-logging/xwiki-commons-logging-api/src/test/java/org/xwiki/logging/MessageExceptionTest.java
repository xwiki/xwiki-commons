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
package org.xwiki.logging;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.logging.marker.TranslationMarker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Validate {@link AbstractMessageException}.
 * 
 * @version $Id$
 */
class MessageExceptionTest
{
    @Test
    void create()
    {
        Exception exception = new Exception();

        AbstractMessageException messageException1 =
            new AbstractMessageException("translation", "message", "argument1", exception)
            {
            };

        AbstractMessageException messageException2 = new AbstractMessageException(new TranslationMarker("translation"),
            "message", new Object[] {"argument1"}, exception)
        {
        };

        assertEquals(messageException1.getParameterizedMessage(), messageException2.getParameterizedMessage());

        assertSame(exception, messageException2.getParameterizedMessage().getThrowable());
        assertEquals("message", messageException2.getParameterizedMessage().getMessage());
        assertEquals(List.of("argument1"), List.of(messageException2.getParameterizedMessage().getArgumentArray()));
    }
}
