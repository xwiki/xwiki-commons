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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.environment.Environment;
import org.xwiki.extension.DefaultExtensionAuthor;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionException;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionLicenseManager;
import org.xwiki.extension.ExtensionNotFoundException;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.test.MockitoRepositoryUtilsRule;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;
import org.xwiki.test.annotation.AllComponents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

@AllComponents
public class AetherDefaultRepositoryManagerTest
{
    private static final String GROUPID = "groupid";

    private static final String ARTIFACTID = "artifactid";

    @Rule
    public MockitoRepositoryUtilsRule repositoryUtil = new MockitoRepositoryUtilsRule();

    private ExtensionRepositoryManager repositoryManager;

    private ExtensionId extensionId;

    private ExtensionId snapshotExtensionId;

    private ExtensionId extensionDependencyId;

    private ExtensionId extensionIdClassifier;

    private ExtensionDependency dependencyExtensionId;

    private ExtensionDependency dependencyExtensionIdRange;

    private ExtensionLicenseManager extensionLicenseManager;

    private ExtensionId bundleExtensionId;

    private ExtensionId mavenpluginExtensionId;

    private ExtensionId webjarExtensionId;

    private ExtensionId sextensionId;

    private ExtensionId sextensionDependencyId;

    private Environment environment;

    @Before
    public void setUp() throws Exception
    {
        this.extensionId = new ExtensionId(GROUPID + ':' + ARTIFACTID, "version");
        this.snapshotExtensionId = new ExtensionId(GROUPID + ':' + ARTIFACTID, "1.0-SNAPSHOT");
        this.extensionDependencyId = new ExtensionId("dgroupid:dartifactid", "dversion");

        this.extensionIdClassifier = new ExtensionId(GROUPID + ':' + ARTIFACTID + ":classifier", "version");
        this.dependencyExtensionId = new DefaultExtensionDependency(this.extensionDependencyId.getId(),
            new DefaultVersionConstraint(this.extensionDependencyId.getVersion().getValue()));
        this.dependencyExtensionIdRange = new DefaultExtensionDependency(this.extensionDependencyId.getId(),
            new DefaultVersionConstraint("[dversion,)"));

        this.bundleExtensionId = new ExtensionId("groupid:bundleartifactid", "version");

        this.mavenpluginExtensionId = new ExtensionId("groupid:mavenplugin", "version");

        this.webjarExtensionId = new ExtensionId("wgroupid:wartifactid", "wversion");

        this.sextensionId = new ExtensionId("sgroupid:sartifactid", "version");
        this.sextensionDependencyId = new ExtensionId("sgroupid:sdartifactid", "version");

        // lookup

        this.repositoryManager =
            this.repositoryUtil.getComponentManager().getInstance(ExtensionRepositoryManager.class);
        this.extensionLicenseManager =
            this.repositoryUtil.getComponentManager().getInstance(ExtensionLicenseManager.class);
        this.environment =
            this.repositoryUtil.getComponentManager().getInstance(Environment.class);
    }

