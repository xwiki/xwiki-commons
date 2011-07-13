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
package org.xwiki.observation;

import junit.framework.Assert;

import org.hamcrest.core.IsNot;
import org.jmock.Expectations;
import org.junit.Test;
import org.xwiki.observation.event.BeginEvent;
import org.xwiki.observation.event.EndEvent;
import org.xwiki.observation.internal.DefaultObservationContext;
import org.xwiki.test.AbstractComponentTestCase;

/**
 * Validate {@link DefaultObservationContext}.
 * 
 * @version $Id$
 */
public class DefaultObservationContextTest extends AbstractComponentTestCase
{
    private ObservationManager manager;

    private ObservationContext observationContext;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        this.manager = getComponentManager().lookup(ObservationManager.class);
        this.observationContext = getComponentManager().lookup(ObservationContext.class);
    }

    @Test
    public void test()
    {
        final BeginEvent beginEvent1 = getMockery().mock(BeginEvent.class, "begin1");
        final BeginEvent beginEvent2 = getMockery().mock(BeginEvent.class, "begin2");
        final EndEvent endEvent1 = getMockery().mock(EndEvent.class, "end1");
        final EndEvent endEvent2 = getMockery().mock(EndEvent.class, "end2");

        getMockery().checking(new Expectations() {{
            allowing(beginEvent1).matches(with(same(beginEvent1))); will(returnValue(true));
            allowing(beginEvent1).matches(with(IsNot.not(same(beginEvent1)))); will(returnValue(false));

            allowing(beginEvent2).matches(with(same(beginEvent2))); will(returnValue(true));
            allowing(beginEvent2).matches(with(IsNot.not(same(beginEvent2)))); will(returnValue(false));

            allowing(endEvent1).matches(with(same(endEvent1))); will(returnValue(true));
            allowing(endEvent1).matches(with(IsNot.not(same(endEvent1)))); will(returnValue(false));

            allowing(endEvent2).matches(with(same(endEvent2))); will(returnValue(true));
            allowing(endEvent2).matches(with(IsNot.not(same(endEvent2)))); will(returnValue(false));
        }});

        Assert.assertFalse(this.observationContext.isIn(beginEvent1));
        Assert.assertFalse(this.observationContext.isIn(beginEvent2));

        this.manager.notify(beginEvent1, null);

        Assert.assertTrue(this.observationContext.isIn(beginEvent1));

        this.manager.notify(beginEvent2, null);

        Assert.assertTrue(this.observationContext.isIn(beginEvent1));
        Assert.assertTrue(this.observationContext.isIn(beginEvent2));

        this.manager.notify(endEvent2, null);

        Assert.assertTrue(this.observationContext.isIn(beginEvent1));
        Assert.assertFalse(this.observationContext.isIn(beginEvent2));

        this.manager.notify(endEvent1, null);

        Assert.assertFalse(this.observationContext.isIn(beginEvent1));
        Assert.assertFalse(this.observationContext.isIn(beginEvent2));
    }
}
