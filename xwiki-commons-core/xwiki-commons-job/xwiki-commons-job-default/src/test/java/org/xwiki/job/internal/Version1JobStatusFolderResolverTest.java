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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.job.JobManagerConfiguration;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    void getFolderWithNullId()
    {
        assertEquals(this.storageDir, this.resolver.getFolder(null));

        assertEquals(List.of(), this.resolver.getFolderSegments(null));
    }

    @Test
    void getFolderWithEmptyId()
    {
        assertEquals(this.storageDir, this.resolver.getFolder(List.of()));

        assertEquals(List.of(), this.resolver.getFolderSegments(List.of()));
    }

    @Test
    void getFolderWithSingleElementId()
    {
        List<String> id = List.of("element");
        File expected = new File(this.storageDir, "element");
        assertEquals(expected, this.resolver.getFolder(id));

        assertEquals(id, this.resolver.getFolderSegments(id));
    }

    @Test
    void getFolderWithMultipleElementId()
    {
        List<String> id = List.of("first", "second", "third");
        File expected = new File(new File(new File(this.storageDir, "first"), "second"), "third");
        assertEquals(expected, this.resolver.getFolder(id));

        assertEquals(id, this.resolver.getFolderSegments(id));
    }

    @Test
    void getFolderWithNullElementInId()
    {
        List<String> id = Arrays.asList("first", null, "third");
        File expected = new File(new File(new File(this.storageDir, "first"), "&null"), "third");
        assertEquals(expected, this.resolver.getFolder(id));

        assertEquals(List.of("first", "&null", "third"), this.resolver.getFolderSegments(id));
    }

    @Test
    void getFolderWithSpecialCharactersInId()
    {
        List<String> id = List.of("a/b", "c?d", "e&f");
        File expected = new File(new File(new File(this.storageDir, "a%2Fb"), "c%3Fd"), "e%26f");
        assertEquals(expected, this.resolver.getFolder(id));

        assertEquals(List.of("a%2Fb", "c%3Fd", "e%26f"), this.resolver.getFolderSegments(id));
    }

    @Test
    void getFolderWithSpacesInId()
    {
        List<String> id = List.of("element with spaces");
        File expected = new File(this.storageDir, "element+with+spaces");
        assertEquals(expected, this.resolver.getFolder(id));

        assertEquals(List.of("element+with+spaces"), this.resolver.getFolderSegments(id));
    }
}