    @Test
    public void testResolve() throws ResolveException, IOException
    {
        Extension extension = this.repositoryManager.resolve(this.extensionId);

        assertNotNull(extension);
        assertEquals(this.extensionId.getId(), extension.getId().getId());
        assertEquals(this.extensionId.getVersion(), extension.getId().getVersion());
        assertEquals("type", extension.getType());
        assertEquals(this.repositoryUtil.getMavenRepositoryId(),
            extension.getRepository().getDescriptor().getId());
        assertEquals("name", extension.getName());
        assertEquals("summary", extension.getSummary());
        assertEquals("http://website", extension.getWebSite());
        assertEquals("category", extension.getCategory());
        assertEquals(Arrays.asList(new DefaultExtensionAuthor("Full Name", "http://profile")),
            new ArrayList<>(extension.getAuthors()));
        assertEquals(new HashSet<>(Arrays.asList("groupid1:feature1", "groupid2:feature2")),
            new HashSet<>(extension.getFeatures()));
        assertEquals(
            new HashSet<>(Arrays.asList(new ExtensionId("groupid1:feature1", this.extensionId.getVersion()),
                new ExtensionId("groupid2:feature2", "42"))),
            new HashSet<>(extension.getExtensionFeatures()));
        assertEquals(new HashSet<>(Arrays.asList("namespace1", "{root}")),
            new HashSet<>(extension.getAllowedNamespaces()));
        assertSame(this.extensionLicenseManager.getLicense("GNU Lesser General Public License 2.1"),
            extension.getLicenses().iterator().next());

        assertEquals("http://url", extension.getIssueManagement().getURL());
        assertEquals("system", extension.getIssueManagement().getSystem());

        assertEquals("http://url", extension.getScm().getUrl());
        assertEquals("git", extension.getScm().getConnection().getSystem());
        assertEquals("git://url.git", extension.getScm().getConnection().getPath());
        assertEquals("git", extension.getScm().getDeveloperConnection().getSystem());
        assertEquals("git@url.git", extension.getScm().getDeveloperConnection().getPath());

        List<ExtensionRepositoryDescriptor> repositories = new ArrayList<>();
        repositories.add(extension.getRepository().getDescriptor());

        assertEquals(repositories, extension.getRepositories());

        Iterator<ExtensionDependency> dependencyIterator = extension.getDependencies().iterator();

        ExtensionDependency dependency = dependencyIterator.next();
        assertEquals(this.dependencyExtensionId.getId(), dependency.getId());
        assertEquals(this.dependencyExtensionId.getVersionConstraint(), dependency.getVersionConstraint());
        assertEquals(repositories, dependency.getRepositories());

        dependency = dependencyIterator.next();
        assertEquals("legacygroupid:legacyartifactid", dependency.getId());
        assertEquals("legacyversion", dependency.getVersionConstraint().getValue());
        assertEquals(repositories, dependency.getRepositories());

        // check that a new resolve of an already resolved extension provide the proper repository
        extension = this.repositoryManager.resolve(this.extensionId);
        assertEquals(this.repositoryUtil.getMavenRepositoryId(),
            extension.getRepository().getDescriptor().getId());

        // Modify the file on the descriptor on the repository
        File pomFile = new File(this.repositoryUtil.getMavenRepository(),
            this.extensionId.getId().replace('.', '/').replace(':', '/') + '/' + this.extensionId.getVersion() + '/'
                + ARTIFACTID + '-' + this.extensionId.getVersion() + ".pom");
        FileUtils.writeStringToFile(pomFile, FileUtils.readFileToString(pomFile, "UTF-8")
            .replace("<description>summary</description>", "<description>modified summary</description>"), "UTF-8");
        extension = this.repositoryManager.resolve(this.extensionId);
        assertEquals("modified summary", extension.getSummary());

        // Make sure the temporary local Maven repository was cleaned
        Path downloadDirectory = XWikiRepositorySystemSession.getDownloadDirectory(this.environment);
        assertEquals(0, Files.list(downloadDirectory).count());
    }

    @Test
    public void testResolvePOM() throws ResolveException, IOException
    {
        Extension extension = this.repositoryManager.resolve(new ExtensionId("groupid:pom", "version"));

        assertNotNull(extension);
        assertNull(extension.getType());
    }

    @Test
    public void testResolveNonDefaultTypeDependency() throws ResolveException, IOException
    {
        Extension extension =
            this.repositoryManager.resolve(new ExtensionId("groupid:nondefaulttypedependency", "version"));

        ExtensionDependency dependency = extension.getDependencies().iterator().next();

        Extension dependencyExtension = this.repositoryManager.resolve(dependency);

        assertEquals("groupid:artifactid::othertype", dependencyExtension.getId().getId());
        assertEquals("version", dependencyExtension.getId().getVersion().getValue());
        assertEquals("othertype", dependencyExtension.getType());
    }

