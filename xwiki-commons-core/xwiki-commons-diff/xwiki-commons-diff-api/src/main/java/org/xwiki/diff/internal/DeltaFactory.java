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

import org.xwiki.diff.Chunk;
import org.xwiki.diff.Delta;

/**
 * A factory for creating delta element based on their type.
 *
 * @version $Id$
 * @since 11.8RC1
 */
public final class DeltaFactory
{
    private static final String ERROR_MESSAGE = "Cannot find a delta constructor for the type [%s].";

    private DeltaFactory()
    {
    }

    /**
     * Create a new delta based on the given argument.
     *
     * @param previous the previous chunk of the delta.
     * @param next the next chunk of the delta.
     * @param type the type of the delta.
     * @param <E> the type of element being compared.
     * @return a new instance of a delta.
     */
    public static <E> Delta<E> createDelta(Chunk<E> previous, Chunk<E> next, Delta.Type type)
    {
        switch (type)
        {
            case CHANGE:
                return new ChangeDelta<E>(previous, next);

            case DELETE:
                return new DeleteDelta<E>(previous, next);

            case INSERT:
                return new InsertDelta<E>(previous, next);

            default:
                throw new IllegalArgumentException(String.format(ERROR_MESSAGE, type));
        }
    }

    /**
     * Create a new delta based on the given argument.
     *
     * @param original the previous chunk of the delta.
     * @param revised the next chunk of the delta.
     * @param type the type of the delta.
     * @param <E> the type of element being compared.
     * @return a new instance of a delta.
     */
    public static <E> Delta<E> createDelta(difflib.Chunk<E> original, difflib.Chunk<E> revised, Delta.Type type)
    {
        switch (type)
        {
            case CHANGE:
                return new ChangeDelta<E>(original, revised);

            case DELETE:
                return new DeleteDelta<E>(original, revised);

            case INSERT:
                return new InsertDelta<E>(original, revised);

            default:
                throw new IllegalArgumentException(String.format(ERROR_MESSAGE, type));
        }
    }

}
