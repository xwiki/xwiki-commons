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
package org.xwiki.extension.maven.internal.converter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.inject.Named;

import org.apache.commons.collections4.ListUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.junit.jupiter.api.Test;
import org.xwiki.extension.DefaultExtensionComponent;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionFeaturesInjector;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionPattern;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Validate {@link ModelConverter} component.
 *
 * @version $Id$
 */
@AllComponents
@ComponentTest
class ModelConverterTest
{
    private static final List<ExtensionId> INJECTED_FEATURES =
        Arrays.asList(new ExtensionId("injectedfeature1", "injectedversion1"),
            new ExtensionId("injectedfeature2", "injectedversion2"));

    @MockComponent
    @Named("test")
    private ExtensionFeaturesInjector featureProvider;

    @InjectMockComponents
    private DefaultConverterManager converter;

    @BeforeComponent
    void beforeComponent()
    {
        when(this.featureProvider.getFeatures(any(Extension.class))).thenReturn(INJECTED_FEATURES);
    }

    @Test
    void convertToExtension() throws SecurityException, URISyntaxException
    {
        Model model = new Model();

        model.setGroupId("groupid");
        model.setArtifactId("artifactid");
        model.setVersion("version");
        model.addProperty(Extension.IKEYPREFIX + Extension.FIELD_CATEGORY, "category");
        model.addProperty(Extension.IKEYPREFIX + Extension.FIELD_NAMESPACES,
            "namespace1, namespace2,\r\n\t {root}, \"namespace3\", 'namespace4'");
        model.addProperty(Extension.IKEYPREFIX + Extension.FIELD_FEATURES, "feature1/1.0");
        model.addProperty(Extension.IKEYPREFIX + Extension.FIELD_COMPONENTS,
            "org.xwiki.contib.MyClassName<Generic1, Generic2>/hint\n"
                + "org.xwiki.contib.MyClassName2<Generic12, Generic22>/hint2");
        Repository repository = new Repository();
        repository.setId("repository-id");
        repository.setUrl("http://url");
        model.addRepository(repository);
        Dependency dependency = new Dependency();
        dependency.setGroupId("dgroupid");
        dependency.setArtifactId("dartifactId");
        dependency.setVersion("1.0");
        dependency.setOptional(false);
        Exclusion exclusion1 = new Exclusion();
        exclusion1.setArtifactId("excludedartifact1");
        exclusion1.setGroupId("excludedgroup1");
        dependency.addExclusion(exclusion1);
        Exclusion exclusion2 = new Exclusion();
        exclusion2.setArtifactId("excludedartifact2");
        exclusion2.setGroupId("*");
        dependency.addExclusion(exclusion2);
        Exclusion exclusion3 = new Exclusion();
        exclusion3.setArtifactId("*");
        exclusion3.setGroupId("excludedgroup3");
        dependency.addExclusion(exclusion3);
        model.addDependency(dependency);

        Extension extension = this.converter.convert(Extension.class, model);

        assertEquals(model.getGroupId() + ':' + model.getArtifactId(), extension.getId().getId());
        assertEquals(model.getVersion(), extension.getId().getVersion().getValue());
        assertEquals(model.getPackaging(), extension.getType());
        assertEquals(ListUtils.union(Arrays.asList(new ExtensionId("feature1", "1.0")), INJECTED_FEATURES),
            new ArrayList<>(extension.getExtensionFeatures()));
        assertEquals("category", extension.getCategory());
        assertNull(extension.getProperty(Extension.IKEYPREFIX + Extension.FIELD_CATEGORY));
        assertEquals(Arrays.asList("namespace1", "namespace2", "{root}", "namespace3", "namespace4"),
            new ArrayList<>(extension.getAllowedNamespaces()));
        assertNull(extension.getProperty(Extension.IKEYPREFIX + Extension.FIELD_NAMESPACES));
        assertEquals(
            Arrays.asList(new DefaultExtensionRepositoryDescriptor("repository-id", "maven", new URI("http://url"))),
            extension.getRepositories());
        assertEquals(1, extension.getDependencies().size());
        ExtensionDependency extensionDependency = extension.getDependencies().iterator().next();
        assertEquals("dgroupid:dartifactId", extensionDependency.getId());
        assertEquals("1.0", extensionDependency.getVersionConstraint().getValue());
        Collection<ExtensionPattern> extensionExclusions = extensionDependency.getExclusions();
        Iterator<ExtensionPattern> patternIt = extensionExclusions.iterator();
        ExtensionPattern pattern = patternIt.next();
        assertEquals("excludedgroup1:excludedartifact1", pattern.getIdPattern().toString());
        pattern = patternIt.next();
        assertEquals(".*:\\Qexcludedartifact2\\E", pattern.getIdPattern().toString());
        pattern = patternIt.next();
        assertEquals("\\Qexcludedgroup3\\E:.*", pattern.getIdPattern().toString());
        assertEquals(
            Arrays.asList(new DefaultExtensionComponent("org.xwiki.contib.MyClassName<Generic1, Generic2>", "hint"),
                new DefaultExtensionComponent("org.xwiki.contib.MyClassName2<Generic12, Generic22>", "hint2")),
            extension.getComponents());
    }

