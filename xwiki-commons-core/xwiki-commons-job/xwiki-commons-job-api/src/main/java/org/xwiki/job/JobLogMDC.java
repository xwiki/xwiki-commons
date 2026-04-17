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
package org.xwiki.job;

import java.util.List;
import java.util.StringJoiner;

import org.apache.commons.codec.digest.DigestUtils;
import org.xwiki.job.internal.JobIdPathEncoder;
import org.xwiki.stability.Unstable;

/**
 * Utility methods and MDC keys used to tag job logs.
 *
 * @version $Id$
 * @since 18.3.0RC1
 */
@Unstable
public final class JobLogMDC
{
    /**
     * The MDC key set for any log produced while executing a job.
     */
    public static final String KEY_JOB = "job";

    /**
     * The MDC key containing the job type.
     */
    public static final String KEY_JOB_TYPE = "jobType";

    /**
     * The MDC key indicating if the current job declares isolated logging.
     */
    public static final String KEY_JOB_ISOLATED = "jobIsolated";

    /**
     * The MDC key containing the human-readable job identifier.
     */
    public static final String KEY_JOB_ID = "jobId";

    /**
     * The MDC key containing the file-system-safe job identifier used for log routing.
     */
    public static final String KEY_JOB_CLEAN_ID = "jobCleanId";

    private static final int MAX_CLEAN_JOB_ID_LENGTH = 200;

    private JobLogMDC()
    {
    }

    /**
     * @param jobId the raw job identifier
     * @return the human-readable job identifier, or {@code null} if there is no job id
     */
    public static String toId(List<String> jobId)
    {
        if (jobId == null || jobId.isEmpty()) {
            return null;
        }

        StringJoiner joiner = new StringJoiner("/");
        for (String element : jobId) {
            joiner.add(String.valueOf(element));
        }

        return joiner.toString();
    }

    /**
     * @param jobId the raw job identifier
     * @return a file-system-safe job identifier suitable for log routing, or {@code null} if there is no job id
     */
    public static String toCleanId(List<String> jobId)
    {
        String rawJobId = toId(jobId);

        if (rawJobId == null) {
            return null;
        }

        String cleanJobId = JobIdPathEncoder.encode(rawJobId);
        if (cleanJobId.length() > MAX_CLEAN_JOB_ID_LENGTH) {
            String hash = DigestUtils.sha256Hex(rawJobId);
            cleanJobId = cleanJobId.substring(0, MAX_CLEAN_JOB_ID_LENGTH - hash.length() - 1) + '-' + hash;
        }

        return cleanJobId;
    }
}
