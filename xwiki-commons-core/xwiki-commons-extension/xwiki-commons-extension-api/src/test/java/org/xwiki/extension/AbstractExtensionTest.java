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
package org.xwiki.extension;

import org.junit.Before;
import org.junit.Test;
import org.xwiki.extension.repository.ExtensionRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

/**
 * @version $Id$
 */
public class AbstractExtensionTest
{
    private AbstractExtension extension;

    private ExtensionRepository repository;

    private ExtensionId id;

    private String type;

    private static class TestExtension extends AbstractExtension
    {
        public TestExtension(ExtensionId id, String type, ExtensionId... features)
        {
            super(null, id, type);

            for (ExtensionId feature : features) {
                addExtensionFeature(feature);
            }
        }
    }

    private AbstractExtension toExtension(String id, String version, ExtensionId... features)
    {
        return new TestExtension(new ExtensionId(id, version), "test", features);
    }

    private void assertCompareTo(int comparizon, Extension e1, Extension e2)
    {
        assertEquals(comparizon, e1.compareTo(e2));
    }

    // Tests

    @Before
    public void before()
    {
        this.repository = mock(ExtensionRepository.class);
        this.id = new ExtensionId("extesionid", "extensionversion");
        this.type = "extensiontype";

        this.extension = new AbstractExtension(this.repository, this.id, this.type)
        {
        };
    }

    @Test
    public void testGet()
    {
        assertSame(this.repository, this.extension.get("repository"));
        assertEquals(this.id.getId(), this.extension.get("id"));
        assertEquals(this.id.getVersion(), this.extension.get("version"));
        assertEquals(this.type, this.extension.get("type"));
    }

    @Test
    public void testCompareTo()
    {
        assertCompareTo(0, toExtension("id", "2.0"), toExtension("id", "2.0"));
        assertCompareTo(-1, toExtension("id", "2.0"), toExtension("id", "3.0"));
        assertCompareTo(1, toExtension("id", "2.0"), toExtension("id", "1.0"));

        assertCompareTo(0, toExtension("feature", "2.0"), toExtension("id", "2.0", new ExtensionId("feature", "2.0")));
        assertCompareTo(-1, toExtension("feature", "2.0"), toExtension("id", "2.0", new ExtensionId("feature", "3.0")));
        assertCompareTo(1, toExtension("feature", "2.0"), toExtension("id", "2.0", new ExtensionId("feature", "1.0")));

        assertCompareTo(0, toExtension("id", "2.0", new ExtensionId("feature", "2.0")), toExtension("feature", "2.0"));
        assertCompareTo(-1, toExtension("id", "2.0", new ExtensionId("feature", "2.0")), toExtension("feature", "3.0"));
        assertCompareTo(1, toExtension("id", "2.0", new ExtensionId("feature", "2.0")), toExtension("feature", "1.0"));

        assertCompareTo(-1, toExtension("id", "1.0"), toExtension("id2", "1.0"));

        assertCompareTo(-1, toExtension("id", "1.0"), null);
    }
}
