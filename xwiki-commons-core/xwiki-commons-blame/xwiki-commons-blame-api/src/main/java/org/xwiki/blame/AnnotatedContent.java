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
package org.xwiki.blame;

import java.util.Iterator;

/**
 * Annotated content.
 *
 * @param <R> type of the revision object that old metadata about the revision.
 * @param <E> type of the element to annotate (ie: String holding a line).
 * @version $Id$
 * @since 6.2M2
 */
public interface AnnotatedContent<R, E> extends Iterable<AnnotatedElement<R, E>>
{
    @Override
    Iterator<AnnotatedElement<R, E>> iterator();

    /**
     * @return the oldest revision provided to this annotated content. If you do not have further revision in
     * the past, you may consider all unannotated line to belong to that revision. To do so, you may also call
     * BlameManager#blame(AnnotatedContent, null, null) with this annotated content.
     */
    R getOldestRevision();

    /**
     * @return true if all elements as receive its source revision, which means that further call to BlameManager#blame
     *              are useless.
     */
    boolean isEntirelyAnnotated();
}
