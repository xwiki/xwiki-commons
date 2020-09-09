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

import org.junit.jupiter.api.Test;
import org.slf4j.ILoggerFactory;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LogbackUtils}.
 *
 * @version $Id$
 * @since 4.4RC1
 */
class LogBackUtilsTest
{
    @Test
    void getLoggerContextIsNullWhenLogbackNotPresent()
    {
        // Simulate that LogbackUtils.getLoggerFactory() returns a non LoggerContext instance.
        LogbackUtils utils = spy(new LogbackUtils());
        ILoggerFactory loggerFactory = mock(ILoggerFactory.class);
        when(utils.getLoggerFactory()).thenReturn(loggerFactory);

        assertNull(utils.getLoggerContext());
    }
}
