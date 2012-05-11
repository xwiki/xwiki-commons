package org.xwiki.diff;

import java.util.List;

public class InsertDelta<E> extends AbstractDelta<E>
{
    public InsertDelta(Chunk<E> original, Chunk<E> revised)
    {
        super(original, revised);
    }

    @Override
    public void apply(List<E> target) throws PatchException
    {
        verify(target);

        int index = getPrevious().getIndex();
        List<E> elements = getNext().getElements();
        for (int i = 0; i < elements.size(); i++) {
            target.add(index + i, elements.get(i));
        }
    }

    @Override
    public void restore(List<E> target)
    {
        int index = getNext().getIndex();
        int size = getNext().size();
        for (int i = 0; i < size; i++) {
            target.remove(index);
        }
    }

    @Override
    public void verify(List<E> target) throws PatchException
    {
        if (getPrevious().getIndex() > target.size()) {
            throw new PatchException("Incorrect patch for delta: delta original position > target size");
        }

    }

    @Override
    public TYPE getType()
    {
        return Delta.TYPE.INSERT;
    }
}
