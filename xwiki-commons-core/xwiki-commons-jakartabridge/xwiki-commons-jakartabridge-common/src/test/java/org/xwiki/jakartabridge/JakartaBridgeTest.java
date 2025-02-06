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
package org.xwiki.jakartabridge;

import org.junit.jupiter.api.Test;
import org.xwiki.jakartabridge.internal.AbstractJakartaToJavaxWrapper;
import org.xwiki.jakartabridge.internal.AbstractJavaxToJakartaWrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Validate {@link JakartaBridge}.
 * 
 * @version $Id$
 */
class JakartaBridgeTest
{
    interface TestJakarta
    {

    }

    class DefaultTestJakarta implements TestJakarta
    {

    }

    interface TestJavax
    {

    }

    class DefaultTestJavax implements TestJavax
    {

    }

    public class TestJavaxToJakarta extends AbstractJavaxToJakartaWrapper<TestJakarta> implements TestJavax
    {
        public TestJavaxToJakarta(TestJakarta wrapped)
        {
            super(wrapped);
        }

    }

    public class TestJakartaToJavax extends AbstractJakartaToJavaxWrapper<TestJavax> implements TestJakarta
    {
        public TestJakartaToJavax(TestJavax wrapped)
        {
            super(wrapped);
        }
    }

    @Test
    void single()
    {
        assertNull(JakartaBridge.toJavax(null, null));
        assertNull(JakartaBridge.toJakarta(null, null));

        ///

        TestJakarta jakarta = new DefaultTestJakarta();

        TestJavax javax = JakartaBridge.toJavax(jakarta, TestJavaxToJakarta::new);

        assertNotNull(javax);
        assertSame(jakarta, JakartaBridge.toJakarta(javax, TestJakartaToJavax::new));

        ///

        javax = new DefaultTestJavax();

        jakarta = JakartaBridge.toJakarta(javax, TestJakartaToJavax::new);

        assertNotNull(jakarta);
        assertSame(javax, JakartaBridge.toJavax(jakarta, TestJavaxToJakarta::new));
    }

    @Test
    void equal()
    {
        TestJavax javax = new DefaultTestJavax();

        TestJakarta jakarta1 = JakartaBridge.toJakarta(javax, TestJakartaToJavax::new);
        TestJakarta jakarta2 = JakartaBridge.toJakarta(javax, TestJakartaToJavax::new);

        assertEquals(jakarta1, jakarta2);
        assertEquals(jakarta1.hashCode(), jakarta2.hashCode());

        TestJakarta jakarta = new DefaultTestJakarta();

        TestJavax javax1 = JakartaBridge.toJavax(jakarta, TestJavaxToJakarta::new);
        TestJavax javax2 = JakartaBridge.toJavax(jakarta, TestJavaxToJakarta::new);

        assertEquals(javax1, javax2);
        assertEquals(javax1.hashCode(), javax2.hashCode());
    }
}
