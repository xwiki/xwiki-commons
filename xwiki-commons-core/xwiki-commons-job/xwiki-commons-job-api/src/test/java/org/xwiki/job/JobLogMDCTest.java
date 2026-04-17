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
package org.xwiki.job;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link JobLogMDC}.
 *
 * @version $Id$
 */
class JobLogMDCTest
{
    @Test
    void toId()
    {
        assertNull(JobLogMDC.toId(null));
        assertNull(JobLogMDC.toId(List.of()));
        assertEquals("wiki/page", JobLogMDC.toId(List.of("wiki", "page")));
        assertEquals("null/page", JobLogMDC.toId(Arrays.asList(null, "page")));
    }

    @Test
    void toCleanId()
    {
        String cleanId = JobLogMDC.toCleanId(List.of("Parent Job", "Space/Slash"));

        assertEquals("%50arent%20%4Aob%2F%53pace%2F%53lash", cleanId);
    }

    @Test
    void toCleanIdWithLongValue()
    {
        String cleanId = JobLogMDC.toCleanId(List.of("a".repeat(400)));

        assertNotNull(cleanId);
        assertEquals(200, cleanId.length());
        assertTrue(cleanId.startsWith("a"));
        assertTrue(cleanId.contains("-"));
    }
}