    @Test
    public void testResolveSNAPSHOT() throws ResolveException
    {
        Extension extension = this.repositoryManager.resolve(this.snapshotExtensionId);

        assertNotNull(extension);
        assertEquals(this.snapshotExtensionId.getId(), extension.getId().getId());
        assertEquals(this.snapshotExtensionId.getVersion(), extension.getId().getVersion());
        assertEquals("type", extension.getType());
        assertEquals(this.repositoryUtil.getMavenRepositoryId(),
            extension.getRepository().getDescriptor().getId());
    }

    @Test
    public void testResolveAetherDependency() throws ResolveException
    {
        Artifact artifact = new DefaultArtifact("groupid", "artifactid", "", "type", "version");
        Dependency aetherDependency = new Dependency(artifact, null);
        AetherExtensionDependency dependency = new AetherExtensionDependency(aetherDependency);

        Extension extension = this.repositoryManager.resolve(dependency);

        assertNotNull(extension);
        assertEquals(this.extensionId.getId(), extension.getId().getId());
        assertEquals(this.extensionId.getVersion(), extension.getId().getVersion());
        assertEquals("type", extension.getType());
    }

    @Test
    public void testResolveAetherDependencySNAPSHOT() throws ResolveException
    {
        Artifact artifact = new DefaultArtifact("groupid", "artifactid", "", "type", "1.0-SNAPSHOT");
        Dependency aetherDependency = new Dependency(artifact, null);
        AetherExtensionDependency dependency = new AetherExtensionDependency(aetherDependency);

        Extension extension = this.repositoryManager.resolve(dependency);

        assertNotNull(extension);
        assertEquals(this.snapshotExtensionId.getId(), extension.getId().getId());
        assertEquals(this.snapshotExtensionId.getVersion(), extension.getId().getVersion());
        assertEquals("type", extension.getType());
        assertEquals(this.repositoryUtil.getMavenRepositoryId(),
            extension.getRepository().getDescriptor().getId());
    }

    @Test
    public void testResolveAetherDependencyRange() throws ResolveException
    {
        Artifact artifact = new DefaultArtifact("groupid", "artifactid", "", "type", "[1.0,)");
        Dependency aetherDependency = new Dependency(artifact, null);
        AetherExtensionDependency dependency = new AetherExtensionDependency(aetherDependency);

        Extension extension = this.repositoryManager.resolve(dependency);

        assertNotNull(extension);
        assertEquals(this.extensionId.getId(), extension.getId().getId());
        assertEquals("2.0", extension.getId().getVersion().toString());
    }

    @Test
    public void testResolveAetherDependencyRange2() throws ResolveException
    {
        Artifact artifact = new DefaultArtifact("groupid", "artifactid", "", "type", "[1.0,2.0)");
        Dependency aetherDependency = new Dependency(artifact, null);
        AetherExtensionDependency dependency = new AetherExtensionDependency(aetherDependency);

        Extension extension = this.repositoryManager.resolve(dependency);

        assertNotNull(extension);
        assertEquals(this.extensionId.getId(), extension.getId().getId());
        assertEquals("1.0", extension.getId().getVersion().toString());
    }

    @Test
    public void testResolveOverriddenProperties() throws ResolveException
    {
        Extension extension = this.repositoryManager.resolve(new ExtensionId("groupid:oartifactid", "version"));

        assertNotNull(extension);
        assertEquals("oname", extension.getName());
        assertEquals("osummary", extension.getSummary());
        assertEquals("http://owebsite", extension.getWebSite());
    }

