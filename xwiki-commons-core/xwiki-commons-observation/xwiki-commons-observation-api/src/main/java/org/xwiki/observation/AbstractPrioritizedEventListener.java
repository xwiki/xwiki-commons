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

/**
 * Helper to implement Event Listeners, providing some default implementation.
 * We recommend Event Listener writers to extend this class.
 *
 * @version $Id$
 * @since 3.3RC1
 */
public abstract class AbstractPrioritizedEventListener implements PrioritizedEventListener
{
    /**
     * @see #getPriority()
     */
    private int priority = 1000;

    @Override
    public int getPriority()
    {
        return this.priority;
    }

    /**
     * @param priority the listener priority to use (lower means executed before others)
     */
    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    @Override
    public int compareTo(PrioritizedEventListener prioritizedEventListener)
    {
        return getPriority() - prioritizedEventListener.getPriority();
    }
}
