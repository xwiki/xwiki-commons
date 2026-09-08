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
package org.xwiki.job.internal;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xwiki.job.JobException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the protection against job ids escaping the storage folder implemented in
 * {@link AbstractJobStatusFolderResolver}.
 *
 * @version $Id$
 */
class AbstractJobStatusFolderResolverTest
{
    private static final File BASE_FOLDER = new File("target/test/storage");

    /**
     * A resolver that stores each id element as-is, so that the protection of the base class can be tested
     * independently of the encoding applied by a concrete resolver.
     */
    private static final class RawJobStatusFolderResolver extends AbstractJobStatusFolderResolver
    {
        @Override
        protected File getBaseFolder()
        {
            return BASE_FOLDER;
        }

        @Override
        protected File addIDElement(String idElement, File folder)
        {
            return new File(folder, idElement);
        }
    }

    private final AbstractJobStatusFolderResolver resolver = new RawJobStatusFolderResolver();

    @Test
    void getFolderWithIdElementStayingInsideTheBaseFolder() throws JobException
    {
        assertEquals(new File(new File(BASE_FOLDER, "first"), "second"),
            this.resolver.getFolder(Arrays.asList("first", "second")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "..",
        "../..",
        "../sibling"
    })
    void getFolderWithIdElementEscapingTheBaseFolder(String idElement)
    {
        JobException exception = assertThrows(JobException.class, () -> this.resolver.getFolder(List.of(idElement)));

        assertEquals("The job id element [" + idElement + "] is resolved outside its parent folder",
            exception.getMessage());
    }

    @Test
    void getFolderWithEscapingIdElementAfterAValidOne()
    {
        JobException exception =
            assertThrows(JobException.class, () -> this.resolver.getFolder(Arrays.asList("first", "..")));

        assertEquals("The job id element [..] is resolved outside its parent folder", exception.getMessage());
    }
}
