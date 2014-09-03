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
package org.xwiki.extension.repository.aether.internal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.xwiki.extension.DefaultExtensionScmConnection;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionScmConnection;
import org.xwiki.extension.ResolveException;

/**
 * @version $Id$
 * @since 4.0M1
 */
public final class AetherUtils
{
    public static final String JAR_EXTENSION = "jar";

    public static final String JAVA_LANGUAGE = "java";

    private static final Pattern PARSER_ID = Pattern.compile("([^: ]+):([^: ]+)(:([^: ]+))?");

    public static DefaultArtifact createArtifact(String id, String version) throws ResolveException
    {
        Matcher matcher = PARSER_ID.matcher(id);
        if (!matcher.matches()) {
            throw new ResolveException("Bad id " + id + ", expected format is <groupId>:<artifactId>[:<classifier>]");
        }

        return new DefaultArtifact(matcher.group(1), matcher.group(2), StringUtils.defaultString(matcher.group(4), ""),
            "jar", version);
    }

    public static ExtensionId createExtensionId(Artifact artifact)
    {
        StringBuilder builder = new StringBuilder();

        builder.append(artifact.getGroupId());
        builder.append(':');
        builder.append(artifact.getArtifactId());
        if (StringUtils.isNotEmpty(artifact.getClassifier())) {
            builder.append(':');
            builder.append(artifact.getClassifier());
        }

        return new ExtensionId(builder.toString(), artifact.getVersion());
    }

    public static ExtensionScmConnection toExtensionScmConnection(String connectionURL)
    {
        if (connectionURL == null) {
            return null;
        }

        String path = connectionURL;

        if (path.startsWith("scm:")) {
            path = path.substring("scm:".length());
        }

        String system = "git";
        int index = path.indexOf(':');
        if (index >= 0) {
            if (index != 0) {
                system = path.substring(0, index);
            }
            path = path.substring(index + 1);
        }

        return new DefaultExtensionScmConnection(system, path);
    }
}
