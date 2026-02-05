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

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.xwiki.job.JobManagerConfiguration;

/**
 * Abstract base class for {@link JobStatusFolderResolver} implementations to avoid code duplication.
 *
 * @version $Id$
 * @since 17.2.0RC1
 * @since 16.10.6
 */
public abstract class AbstractJobStatusFolderResolver implements JobStatusFolderResolver
{
    /**
     * The encoded version of a <code>null</code> value in the id list.
     */
    protected static final String FOLDER_NULL = "&null";

    /**
     * Used to get the storage directory.
     */
    @Inject
    protected JobManagerConfiguration configuration;

    @Override
    public File getFolder(List<String> id)
    {
        File folder = this.configuration.getStorage();

        for (String pathSegment : getFolderSegments(id)) {
            folder = new File(folder, pathSegment);
        }

        return folder;
    }

    @Override
    public List<String> getFolderSegments(List<String> jobID)
    {
        List<String> result = new ArrayList<>(getBaseFolderSegments());

        if (jobID != null) {
            for (String idElement : jobID) {
                result.addAll(encodeAndSplit(idElement));
            }
        }

        return result;
    }

    protected String nullAwareURLEncode(String value)
    {
        String encoded;

        if (value != null) {
            encoded = URLEncoder.encode(value, StandardCharsets.UTF_8);
        } else {
            encoded = FOLDER_NULL;
        }

        return encoded;
    }

    /**
     * @return the base folder segments for the job status folder, override to provide a custom base folder
     */
    protected List<String> getBaseFolderSegments()
    {
        return List.of();
    }

    /**
     * @param idElement the id element to encode and split
     * @return the encoded and split id element
     */
    protected abstract List<String> encodeAndSplit(String idElement);
}
