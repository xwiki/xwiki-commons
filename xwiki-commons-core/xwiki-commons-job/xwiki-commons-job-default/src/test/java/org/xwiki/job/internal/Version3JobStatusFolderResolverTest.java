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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xwiki.job.JobManagerConfiguration;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.text.StringUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Version3JobStatusFolderResolver}.
 *
 * @version $Id$
 */
@ComponentTest
class Version3JobStatusFolderResolverTest
{
    private static final String ELEMENT_1 = "element";

    private static final String FIRST = "first";

    private static final String SECOND = "second";

    private static final String THIRD = "third";

    @InjectMockComponents
    private Version3JobStatusFolderResolver resolver;

    @MockComponent
    private JobManagerConfiguration configuration;

    private File baseDir;

    @BeforeEach
    void setup()
    {
        File storageDir = new File("target/test/storage");
        this.baseDir = new File(storageDir, "3");
        when(this.configuration.getStorage()).thenReturn(storageDir);
    }

    @Test
    void getFolderWithNullId()
    {
        assertEquals(this.baseDir, this.resolver.getFolder(null));
    }

    @Test
    void getFolderWithEmptyId()
    {
        assertEquals(this.baseDir, this.resolver.getFolder(Collections.emptyList()));
    }

    @Test
    void getFolderWithSingleElementId()
    {
        List<String> idElements = Collections.singletonList(ELEMENT_1);
        File expected = getExpectedFolder(idElements);
        assertEquals(expected, this.resolver.getFolder(idElements));
    }

    @Test
    void getFolderWithMultipleElementId()
    {
        List<String> idElements = Arrays.asList(FIRST, SECOND, THIRD);
        File expected = getExpectedFolder(idElements);
        assertEquals(expected, this.resolver.getFolder(idElements));
    }

    @Test
    void getFolderWithNullElementInId()
    {
        List<String> id = Arrays.asList(FIRST, null, THIRD);
        File expected = getExpectedFolder(List.of(FIRST, "&null", THIRD));
        assertEquals(expected, this.resolver.getFolder(id));
    }

    @Test
    void getFolderWithLongIdElement()
    {
        // Create a string that exceeds 255 characters
        String element = "a".repeat(400);
        List<String> id = List.of(element);
        File expected = getExpectedFolder(List.of(element.substring(0, 250), element.substring(250)));

        assertEquals(expected, this.resolver.getFolder(id));
    }

    @ParameterizedTest
    @CsvSource({
        "file*.txt, file%2A.txt",
        ".hidden, %2Ehidden",
        "filename., filename%2E",
        ".filename., %2Efilename%2E",
        "file.name, file.name",
        "näme, n%C3%A4me",
        "/?&, %2F%3F%26",
        "space inside, space%20inside",
        "CamelCase, %43amel%43ase"
    })
    void getFolderWithSpecialCharactersInId(String idElement, String expectedEncodedElement)
    {
        List<String> id = Collections.singletonList(idElement);
        File expected = new File(this.baseDir, expectedEncodedElement);
        assertEquals(expected, this.resolver.getFolder(id));
    }

    @Test
    void getFolderWithLongIdElementContainingSpecialChars()
    {
        // Create a string that will exceed 255 characters when encoded and contains special characters
        String element = "a".repeat(230) + "*/" + ".".repeat(195);

        List<String> id = List.of(element);

        File folder = this.resolver.getFolder(id);
        List<String> encodedElements = new ArrayList<>();
        for (File cur = folder; !this.baseDir.equals(cur); cur = cur.getParentFile()) {
            encodedElements.add(cur.getName());
        }

        for (String encodedElement : encodedElements) {
            assertFalse(StringUtils.startsWith(encodedElement, "."));
            assertFalse(StringUtils.endsWith(encodedElement, "."));
            assertFalse(StringUtils.contains(encodedElement, "/"));
            assertFalse(StringUtils.contains(encodedElement, "*"));
            assertTrue(encodedElement.length() <= 255);
        }
    }

    private File getExpectedFolder(List<String> idElements)
    {
        File expected = this.baseDir;
        for (String element : idElements) {
            expected = new File(expected, element);
        }
        return expected;
    }
}
