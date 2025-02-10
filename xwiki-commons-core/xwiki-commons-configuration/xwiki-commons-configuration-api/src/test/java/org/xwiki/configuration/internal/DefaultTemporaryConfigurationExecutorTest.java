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
package org.xwiki.configuration.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import javax.inject.Named;
import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

/**
 * Unit tests for {@link DefaultTemporaryConfigurationExecutor}.
 * 
 * @version $Id$
 */
@ComponentTest
class DefaultTemporaryConfigurationExecutorTest
{
    @InjectMockComponents
    private DefaultTemporaryConfigurationExecutor executor;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @MockComponent
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @BeforeEach
    void configure()
    {
        when(this.componentManagerProvider.get()).thenReturn(this.componentManager);
    }

    @Test
    void call() throws Exception
    {
        ConfigurationSource source = this.componentManager.registerMockComponent(ConfigurationSource.class, "foo");
        when(source.containsKey("age")).thenReturn(true);
        when(source.getProperty("age")).thenReturn(27);

        assertEquals("done", this.executor.call("foo", Map.of("color", "blue", "age", 13), () -> {
            return "done";
        }));

        // Set.
        verify(source).setProperty("color", "blue");
        verify(source).setProperty("age", 13);

        // Restore.
        verify(source).removeProperty("color");
        verify(source).setProperty("age", 27);
    }
}
