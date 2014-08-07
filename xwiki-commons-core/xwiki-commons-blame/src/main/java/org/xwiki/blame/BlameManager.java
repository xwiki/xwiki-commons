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

import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * Annotate/Blame/Praise tool that allow building annotated content from the content of all revision starting with
 * the most recent one.
 *
 * @version $Id$
 * @since 6.2M2
 */
@Role
@Unstable
public interface BlameManager
{
    /**
     * Annotate content with current revision based on a diff with a previous revision.
     *
     * @param <R> type of the revision object that old metadata about the revision.
     * @param <E> type of the element to annotate (ie: String holding a line).
     * @param content the annotated content (up to the revision preceding the one given), use null to start a new
     *                blame.
     * @param revision the revision metadata to associate with the given revision.
     * @param previous the content of the previous revision to diff against the currently annotated content, use the
     *                 latest revision to start a new blame.
     * @return the updated annotated content.
     */
    <R, E> AnnotatedContent<R, E> blame(AnnotatedContent<R, E> content, R revision, List<E> previous);
}
