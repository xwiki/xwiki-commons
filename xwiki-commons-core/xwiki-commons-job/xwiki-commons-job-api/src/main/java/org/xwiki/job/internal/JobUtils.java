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
package org.xwiki.job.internal;

import org.xwiki.job.annotation.Serializable;
import org.xwiki.job.event.status.JobStatus;

/**
 * Various job related utility methods.
 * 
 * @version $Id$
 * @since 12.6RC1
 * @since 11.10.6
 */
public final class JobUtils
{
    private JobUtils()
    {
    }

    /**
     * @param status the job status
     * @return true if the status is serializable
     */
    public static boolean isSerializable(JobStatus status)
    {
        if (status.getRequest().getId() == null || !status.isSerialized()) {
            return false;
        }

        Serializable serializable = status.getClass().getAnnotation(Serializable.class);
        if (serializable != null) {
            return serializable.value();
        }

        return status instanceof java.io.Serializable;
    }
}
