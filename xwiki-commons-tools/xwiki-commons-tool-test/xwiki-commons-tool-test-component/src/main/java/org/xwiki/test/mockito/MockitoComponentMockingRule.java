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
package org.xwiki.test.mockito;

import java.lang.reflect.Type;
import java.util.List;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.xwiki.component.internal.RoleHint;
import org.xwiki.component.manager.ComponentLookupException;

import static org.mockito.Mockito.mock;

/**
 * Unit tests for Components should extend this class instead of using {@link MockitoComponentManagerRule} or
 * {@link org.xwiki.test.ComponentManagerRule} which should only be used for integration tests.
 * <p>
 * To use this class, define a JUnit {@code @Rule} and pass the component implementation class that you wish to have
 * mocked for you. Then in your test code, do a lookup of your component under test and you'll get a component instance
 * which has all its injected dependencies mocked automatically.
 * <p>
 * For example:
 * 
 * <pre>
 * {@code
 * public class MyComponentTest
 * {
 *     &#64;Rule
 *     public final MockitoComponentMockingRule&lt;MyComponent&gt; mocker =
 *         new MockitoComponentMockingRule(MyImplementation.class);
 * 
 *     &#64;Test
 *     public void someTest() throws Exception
 *     {
 *         MyComponent myComponent = mocker.getComponentUnderTest();
 *         ...
 *     }
 * ...
 * }
 * }
 * </pre>
 *
 * Note that by default there are no component registered against the component manager except those mocked
 * automatically by the Rule (except for the MockitoComponentMockingRule itself, which means that if your component
 * under test is injected a default ComponentManager, it'll be the MockitoComponentMockingRule which will get injected.
 * See more below). This has 2 advantages:
 * <ul>
 * <li>This is the spirit of this Rule since it's for unit testing and this testing your component in isolation from the
 * rest</li>
 * <li>It makes the tests up to 10 times faster</li>
 * </ul>
 * If you really need to register some components, use the {@link org.xwiki.test.annotation.ComponentList} annotation
 * and if you really really need to register all components (it takes time) then use
 * {@link org.xwiki.test.annotation.AllComponents}.
 * <p>
 * In addition, you can perform some action before any component is registered in the Component Manager by having one or
 * several methods annotated with {@link org.xwiki.test.annotation.BeforeComponent}. Similarly, you can perform an
 * action after all components have been registered in the Component Manager by having one or several methods annotated
 * with {@link org.xwiki.test.annotation.AfterComponent}.
 * <p>
 * This can be useful (for example) in the case you wish to register a mock ComponentManager in your component under
 * test. You would write:
 * 
 * <pre>
 * {@code
 * &#64;Rule
 * public final MockitoComponentManagerRule mocker = new MockitoComponentManagerRule();
 * 
 * &#64;AfterComponent
 * public void overrideComponents() throws Exception
 * {
 *     this.mocker.registerMockComponent(ComponentManager.class);
 * }
 * }
 * </pre>
 *
 * @param <T> the component role type, used to provide a typed instance when calling {@link #getComponentUnderTest()}
 * @version $Id$
 * @since 4.3.1
 */
public class MockitoComponentMockingRule<T> extends MockitoComponentManagerRule
{
    /**
     * The mocked logger if any.
     */
    private Logger mockLogger;

    /**
     * The Role Type and Hint of the mocked component.
     */
    private RoleHint<T> mockedComponentHint;

    private MockitoComponentMocker<T> mocker;

    /**
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     */
    public MockitoComponentMockingRule(Class<? extends T> componentImplementationClass)
    {
        this.mocker = new MockitoComponentMocker<T>(this, componentImplementationClass);
    }

    /**
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param excludedComponentRoleDependencies list of component dependency role classes that we don't want mocked
     */
    public MockitoComponentMockingRule(Class<? extends T> componentImplementationClass,
        List<? extends Class<?>> excludedComponentRoleDependencies)
    {
        this.mocker =
            new MockitoComponentMocker<T>(this, componentImplementationClass, excludedComponentRoleDependencies);
    }

