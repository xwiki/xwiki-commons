package org.xwiki.diff;

import java.util.List;

import org.xwiki.logging.LogQueue;

public interface DiffResult<E>
{
    List<E> getNext();

    List<E> getPrevious();

    LogQueue getLog();

    Patch<E> getPatch();
}
