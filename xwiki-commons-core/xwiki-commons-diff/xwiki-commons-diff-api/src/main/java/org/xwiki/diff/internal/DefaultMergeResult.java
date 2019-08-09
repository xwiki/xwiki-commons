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

import java.util.ArrayList;
import java.util.List;

import org.xwiki.diff.Conflict;
import org.xwiki.diff.MergeResult;
import org.xwiki.logging.LogQueue;

/**
 * Default implementation of {@link MergeResult}.
 *
 * @param <E> the type of compared elements
 * @version $Id$
 */
public class DefaultMergeResult<E> implements MergeResult<E>
{
    /**
     * @see #getCommonAncestor()
     */
    private List<E> commonAncestor;

    /**
     * @see #getNext()
     */
    private List<E> next;

    /**
     * @see #getCurrent()
     */
    private List<E> current;

    /**
     * @see #getMerged()
     */
    private List<E> merged;

    /**
     * @see #getConflicts()
     */
    private List<Conflict> conflicts;

    /**
     * @see #getLog()
     */
    private LogQueue log = new LogQueue();

    /**
     * @param commonAncestor the common ancestor
     * @param next the new version
     * @param current the current version
     */
    public DefaultMergeResult(List<E> commonAncestor, List<E> next, List<E> current)
    {
        this.commonAncestor = commonAncestor;
        this.next = next;
        this.current = current;

        // Default to current
        this.merged = current;
        this.conflicts = new ArrayList<>();
    }

    @Override
    public List<E> getCommonAncestor()
    {
        return this.commonAncestor;
    }

    @Override
    public List<E> getNext()
    {
        return this.next;
    }

    @Override
    public List<E> getCurrent()
    {
        return this.current;
    }

    @Override
    public LogQueue getLog()
    {
        return this.log;
    }

    @Override
    public List<E> getMerged()
    {
        return this.merged;
    }

    @Override
    public List<Conflict> getConflicts()
    {
        return this.conflicts;
    }

    /**
     * Record a new conflict.
     * @param conflict the conflict to be recorded.
     * @since 11.7RC1
     */
    public void addConflict(Conflict conflict)
    {
        this.conflicts.add(conflict);
    }

    /**
     * @param merged the merged version
     */
    public void setMerged(List<E> merged)
    {
        this.merged = merged;
    }
}
