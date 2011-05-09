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
package org.xwiki.component.logging;

/**
 * Logger that doesn't do anything. Useful to use when no logger implementation is selected to prevent NPEs.
 * In a component-based environment loggers are always set but when component classes are used as simple
 * Java Beans the logger needs to be set and this logger implementation can be used when the user doesn't
 * set explicitly a logger.
 *  
 * @version $Id$
 * @since 1.8RC3
 * @deprecated starting with 3.1M2 use SLF4J's NOPLogger instead
 */
@Deprecated
public class VoidLogger implements Logger
{
    /**
     * {@inheritDoc}
     */
    public void debug(String message)
    {
        // Don't do anything voluntarily
    }

    /**
     * {@inheritDoc}
     */
    public void debug(String message, Throwable throwable)
    {
        // Don't do anything voluntarily
    }

    /**
     * {@inheritDoc}
     */
    public void debug(String message, Object... objects)
    {
        // Don't do anything voluntarily
    }

    /**
     * {@inheritDoc}
     */
    public void debug(String message, Throwable throwable, Object... objects)
    {
        // Don't do anything voluntarily
    }

    /**
     * {@inheritDoc}
     */
    public void error(String message)
    {
        // Don't do anything voluntarily
    }

    /**
     * {@inheritDoc}
     */
    public void error(String message, Throwable throwable)
    {
        // Don't do anything voluntarily
    }

    /**
     * {@inheritDoc}
     */
    public void error(String message, Object... objects)
    {
        // Don't do anything voluntarily
    }

    /**
     * {@inheritDoc}
     */
    public void error(String message, Throwable throwable, Object... objects)
    {
        // Don't do anything voluntarily
    }

    /**
     * {@inheritDoc}
     */
    public void info(String message)
    {
        // Don't do anything voluntarily
    }

    /**
     * {@inheritDoc}
     */
    public void info(String message, Throwable throwable)
    {
        // Don't do anything voluntarily
    }

    /**
     * {@inheritDoc}
     */
    public void info(String message, Object... objects)
    {
        // Don't do anything voluntarily
    }

    /**
     * {@inheritDoc}
     */
    public void info(String message, Throwable throwable, Object... objects)
    {
        // Don't do anything voluntarily
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDebugEnabled()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isErrorEnabled()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInfoEnabled()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWarnEnabled()
    {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void warn(String message)
    {
        // Don't do anything voluntarily
    }

    /**
     * {@inheritDoc}
     */
    public void warn(String message, Throwable throwable)
    {
        // Don't do anything voluntarily
    }

    /**
     * {@inheritDoc}
     */
    public void warn(String message, Object... objects)
    {
        // Don't do anything voluntarily
    }

    /**
     * {@inheritDoc}
     */
    public void warn(String message, Throwable throwable, Object... objects)
    {
        // Don't do anything voluntarily
    }
}
