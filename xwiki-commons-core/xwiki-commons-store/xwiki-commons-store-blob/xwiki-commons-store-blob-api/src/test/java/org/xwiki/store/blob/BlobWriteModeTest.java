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
package org.xwiki.store.blob;

import org.junit.jupiter.api.Test;
import org.xwiki.store.blob.internal.BlobOptionSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link BlobWriteMode}.
 *
 * @version $Id$
 */
class BlobWriteModeTest
{
    @Test
    void resolveReturnsDefaultWhenNoModeSpecified()
    {
        BlobWriteMode resolved = BlobWriteMode.resolve(BlobWriteMode.CREATE_NEW);

        assertEquals(BlobWriteMode.CREATE_NEW, resolved);
    }

    @Test
    void resolveReturnsExplicitMode()
    {
        BlobWriteMode resolved = BlobWriteMode.resolve(BlobWriteMode.CREATE_NEW, BlobWriteMode.REPLACE_EXISTING);

        assertEquals(BlobWriteMode.REPLACE_EXISTING, resolved);
    }

    @Test
    void resolvePrefersExplicitModeOverDefault()
    {
        BlobWriteMode resolved = BlobWriteMode.resolve(BlobWriteMode.REPLACE_EXISTING, BlobWriteMode.CREATE_NEW);

        assertEquals(BlobWriteMode.CREATE_NEW, resolved);
    }

    @Test
    void resolveFailsOnMultipleModes()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> BlobWriteMode.resolve(BlobWriteMode.CREATE_NEW,
                BlobWriteMode.CREATE_NEW, BlobWriteMode.REPLACE_EXISTING));

        assertEquals("Multiple options of type [BlobWriteMode] are not supported.", exception.getMessage());
    }

    @Test
    void blobOptionSupportRejectsMixedModes()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> BlobOptionSupport.findSingleOption(BlobWriteMode.class,
                BlobWriteMode.CREATE_NEW, BlobWriteMode.REPLACE_EXISTING));

        assertEquals("Multiple options of type [BlobWriteMode] are not supported.", exception.getMessage());
    }
}
