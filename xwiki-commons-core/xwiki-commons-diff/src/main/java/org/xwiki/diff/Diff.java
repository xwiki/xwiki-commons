package org.xwiki.diff;

import java.util.List;

public interface Diff
{
    <E> Patch<E> diff(List<E> previous, List<E> next, DiffConfiguration<E> diff) throws DiffException;

    <E> MergeResult<E> merge(List<E> commonAncestor, List<E> previous, List<E> next, MergeConfiguration<E> configuration)
        throws MergeException;
}
