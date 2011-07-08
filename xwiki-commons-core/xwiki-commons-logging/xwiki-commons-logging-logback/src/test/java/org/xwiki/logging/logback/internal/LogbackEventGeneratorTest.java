package org.xwiki.logging.logback.internal;

import java.util.Arrays;

import org.jmock.Expectations;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.logging.event.LogLevel;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.ObservationManager;
import org.xwiki.observation.event.Event;
import org.xwiki.test.AbstractComponentTestCase;

public class LogbackEventGeneratorTest extends AbstractComponentTestCase
{
    private Logger logger;
    
    private ObservationManager observationManager;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        this.observationManager = getComponentManager().lookup(ObservationManager.class);
        
        this.logger = LoggerFactory.getLogger(LogbackEventGeneratorTest.class);   
    }

    @Test
    public void test()
    {
        final EventListener listener = getMockery().mock(EventListener.class);
        final Event event = new LogEvent(LogLevel.ERROR, "error message", null, null);

        getMockery().checking(new Expectations() {{
            allowing(listener).getName(); will(returnValue("mylistener"));
            allowing(listener).getEvents(); will(returnValue(Arrays.asList(event)));
            oneOf(listener).onEvent(with(any(LogEvent.class)), with(anything()), with(anything()));
        }});

        this.observationManager.addListener(listener);

        this.logger.error("error message");
    }
}
