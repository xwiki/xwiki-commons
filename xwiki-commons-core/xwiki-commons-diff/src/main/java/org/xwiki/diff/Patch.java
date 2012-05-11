package org.xwiki.diff;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class Patch<E> extends LinkedList<Delta<E>>
{
    private static final long serialVersionUID = 1L;

    public List<E> apply(List<E> target) throws PatchException
    {
        List<E> result = new LinkedList<E>(target);
        ListIterator<Delta<E>> it = listIterator(size());
        while (it.hasPrevious()) {
            Delta<E> delta = it.previous();
            delta.apply(result);
        }

        return result;
    }

    public List<E> restore(List<E> target)
    {
        List<E> result = new LinkedList<E>(target);
        ListIterator<Delta<E>> it = listIterator(size());
        while (it.hasPrevious()) {
            Delta<E> delta = it.previous();
            delta.restore(result);
        }

        return result;
    }
}
