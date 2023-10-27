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
package org.xwiki.extension.version.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.jupiter.api.Test;
import org.xwiki.extension.version.Version;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link DefaultVersion}.
 *
 * @version $Id$
 */
class DefaultVersionTest
{
    private void validateSerialize(Version version) throws IOException, ClassNotFoundException
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(outputStream);
        out.writeObject(version);
        out.close();
        outputStream.close();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        ObjectInputStream in = new ObjectInputStream(inputStream);
        assertEquals(version, in.readObject());
        in.close();
        inputStream.close();
    }

    @Test
    void equal()
    {
        DefaultVersion version = new DefaultVersion("1.0");

        assertFalse(version.equals(null));
        assertTrue(version.equals(version));

        assertEquals(new DefaultVersion("1.1"), new DefaultVersion("1.1"));
        assertEquals(new DefaultVersion("1a"), new DefaultVersion("1-a"));
        assertEquals(new DefaultVersion("1m2"), new DefaultVersion("1-m-2"));
        assertEquals(new DefaultVersion("1M2"), new DefaultVersion("1-M-2"));
        assertEquals(new DefaultVersion("1-M2"), new DefaultVersion("1-M-2"));
        assertEquals(new DefaultVersion("1a2"), new DefaultVersion("1-alpha-2"));
        assertEquals(new DefaultVersion("1b2"), new DefaultVersion("1-beta-2"));
        assertEquals(new DefaultVersion("1m2"), new DefaultVersion("1-milestone-2"));
    }

    @Test
    void compareTo()
    {
        assertEquals(0, new DefaultVersion("1.1").compareTo(new DefaultVersion("1.1")));
        assertEquals(0, new DefaultVersion("1.1").compareTo(new DefaultVersion("1.1.0")));
        assertEquals(0, new DefaultVersion("1.1").compareTo(new DefaultVersion("1.1.")));
        assertEquals(0, new DefaultVersion("1.1").compareTo(new DefaultVersion("1.1ga")));
        assertEquals(0, new DefaultVersion("1.1").compareTo(new DefaultVersion("1.1final")));
        assertTrue(new DefaultVersion("1.2").compareTo(new DefaultVersion("1.1")) > 0);
        assertTrue(new DefaultVersion("1.1").compareTo(new DefaultVersion("1.2")) < 0);

        assertTrue(new DefaultVersion("1.1").compareTo(new DefaultVersion("1.1w")) < 0);

        assertTrue(new DefaultVersion("1.1").compareTo(new DefaultVersion("1.1-milestone-1")) > 0);
        assertTrue(new DefaultVersion("1.1.1").compareTo(new DefaultVersion("1.1-milestone-1")) > 0);

        assertTrue(new DefaultVersion("1.1-SNAPSHOT").compareTo(new DefaultVersion("1.1-20230702.094921-16")) > 0);
        assertTrue(
            new DefaultVersion("1.1-20230702.094921-16").compareTo(new DefaultVersion("1.1-20230702.094922-16")) < 0);
        assertTrue(
            new DefaultVersion("1.1-20230702.094922-16").compareTo(new DefaultVersion("1.1-20230702.094921-16")) > 0);
    }

    @Test
    void getType()
    {
        assertEquals(Version.Type.SNAPSHOT, new DefaultVersion("1.1-SNAPSHOT").getType());
        assertEquals(Version.Type.SNAPSHOT, new DefaultVersion("1.1-SNAPSHOT-1").getType());
        assertEquals(Version.Type.SNAPSHOT, new DefaultVersion("1.1-SNAPSHOT-20230702.094921-16").getType());
        assertEquals(Version.Type.SNAPSHOT, new DefaultVersion("1.1-20230702.094921-16").getType());
        assertEquals(Version.Type.SNAPSHOT, new DefaultVersion("1.1-20230705.143513-0").getType());
        assertEquals(Version.Type.STABLE, new DefaultVersion("1.1-20230702.094921").getType());
        assertEquals(Version.Type.BETA, new DefaultVersion("1.1-milestone-1").getType());
        assertEquals(Version.Type.STABLE, new DefaultVersion("1.1").getType());
        assertEquals(Version.Type.BETA, new DefaultVersion("1.1A2").getType());
        assertEquals(Version.Type.BETA, new DefaultVersion("1.1B2").getType());
        assertEquals(Version.Type.BETA, new DefaultVersion("1.1M2").getType());
    }

    @Test
    void serialize() throws IOException, ClassNotFoundException
    {
        validateSerialize(new DefaultVersion("1.1"));
        validateSerialize(new DefaultVersion("1.1-milestone-1"));
    }

    @Test
    void getTypeForBigInteger()
    {
        assertSame(Version.Type.STABLE, new DefaultVersion("1.2147483648").getType());
    }

    @Test
    void getSourceVersion()
    {
        assertEquals(new DefaultVersion("1.0"), new DefaultVersion("1.0").getSourceVersion());
        assertEquals(new DefaultVersion("1.0-SNAPSHOT"), new DefaultVersion("1.0-SNAPSHOT").getSourceVersion());
        assertEquals(new DefaultVersion("1.0-SNAPSHOT"),
            new DefaultVersion("1.0-20230705.143513-0").getSourceVersion());
    }

    @Test
    void testHashCode()
    {
        assertEquals(new DefaultVersion("1.1").hashCode(), new DefaultVersion("1.1").hashCode());
        assertEquals(new DefaultVersion("1.1").hashCode(), new DefaultVersion("1.1.0").hashCode());

        assertNotEquals(new DefaultVersion("1.1").hashCode(), new DefaultVersion("2.0").hashCode());
    }
}
