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
package org.xwiki.extension.maven.internal;

import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.xwiki.extension.DefaultExtensionScmConnection;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionScmConnection;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.internal.DefaultVersion;

/**
 * Various Maven related helpers.
 * 
 * @version $Id$
 * @since 7.0M1
 */
public class MavenUtils
{
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
     * Unknown.
     */
    public static final String UNKNOWN = "unknown";

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

        return new DefaultExtensionScmConnection(connectionURL);
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
     * Create a extension identifier from Maven artifact identifier elements.
     * 
     * @param groupId the group id
     * @param artifactId the artifact id
     * @param classifier the classifier
     * @param version the version
     * @return the extension identifier
     * @since 10.9RC1
     * @since 10.8.1
     */
    public static ExtensionId toExtensionId(String groupId, String artifactId, String classifier, String version)
    {
        return toExtensionId(groupId, artifactId, classifier, version != null ? new DefaultVersion(version) : null);
    }

    /**
     * Create a extension identifier from Maven artifact identifier elements.
     * 
     * @param groupId the group id
     * @param artifactId the artifact id
     * @param classifier the classifier
     * @param version the version
     * @return the extension identifier
     * @since 10.9RC1
     * @since 10.8.1
     */
    public static ExtensionId toExtensionId(String groupId, String artifactId, String classifier, Version version)
    {
        String extensionId = toExtensionId(groupId, artifactId, classifier);

        return new ExtensionId(extensionId, version);
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

    /**
     * @param mavenModel the Maven Model instance
     * @return the resolved version
     * @since 8.1M1
     */
    public static String resolveVersion(Model mavenModel)
    {
        return resolveVersion(mavenModel.getVersion(), mavenModel, false);
    }

    /**
     * @param modelVersion the current String representing the version to resolve
     * @param mavenModel the Maven Model instance
     * @param dependency indicate if it's a dependency version
     * @return the resolved version
     * @since 8.1M1
     */
    public static String resolveVersion(String modelVersion, Model mavenModel, boolean dependency)
    {
        String version = modelVersion;

        // TODO: download parents and resolve pom.xml properties using standard tools ? could be pretty expensive for
        // the init
        if (version == null) {
            if (!dependency) {
                Parent parent = mavenModel.getParent();

                if (parent != null) {
                    version = parent.getVersion();
                }
            }
        } else if (version.startsWith("$")) {
            String propertyName = version.substring(2, version.length() - 1);

            if (propertyName.equals("project.version") || propertyName.equals("pom.version")
                || propertyName.equals("version")) {
                version = resolveVersion(mavenModel.getVersion(), mavenModel, false);
            } else {
                String value = mavenModel.getProperties().getProperty(propertyName);
                if (value != null) {
                    version = value;
                }
            }
        }

        if (version == null) {
            version = UNKNOWN;
        }

        return version;
    }

    /**
     * @param mavenModel the Maven Model instance
     * @return the resolved group id
     * @since 8.1M1
     */
    public static String resolveGroupId(Model mavenModel)
    {
        return resolveGroupId(mavenModel.getGroupId(), mavenModel, false);
    }

    /**
     * @param modelVersion the current String representing the group id to resolve
     * @param mavenModel the Maven Model instance
     * @param dependency indicate if it's a dependency group id
     * @return the resolved group id
     * @since 8.1M1
     */
    public static String resolveGroupId(String modelGroupId, Model mavenModel, boolean dependency)
    {
        String groupId = modelGroupId;

        // TODO: download parents and resolve pom.xml properties using standard tools ? could be pretty expensive for
        // the init
        if (groupId == null) {
            if (!dependency) {
                Parent parent = mavenModel.getParent();

                if (parent != null) {
                    groupId = parent.getGroupId();
                }
            }
        } else if (groupId.startsWith("$")) {
            String propertyName = groupId.substring(2, groupId.length() - 1);

            String value = mavenModel.getProperties().getProperty(propertyName);
            if (value != null) {
                groupId = value;
            }
        }

        if (groupId == null) {
            groupId = UNKNOWN;
        }

        return groupId;
    }

    /**
     * @param model the Maven Model instance to update
     */
    // TODO: use standard Maven tools for that
    public static void resolveVariables(Model model)
    {
        // Resolve version
        model.setVersion(resolveVersion(model));

        // Resolve groupid
        model.setGroupId(resolveGroupId(model));

        // Resolve properties
        for (Map.Entry<Object, Object> entry : model.getProperties().entrySet()) {
            if (entry.getValue() instanceof String) {
                String value = (String) entry.getValue();
                entry.setValue(value.replace("${project.version}", model.getVersion()));
            }
        }
    }
}
