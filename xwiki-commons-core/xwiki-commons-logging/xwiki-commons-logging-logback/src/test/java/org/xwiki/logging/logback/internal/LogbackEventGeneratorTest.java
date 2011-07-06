package org.xwiki.logging.logback.internal;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.observation.ObservationManager;
import org.xwiki.test.AbstractComponentTestCase;

public class LogbackEventGeneratorTest extends AbstractComponentTestCase
{
    private Logger logger;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        getComponentManager().lookup(ObservationManager.class);
        
        this.logger = LoggerFactory.getLogger(LogbackEventGeneratorTest.class);   
    }

    @Test
    public void test()
    {
        this.logger.error("error message");
    }
}
