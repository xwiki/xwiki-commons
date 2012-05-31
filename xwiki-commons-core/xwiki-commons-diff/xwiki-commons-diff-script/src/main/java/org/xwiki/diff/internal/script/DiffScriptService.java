/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.diff.internal.script;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.diff.DiffConfiguration;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.MergeConfiguration;
import org.xwiki.diff.MergeException;
import org.xwiki.diff.MergeResult;
import org.xwiki.diff.display.ExtendedDiffDisplayer;
import org.xwiki.diff.display.InlineDiffChunk;
import org.xwiki.diff.display.InlineDiffDisplayer;
import org.xwiki.diff.display.Splitter;
import org.xwiki.diff.display.UnifiedDiffBlock;
import org.xwiki.diff.display.UnifiedDiffDisplayer;
import org.xwiki.diff.internal.DefaultDiffResult;
import org.xwiki.diff.internal.DefaultMergeResult;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.script.service.ScriptService;

/**
 * Provide script oriented APIs to do diff and merges.
 * 
 * @version $Id$
 */
@Component
@Named("diff")
@Singleton
public class DiffScriptService implements ScriptService
{
    /**
     * The key under which the last encountered error is stored in the current execution context.
     */
    private static final String DIFF_ERROR_KEY = "scriptservice.diff.error";

    /**
     * The component used to access the execution context.
     */
    @Inject
    private Execution execution;

    /**
     * The component used to split a text into lines.
     */
    @Inject
    @Named("line")
    private Splitter<String, String> lineSplitter;

    /**
     * The component used to split a text into its characters.
     */
    @Inject
    @Named("char")
    private Splitter<String, Character> charSplitter;

    /**
     * The component used to create the diff.
     */
    @Inject
    private DiffManager diffManager;

    /**
     * Produce a diff between the two provided versions.
     * 
     * @param <E> the type of compared elements
     * @param previous the previous version of the content to compare
     * @param next the next version of the content to compare
     * @param configuration the configuration of the diff behavior
     * @return the result of the diff
     */
    public <E> DiffResult<E> diff(List<E> previous, List<E> next, DiffConfiguration<E> configuration)
    {
        DiffResult<E> result;
        try {
            result = this.diffManager.diff(previous, next, configuration);
        } catch (DiffException e) {
            result = new DefaultDiffResult<E>(previous, next);
            result.getLog().add(new LogEvent(LogLevel.ERROR, "Failed to execute diff", null, e));
        }

        return result;
    }

    /**
     * Execute a 3-way merge on provided versions.
     * 
     * @param <E> the type of compared elements
     * @param commonAncestor the common ancestor of the two versions of the content to compare
     * @param next the next version of the content to compare
     * @param current the current version of the content to compare
     * @param configuration the configuration of the merge behavior
     * @return the result of the merge
     */
    public <E> MergeResult<E> merge(List<E> commonAncestor, List<E> next, List<E> current,
        MergeConfiguration<E> configuration)
    {
        MergeResult<E> result;
        try {
            result = this.diffManager.merge(commonAncestor, next, current, configuration);
        } catch (MergeException e) {
            result = new DefaultMergeResult<E>(commonAncestor, next, current);
            result.getLog().add(new LogEvent(LogLevel.ERROR, "Failed to execute merge", null, e));
        }

        return result;
    }

    /**
     * Builds a unified diff between two versions of a text.
     * 
     * @param previous the previous version
     * @param next the next version version
     * @return the list of unified diff blocks
     */
    public List<UnifiedDiffBlock<String>> unified(String previous, String next)
    {
        setError(null);

        try {
            return new UnifiedDiffDisplayer<String>().display(diffManager.diff(lineSplitter.split(previous),
                lineSplitter.split(next), null));
        } catch (Exception e) {
            setError(e);
            return null;
        }
    }

    /**
     * Builds an in-line diff between two versions of a text.
     * 
     * @param previous the previous version
     * @param next the next version
     * @return the list of in-line diff chunks
     */
    public List<InlineDiffChunk<Character>> inline(String previous, String next)
    {
        setError(null);

        try {
            return new InlineDiffDisplayer().display(diffManager.diff(charSplitter.split(previous),
                charSplitter.split(next), null));
        } catch (DiffException e) {
            setError(e);
            return null;
        }
    }

    /**
     * Builds an extended diff between two versions of a text. The extended diff is a mix between a unified diff and an
     * in-line diff: it provides information about both line-level and character-level changes (the later only when a
     * line is modified).
     * 
     * @param previous the previous version
     * @param next the next version
     * @return the list of extended diff blocks
     */
    public List<UnifiedDiffBlock<String>> extended(String previous, String next)
    {
        setError(null);

        try {
            DiffResult<String> diffResult =
                diffManager.diff(lineSplitter.split(previous), lineSplitter.split(next), null);
            return new ExtendedDiffDisplayer<String, Character>(diffManager, charSplitter).display(diffResult);
        } catch (DiffException e) {
            setError(e);
            return null;
        }
    }

    /**
     * Get the error generated while performing the previously called action.
     * 
     * @return an eventual exception or {@code null} if no exception was thrown
     */
    public Exception getLastError()
    {
        return (Exception) this.execution.getContext().getProperty(DIFF_ERROR_KEY);
    }

    /**
     * Store a caught exception in the context, so that it can be later retrieved using {@link #getLastError()}.
     * 
     * @param e the exception to store, can be {@code null} to clear the previously stored exception
     * @see #getLastError()
     */
    private void setError(Exception e)
    {
        this.execution.getContext().setProperty(DIFF_ERROR_KEY, e);
    }
}
