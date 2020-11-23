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
package org.xwiki.diff.display.internal;

import org.junit.jupiter.api.Test;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

/**
 * Unit tests for {@link LineSplitter}.
 *
 * @version $Id$
 */
@ComponentTest
class LineSplitterTest
{
    @InjectMockComponents
    private LineSplitter splitter;

    @Test
    void split()
    {
        assertThat(this.splitter.split("hello\nworld"), contains("hello", "world"));
    }

    @Test
    void splitWhenNull()
    {
        assertThat(this.splitter.split(null), empty());
    }

    @Test
    void splitWhenEmptyString()
    {
        assertThat(this.splitter.split(""), empty());
    }

    /**
     * Allows to differentiate two contents, one with a trailing new line and one without.
     */
    @Test
    void splitWhenLastLineEndsWithNewLineCharacter()
    {
        assertThat(this.splitter.split("hello\nworld\n"), contains("hello", "world", ""));
    }
}
