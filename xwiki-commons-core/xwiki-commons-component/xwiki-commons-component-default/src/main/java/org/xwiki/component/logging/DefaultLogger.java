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

import org.slf4j.LoggerFactory;

/**
 * Bridge between XWiki Logging and SLF4J Logging.
 *
 * @version $Id$
 * @since 3.1M2
 * @deprecated already deprecated since 3.1M2, use {@link javax.inject.Inject} annotation to get injected a SLF4J
 *             Logger instead
 */
public class DefaultLogger extends AbstractLogger
{
    /**
     * Wrapped SLF4J logger object. This communicates with the underlying logging framework.
     */
    private org.slf4j.Logger logger;

    /**
     * @param clazz the class to use when logging messages
     */
    public DefaultLogger(Class<?> clazz)
    {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    /**
     * {@inheritDoc}
     */
    public void debug(String message, Object... objects)
    {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(formatMessage(message, objects));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void debug(String message, Throwable throwable, Object... objects)
    {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(formatMessage(message, objects), throwable);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void debug(String message, Throwable throwable)
    {
        this.logger.debug(message, throwable);
    }

    /**
     * {@inheritDoc}
     */
    public void debug(String message)
    {
        this.logger.debug(message);
    }

    /**
     * {@inheritDoc}
     */
    public void error(String message, Object... objects)
    {
        if (this.logger.isErrorEnabled()) {
            this.logger.error(formatMessage(message, objects));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void error(String message, Throwable throwable, Object... objects)
    {
        if (this.logger.isErrorEnabled()) {
            this.logger.error(formatMessage(message, objects), throwable);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void error(String message, Throwable throwable)
    {
        this.logger.error(message, throwable);
    }

    /**
     * {@inheritDoc}
     */
    public void error(String message)
    {
        this.logger.error(message);
    }

    /**
     * {@inheritDoc}
     */
    public void info(String message, Object... objects)
    {
        if (this.logger.isInfoEnabled()) {
            this.logger.info(formatMessage(message, objects));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void info(String message, Throwable throwable, Object... objects)
    {
        if (this.logger.isInfoEnabled()) {
            this.logger.info(formatMessage(message, objects), throwable);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void info(String message, Throwable throwable)
    {
        this.logger.info(message, throwable);
    }

    /**
     * {@inheritDoc}
     */
    public void info(String message)
    {
        this.logger.info(message);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isDebugEnabled()
    {
        return this.logger.isDebugEnabled();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isErrorEnabled()
    {
        return this.logger.isErrorEnabled();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isInfoEnabled()
    {
        return this.logger.isInfoEnabled();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isWarnEnabled()
    {
        return this.logger.isWarnEnabled();
    }

    /**
     * {@inheritDoc}
     */
    public void warn(String message, Object... objects)
    {
        if (this.logger.isWarnEnabled()) {
            this.logger.warn(formatMessage(message, objects));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void warn(String message, Throwable throwable, Object... objects)
    {
        if (this.logger.isWarnEnabled()) {
            this.logger.warn(formatMessage(message, objects), throwable);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void warn(String message, Throwable throwable)
    {
        this.logger.warn(message, throwable);
    }

    /**
     * {@inheritDoc}
     */
    public void warn(String message)
    {
        this.logger.warn(message);
    }
}
