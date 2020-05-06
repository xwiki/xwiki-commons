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

import org.apache.maven.model.Model;
import org.xwiki.extension.AbstractExtension;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.version.Version;

/**
 * Base class for all Maven specific {@link Extension}s.
 * 
 * @version $Id$
 * @since 7.3M1
 */
public abstract class AbstractMavenExtension extends AbstractExtension implements MavenExtension
{
    /**
     * The prefix used in all Maven related extension properties.
     */
    public static final String PKEY_MAVENPRFIX = "maven.";

    /**
     * The name of the property containing the source Model if any.
     */
    public static final String PKEY_MAVEN_MODEL = PKEY_MAVENPRFIX + "Model";

    /**
     * The name of the property containing the artifact id.
     */
    public static final String PKEY_MAVEN_ARTIFACTID = PKEY_MAVENPRFIX + "artifactid";

    /**
     * The name of the property containing the group id.
     */
    public static final String PKEY_MAVEN_GROUPID = PKEY_MAVENPRFIX + "groupid";

    /**
     * The name of the property containing the type.
     */
    public static final String PKEY_MAVEN_TYPE = PKEY_MAVENPRFIX + "type";

    /**
     * The name of the property containing the classifier.
     * 
     * @since 10.9
     * @since 10.8.1
     */
    public static final String PKEY_MAVEN_CLASSIFIER = PKEY_MAVENPRFIX + "classifier";

    /**
     * @param repository the repository where this extension comes from
     * @param groupId the maven artifact group id
     * @param artifactId the maven artifact artifact id
     * @param version the maven artifact version
     * @param type the extension type
     */
    public AbstractMavenExtension(ExtensionRepository repository, String groupId, String artifactId, String version,
        String type)
    {
        super(repository, new ExtensionId(groupId + ':' + artifactId, version), type);

        setMavenGroupId(groupId);
        setMavenArtifactId(artifactId);
    }

    /**
     * @param repository the repository where this extension comes from
     * @param groupId the maven artifact group id
     * @param artifactId the maven artifact artifact id
     * @param version the maven artifact version
     * @param type the extension type
     * @since 8.4
     */
    public AbstractMavenExtension(ExtensionRepository repository, String groupId, String artifactId, Version version,
        String type)
    {
        this(repository, groupId, artifactId, null, version, type);
    }

    /**
     * @param repository the repository where this extension comes from
     * @param groupId the maven artifact group id
     * @param artifactId the maven artifact artifact id
     * @param classifier the maven artifact classifier
     * @param version the maven artifact version
     * @param type the extension type
     * @since 10.9
     * @since 10.8.1
     */
    public AbstractMavenExtension(ExtensionRepository repository, String groupId, String artifactId, String classifier,
        Version version, String type)
    {
        super(repository, MavenUtils.toExtensionId(groupId, artifactId, classifier, version), type);

        setMavenGroupId(groupId);
        setMavenArtifactId(artifactId);
        if (classifier != null) {
            setMavenClassifier(classifier);
        }
    }

    /**
     * Create new Maven extension descriptor by copying provided one.
     *
     * @param repository the repository where this extension comes from
     * @param extension the extension to copy
     */
    public AbstractMavenExtension(ExtensionRepository repository, Extension extension)
    {
        super(repository, extension);

        if (extension instanceof MavenExtension) {
            MavenExtension mavenExtension = (MavenExtension) extension;

            setMavenArtifactId(mavenExtension.getMavenArtifactId());
            setMavenGroupId(mavenExtension.getMavenGroupId());
        }
    }

    @Override
    public String getMavenGroupId()
    {
        return getProperty(PKEY_MAVEN_GROUPID);
    }

    /**
     * @param groupId the Maven group id
     */
    public void setMavenGroupId(String groupId)
    {
        putProperty(PKEY_MAVEN_GROUPID, groupId);
    }

    @Override
    public String getMavenArtifactId()
    {
        return getProperty(PKEY_MAVEN_ARTIFACTID);
    }

    /**
     * @param artifactId the Maven artifact id
     */
    public void setMavenArtifactId(String artifactId)
    {
        putProperty(PKEY_MAVEN_ARTIFACTID, artifactId);
    }

    /**
     * @param classifier the Maven classifier
     * @since 10.9
     * @since 10.8.1
     */
    public void setMavenClassifier(String classifier)
    {
        putProperty(PKEY_MAVEN_CLASSIFIER, classifier);
    }

    /**
     * @return the Maven model object if any
     */
    public Model getMavenModel()
    {
        return (Model) getProperty(PKEY_MAVEN_MODEL);
    }
}
