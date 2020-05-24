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
package org.xwiki.velocity.internal;

import java.util.Properties;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.velocity.VelocityEngine;
import org.xwiki.velocity.XWikiVelocityException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link DefaultVelocityFactory}.
 *
 * @version $Id$
 */
@ComponentTest
public class DefaultVelocityFactoryTest
{
    @InjectMockComponents
    private DefaultVelocityFactory velocityFactory;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @Test
    void createVelocityEngine() throws Exception
    {
        assertNull(this.velocityFactory.getVelocityEngine("somekey"));
        assertFalse(this.velocityFactory.hasVelocityEngine("somekey"));
        VelocityEngine mockEngine = this.componentManager.registerMockComponent(VelocityEngine.class);
        Properties props = new Properties();
        props.setProperty("prop1", "value1");

        this.velocityFactory.createVelocityEngine("somekey", props);

        assertSame(mockEngine, this.velocityFactory.getVelocityEngine("somekey"));
        assertTrue(this.velocityFactory.hasVelocityEngine("somekey"));
        verify(mockEngine).initialize(props);
    }

    @Test
    void removeVelocityEngine() throws Exception
    {
        // Try to remove non-existing engine and verify it doesn't throw any error
        this.velocityFactory.removeVelocityEngine("somekey");

        this.componentManager.registerMockComponent(VelocityEngine.class);
        this.velocityFactory.createVelocityEngine("somekey", new Properties());
        assertNotNull(this.velocityFactory.getVelocityEngine("somekey"));
        assertTrue(this.velocityFactory.hasVelocityEngine("somekey"));

        this.velocityFactory.removeVelocityEngine("somekey");

        assertNull(this.velocityFactory.getVelocityEngine("somekey"));
        assertFalse(this.velocityFactory.hasVelocityEngine("somekey"));
    }

    @Test
    void createVelocityEngineWhenFailing()
    {
        Throwable exception = assertThrows(XWikiVelocityException.class, () -> {
            this.velocityFactory.createVelocityEngine("somekey", new Properties());
        });
        assertEquals("Failed to create Velocity Engine", exception.getMessage());
        // Verify that the nested exception is there
        assertEquals("ComponentLookupException: Can't find descriptor for the component with type "
                + "[interface org.xwiki.velocity.VelocityEngine] and hint [null]",
            ExceptionUtils.getRootCauseMessage(exception));
    }
}
