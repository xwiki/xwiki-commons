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

import org.xwiki.diff.MergeResult;
import org.xwiki.logging.LogQueue;

public class DefaultMergeResult<E> implements MergeResult<E>
{
    private List<E> commonAncestor;

    private List<E> previous;

    private List<E> next;

    private List<E> merged;

    private LogQueue log = new LogQueue();

    public DefaultMergeResult(List<E> commonAncestor, List<E> previous, List<E> next)
    {
        this.commonAncestor = commonAncestor;
        this.previous = previous;
        this.next = next;
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
    public List<E> getPrevious()
    {
        return this.previous;
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

    public void setMerged(List<E> merged)
    {
        this.merged = merged;
    }
}
