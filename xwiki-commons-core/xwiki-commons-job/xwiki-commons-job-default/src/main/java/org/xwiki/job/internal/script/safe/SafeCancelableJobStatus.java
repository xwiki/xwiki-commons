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
package org.xwiki.job.internal.script.safe;

import org.xwiki.job.event.status.CancelableJobStatus;
import org.xwiki.script.internal.safe.ScriptSafeProvider;

/**
 * Provide a public script access to a cancelable job status.
 * 
 * @param <J>
 * @version $Id$
 * @since 10.2
 */
public class SafeCancelableJobStatus<J extends CancelableJobStatus> extends SafeJobStatus<J>
    implements CancelableJobStatus
{
    /**
     * @param status the wrapped job status
     * @param safeProvider the provider of instances safe for public scripts
     */
    public SafeCancelableJobStatus(J status, ScriptSafeProvider<?> safeProvider)
    {
        super(status, safeProvider);
    }

    @Override
    public boolean isCancelable()
    {
        return getWrapped().isCancelable();
    }

    @Override
    public void cancel()
    {
        // Don't allow anyone to cancel a job
    }

    @Override
    public boolean isCanceled()
    {
        return getWrapped().isCanceled();
    }
}
