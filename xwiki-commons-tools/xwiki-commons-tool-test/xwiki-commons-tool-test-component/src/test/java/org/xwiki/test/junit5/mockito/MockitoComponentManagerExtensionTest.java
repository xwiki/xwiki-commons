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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockingDetails;
import org.mockito.Mockito;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MockitoComponentManagerExtension}.
 *
 * @version $Id$
 * @since 10.3RC1
 */
@ComponentTest
@ComponentList({
    Component3Impl.class
})
public class MockitoComponentManagerExtensionTest
{
    @Mock
    private List<String> list;

    @MockComponent
    private Component1Role<String> component1;

    @InjectMocks
    @InjectMockComponents
    private Component4Impl component4;

    @InjectMockComponents(role = Component2Role.class)
    private Component5Impl component5Role1;

    @InjectMockComponents(role = Component3Role.class)
    private Component5Impl component5Role2;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @BeforeComponent
    public void beforeComponent(MockitoComponentManager componentManager) throws Exception
    {
        componentManager.registerComponent(Component2Role.class, mock(Component2Role.class, "beforeComponent"));
    }

    @BeforeEach
    public void before(MockitoComponentManager componentManager)
    {
        // Verify that we can get a Mockito CM injected
        assertNotNull(componentManager);
    }

    @Test
    public void testMockitoCMAsParameter(MockitoComponentManager componentManager)
    {
        // Verify that we can get a Mockito CM injected
        assertNotNull(componentManager);
        assertTrue(componentManager instanceof MockitoComponentManager);
    }

    @Test
    public void testComponentMangerAsParameter(ComponentManager componentManager)
    {
        // Verify that we can get a Mockito CM injected when the type is ComponentManager
        assertNotNull(componentManager);
        assertTrue(componentManager instanceof MockitoComponentManager);
    }

    @Test
    public void testVariousScenarios() throws Exception
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

        // Verify that component2 inside component4 is the mock we defined in the @BeforeComponent above (this verifies
        // that @BeforeComponent are executed before @InjectMockComponents)
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

    @Test
    public void testInjectCM()
    {
        assertNotNull(this.componentManager);
    }

    @Test
    public void testCMDoesntLeak1(MockitoComponentManager componentManager) throws Exception
    {
        // Note: the name of the test is set so that testCMDoesntLeak1() executes before testCMDoesntLeak2(), thus
        // ensuring that the CM defined in testCMDoesntLeak1() doesn't leak on testCMDoesntLeak2().
        componentManager.registerMockComponent(Component1Role.class, "testCMDoesntLeak");
    }

    @Test
    public void testCMDoesntLeak2(MockitoComponentManager componentManager) throws Exception
    {
        // See comment in testCMDoesntLeak1()
        assertNull(componentManager.getInstanceMap(Component1Role.class).get("testCMDoesntLeak"));
    }
}
