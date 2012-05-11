package org.xwiki.diff;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public abstract class AbstractDelta<E> implements Delta<E>
{
    private Chunk<E> previous;

    private Chunk<E> next;

    public AbstractDelta(Chunk<E> previous, Chunk<E> next)
    {
        this.previous = previous;
        this.next = next;
    }

    @Override
    public Chunk<E> getPrevious()
    {
        return this.previous;
    }

    public void setPrevious(Chunk<E> previous)
    {
        this.previous = previous;
    }

    @Override
    public Chunk<E> getNext()
    {
        return this.next;
    }

    public void setNext(Chunk<E> next)
    {
        this.next = next;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(getPrevious());
        builder.append(getNext());

        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Delta) {
            Delta<E> otherDelta = (Delta<E>) obj;

            return ObjectUtils.equals(getPrevious(), otherDelta.getPrevious())
                && ObjectUtils.equals(getNext(), otherDelta.getNext());
        }

        return false;
    }
}
