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

import java.util.List;

import javax.inject.Singleton;

import org.xwiki.blame.AnnotatedContent;
import org.xwiki.blame.BlameManager;
import org.xwiki.component.annotation.Component;

/**
 * Default implementation of {@link org.xwiki.blame.BlameManager}.
 *
 * @version $Id$
 * @since 6.2M2
 */
@Component
@Singleton
public class DefaultBlameManager implements BlameManager
{
    @Override
    public <R, E> AnnotatedContent<R, E> blame(AnnotatedContent<R, E> content, R revision, List<E> previous)
    {
        if (content == null) {
            if (previous == null || revision == null) {
                return null;
            }
            return new DefaultAnnotatedContent<>(revision, previous);
        }

        if (!(content instanceof DefaultAnnotatedContent)) {
            throw new IllegalArgumentException("Incompatible annotated content type provided");
        }

        ((DefaultAnnotatedContent<R, E>) content).analyseRevision(revision, previous);
        return content;
    }
}
