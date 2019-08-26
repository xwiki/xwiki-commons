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
package org.xwiki.diff.script;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.diff.Conflict;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.display.InlineDiffChunk;
import org.xwiki.diff.display.InlineDiffDisplayer;
import org.xwiki.diff.display.Splitter;
import org.xwiki.diff.display.UnifiedDiffBlock;
import org.xwiki.diff.display.UnifiedDiffConfiguration;
import org.xwiki.diff.display.UnifiedDiffDisplayer;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

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
    private Splitter<String, Character> charSplitter;

    /**
     * The component used to create the diff.
     */
    @Inject
    private DiffManager diffManager;

    /**
     * The component used to display in-line diffs.
     */
    @Inject
    private InlineDiffDisplayer inlineDiffDisplayer;

    /**
     * The component used to display unified diffs.
     */
    @Inject
    private UnifiedDiffDisplayer unifiedDiffDisplayer;

    /**
     * Builds an in-line diff between two versions of a list of elements.
     *
     * @param previous the previous version
     * @param next the next version
     * @param <E> the type of elements that are compared to produce the diff
     * @return the list of in-line diff chunks
     */
    public <E> List<InlineDiffChunk<E>> inline(List<E> previous, List<E> next)
    {
        setError(null);

        try {
            return this.inlineDiffDisplayer.display(this.diffManager.diff(previous, next, null));
        } catch (DiffException e) {
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
            return this.inlineDiffDisplayer
                .display(this.diffManager.diff(this.charSplitter.split(previous), this.charSplitter.split(next), null));
        } catch (DiffException e) {
            setError(e);
            return null;
        }
    }

    /**
     * Builds an unified diff between two versions of a text. The unified diff provides information about both
     * line-level and character-level changes (the later only when a line is modified).
     *
     * @param previous the previous version
     * @param next the next version
     * @param conflicts the {@link Conflict} to take into consideration for the display.
     * @return the list of extended diff blocks
     * @since 11.7RC1
     */
    @Unstable
    public List<UnifiedDiffBlock<String, Character>> unified(String previous, String next,
        List<Conflict<String>> conflicts)
    {
        setError(null);

        try {
            DiffResult<String> diffResult =
                this.diffManager.diff(this.lineSplitter.split(previous), this.lineSplitter.split(next), null);
            UnifiedDiffConfiguration<String, Character> config = this.unifiedDiffDisplayer.getDefaultConfiguration();
            config.setSplitter(this.charSplitter);
            return this.unifiedDiffDisplayer.display(diffResult, conflicts, config);
        } catch (DiffException e) {
            setError(e);
            return null;
        }
    }

    /**
     * Builds an unified diff between two versions of a text. The unified diff provides information about both
     * line-level and character-level changes (the later only when a line is modified).
     *
     * @param previous the previous version
     * @param next the next version
     * @return the list of extended diff blocks
     */
    public List<UnifiedDiffBlock<String, Character>> unified(String previous, String next)
    {
        return unified(previous, next, null);
    }

    /**
     * Builds an unified diff between two versions of a list of elements. If a splitter is provided through the given
     * configuration object then the unified diff will display changes at two levels of granularity: elements and their
     * sub-elements.
     *
     * @param previous the previous version
     * @param next the next version
     * @param config the configuration object
     * @param <E> the type of composite elements that are compared to produce the first level diff
     * @param <F> the type of sub-elements that are compared to produce the second level diff when a composite element
     * @param conflicts the {@link Conflict} to take into consideration for the display.
     * @return the list of extended diff blocks
     * @since 11.7RC1
     */
    @Unstable
    public <E, F> List<UnifiedDiffBlock<E, F>> unified(List<E> previous, List<E> next,
        UnifiedDiffConfiguration<E, F> config, List<Conflict<E>> conflicts)
    {
        setError(null);

        try {
            return this.unifiedDiffDisplayer.display(this.diffManager.diff(previous, next, null), conflicts, config);
        } catch (DiffException e) {
            setError(e);
            return null;
        }
    }

    /**
     * Builds an unified diff between two versions of a list of elements. If a splitter is provided through the given
     * configuration object then the unified diff will display changes at two levels of granularity: elements and their
     * sub-elements.
     *
     * @param previous the previous version
     * @param next the next version
     * @param config the configuration object
     * @param <E> the type of composite elements that are compared to produce the first level diff
     * @param <F> the type of sub-elements that are compared to produce the second level diff when a composite element
     * @return the list of extended diff blocks
     */
    public <E, F> List<UnifiedDiffBlock<E, F>> unified(List<E> previous, List<E> next,
        UnifiedDiffConfiguration<E, F> config)
    {
        return unified(previous, next, config, null);
    }

    /**
     * @param <E> the type of composite elements that are compared to produce the first level diff
     * @param <F> the type of sub-elements that are compared to produce the second level diff when a composite element
     *            is modified
     * @return the unified diff configuration
     */
    public <E, F> UnifiedDiffConfiguration<E, F> getUnifiedDiffConfiguration()
    {
        return this.unifiedDiffDisplayer.getDefaultConfiguration();
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
