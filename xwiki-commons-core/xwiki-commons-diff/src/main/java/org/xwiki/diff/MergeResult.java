package org.xwiki.diff;

import java.util.List;

public class MergeResult<E>
{
    private boolean modified;

    private List<E> commonAncestor;

    private List<E> previous;

    private List<E> next;

    public MergeResult(List<E> commonAncestor, List<E> previous, List<E> next)
    {
        this.commonAncestor = commonAncestor;
        this.previous = previous;
        this.next = next;
    }

    public List<E> getCommonAncestor()
    {
        return this.commonAncestor;
    }

    public List<E> getNext()
    {
        return this.next;
    }

    public List<E> getPrevious()
    {
        return this.previous;
    }

    public boolean isModified()
    {
        return this.modified;
    }

    public void setModified(boolean b)
    {
        this.modified = true;
    }
}
