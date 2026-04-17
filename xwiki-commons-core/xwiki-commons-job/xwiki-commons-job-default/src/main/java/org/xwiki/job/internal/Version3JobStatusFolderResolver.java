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

import java.util.List;

import jakarta.annotation.Priority;
import jakarta.inject.Named;

import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;

/**
 * Version 3 of {@link JobStatusFolderResolver} that uses URL encoding but with length limit and additional encoding
 * for reserved characters. Introduced in XWiki 16.10.6 and 17.2.0.
 *
 * @version $Id$
 * @since 17.2.0RC1
 * @since 16.10.6
 */
@Component
@Singleton
@Named("version3")
@Priority(9800)
public class Version3JobStatusFolderResolver extends AbstractJobStatusFolderResolver
{
    private static final int PART_LIMIT = 250;

    @Override
    public List<String> getBaseFolderSegments()
    {
        return List.of("3");
    }

    @Override
    protected List<String> encodeAndSplit(String content)
    {
        if (content == null) {
            return List.of(FOLDER_NULL);
        }

        // Cut each element if it's bigger than 250 bytes (and not characters) since 255 is a very common
        // limit for a single element of the path among file systems.
        return JobIdPathEncoder.encodeAndSplit(content, PART_LIMIT);
    }
}
