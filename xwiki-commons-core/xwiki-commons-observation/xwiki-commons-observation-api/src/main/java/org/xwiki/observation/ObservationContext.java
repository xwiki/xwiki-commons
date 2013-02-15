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

import org.xwiki.component.annotation.Role;
import org.xwiki.observation.event.BeginEvent;

/**
 * Allow to get observation related information from the execution context.
 * 
 * @version $Id$
 * @since 3.2M1
 */
@Role
public interface ObservationContext
{
    /**
     * Indicates if the provided begin event is still in the current event stack.
     * 
     * @param event the event to check
     * @return true if the provided event is found in the current event stack
     */
    boolean isIn(BeginEvent event);
}
