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
package org.xwiki.diff.display;

import java.util.List;

import org.xwiki.component.annotation.Role;
import org.xwiki.diff.DiffResult;

/**
 * Displays a {@link DiffResult} as an in-line diff. An in-line diff is made of a list of chunks, each marked as added,
 * removed or unmodified. For instance, if changes are computed at word level then you could have this in-line diff:
 * 
 * <pre>
 * {@code the <del>quick</del><ins>sick</ins> brown fox}
 * </pre>
 * 
 * At character level the diff looks a bit different:
 * 
 * <pre>
 * {@code the <del>qu</del><ins>s</ins>ick brown fox}
 * </pre>
 * 
 * In this case the first chunk is "the ", an unmodified chunk, made of 4 characters and the second chunk is "qu", a
 * removed chunk, made of 2 characters. An in-line diff can be displayed either as you've seen above, mixing added and
 * removed chunks in one line, or it can be displayed on two lines, one showing the removed chunks and the other the
 * added chunks:
 * 
 * <pre>
 * {@code the <del>quick</del> brown fox
 * the <ins>sick</ins> brown fox}
 * </pre>
 * 
 * @version $Id$
 * @since 4.1RC1
 */
@Role
public interface InlineDiffDisplayer
{
    /**
     * Displays the given diff result as an in-line diff. An in-line diff is a list of group of elements, each group
     * being marked as added, removed or unmodified. The in-line diff includes all the elements from the previous and
     * the next version that were compared to produce the diff:
     * <ul>
     * <li>the elements found in the previous version but not in the next version are marked at removed</li>
     * <li>the elements from the next version that are not present in the previous version are marked as added</li>
     * <li>the rest of the elements that are found in both versions are marked as unmodified.</li>
     * </ul>
     * If changes are computed at character level, i.e. the type of elements that are compared is {@link Character},
     * then the in-line diff between "the quick fox" and "the sick fox" is:
     * 
     * <pre>
     * {@code the <del>qu</del><ins>s</ins>ick fox}
     * </pre>
     * 
     * and is made of 4 groups of {@link Character}s: "the " unmodified, "qu" removed, "s" added and "ick fox"
     * unmodified.
     * 
     * @param <E> the type of elements that are add/remove/modified in the given diff result (specifies the granularity
     *            level of changes)
     * @param diffResult the diff result to be displayed
     * @return the list of chunks that form the in-line diff
     */
    <E> List<InlineDiffChunk<E>> display(DiffResult<E> diffResult);
}
