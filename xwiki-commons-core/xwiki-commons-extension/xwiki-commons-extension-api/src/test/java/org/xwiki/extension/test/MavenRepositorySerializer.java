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
package org.xwiki.extension.test;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

public class MavenRepositorySerializer extends AbstractRepositorySerializer
{
    public static final String DEFAULT_GROUPID = "maven";

    public MavenRepositorySerializer(File root)
    {
        super(root);
    }

    @Override
    public String resolveId(String id)
    {
        int index = id.indexOf(':');
        if (index > 0) {
            return id;
        }

        return DEFAULT_GROUPID + ':' + id;
    }

    @Override
    public File getFile(String id, String version, String type)
    {
        String groupId;
        String artifactId;

        int index = id.indexOf(':');
        if (index > 0) {
            groupId = id.substring(0, index);
            artifactId = id.substring(index + 1);
        } else {
            groupId = DEFAULT_GROUPID;
            artifactId = id;
        }

        File path = this.root;
        for (String element : StringUtils.split(groupId, '.')) {
            path = new File(path, element);
        }

        path = new File(path, artifactId);

        path = new File(path, version);

        path = new File(path, artifactId + '-' + version + '.' + type);

        return path;
    }
}
