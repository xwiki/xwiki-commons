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
package org.xwiki.observation;

import java.util.List;

import org.xwiki.observation.event.Event;

/**
 * Wrap a provide listener and filter received events coming from provided {@link Thread}.
 * 
 * @version $Id$
 * @since 3.2M3
 */
public class WrappedThreadEventListener extends AbstractThreadEventListener
{
    /**
     * The wrapped listener.
     */
    private EventListener listener;

    /**
     * @param listener the wrapped listener
     * @param thread the thread to match to receive events
     */
    public WrappedThreadEventListener(EventListener listener, Thread thread)
    {
        super(thread);

        this.listener = listener;
    }

    @Override
    public List<Event> getEvents()
    {
        return this.listener.getEvents();
    }

    @Override
    public String getName()
    {
        return this.listener.getName();
    }

    @Override
    protected void onEventInternal(Event event, Object source, Object data)
    {
        this.listener.onEvent(event, source, data);
    }
}
