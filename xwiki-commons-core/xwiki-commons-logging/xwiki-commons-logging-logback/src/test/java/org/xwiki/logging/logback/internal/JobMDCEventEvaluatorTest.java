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

import java.util.Map;

import org.junit.jupiter.api.Test;

import ch.qos.logback.classic.spi.ILoggingEvent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JobMDCEventEvaluator}.
 *
 * @version $Id$
 */
class JobMDCEventEvaluatorTest
{
    private final JobMDCEventEvaluator evaluator = new JobMDCEventEvaluator();

    @Test
    void evaluateWhenJobLogCanBeRouted() throws Exception
    {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getMDCPropertyMap()).thenReturn(Map.of("job", "true", "jobCleanId", "job-1"));

        assertTrue(this.evaluator.evaluate(event));
    }

    @Test
    void evaluateWhenNotAJobLog() throws Exception
    {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getMDCPropertyMap()).thenReturn(Map.of("jobCleanId", "job-1"));

        assertFalse(this.evaluator.evaluate(event));
    }

    @Test
    void evaluateWhenMissingCleanJobId() throws Exception
    {
        ILoggingEvent event = mock(ILoggingEvent.class);
        when(event.getMDCPropertyMap()).thenReturn(Map.of("job", "true"));

        assertFalse(this.evaluator.evaluate(event));
    }
}
