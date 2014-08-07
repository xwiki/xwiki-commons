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

package org.xwiki.blame.internal;

import org.xwiki.blame.AnnotatedElement;

/**
 * A single element linked with its source revision.
 *
 * @param <R> type of the revision object that old metadata about the revision.
 * @param <E> type of the element to annotate (ie: String holding a line).
 * @version $Id$
 * @since 6.2M2
 */
public class DefaultAnnotatedElement<R, E> implements AnnotatedElement<R, E>
{
    private E element;
    private R revision;

    DefaultAnnotatedElement(R revision, E element)
    {
        this.revision = revision;
        this.element = element;
    }

    @Override
    public E getElement()
    {
        return element;
    }

    @Override
    public R getRevision()
    {
        return revision;
    }
}
