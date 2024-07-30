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
package org.xwiki.extension.test;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.xwiki.environment.Environment;
import org.xwiki.test.TestEnvironment;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.test.mockito.MockitoComponentManagerRule;

public class MockitoRepositoryUtilsRule extends MockitoRepositoryUtils implements MethodRule
{
    private final MethodRule parent;

    public MockitoRepositoryUtilsRule()
    {
        this(new MockitoComponentManagerRule());
    }

    public MockitoRepositoryUtilsRule(MockitoComponentManagerRule componentManager)
    {
        this(componentManager, componentManager);
    }

    /**
     * @since 10.2
     */
    public MockitoRepositoryUtilsRule(MockitoComponentManager componentManager, MethodRule parent)
    {
        super(componentManager);

        this.parent = parent;
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target)
    {
        final Statement statement = new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                // Force a test environment
                componentManager.registerComponent(Environment.class, new TestEnvironment());

                setup();
                base.evaluate();
            }
        };

        return this.parent != null ? this.parent.apply(statement, method, target) : statement;
    }
}
