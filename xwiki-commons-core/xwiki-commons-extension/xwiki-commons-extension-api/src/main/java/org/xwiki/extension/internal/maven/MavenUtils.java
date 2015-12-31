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
package org.xwiki.extension.internal.maven;

import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.extension.DefaultExtensionScmConnection;
import org.xwiki.extension.ExtensionScmConnection;

/**
 * Various Maven related helpers.
 * 
 * @version $Id$
 * @since 7.0M1
 */
public class MavenUtils
{
    public static final String MPKEYPREFIX = "xwiki.extension.";

    public static final String MPNAME_NAME = "name";

    public static final String MPNAME_SUMMARY = "summary";

    public static final String MPNAME_WEBSITE = "website";

    public static final String MPNAME_FEATURES = "features";

    public static final String MPNAME_CATEGORY = "category";

    public static final String MPNAME_NAMESPACES = "namespaces";

    public static final String PKEY_MAVEN_MODEL = "maven.Model";

    public static final String JAR_EXTENSION = "jar";

    public static final String JAVA_LANGUAGE = "java";

    /**
     * The package containing maven informations in a jar file.
     */
    public static final String MAVENPACKAGE = "META-INF.maven";

    /**
     * SNAPSHOT suffix in versions.
     */
    public static final String SNAPSHOTSUFFIX = "-SNAPSHOT";

    /**
     * Used to parse extension id into maven informations.
     */
    public static final Pattern PARSER_ID = Pattern.compile("([^: ]+):([^: ]+)(:([^: ]+))?");

    /**
     * MANIFEST.MF attribute containing extension identifier.
     */
    public static final String MF_EXTENSION_ID = "XWiki-Extension-Id";

    /**
     * Parse a Maven scm URL to generate a {@link ExtensionScmConnection}.
     * 
     * @param connectionURL the connection URL
     * @return the {@link ExtensionScmConnection}
     */
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

    /**
     * Create a extension identifier from Maven artifact identifier elements.
     * 
     * @param groupId the group id
     * @param artifactId the artifact id
     * @param classifier the classifier
     * @return the extension identifier
     */
    public static String toExtensionId(String groupId, String artifactId, String classifier)
    {
        StringBuilder builder = new StringBuilder();

        builder.append(groupId);
        builder.append(':');
        builder.append(artifactId);
        if (StringUtils.isNotEmpty(classifier)) {
            builder.append(':');
            builder.append(classifier);
        }

        return builder.toString();
    }

    /**
     * Get the extension type from maven packaging.
     *
     * @param packaging the maven packaging
     * @return the extension type
     */
    public static String packagingToType(String packaging)
    {
        // support bundle packaging
        if (packaging.equals("bundle")) {
            return "jar";
        }

        return packaging;
    }
}
