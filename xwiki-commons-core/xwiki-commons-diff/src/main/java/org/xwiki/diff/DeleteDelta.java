package org.xwiki.diff;

import java.util.List;

public class DeleteDelta<E> extends AbstractDelta<E>
{
    public DeleteDelta(Chunk<E> original, Chunk<E> revised)
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
    }

    @Override
    public void restore(List<E> target)
    {
        int index = getNext().getIndex();
        List<E> elements = getPrevious().getElements();
        for (int i = 0; i < elements.size(); i++) {
            target.add(index + i, elements.get(i));
        }
    }

    @Override
    public TYPE getType()
    {
        return Delta.TYPE.DELETE;
    }

    @Override
    public void verify(List<E> target) throws PatchException
    {
        getPrevious().verify(target);
    }
}
