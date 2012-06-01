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
import org.xwiki.diff.DiffException;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.display.ExtendedDiffDisplayer;
import org.xwiki.diff.display.InlineDiffChunk;
import org.xwiki.diff.display.InlineDiffDisplayer;
import org.xwiki.diff.display.Splitter;
import org.xwiki.diff.display.UnifiedDiffBlock;
import org.xwiki.diff.display.UnifiedDiffDisplayer;
import org.xwiki.script.service.ScriptService;

/**
 * Provide script oriented APIs to display diff.
 * 
 * @version $Id$
 * @since 4.1RC1
 */
@Component
@Named("diff.display")
@Singleton
public class DiffDisplayerScriptService implements ScriptService
{
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
        return (Exception) this.execution.getContext().getProperty(DiffScriptService.DIFF_ERROR_KEY);
    }

    /**
     * Store a caught exception in the context, so that it can be later retrieved using {@link #getLastError()}.
     * 
     * @param e the exception to store, can be {@code null} to clear the previously stored exception
     * @see #getLastError()
     */
    private void setError(Exception e)
    {
        this.execution.getContext().setProperty(DiffScriptService.DIFF_ERROR_KEY, e);
    }
}
