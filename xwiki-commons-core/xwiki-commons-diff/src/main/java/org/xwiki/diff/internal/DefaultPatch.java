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

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.xwiki.diff.Delta;
import org.xwiki.diff.Patch;
import org.xwiki.diff.PatchException;

public class DefaultPatch<E> extends LinkedList<Delta<E>> implements Patch<E>
{
    private static final long serialVersionUID = 1L;

    @Override
    public List<E> apply(List<E> target) throws PatchException
    {
        List<E> result = new LinkedList<E>(target);
        ListIterator<Delta<E>> it = listIterator(size());
        while (it.hasPrevious()) {
            Delta<E> delta = it.previous();
            delta.apply(result);
        }

        return result;
    }

    @Override
    public List<E> restore(List<E> target)
    {
        List<E> result = new LinkedList<E>(target);
        ListIterator<Delta<E>> it = listIterator(size());
        while (it.hasPrevious()) {
            Delta<E> delta = it.previous();
            delta.restore(result);
        }

        return result;
    }
}
