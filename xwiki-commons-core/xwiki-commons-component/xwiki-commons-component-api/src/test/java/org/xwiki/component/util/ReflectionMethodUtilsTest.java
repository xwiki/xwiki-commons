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
package org.xwiki.component.util;

import org.junit.jupiter.api.Test;
import org.xwiki.component.test.TestImplementation;
import org.xwiki.component.test.TestInheritedAnnotation;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link ReflectionMethodUtils}.
 *
 * @version $Id$
 */
public class ReflectionMethodUtilsTest
{
    @Test
    public void testGetMethodParameterAnnotations() throws SecurityException, NoSuchMethodException
    {
        assertEquals(
            1,
            ReflectionMethodUtils.getMethodParameterAnnotations(
                TestImplementation.class.getMethod("methodWithAnnotationParameter", new Class<?>[]{ Object.class }),
                0, TestInheritedAnnotation.class, true).size());

        assertEquals(
            0,
            ReflectionMethodUtils.getMethodParameterAnnotations(
                TestImplementation.class.getMethod("methodWithAnnotationParameter", new Class<?>[]{ Object.class }),
                0, TestInheritedAnnotation.class, false).size());
    }
}
