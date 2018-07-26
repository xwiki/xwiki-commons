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

import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.xwiki.logging.event.LogEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test {@link LogTree}.
 *
 * @version $Id$
 */
public class LogTreeTest
{
    @Test
    public void twoLevel0()
    {
        LogTree logTree = new LogTree();

        logTree.error("message1");
        logTree.error("message2");

        assertEquals(2, logTree.size(false));
        assertEquals(2, logTree.size(true));
    }

    @Test
    public void ThreeLevels()
    {
        LogTree logTree = new LogTree();

        logTree.error(LogEvent.MARKER_BEGIN, "begin1");
        logTree.error("message11");
        logTree.error(LogEvent.MARKER_BEGIN, "begin12");
        logTree.error("message121");
        logTree.error("message122");
        logTree.error(LogEvent.MARKER_END, "end12");
        logTree.error(LogEvent.MARKER_END, "end1");

        logTree.error(LogEvent.MARKER_BEGIN, "begin2");
        logTree.error("message21");
        logTree.error(LogEvent.MARKER_BEGIN, "begin22");
        logTree.error("message221");
        logTree.error("message222");
        logTree.error(LogEvent.MARKER_END, "end22");
        logTree.error(LogEvent.MARKER_END, "end2");

        assertEquals(2, logTree.size(false));
        assertEquals(14, logTree.size(true));

        Iterator<LogEvent> iterator0 = logTree.iterator();

        LogTreeNode node1 = (LogTreeNode) iterator0.next();

        assertEquals(3, node1.size(false));
        assertEquals(6, node1.size(true));

        Iterator<LogEvent> iterator1 = node1.iterator();

        iterator1.next();

        LogTreeNode node11 = (LogTreeNode) iterator1.next();

        assertEquals(3, node11.size(false));
        assertEquals(3, node11.size(true));
    }
}
