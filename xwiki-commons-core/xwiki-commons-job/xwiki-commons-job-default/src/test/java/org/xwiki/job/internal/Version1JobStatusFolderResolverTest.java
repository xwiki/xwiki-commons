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
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.job.JobException;
import org.xwiki.job.JobManagerConfiguration;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Version1JobStatusFolderResolver}.
 *
 * @version $Id$
 */
@ComponentTest
class Version1JobStatusFolderResolverTest
{
    @InjectMockComponents
    private Version1JobStatusFolderResolver resolver;

    @MockComponent
    private JobManagerConfiguration configuration;

    private File storageDir;

    @BeforeEach
    void setup()
    {
        this.storageDir = new File("target/test/storage");
        when(this.configuration.getStorage()).thenReturn(this.storageDir);
    }

    @Test
    void getFolderWithNullId() throws JobException
    {
        assertEquals(this.storageDir, this.resolver.getFolder(null));
    }

    @Test
    void getFolderWithEmptyId() throws JobException
    {
        assertEquals(this.storageDir, this.resolver.getFolder(Collections.emptyList()));
    }

    @Test
    void getFolderWithSingleElementId() throws JobException
    {
        List<String> id = Collections.singletonList("element");
        File expected = new File(this.storageDir, "element");
        assertEquals(expected, this.resolver.getFolder(id));
    }

    @Test
    void getFolderWithMultipleElementId() throws JobException
    {
        List<String> id = Arrays.asList("first", "second", "third");
        File expected = new File(new File(new File(this.storageDir, "first"), "second"), "third");
        assertEquals(expected, this.resolver.getFolder(id));
    }

    @Test
    void getFolderWithNullElementInId() throws JobException
    {
        List<String> id = Arrays.asList("first", null, "third");
        File expected = new File(new File(new File(this.storageDir, "first"), "&null"), "third");
        assertEquals(expected, this.resolver.getFolder(id));
    }

    @Test
    void getFolderWithSpecialCharactersInId() throws JobException
    {
        List<String> id = Arrays.asList("a/b", "c?d", "e&f");
        File expected = new File(new File(new File(this.storageDir, "a%2Fb"), "c%3Fd"), "e%26f");
        assertEquals(expected, this.resolver.getFolder(id));
    }

    @Test
    void getFolderWithParentReferenceInId()
    {
        // URL encoding leaves ".." untouched, so this encoding is the one that actually needs the protection of the
        // base class.
        JobException exception =
            assertThrows(JobException.class, () -> this.resolver.getFolder(Collections.singletonList("..")));

        assertEquals("The job id element [..] is going outside its parent folder", exception.getMessage());
    }

    @Test
    void getFolderWithParentReferenceAmongOtherElementsInId()
    {
        List<String> id = Arrays.asList("first", "..", "third");

        JobException exception = assertThrows(JobException.class, () -> this.resolver.getFolder(id));

        assertEquals("The job id element [..] is going outside its parent folder", exception.getMessage());
    }

    @Test
    void getFolderWithSeparatorsAroundParentReferenceInId() throws JobException
    {
        // The separators are encoded, so the whole element stays a single folder name and doesn't escape.
        List<String> id = Collections.singletonList("../../etc");
        File expected = new File(this.storageDir, "..%2F..%2Fetc");
        assertEquals(expected, this.resolver.getFolder(id));
    }

    @Test
    void getFolderWithSpacesInId() throws JobException
    {
        List<String> id = Collections.singletonList("element with spaces");
        File expected = new File(this.storageDir, "element+with+spaces");
        assertEquals(expected, this.resolver.getFolder(id));
    }
}
