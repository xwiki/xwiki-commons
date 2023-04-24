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

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockingDetails;
import org.mockito.Mockito;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MockitoComponentManagerExtension} using JUnit5's {@link Launcher}.
 *
 * @since 12.2
 */
public class MockitoComponentManagerExtensionTest
{
    @ExtendWith(MockitoComponentManagerExtension.class)
    static class NestedComponentsTestCase
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

    @ExtendWith(MockitoComponentManagerExtension.class)
    @ComponentList({
        DefaultComponentRole.class,
        TestComponentRole.class
    })
    static class InjectComponentsTestCase
    {
        @Inject
        private ComponentRole componentRole1;

        @Inject
        @Named("test")
        private ComponentRole componentRole2;

        @Test
        void test()
        {
            assertNotNull(this.componentRole1);
            assertNotNull(this.componentRole2);
        }
    }

    @ExtendWith(MockitoComponentManagerExtension.class)
    static class InjectMockitoComponentManagerAsMethodParameterTestCase
    {
        @Test
        void test(MockitoComponentManager componentManager)
        {
            // Verify that we can get a Mockito CM injected
            assertNotNull(componentManager);
        }
    }

    @ExtendWith(MockitoComponentManagerExtension.class)
    static class InjectComponentManagerAsMethodParameterTestCase
    {
        @Test
        void test(ComponentManager componentManager)
        {
            // Verify that we can get a Mockito CM injected when the type is ComponentManager
            assertNotNull(componentManager);
            assertTrue(componentManager instanceof MockitoComponentManager);
        }
    }

    @ExtendWith(MockitoComponentManagerExtension.class)
    static class InjectMockitoComponentManagerInClassVariableTestCase
    {
        @InjectComponentManager
        private MockitoComponentManager componentManager;

        @Test
        void test()
        {
            assertNotNull(this.componentManager);
        }
    }

    @ExtendWith(MockitoComponentManagerExtension.class)
    @ComponentList({
        Component3Impl.class
    })
    static class VariousTestCase
    {
        @Mock
        private List<String> list;

        @MockComponent
        private Component1Role<String> component1;

        @InjectMocks
        @InjectMockComponents
        private Component4Impl component4;

        /**
         * Test a component having several roles by explicitly picking one.
         */
        @InjectMockComponents(role = Component2Role.class)
        private Component5Impl component5Role1;

        /**
         * Test a component having several roles by explicitly picking one.
         */
        @InjectMockComponents(role = Component3Role.class)
        private Component5Impl component5Role2;

        /**
         * Test a component having several hints but the same role.
         */
        @InjectMockComponents
        private Component6Impl component6;

        @InjectComponentManager
        private MockitoComponentManager componentManager;

        @BeforeComponent
        void beforeComponent(MockitoComponentManager componentManager) throws Exception
        {
            // Verify that we can pass a MockitoComponentManager in parameter
            componentManager.registerComponent(Component2Role.class, mock(Component2Role.class, "beforeComponent"));

            // Verify also that any fields annotated with @InjectComponentManager have been injected at this stage
            assertNotNull(this.componentManager);
        }

        @BeforeEach
        void before(MockitoComponentManager componentManager)
        {
            // Verify that we can get a Mockito CM injected
            assertNotNull(componentManager);
        }

        @Test
        void test() throws Exception
        {
            // Verify that a standard mock has been created for the list by Mockito (i.e. for a non-component class)
            assertNotNull(this.list);
            when(this.list.size()).thenReturn(2);

            // Verify that a mock component has been created for component1.
            assertNotNull(this.component1);
            when(this.component1.size(this.list)).thenReturn(3);

            // Verify that component4 has been injected mocks for its @Inject-annotated fields.
            assertNotNull(this.component4.getRole2());

            // Verify that the list field has been mocked in component4 thanks to Mockito's @InjectMock annotation
            assertEquals(3, this.component4.size());

            // Verify that component2 inside component4 is the mock we defined in the @BeforeComponent above
            // (this verifies that @BeforeComponent are executed before @InjectMockComponents)
            MockingDetails details = Mockito.mockingDetails(this.component4.getRole2());
            assertEquals("beforeComponent", details.getMockCreationSettings().getMockName().toString());

            // Verify that component3 is not a mock (i.e. it's the real component impl since it's listed in the
            // @ComponentList annotation.
            assertNotNull(this.component4.getRole3());
            details = Mockito.mockingDetails(this.component4.getRole3());
            assertFalse(details.isMock());

            // Verify that we also support components that implement several roles
            assertNotNull(this.component5Role1);
            assertNotNull(this.component5Role2);

            // Verify that the mock created for:
            //   @MockComponent
            //   private Component1Role<String> component1;
            // Is of the correct generic type. This verifies generics are taken into account.
            assertEquals(0, this.componentManager.getInstanceList(Component1Role.class).size());
            assertEquals(1, this.componentManager.getInstanceList(
                new DefaultParameterizedType(null, Component1Role.class, String.class)).size());
            assertSame(this.component1, this.component4.getRole1());
        }
    }

    /**
     * Verify that the CM doesn't leak between tests.
     */
    @ExtendWith(MockitoComponentManagerExtension.class)
    // Execute tests based on the @Order annotation.
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    // Tell JUnit to not create a new instance of the test class for each test, so that we can share the
    // componentManager variable.
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    static class CMDoesntLeakBetweenTestTestCase
    {
        private ComponentManager componentManager;

        @Test
        @Order(1)
        void test1(MockitoComponentManager componentManager)
        {
            // Save the passed CM instance to be able to compare it in test2.
            this.componentManager = componentManager;
        }

        @Test
        @Order(2)
        void test2(MockitoComponentManager componentManager)
        {
            // Verify that the passed CM is different from the CM saved in test1.
            assertNotSame(this.componentManager, componentManager);
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

    /**
     * Verify that {@code @Inject} annotations are supported.
     */
    @Test
    void injectComponents()
    {
        execute(InjectComponentsTestCase.class);
    }

    @Test
    void injectMockitoComponentManagerAsMethodParameter()
    {
        execute(InjectMockitoComponentManagerAsMethodParameterTestCase.class);
    }

    @Test
    void injectMockitoComponentManagerInClassVariable()
    {
        execute(InjectMockitoComponentManagerInClassVariableTestCase.class);
    }

    @Test
    void injectComponentManagerAsMethodParameter()
    {
        execute(InjectComponentManagerAsMethodParameterTestCase.class);
    }

    @Test
    void various()
    {
        execute(VariousTestCase.class);
    }

    @Test
    void cmDoesntLeakBetweenTest()
    {
        execute(CMDoesntLeakBetweenTestTestCase.class);
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
        String message = summary.getFailures().size() > 0
            ? ExceptionUtils.getStackTrace(summary.getFailures().get(0).getException()) : "";
        assertEquals(0, summary.getFailures().size(), message);
    }
}
