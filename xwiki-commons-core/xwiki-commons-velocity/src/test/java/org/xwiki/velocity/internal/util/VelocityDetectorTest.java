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
package org.xwiki.velocity.internal.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for {@link VelocityDetector}.
 *
 * @version $Id$
 */
@ComponentTest
class VelocityDetectorTest
{
    @InjectMockComponents
    private VelocityDetector detector;

    @ParameterizedTest
    @CsvSource({
        "true, #set($foo = 'bar')",
        "true, Hello$foo",
        "true, Hello #XWiki",
        "false, Hello World",
        "false, 42'foo'"
    })
    void containsVelocityScript(boolean isVelocity, String input)
    {
        assertEquals(isVelocity, this.detector.containsVelocityScript(input), input);
    }
}
