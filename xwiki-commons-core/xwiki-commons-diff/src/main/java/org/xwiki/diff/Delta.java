package org.xwiki.diff;

import java.util.List;

public interface Delta<E>
{
    public enum TYPE
    {
        CHANGE,
        DELETE,
        INSERT
    }

    public abstract void verify(List<E> target) throws PatchException;

    public abstract void apply(List<E> target) throws PatchException;

    public abstract void restore(List<E> target);

    public abstract TYPE getType();

    public Chunk<E> getPrevious();

    public Chunk<E> getNext();
}
