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
import org.xwiki.diff.DiffException;
import org.xwiki.diff.Patch;
import org.xwiki.diff.PatchException;

/**
 * Default implementation of {@link Patch}.
 *
 * @param <E> the type of compared elements
 * @version $Id$
 */
public class DefaultPatch<E> extends LinkedList<Delta<E>> implements Patch<E>
{
    /**
     * Provides an id for serialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public DefaultPatch()
    {
    }

    /**
     * Convert {@link difflib.Patch} into {@link Patch}.
     *
     * @param patch the {@link difflib.Patch} to convert
     * @throws DiffException error when converting the patch
     */
    public DefaultPatch(difflib.Patch<E> patch) throws DiffException
    {
        for (difflib.Delta<E> delta : patch.getDeltas()) {
            add(toDelta(delta));
        }
    }

    /**
     * Convert {@link difflib.Delta} into {@link Delta}.
     *
     * @param delta the {@link difflib.Delta} to convert
     * @return the {@link Delta}
     * @throws DiffException error when converting the delta
     */
    private Delta<E> toDelta(difflib.Delta<E> delta) throws DiffException
    {
        try {
            return DeltaFactory
                .createDelta(delta.getOriginal(), delta.getRevised(), Delta.Type.valueOf(delta.getType().name()));
        } catch (IllegalArgumentException e) {
            throw new DiffException(String.format("Failed to convert [%s] info [%s]. Unknown type [%s]", delta
                .getClass().getName(), Delta.class.getName(), delta.getType().toString()), e);
        }
    }

    @Override
    public List<E> apply(List<E> target) throws PatchException
    {
        List<E> result = new LinkedList<>(target);
        ListIterator<Delta<E>> it = listIterator(size());
        while (it.hasPrevious()) {
            Delta<E> delta = it.previous();
            delta.apply(result);
        }

        return result;
    }

    @Override
    public List<E> restore(List<E> target) throws PatchException
    {
        List<E> result = new LinkedList<>(target);
        ListIterator<Delta<E>> it = listIterator(size());
        while (it.hasPrevious()) {
            Delta<E> delta = it.previous();
            delta.restore(result);
        }

        return result;
    }
}
