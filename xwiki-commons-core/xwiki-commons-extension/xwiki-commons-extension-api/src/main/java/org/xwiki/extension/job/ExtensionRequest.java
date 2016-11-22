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
package org.xwiki.extension.job;

import java.beans.Transient;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionRewriter;
import org.xwiki.extension.ExtensionId;
import org.xwiki.job.Request;
import org.xwiki.stability.Unstable;

/**
 * Extension manipulation related {@link Request}.
 *
 * @version $Id$
 * @since 4.0M1
 */
public interface ExtensionRequest extends Request
{
    /**
     * The prefix put behind all job ids.
     * 
     * @since 8.2RC1
     */
    String JOBID_PREFIX = "extension";

    /**
     * The prefix put behind all job ids which are actual actions.
     * 
     * @since 8.2RC1
     */
    String JOBID_ACTION_PREFIX = "action";

    /**
     * The prefix put behind all job ids which are information gathering.
     * 
     * @since 8.2RC1
     */
    String JOBID_PLAN_PREFIX = "plan";

    /**
     * @param prefix the prefix, usually {@link ExtensionRequest#JOBID_ACTION_PREFIX} or
     *            {@link ExtensionRequest#JOBID_PLAN_PREFIX}
     * @param extensionId the id of the extension for which to create a job id
     * @param namespace the namespace for which to create a job id
     * @return the job id
     * @since 8.2RC1
     */
    static List<String> getJobId(String prefix, String extensionId, String namespace)
    {
        List<String> jobId;

        if (namespace != null) {
            jobId = Arrays.asList(JOBID_PREFIX, prefix, extensionId, namespace);
        } else {
            jobId = Arrays.asList(JOBID_PREFIX, prefix, extensionId);
        }

        return jobId;
    }

    /**
     * @return the extension on which to apply the task.
     */
    Collection<ExtensionId> getExtensions();

    /**
     * @return the namespaces on which to apply the task.
     */
    Collection<String> getNamespaces();

    /**
     * @return indicate if the request is applied on specific namespace or all of them
     */
    boolean hasNamespaces();

    /**
     * @return indicate if it's allowed to do modifications on root namespace during the job execution (not taken into
     *         account if the target of the request is root namespace)
     * @since 8.1M1
     */
    default boolean isRootModificationsAllowed()
    {
        return true;
    }

    /**
     * Allow modifying manipulated {@link Extension}s on the fly (change allowed namespaces, dependencies, etc.).
     * 
     * @return the filter
     * @since 8.4.2
     * @since 9.0RC1
     */
    @Transient
    @Unstable
    default ExtensionRewriter getRewriter()
    {
        return null;
    }
}
