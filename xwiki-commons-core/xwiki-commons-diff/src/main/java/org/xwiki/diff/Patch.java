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
package org.xwiki.diff;

import java.util.List;

/**
 * A list of deltas between two versions.
 * 
 * @param <E> the type of compared elements
 * @version $Id$
 */
public interface Patch<E> extends List<Delta<E>>
{
    /**
     * Apply the patch.
     * 
     * @param target the list on which to apply the patch
     * @return the modified list
     * @throws PatchException failed to apply the patch
     */
    List<E> apply(List<E> target) throws PatchException;

    /**
     * Invert the patch.
     * 
     * @param target the list on which to apply an inverted version of the patch
     * @return the modified list
     * @throws PatchException failed to apply the patch
     */
    List<E> restore(List<E> target) throws PatchException;
}