    /**
     * Make sure any <code>version</code> property coming from system properties will not be resolved instead of the
     * actual pom version.
     */
    @Test
    public void testResolveWithVersionAsSystemProperty() throws ResolveException
    {
        System.setProperty("version", "systemversion");
        System.setProperty("groupId", "systemgroupId");

        Extension extension = this.repositoryManager.resolve(this.sextensionId);

        assertNotNull(extension);
        assertEquals(this.sextensionId.getId(), extension.getId().getId());
        assertEquals(this.sextensionId.getVersion(), extension.getId().getVersion());

        ExtensionDependency dependency = extension.getDependencies().iterator().next();
        assertEquals(this.sextensionDependencyId.getId(), dependency.getId());
        assertEquals(this.sextensionDependencyId.getVersion().getValue(), dependency.getVersionConstraint().getValue());
    }

    @Test
    public void testResolveVersionRange() throws ResolveException
    {
        Extension extension = this.repositoryManager.resolve(this.dependencyExtensionIdRange);

        assertNotNull(extension);
        assertEquals(this.extensionDependencyId.getId(), extension.getId().getId());
        assertEquals(this.extensionDependencyId.getVersion(), extension.getId().getVersion());
    }

    @Test
    public void testResolveVersionRangeWithNoResult()
    {
        assertThrows(ExtensionNotFoundException.class,
            () -> this.repositoryManager.resolve(new DefaultExtensionDependency(this.extensionDependencyId.getId(),
                new DefaultVersionConstraint("[42000,)"))));
    }

    @Test
    public void testDownload() throws ExtensionException, IOException
    {
        Extension extension = this.repositoryManager.resolve(this.extensionId);

        try (InputStream is = extension.getFile().openStream()) {
            assertEquals("content", IOUtils.toString(is));
        }
    }

    @Test
    public void testDownloadSNAPSHOT() throws ExtensionException, IOException
    {
        Extension extension = this.repositoryManager.resolve(this.snapshotExtensionId);

        try (InputStream is = extension.getFile().openStream()) {
            assertEquals("snapshot content", IOUtils.toString(is));
        }
    }

    @Test
    public void testDownloadClassifier() throws ExtensionException, IOException
    {
        Extension extension = this.repositoryManager.resolve(this.extensionIdClassifier);

        assertNotNull(extension);
        assertEquals(this.extensionIdClassifier, extension.getId());
        assertEquals("type", extension.getType());
        try (InputStream is = extension.getFile().openStream()) {
            assertEquals("classifier content", IOUtils.toString(is));
        }
    }

    @Test
    public void testDownloadClassifierDependency() throws ResolveException, IOException
    {
        Artifact artifact = new DefaultArtifact(GROUPID, ARTIFACTID, "classifier", "type",
            this.extensionIdClassifier.getVersion().getValue());
        Dependency aetherDependency = new Dependency(artifact, null);
        AetherExtensionDependency dependency = new AetherExtensionDependency(aetherDependency);

        Extension extension = this.repositoryManager.resolve(dependency);

        assertNotNull(extension);
        assertEquals(this.extensionIdClassifier, extension.getId());
        assertEquals("type", extension.getType());
        try (InputStream is = extension.getFile().openStream()) {
            assertEquals("classifier content", IOUtils.toString(is));
        }
    }

    @Test
    public void testDownloadBundle() throws ExtensionException, IOException
    {
        Extension extension = this.repositoryManager.resolve(this.bundleExtensionId);

        assertEquals(this.bundleExtensionId, extension.getId());
        assertEquals("jar", extension.getType());
        try (InputStream is = extension.getFile().openStream()) {
            assertEquals("content", IOUtils.toString(is));
        }
    }

    @Test
    public void testDownloadBundleDependency() throws ResolveException, IOException
    {
        Artifact artifact = new DefaultArtifact("groupid", "bundleartifactid", "", "bundle",
            this.bundleExtensionId.getVersion().getValue());
        Dependency aetherDependency = new Dependency(artifact, null);
        AetherExtensionDependency dependency = new AetherExtensionDependency(aetherDependency);

        Extension extension = this.repositoryManager.resolve(dependency);

        assertNotNull(extension);
        assertEquals(this.bundleExtensionId, extension.getId());
        assertEquals("jar", extension.getType());
        try (InputStream is = extension.getFile().openStream()) {
            assertEquals("content", IOUtils.toString(is));
        }
    }

