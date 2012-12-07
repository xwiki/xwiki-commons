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

import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.Before;
import org.junit.Rule;

/**
 * Configures Logback logging and provides Mockery object to tests.
 *
 * @version $Id$
 * @since 4.4M1
 */
public abstract class AbstractTestCase
{
    /**
     * Automatically verify JMock expectations.
     */
    @Rule
    public final JUnitRuleMockery jUnitRuleMockery = new JUnitRuleMockery();

    /**
     * @see #getMockery()
     */
    private Mockery mockery = new JUnit4Mockery();

    /**
     * Several of our tests run with several threads (for example there can be Finalizer threads) and when we mock
     * an object that's accessed by different threads JMock will warn us about the issue since by default its
     * mocks are not threadsafe. Thus we make them thread safe to be on the safe side.
     */
    @Before
    public void setJMockThreadSafety()
    {
        getMockery().setThreadingPolicy(new Synchroniser());
    }

    /**
     * @return the JMock Mockery objects to mock interfaces/classes and set expectations on
     */
    public Mockery getMockery()
    {
        return this.mockery;
    }
}
