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
package org.xwiki.test;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * A JUnit {@link MethodRule} to initialize a {@link TestComponentManager}.
 *
 * @version $Id$
 * @since 4.3.1
 * @deprecated use {@link org.xwiki.test.junit5.mockito.ComponentTest} instead
 */
@Deprecated(since = "10.3RC1")
public class ComponentManagerRule extends TestComponentManager implements MethodRule
{
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
    protected void before(final Statement base, final FrameworkMethod method, final Object target) throws Throwable
    {
        initializeTest(target);
    }

    /**
     * Called after the test.
     *
     * @param base The {@link Statement} to be modified
     * @param method The method to be run
     * @param target The object on with the method will be run.
     * @throws Throwable if anything goes wrong
     * @since 5.1M1
     */
    protected void after(final Statement base, final FrameworkMethod method, final Object target) throws Throwable
    {
        shutdownTest();
    }
}
