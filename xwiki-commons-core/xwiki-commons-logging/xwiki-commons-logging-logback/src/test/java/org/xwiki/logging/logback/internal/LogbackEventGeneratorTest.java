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
package org.xwiki.logging.logback.internal;

import java.util.Arrays;

import org.jmock.Expectations;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;
import org.xwiki.test.jmock.AbstractComponentTestCase;

/**
 * Unit tests for {@link LogbackEventGenerator}.
 * 
 * @version $Id$
 * @since 3.2M3
 */
public class LogbackEventGeneratorTest extends AbstractComponentTestCase
{
    private Logger logger;

    private ObservationManager observationManager;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        this.observationManager = getComponentManager().getInstance(ObservationManager.class);

        this.logger = LoggerFactory.getLogger(getClass());
    }

    /**
     * Verify that logging an error will generate a Log Event.
     */
    @Test
    public void test()
    {
        final EventListener listener = getMockery().mock(EventListener.class);
        final Event event = new LogEvent(null, LogLevel.ERROR, "error message", null, null);

        getMockery().checking(new Expectations()
        {
            {
                allowing(listener).getName();
                will(returnValue("mylistener"));
                allowing(listener).getEvents();
                will(returnValue(Arrays.asList(event)));
                oneOf(listener).onEvent(with(any(LogEvent.class)), with(anything()), with(anything()));
            }
        });

        this.observationManager.addListener(listener);

        this.logger.error("error message");
    }
}
