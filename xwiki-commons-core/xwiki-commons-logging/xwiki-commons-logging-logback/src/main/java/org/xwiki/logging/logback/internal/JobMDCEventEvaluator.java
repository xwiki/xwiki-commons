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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluatorBase;

/**
 * Accepts log events that belong to a job routing context and provide a clean job identifier for file routing.
 *
 * @version $Id$
 * @since 18.3.0RC1
 */
public class JobMDCEventEvaluator extends EventEvaluatorBase<ILoggingEvent>
{
    @Override
    public boolean evaluate(ILoggingEvent event) throws NullPointerException, EvaluationException
    {
        Map<String, String> mdc = event.getMDCPropertyMap();
        String cleanJobId = mdc.get("jobCleanId");

        return "true".equals(mdc.get("job")) && cleanJobId != null && !cleanJobId.isBlank();
    }
}
