package org.xwiki.diff;

import java.util.List;

import org.xwiki.logging.LogQueue;

/**
 * The result of the execution of a diff on two lists.
 * 
 * @param <E> the type of compared elements
 * @version $Id$
 */
public interface DiffResult<E>
{
    /**
     * @return the list before the modification
     */
    List<E> getNext();

    /**
     * @return the list after the modification
     */
    List<E> getPrevious();

    /**
     * @return the log of what append during the diff executing
     */
    LogQueue getLog();

    /**
     * @return the produced patch
     */
    Patch<E> getPatch();

    /**
     * @return the unified diff
     */
    Patch<E> getUnifiedDiff();
}
