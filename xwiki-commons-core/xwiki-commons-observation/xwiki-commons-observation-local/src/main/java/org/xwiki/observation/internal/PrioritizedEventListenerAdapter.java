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
package org.xwiki.observation.internal;

import java.util.List;

import org.xwiki.observation.AbstractPrioritizedEventListener;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;

/**
 * Converts an {@link EventListener} implementation into a {@link PrioritizedEventListenerAdapter} one.
 *
 * @version $Id$
 * @since 3.3RC1
 */
public class PrioritizedEventListenerAdapter extends AbstractPrioritizedEventListener
{
    /**
     * @see #getWrappedListener() 
     */
    private EventListener listener;

    /**
     * @param listener see {@link #getWrappedListener()}
     */
    public PrioritizedEventListenerAdapter(EventListener listener)
    {
        this.listener = listener;
    }

    /**
     * @return the original, wrapped listener
     */
    public EventListener getWrappedListener()
    {
        return this.listener;
    }

    @Override
    public List<Event> getEvents()
    {
        return getWrappedListener().getEvents();
    }

    @Override
    public String getName()
    {
        return getWrappedListener().getName();
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        getWrappedListener().onEvent(event, source, data);
    }
}