    @Test
    public void testDownloadMavenPlugin() throws ExtensionException, IOException
    {
        Extension extension = this.repositoryManager.resolve(this.mavenpluginExtensionId);

        assertEquals(this.mavenpluginExtensionId, extension.getId());
        assertEquals("jar", extension.getType());
        try (InputStream is = extension.getFile().openStream()) {
            assertEquals("maven-plugin", IOUtils.toString(is));
        }
    }

    @Test
    public void testDownloadMavenPluginDependency() throws ResolveException, IOException
    {
        Artifact artifact = new DefaultArtifact("groupid", "mavenplugin", "", "bundle",
            this.mavenpluginExtensionId.getVersion().getValue());
        Dependency aetherDependency = new Dependency(artifact, null);
        AetherExtensionDependency dependency = new AetherExtensionDependency(aetherDependency);

        Extension extension = this.repositoryManager.resolve(dependency);

        assertNotNull(extension);
        assertEquals(this.mavenpluginExtensionId, extension.getId());
        assertEquals("jar", extension.getType());
        try (InputStream is = extension.getFile().openStream()) {
            assertEquals("maven-plugin", IOUtils.toString(is));
        }
    }

    @Test
    public void testDownloadWebjar() throws ResolveException, IOException
    {
        Extension extension = this.repositoryManager.resolve(this.webjarExtensionId);

        assertEquals(this.webjarExtensionId, extension.getId());
        assertEquals("webjar", extension.getType());
        try (InputStream stream = extension.getFile().openStream()) {
            assertEquals("webjar", IOUtils.toString(stream));
        }
    }

    @Test
    public void testDownloadWebjarDependency() throws ResolveException, IOException
    {
        Artifact artifact =
            new DefaultArtifact("wgroupid", "wartifactid", "", "webjar", webjarExtensionId.getVersion().getValue());
        Dependency aetherDependency = new Dependency(artifact, null);
        AetherExtensionDependency dependency = new AetherExtensionDependency(aetherDependency);

        Extension extension = this.repositoryManager.resolve(dependency);

        assertNotNull(extension);
        assertEquals(this.webjarExtensionId, extension.getId());
        assertEquals("webjar", extension.getType());
        try (InputStream stream = extension.getFile().openStream()) {
            assertEquals("webjar", IOUtils.toString(stream));
        }
    }

    @Test
    public void testDownloadWebjarDependencyAsJAR() throws ResolveException, IOException
    {
        Artifact artifact =
            new DefaultArtifact("wgroupid", "wartifactid", "", "jar", webjarExtensionId.getVersion().getValue());
        Dependency aetherDependency = new Dependency(artifact, null);
        AetherExtensionDependency dependency = new AetherExtensionDependency(aetherDependency);

        Extension extension = this.repositoryManager.resolve(dependency);

        assertNotNull(extension);
        assertEquals(this.webjarExtensionId, extension.getId());
        assertEquals("webjar", extension.getType());
        try (InputStream stream = extension.getFile().openStream()) {
            assertEquals("webjar", IOUtils.toString(stream));
        }
    }

    @Test
    public void testDownloadRelocation() throws ExtensionException, IOException
    {
        Extension extension = this.repositoryManager.resolve(new ExtensionId("groupid:relocation", "version"));

        try (InputStream is = extension.getFile().openStream()) {
            assertEquals("content", IOUtils.toString(is));
        }
    }

