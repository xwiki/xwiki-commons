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
package org.xwiki.environment.internal;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.environment.Environment;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.ApplicationStartedEvent;
import org.xwiki.observation.event.Event;

/**
 * Initializes the Servlet Environment Cache when the application starts.
 *
 * @since 17.5.0RC1
 * @since 17.4.1
 * @since 16.10.9
 * @version $Id$
 */
@Component
@Singleton
@Named(ServletEnvironmentCacheInitializer.NAME)
public class ServletEnvironmentCacheInitializer extends AbstractEventListener
{
    /**
     * The name of this event listener.
     */
    public static final String NAME = "org.xwiki.environment.internal.ServletEnvironmentCacheInitializer";

    @Inject
    private Provider<Environment> environment;

    /**
     * Default constructor.
     */
    public ServletEnvironmentCacheInitializer()
    {
        super(NAME, List.of(new ApplicationStartedEvent()));
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (this.environment.get() instanceof ServletEnvironment servletEnvironment) {
            servletEnvironment.initializeCache();
        }
    }
}
