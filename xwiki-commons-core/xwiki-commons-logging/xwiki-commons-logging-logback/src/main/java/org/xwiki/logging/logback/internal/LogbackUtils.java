package org.xwiki.logging.logback.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

/**
 * Provide several Logback related utility methods.
 * 
 * @version $Id$
 * @since 3.2M3
 */
public final class LogbackUtils
{
    /**
     * Utility class.
     */
    private LogbackUtils()
    {

    }

    /**
     * @return the Logback root logger
     */
    public static ch.qos.logback.classic.Logger getRootLogger()
    {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();

        return lc.getLogger(Logger.ROOT_LOGGER_NAME);
    }
}
