package org.xwiki.diff;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Chunk<E>
{
    private final int index;

    private List<E> elements;

    public Chunk(int index, List<E> elements)
    {
        this.index = index;
        this.elements = elements;
    }

    public void verify(List<E> target) throws PatchException
    {
        if (getLastIndex() > target.size()) {
            throw new PatchException("Incorrect Chunk: the position of chunk > target size");
        }

        for (int i = 0; i < size(); i++) {
            if (!target.get(index + i).equals(elements.get(i))) {
                throw new PatchException("Incorrect Chunk: the chunk content doesn't match the target");
            }
        }
    }

    public int getIndex()
    {
        return this.index;
    }

    public void setElements(List<E> lines)
    {
        this.elements = lines;
    }

    public List<E> getElements()
    {
        return this.elements;
    }

    public int size()
    {
        return this.elements.size();
    }

    public int getLastIndex()
    {
        return getIndex() + size() - 1;
    }

    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.append(getElements());
        builder.append(getIndex());

        return builder.toHashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Chunk) {
            Chunk<E> otherChunk = (Chunk<E>) obj;
            return getIndex() == otherChunk.getIndex() && ObjectUtils.equals(getElements(), otherChunk.getElements());
        }

        return false;
    }

    @Override
    public String toString()
    {
        return "[position: " + this.index + ", size: " + size() + ", lines: " + this.elements + "]";
    }
}
