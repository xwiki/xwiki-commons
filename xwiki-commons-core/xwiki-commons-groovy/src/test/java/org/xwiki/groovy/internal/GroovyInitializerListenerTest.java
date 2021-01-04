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
package org.xwiki.groovy.internal;

import java.io.File;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.environment.Environment;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

/**
 * Validate {@link GroovyInitializerListener}.
 * 
 * @version $Id$
 */
@ComponentTest
class GroovyInitializerListenerTest
{
    @InjectMockComponents
    private GroovyInitializerListener listener;

    @MockComponent
    private Environment environment;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @Test
    void init() throws InitializationException
    {
        when(this.environment.getPermanentDirectory()).thenReturn(new File("permdir"));

        this.listener.initialize();

        assertEquals(new File("permdir/cache/groovy").getAbsolutePath(), System.getProperty("groovy.root"));

        this.componentManager.unregisterComponent(Environment.class, null);
        System.clearProperty("groovy.root");

        this.listener.initialize();

        assertNull(System.getProperty("groovy.root"));
    }

    @Test
    void listener()
    {
        assertEquals(GroovyInitializerListener.NAME, this.listener.getName());
        assertEquals(Collections.emptyList(), this.listener.getEvents());
    }
}
