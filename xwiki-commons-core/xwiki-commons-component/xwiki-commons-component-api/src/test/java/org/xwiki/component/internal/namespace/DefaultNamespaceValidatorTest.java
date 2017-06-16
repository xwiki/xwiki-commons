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
package org.xwiki.component.internal.namespace;

import java.util.Arrays;

import org.junit.Test;
import org.xwiki.component.namespace.NamespaceNotAllowedException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Validate {@link DefaultNamespaceValidator}.
 * 
 * @version $Id$
 */
public class DefaultNamespaceValidatorTest
{
    private DefaultNamespaceValidator validator = new DefaultNamespaceValidator();

    // Tests

    @Test
    public void isAllowed()
    {
        assertTrue(this.validator.isAllowed(Arrays.asList("namespace"), "namespace"));
        assertTrue(this.validator.isAllowed(Arrays.asList("{root}"), null));
        assertTrue(this.validator.isAllowed(Arrays.asList("{}"), null));
        assertTrue(this.validator.isAllowed(Arrays.asList("[.*]"), "namespace"));
        assertTrue(this.validator.isAllowed(Arrays.asList("[wiki:(?!xwiki$).*]"), "wiki:test"));

        assertFalse(this.validator.isAllowed(Arrays.asList("namespace"), "wrong"));
        assertFalse(this.validator.isAllowed(Arrays.asList("{root}"), "wrong"));
        assertFalse(this.validator.isAllowed(Arrays.asList("[\\d*]"), "namespace"));
        assertFalse(this.validator.isAllowed(Arrays.asList((String) null), "namespace"));
    }

    @Test
    public void checkAllowed() throws NamespaceNotAllowedException
    {
        this.validator.checkAllowed(Arrays.asList("namespace"), "namespace");
    }

    @Test(expected = NamespaceNotAllowedException.class)
    public void checkNoAllowed() throws NamespaceNotAllowedException
    {
        this.validator.checkAllowed(Arrays.asList("namespace"), "wrong");
    }
}
