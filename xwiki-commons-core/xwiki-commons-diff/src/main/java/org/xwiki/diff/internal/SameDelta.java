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
package org.xwiki.diff.internal;

import java.util.List;

import org.xwiki.diff.Chunk;
import org.xwiki.diff.PatchException;

/**
 * Implementation of {@link org.xwiki.diff.Delta} representing a chunk which is the same in both version.
 * 
 * @param <E> the type of compared elements
 * @version $Id$
 */
public class SameDelta<E> extends AbstractDelta<E>
{
    /**
     * @param chunk the chunk
     */
    public SameDelta(Chunk<E> chunk)
    {
        super(chunk, chunk, null);
    }

    @Override
    public void apply(List<E> target) throws PatchException
    {
        // Do nothing
    }

    @Override
    public void restore(List<E> target)
    {
        // Do nothing
    }

    @Override
    public void verify(List<E> target) throws PatchException
    {
        // Do nothing
    }
}