    @Test
    void convertPomToExtension() throws SecurityException
    {
        Model model = new Model();

        model.setGroupId("groupid");
        model.setArtifactId("artifactid");
        model.setVersion("version");
        model.setPackaging("pom");

        Extension extension = this.converter.convert(Extension.class, model);

        assertNull(extension.getType());
    }

    @Test
    void convertToExtensionWithIncludedOptionalDependencies() throws SecurityException, URISyntaxException
    {
        Model model = new Model();

        model.setGroupId("groupid");
        model.setArtifactId("artifactid");
        model.setVersion("version");
        model.addProperty(Extension.IKEYPREFIX + Extension.FIELD_CATEGORY, "category");
        model.addProperty(Extension.IKEYPREFIX + Extension.FIELD_NAMESPACES,
            "namespace1, namespace2,\r\n\t {root}, \"namespace3\", 'namespace4'");
        model.addProperty(Extension.IKEYPREFIX + Extension.FIELD_FEATURES, "feature1/1.0");
        model.addProperty(Extension.IKEYPREFIX + "optionalIncluded", "true");
        Repository repository = new Repository();
        repository.setId("repository-id");
        repository.setUrl("http://url");
        model.addRepository(repository);
        Dependency dependency = new Dependency();
        dependency.setGroupId("dgroupid");
        dependency.setArtifactId("dartifactId");
        dependency.setVersion("1.0");
        dependency.setOptional(true);
        model.addDependency(dependency);

        Extension extension = this.converter.convert(Extension.class, model);

        assertEquals(model.getGroupId() + ':' + model.getArtifactId(), extension.getId().getId());
        assertEquals(model.getVersion(), extension.getId().getVersion().getValue());
        assertEquals(ListUtils.union(Arrays.asList(new ExtensionId("feature1", "1.0")), INJECTED_FEATURES),
            new ArrayList<>(extension.getExtensionFeatures()));
        assertEquals("category", extension.getCategory());
        assertNull(extension.getProperty(Extension.IKEYPREFIX + Extension.FIELD_CATEGORY));
        assertEquals(Arrays.asList("namespace1", "namespace2", "{root}", "namespace3", "namespace4"),
            new ArrayList<>(extension.getAllowedNamespaces()));
        assertNull(extension.getProperty(Extension.IKEYPREFIX + Extension.FIELD_NAMESPACES));
        assertEquals(
            Arrays.asList(new DefaultExtensionRepositoryDescriptor("repository-id", "maven", new URI("http://url"))),
            extension.getRepositories());
        assertEquals(1, extension.getDependencies().size());
        ExtensionDependency extensionDependency = extension.getDependencies().iterator().next();
        assertTrue(extensionDependency.isOptional());
    }

    @Test
    void convertToXWikiExtensionWithOptionalDependencies() throws SecurityException, URISyntaxException
    {
        Model model = new Model();

        model.setGroupId("org.xwiki");
        model.setArtifactId("artifactid");
        model.setVersion("version");
        model.addProperty(Extension.IKEYPREFIX + Extension.FIELD_CATEGORY, "category");
        model.addProperty(Extension.IKEYPREFIX + Extension.FIELD_NAMESPACES,
            "namespace1, namespace2,\r\n\t {root}, \"namespace3\", 'namespace4'");
        model.addProperty(Extension.IKEYPREFIX + Extension.FIELD_FEATURES, "feature1/1.0");
        Repository repository = new Repository();
        repository.setId("repository-id");
        repository.setUrl("http://url");
        model.addRepository(repository);
        Dependency dependency = new Dependency();
        dependency.setGroupId("dgroupid");
        dependency.setArtifactId("dartifactId");
        dependency.setVersion("1.0");
        dependency.setOptional(true);
        model.addDependency(dependency);

        Extension extension = this.converter.convert(Extension.class, model);

        assertEquals(model.getGroupId() + ':' + model.getArtifactId(), extension.getId().getId());
        assertEquals(model.getVersion(), extension.getId().getVersion().getValue());
        assertEquals(ListUtils.union(Arrays.asList(new ExtensionId("feature1", "1.0")), INJECTED_FEATURES),
            new ArrayList<>(extension.getExtensionFeatures()));
        assertEquals("category", extension.getCategory());
        assertNull(extension.getProperty(Extension.IKEYPREFIX + Extension.FIELD_CATEGORY));
        assertEquals(Arrays.asList("namespace1", "namespace2", "{root}", "namespace3", "namespace4"),
            new ArrayList<>(extension.getAllowedNamespaces()));
        assertNull(extension.getProperty(Extension.IKEYPREFIX + Extension.FIELD_NAMESPACES));
        assertEquals(
            Arrays.asList(new DefaultExtensionRepositoryDescriptor("repository-id", "maven", new URI("http://url"))),
            extension.getRepositories());
        assertEquals(1, extension.getDependencies().size());
        ExtensionDependency extensionDependency = extension.getDependencies().iterator().next();
        assertTrue(extensionDependency.isOptional());
    }

    @Test
    void convertToExtensionAllowedOnRoot() throws SecurityException
    {
        Model model = new Model();

        model.setGroupId("groupid");
        model.setArtifactId("artifactid");
        model.setVersion("version");
        model.addProperty(Extension.IKEYPREFIX + Extension.FIELD_NAMESPACES, "{root}");

        Extension extension = this.converter.convert(Extension.class, model);

        assertEquals(Arrays.asList("{root}"), new ArrayList<>(extension.getAllowedNamespaces()));
    }
}
