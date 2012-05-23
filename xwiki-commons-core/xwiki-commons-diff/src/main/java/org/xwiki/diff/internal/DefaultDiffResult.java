package org.xwiki.diff.internal;

import java.util.List;

import org.xwiki.diff.DiffResult;
import org.xwiki.diff.Patch;
import org.xwiki.logging.LogQueue;

public class DefaultDiffResult<E> implements DiffResult<E>
{
    private List<E> previous;

    private List<E> next;

    private LogQueue log = new LogQueue();

    private Patch<E> patch;

    public DefaultDiffResult(List<E> previous, List<E> next)
    {
        this.previous = previous;
        this.next = next;
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
    public Patch<E> getPatch()
    {
        return this.patch;
    }

    public void setPatch(Patch<E> patch)
    {
        this.patch = patch;
    }
}
