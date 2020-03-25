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
package org.xwiki.test.junit5.mockito;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.xwiki.component.annotation.Role;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * Unit tests for {@link MockitoComponentManagerExtension} using JUnit5's {@link Launcher}.
 *
 * @since 12.2
 */
public class BetterMockitoComponentManagerExtensionTest
{
    @Role
    public interface ComponentRole
    {
        String getValue();
    }

    @ExtendWith(MockitoComponentManagerExtension.class)
    public static class NestedComponentsTestCase
    {
        @MockComponent
        private ComponentRole componentRole1;

        @Test
        void test1()
        {
            assertNotNull(this.componentRole1);
        }

        @Nested
        public class NestedNestedComponentsTestCase
        {
            @MockComponent
            private ComponentRole componentRole2;

            @Test
            void test2()
            {
                // Verify that parent test has had its fields initialized
                assertNotNull(componentRole1);
                assertNotNull(this.componentRole2);
            }
        }
    }

    /**
     * Verify that we can have nested components and that they're all injected.
     */
    @Test
    void nestedComponents()
    {
        execute(NestedComponentsTestCase.class);
    }

    private void execute(Class testClass)
    {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
            .selectors(selectClass(testClass))
            .build();
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
        launcher.execute(request, summaryListener);

        TestExecutionSummary summary = summaryListener.getSummary();
        assertEquals(0, summary.getFailures().size());
    }
}
