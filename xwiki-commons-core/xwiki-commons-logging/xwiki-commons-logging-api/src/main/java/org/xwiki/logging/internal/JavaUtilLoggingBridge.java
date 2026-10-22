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
package org.xwiki.logging.internal;

import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * Redirect the logs produced with java.util.logging (used by some libraries, like Lucene) to SLF4J, so that they follow
 * the logging configuration of XWiki like any other log.
 * <p>
 * Requires {@code org.slf4j:jul-to-slf4j}, which is an optional dependency of this module.
 *
 * @version $Id$
 * @since 18.10.0RC1
 */
public class JavaUtilLoggingBridge
{
    private Handler[] removedHandlers = new Handler[0];

    private boolean installed;

    /**
     * Replace the handlers of the java.util.logging root logger with a handler sending the logs to SLF4J. Nothing is
     * done if such a handler is already installed.
     */
    public void install()
    {
        if (!SLF4JBridgeHandler.isInstalled()) {
            // Remove the existing handlers (usually a console handler) so that the logs are not displayed twice. Not
            // using SLF4JBridgeHandler#removeHandlersForRootLogger() since it does not keep the removed handlers,
            // which are put back by #uninstall().
            Logger rootLogger = getRootLogger();
            this.removedHandlers = rootLogger.getHandlers();
            for (Handler handler : this.removedHandlers) {
                rootLogger.removeHandler(handler);
            }

            SLF4JBridgeHandler.install();

            this.installed = true;
        }
    }

    /**
     * Put back the handlers of the java.util.logging root logger replaced by {@link #install()}. Since the root logger
     * can be shared with other applications (depending on the servlet container), it's important to not leave a
     * handler referencing the classes of XWiki when it stops.
     */
    public void uninstall()
    {
        if (this.installed) {
            SLF4JBridgeHandler.uninstall();

            Logger rootLogger = getRootLogger();
            for (Handler handler : this.removedHandlers) {
                rootLogger.addHandler(handler);
            }

            this.removedHandlers = new Handler[0];
            this.installed = false;
        }
    }

    private Logger getRootLogger()
    {
        return LogManager.getLogManager().getLogger("");
    }
}
