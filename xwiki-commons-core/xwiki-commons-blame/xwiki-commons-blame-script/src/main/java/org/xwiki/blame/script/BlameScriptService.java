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

package org.xwiki.blame.script;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.blame.AnnotatedContent;
import org.xwiki.blame.BlameManager;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.diff.display.Splitter;
import org.xwiki.script.service.ScriptService;

/**
 * Provide script oriented APIs to do annotate/blame/praise.
 *
 * @version $Id$
 * @since 6.2M2
 */
@Component
@Named("blame")
@Singleton
public class BlameScriptService implements ScriptService
{
    /**
     * The key under which the last encountered error is stored in the current execution context.
     */
    static final String BLAME_ERROR_KEY = "scriptservice.blame.error";

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
     * The component used to create the blame.
     */
    @Inject
    private BlameManager blameManager;


    /**
     * Annotate content with current revision based on a diff with a previous revision.
     *
     * @param <R> type of the revision object that old metadata about the revision.
     * @param <E> type of the element to annotate (ie: String holding a line).
     * @param content the annotated content (up to the revision preceding the one given), use null to start a new
     *                blame.
     * @param revision the revision metadata to associate with the given revision.
     * @param previous the content of the previous revision to diff against the currently annotated content, use the
     *                 latest revision to start a new blame.
     * @return the updated annotated content.
     */
    public <R, E> AnnotatedContent<R, E> blame(AnnotatedContent<R, E> content, R revision, List<E> previous)
    {
        setError(null);
        try {
            return blameManager.blame(content, revision, previous);
        } catch (Exception e) {
            setError(e);
            return null;
        }
    }

    /**
     * Annotate content with current revision based on a diff with a previous revision.
     *
     * @param <R> type of the revision object that old metadata about the revision.
     * @param content the annotated content (up to the revision preceding the one given), use null to start a new
     *                blame.
     * @param revision the revision metadata to associate with the given revision.
     * @param previous the content of the previous revision to diff against the currently annotated content, use the
     *                 latest revision to start a new blame.
     * @return the updated annotated content.
     */
    public <R> AnnotatedContent<R, String> blame(AnnotatedContent<R, String> content, R revision, String previous)
    {
        setError(null);
        try {
            return blameManager.blame(content, revision, lineSplitter.split(previous));
        } catch (Exception e) {
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
        return (Exception) this.execution.getContext().getProperty(BLAME_ERROR_KEY);
    }

    /**
     * Store a caught exception in the context, so that it can be later retrieved using {@link #getLastError()}.
     *
     * @param e the exception to store, can be {@code null} to clear the previously stored exception
     * @see #getLastError()
     */
    private void setError(Exception e)
    {
        this.execution.getContext().setProperty(BLAME_ERROR_KEY, e);
    }
}
