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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.job.JobManagerConfiguration;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link Version2JobStatusFolderResolver}.
 *
 * @version $Id$
 */
@ComponentTest
class Version2JobStatusFolderResolverTest
{
    private static final String ELEMENT = "element";

    private static final String FIRST = "first";

    private static final String SECOND = "second";

    private static final String THIRD = "third";

    private static final String PATH_WITH_SLASH = "a/b";

    private static final String PATH_WITH_QUESTION = "c?d";

    private static final String PATH_WITH_AMPERSAND = "e&f";

    private static final String ACCENTED = "éèêë";

    private static final String CHINESE = "汉字";

    private static final String EMOJI = "😀🙂";

    @InjectMockComponents
    private Version2JobStatusFolderResolver resolver;

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
    }

    @Test
    void getFolderWithEmptyId()
    {
        assertEquals(this.storageDir, this.resolver.getFolder(Collections.emptyList()));
    }

    @Test
    void getFolderWithSingleElementId()
    {
        assertFolderPath(Collections.singletonList(ELEMENT));
    }

    @Test
    void getFolderWithMultipleElementId()
    {
        assertFolderPath(Arrays.asList(FIRST, SECOND, THIRD));
    }

    @Test
    void getFolderWithNullElementInId()
    {
        List<String> id = Arrays.asList(FIRST, null, THIRD);
        String encodedFirst = encodeComponent(FIRST);
        String encodedThird = encodeComponent(THIRD);
        File expected = new File(new File(new File(this.storageDir, encodedFirst), "&null"), encodedThird);
        assertEquals(expected, this.resolver.getFolder(id));
    }

    @Test
    void getFolderWithLongIdElement()
    {
        // Create a string that will exceed 510 characters when Base64 encoded
        String element = "a".repeat(400);
        String encodedElement = encodeComponent(element);

        // This resolver has a bug, it omits the last part of the path. As this resolver is also used to migrate job
        // statuses saved with it, it is essential to keep this bug.
        List<String> encodedElements = List.of(encodedElement.substring(0, 255), encodedElement.substring(255, 510));
        File expected = new File(new File(this.storageDir, encodedElements.get(0)), encodedElements.get(1));

        List<String> id = List.of(element);

        assertEquals(expected, this.resolver.getFolder(id));
    }

    @Test
    void getFolderWithSpecialCharactersInId()
    {
        assertFolderPath(Arrays.asList(PATH_WITH_SLASH, PATH_WITH_QUESTION, PATH_WITH_AMPERSAND));
    }

    @Test
    void getFolderWithNonAsciiCharactersInId()
    {
        assertFolderPath(Arrays.asList(ACCENTED, CHINESE, EMOJI));
    }

    /**
     * Asserts that the resolver correctly builds the folder path for the given ID elements.
     *
     * @param idElements the list of ID elements to test
     */
    private void assertFolderPath(List<String> idElements)
    {
        File expected = this.storageDir;
        for (String element : idElements) {
            expected = new File(expected, encodeComponent(element));
        }
        assertEquals(expected, this.resolver.getFolder(idElements));
    }

    private static String encodeComponent(String component)
    {
        return URLEncoder.encode(Base64.encodeBase64String(component.getBytes()), StandardCharsets.UTF_8);
    }
}
