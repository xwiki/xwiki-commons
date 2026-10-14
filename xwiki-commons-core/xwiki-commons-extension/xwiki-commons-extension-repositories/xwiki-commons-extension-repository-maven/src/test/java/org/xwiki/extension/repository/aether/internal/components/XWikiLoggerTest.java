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
package org.xwiki.extension.repository.aether.internal.components;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link XWikiLogger}.
 *
 * @version $Id$
 */
class XWikiLoggerTest
{
    private final Logger logger = mock(Logger.class);

    private final XWikiLogger xwikiLogger = new XWikiLogger(this.logger);

    @Test
    void log()
    {
        Throwable throwable = new Exception();

        this.xwikiLogger.debug("debug");
        this.xwikiLogger.debug("debug", throwable);
        this.xwikiLogger.info("info");
        this.xwikiLogger.info("info", throwable);
        this.xwikiLogger.warn("warn");
        this.xwikiLogger.warn("warn", throwable);
        this.xwikiLogger.error("error");
        this.xwikiLogger.error("error", throwable);
        this.xwikiLogger.fatalError("fatal");
        this.xwikiLogger.fatalError("fatal", throwable);

        verify(this.logger).debug("debug");
        verify(this.logger).debug("debug", throwable);
        verify(this.logger).info("info");
        verify(this.logger).info("info", throwable);
        verify(this.logger).warn("warn");
        verify(this.logger).warn("warn", throwable);
        verify(this.logger).error("error");
        verify(this.logger).error("error", throwable);
        // A fatal error is logged as an error, SLF4J having no fatal level.
        verify(this.logger).error("fatal");
        verify(this.logger).error("fatal", throwable);
    }

    @Test
    void levelsEnabled()
    {
        when(this.logger.isDebugEnabled()).thenReturn(true);
        when(this.logger.isInfoEnabled()).thenReturn(false);
        when(this.logger.isWarnEnabled()).thenReturn(true);
        when(this.logger.isErrorEnabled()).thenReturn(true);

        assertTrue(this.xwikiLogger.isDebugEnabled());
        assertFalse(this.xwikiLogger.isInfoEnabled());
        assertTrue(this.xwikiLogger.isWarnEnabled());
        assertTrue(this.xwikiLogger.isErrorEnabled());
        assertTrue(this.xwikiLogger.isFatalErrorEnabled());
        verify(this.logger, times(2)).isErrorEnabled();
    }

    @Test
    void threshold()
    {
        this.xwikiLogger.setThreshold(org.codehaus.plexus.logging.Logger.LEVEL_ERROR);

        // The level is controlled by the underlying logger, so the threshold is not changed.
        assertEquals(org.codehaus.plexus.logging.Logger.LEVEL_DEBUG, this.xwikiLogger.getThreshold());
    }

    @Test
    void childLoggerAndName()
    {
        assertSame(this.xwikiLogger, this.xwikiLogger.getChildLogger("child"));
        assertEquals("xwiki logger", this.xwikiLogger.getName());
    }
}
