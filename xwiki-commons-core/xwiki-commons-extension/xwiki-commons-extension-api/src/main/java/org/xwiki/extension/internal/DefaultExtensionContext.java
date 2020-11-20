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
package org.xwiki.extension.internal;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.ExtensionContext;
import org.xwiki.extension.ExtensionSession;

/**
 * Default implementation of {@link ExtensionContext}.
 * 
 * @version $Id$
 * @since 12.10
 */
@Component
@Singleton
public class DefaultExtensionContext implements ExtensionContext
{
    private class ExtensionSessionEntry
    {
        private final DefaultExtensionSession session = new DefaultExtensionSession();

        private int level;
    }

    private ThreadLocal<ExtensionSessionEntry> threadLocal = new ThreadLocal<>();

    @Override
    public ExtensionSession pushSession()
    {
        ExtensionSessionEntry entry = this.threadLocal.get();

        if (entry == null) {
            entry = new ExtensionSessionEntry();
            this.threadLocal.set(entry);
        }

        ++entry.level;

        return entry.session;
    }

    @Override
    public void popSession()
    {
        ExtensionSessionEntry entry = this.threadLocal.get();

        if (entry != null) {
            --entry.level;

            // We reached the bottom of the stack, destroy the session
            if (entry.level == 0) {
                entry.session.dispose();
                this.threadLocal.remove();
            }
        }
    }
}
