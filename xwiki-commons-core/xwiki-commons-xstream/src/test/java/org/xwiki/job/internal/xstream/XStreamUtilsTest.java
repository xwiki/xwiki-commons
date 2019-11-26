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
package org.xwiki.job.internal.xstream;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;
import org.xwiki.job.test.TestSerializableXStreamChecker;
import org.xwiki.logging.LogLevel;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.xstream.internal.XStreamUtils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Validate {@link XStreamUtils}.
 * 
 * @version $Id$
 */
@ComponentTest
@ComponentList({ TestSerializableXStreamChecker.class })
public class XStreamUtilsTest
{
    @InjectMockComponents
    private XStreamUtils utils;

    @Test
    public void isSafeType()
    {
        assertTrue(this.utils.isSafeType(null));
        assertTrue(this.utils.isSafeType("string"));
        assertTrue(this.utils.isSafeType(1));
        assertTrue(this.utils.isSafeType(new Object[] {}));
        assertTrue(this.utils.isSafeType(LogLevel.ERROR));

        assertFalse(this.utils.isSafeType(getClass()));
    }

    @Test
    public void isSerializable()
    {
        assertFalse(this.utils.isSerializable(ByteArrayOutputStream.class));
        assertFalse(this.utils.isSerializable(OutputStream.class));
        assertFalse(this.utils.isSerializable(ByteArrayInputStream.class));
        assertFalse(this.utils.isSerializable(InputStream.class));
    }
}
