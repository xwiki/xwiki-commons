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
package org.xwiki.classloader.internal;

import java.util.Objects;

import org.xwiki.observation.event.Event;
import org.xwiki.stability.Unstable;

/**
 * Event triggered whenever a class loader has been dropped
 * (see {@link org.xwiki.classloader.ClassLoaderManager#dropURLClassLoader(String)}) and reloaded. This is used for
 * example when a JAR extension is uninstalled. Note that {@link org.xwiki.classloader.ClassLoaderManager} is not
 * responsible for triggering this, as the trigger needs to be performed after the classloader reload.
 *
 * @version $Id$
 * @since 17.1.0
 * @since 18.0.0RC1
 */
@Unstable
public class ClassLoaderResetEvent implements Event
{
    private final String namespace;

    /**
     * Default constructor for the root classloader.
     */
    public ClassLoaderResetEvent()
    {
        this(null);
    }

    /**
     * Default constructor when the event is triggered for a specific namespace.
     * @param namespace the namespace of the reset classloader.
     */
    public ClassLoaderResetEvent(String namespace)
    {
        this.namespace = namespace;
    }

    /**
     * @return the namespace the event is triggered for.
     */
    public String getNamespace()
    {
        return namespace;
    }

    @Override
    public boolean matches(Object otherEvent)
    {
        return otherEvent instanceof ClassLoaderResetEvent classLoaderResetedEvent
            && Objects.equals(namespace, classLoaderResetedEvent.namespace);
    }
}