    @Test
    public void testDownloadWithFileondifferentrepo() throws ExtensionException, IOException
    {
        Extension extension =
            this.repositoryManager.resolve(new ExtensionId(GROUPID + ":fileondifferentrepo", "version"));

        try (InputStream is = extension.getFile().openStream()) {
            assertEquals("content", IOUtils.toString(is));
        }
    }

    @Test
    public void testResolveVersions() throws ExtensionException
    {
        IterableResult<Version> versions = this.repositoryManager.resolveVersions(this.extensionId.getId(), 0, -1);

        assertEquals(4, versions.getTotalHits());
        assertEquals(4, versions.getSize());
        assertEquals(0, versions.getOffset());
    }

    @Test
    public void testResolveWithExternalParent() throws ResolveException
    {
        ExtensionId extensionId = new ExtensionId("lgroupid:lartifactid", "version");

        Extension extension = this.repositoryManager.resolve(extensionId);

        assertNotNull(extension);
        assertEquals(extensionId.getId(), extension.getId().getId());
        assertEquals(extensionId.getVersion(), extension.getId().getVersion());
        assertEquals("type", extension.getType());
        assertEquals(this.repositoryUtil.getMavenRepositoryId(),
            extension.getRepository().getDescriptor().getId());

        assertEquals("parent description", extension.getSummary());
    }

    @Test
    public void testResolveWithEmptyScmAndIssueManagement() throws ResolveException
    {
        ExtensionId extensionId = new ExtensionId("groupid:emptyscmandissuemanagement", "version");

        Extension extension = this.repositoryManager.resolve(extensionId);

        assertNotNull(extension);
        assertEquals(extensionId.getId(), extension.getId().getId());
        assertEquals(extensionId.getVersion(), extension.getId().getVersion());
        assertEquals("type", extension.getType());
        assertEquals(this.repositoryUtil.getMavenRepositoryId(),
            extension.getRepository().getDescriptor().getId());

        assertNull(extension.getIssueManagement());
        assertNull(extension.getScm());
    }

    @Test
    public void testResolveDependencyFromUnknownRepository() throws ResolveException, IOException
    {
        assertThrows(ResolveException.class,
            () -> this.repositoryManager.resolve(new ExtensionId("ugroupid:uartifactid", "version")));

        // Make sure to put a proper repository in the pom
        File pomFile = new File(this.repositoryUtil.getMavenRepository(),
            "eugroupid/euartifactid/version/euartifactid-version.pom");
        String pom = FileUtils.readFileToString(pomFile, StandardCharsets.UTF_8);
        pom = pom.replace("${{extension.repository.mavenunknown}}",
            this.repositoryUtil.getMavenUnknownRepository().toURI().toString());
        FileUtils.write(pomFile, pom, StandardCharsets.UTF_8);

        Extension extension = this.repositoryManager.resolve(new ExtensionId("eugroupid:euartifactid", "version"));

        ExtensionDependency dependency = extension.getDependencies().iterator().next();

        Extension dependencyExtension = this.repositoryManager.resolve(dependency);

        assertEquals("ugroupid:uartifactid", dependencyExtension.getId().getId());
        assertEquals("version", dependencyExtension.getId().getVersion().getValue());

        assertEquals(dependency.getId(), dependencyExtension.getId().getId());
    }

    // Failures

    @Test(expected = ExtensionNotFoundException.class)
    public void testResolveWithNotExistingVersion() throws ResolveException
    {
        this.repositoryManager.resolve(new ExtensionId(this.extensionId.getId(), "noversion"));
    }

    @Test(expected = ExtensionNotFoundException.class)
    public void testResolveNotExistingExtension() throws ResolveException
    {
        this.repositoryManager.resolve(new ExtensionId("nogroupid:noartifactid", "noversion"));
    }

    @Test(expected = ExtensionNotFoundException.class)
    public void testResolveNotExistingExtensionVersions() throws ExtensionException
    {
        this.repositoryManager.resolveVersions("nogroupid:noartifactid", 0, -1);
    }
}
