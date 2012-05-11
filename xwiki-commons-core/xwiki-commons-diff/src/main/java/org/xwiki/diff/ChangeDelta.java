package org.xwiki.diff;

import java.util.List;

public class ChangeDelta<E> extends AbstractDelta<E>
{
    public ChangeDelta(Chunk<E> original, Chunk<E> revised)
    {
        super(original, revised);
    }

    @Override
    public void apply(List<E> target) throws PatchException
    {
        verify(target);

        int index = getPrevious().getIndex();
        int size = getPrevious().size();
        for (int i = 0; i < size; i++) {
            target.remove(index);
        }

        int i = 0;
        for (E element : getNext().getElements()) {
            target.add(index + i, element);
            i++;
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

        int i = 0;
        for (E element : getPrevious().getElements()) {
            target.add(index + i, element);
            i++;
        }
    }

    @Override
    public void verify(List<E> target) throws PatchException
    {
        getPrevious().verify(target);
        if (getPrevious().getIndex() > target.size()) {
            throw new PatchException("Incorrect patch for delta: delta original position > target size");
        }
    }

    @Override
    public TYPE getType()
    {
        return Delta.TYPE.CHANGE;
    }
}