    /**
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param componentRoleType the role type of the component implementation (when it has several), for disambiguation
     * @param componentRoleHint the role hint of the component implementation (when it has several), for disambiguation
     * @param excludedComponentRoleDependencies list of component dependency role classes that we don't want mocked
     */
    public MockitoComponentMockingRule(Class<? extends T> componentImplementationClass, Type componentRoleType,
        String componentRoleHint, List<? extends Class<?>> excludedComponentRoleDependencies)
    {
        this.mocker = new MockitoComponentMocker<T>(this, componentImplementationClass, componentRoleType,
            componentRoleHint, excludedComponentRoleDependencies);
    }

    /**
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param componentRoleType the role type of the component implementation (when it has several), for disambiguation
     * @param excludedComponentRoleDependencies list of component dependency role classes that we don't want mocked
     */
    public MockitoComponentMockingRule(Class<? extends T> componentImplementationClass, Type componentRoleType,
        List<? extends Class<?>> excludedComponentRoleDependencies)
    {
        this(componentImplementationClass, componentRoleType, null, excludedComponentRoleDependencies);
    }

    /**
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param componentRoleType the role type of the component implementation (when it has several), for disambiguation
     * @param componentRoleHint the role hint of the component implementation (when it has several), for disambiguation
     */
    public MockitoComponentMockingRule(Class<? extends T> componentImplementationClass, Type componentRoleType,
        String componentRoleHint)
    {
        this.mocker = new MockitoComponentMocker<T>(this, componentImplementationClass, componentRoleType,
            componentRoleHint);
    }

    /**
     * @param componentImplementationClass the component implementation for which we wish to have its injection mocked
     * @param componentRoleType the role type of the component implementation (when it has several), for disambiguation
     */
    public MockitoComponentMockingRule(Class<? extends T> componentImplementationClass, Type componentRoleType)
    {
        this(componentImplementationClass, componentRoleType, (String) null);
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target)
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                before(base, method, target);
                try {
                    base.evaluate();
                } finally {
                    after(base, method, target);
                }
            }
        };
    }

    /**
     * Called before the test.
     *
     * @param base The {@link Statement} to be modified
     * @param method The method to be run
     * @param target The object on with the method will be run.
     * @throws Throwable if anything goes wrong
     * @since 5.1M1
     */
    @Override
    protected void before(final Statement base, final FrameworkMethod method, final Object target) throws Throwable
    {
        super.before(base, method, target);

        // Save the mocked component information so that the test can get an instance of this component
        // easily by calling getComponentUnderTest(...)
        this.mockedComponentHint = this.mocker.mockComponent(target);
    }

    /**
     * Overrides EmbeddableComponentManager in order to mock Loggers since they're handled specially and are not
     * components.
     *
     * @param instanceClass the injected class
     * @return the logger
     */
    @Override
    protected Object createLogger(Class<?> instanceClass)
    {
        Object logger;

        if (!this.mocker.getExcludedComponentRoleDependencies().contains(Logger.class)
            && this.mocker.getComponentImplementationClass() == instanceClass) {
            logger = mock(Logger.class, instanceClass.getName());
            this.mockLogger = (Logger) logger;
        } else {
            logger = super.createLogger(instanceClass);
        }
        return logger;
    }

    /**
     * @return the component which is having its injections being mocked by the {@link MockitoComponentMockingRule} rule
     * @throws ComponentLookupException if the component under test has not been properly registered
     */
    public T getComponentUnderTest() throws ComponentLookupException
    {
        return getInstance(this.mockedComponentHint.getRoleType(), this.mockedComponentHint.getHint());
    }

    /**
     * @return the mocked Logger if the Component under Test has requested an Injection of a Logger or null otherwise
     */
    public Logger getMockedLogger()
    {
        // Note: the test class must get the mocked component instance before calling this method since this is this
        // action that injects the mock loggers... Note that we cannot call the mocked component here since the
        // component can be per-lookup and we would get a different instance...
        return this.mockLogger;
    }
}
