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
package org.xwiki.logging.marker;

import org.slf4j.Marker;
import org.xwiki.stability.Unstable;

/**
 * Custom {@link Marker}s which contains values (so which are a bit more than {@link Marker} as defined by SLF4J).
 * 
 * @version $Id$
 * @since 5.4M1
 */
@Unstable
public interface ContainerMarker extends Marker
{
    /**
     * @param <M> the type of the marker
     * @param name the name of the marker
     * @return the marker of null if none could be found
     */
    <M extends Marker> M get(String name);
}
