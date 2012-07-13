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
package org.xwiki.extension.repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.TestResources;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.test.ConfigurableDefaultCoreExtensionRepository;
import org.xwiki.extension.test.RepositoryUtil;
import org.xwiki.test.AbstractComponentTestCase;

public class DefaultInstalledExtensionRepositoryTest extends AbstractComponentTestCase
{
    private InstalledExtensionRepository installedExtensionRepository;

    private LocalExtensionRepository localExtensionRepository;

    private RepositoryUtil repositoryUtil;

    private ExtensionRepositoryManager repositoryManager;

    private TestResources resources;

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        this.repositoryUtil = new RepositoryUtil(getComponentManager(), getMockery());
        this.repositoryUtil.setup();

        // lookup

        this.installedExtensionRepository = getComponentManager().getInstance(InstalledExtensionRepository.class);
        this.localExtensionRepository = getComponentManager().getInstance(LocalExtensionRepository.class);
        this.repositoryManager = getComponentManager().getInstance(ExtensionRepositoryManager.class);

        // resources

        this.resources = new TestResources();
        this.resources.init(this.installedExtensionRepository);
    }

    @Override
    protected void registerComponents() throws Exception
    {
        super.registerComponents();

        registerComponent(ConfigurableDefaultCoreExtensionRepository.class);
    }

    @Test
    public void testInit()
    {
        Assert.assertEquals(3, this.installedExtensionRepository.countExtensions());

        // installedonnamespace
        InstalledExtension extension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ONNAMESPACE_ID.getId(),
                "namespace");
        Assert.assertTrue(extension.isValid("namespace"));
    }

    @Test
    public void testGetLocalExtension()
    {
        Assert.assertNull(this.installedExtensionRepository.getInstalledExtension("unexistingextension", null));

        Extension extension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), null);

        Assert.assertNotNull(extension);
        Assert.assertEquals(TestResources.INSTALLED_ID, extension.getId());
        Assert.assertEquals("type", extension.getType());
        Assert.assertEquals(Arrays.asList(TestResources.INSTALLED_ID.getId() + "-feature"), new ArrayList<String>(
            extension.getFeatures()));

        ExtensionDependency dependency = extension.getDependencies().iterator().next();
        Assert.assertEquals(TestResources.INSTALLED_DEPENDENCY_ID.getId(), dependency.getId());
        Assert.assertEquals(TestResources.INSTALLED_DEPENDENCY_ID.getVersion(), dependency.getVersionConstraint()
            .getVersion());
    }

    @Test
    public void testResolve() throws ResolveException
    {
        try {
            this.installedExtensionRepository.resolve(new ExtensionId("unexistingextension", "version"));

            Assert.fail("Resolve should have failed");
        } catch (ResolveException expected) {
            // expected
        }

        try {
            this.installedExtensionRepository.resolve(new ExtensionId(TestResources.INSTALLED_ID.getId(),
                "wrongversion"));

            Assert.fail("Resolve should have failed");
        } catch (ResolveException expected) {
            // expected
        }

        Extension extension = this.installedExtensionRepository.resolve(TestResources.INSTALLED_ID);

        Assert.assertNotNull(extension);
        Assert.assertEquals(TestResources.INSTALLED_ID, extension.getId());
    }

    @Test
    public void testInstallTwice() throws InstallException
    {
        // Change status
        this.installedExtensionRepository.installExtension(this.resources.installed, "namespace",
            !this.resources.installed.isDependency());

        // Try to install again with the same status
        try {
            this.installedExtensionRepository.installExtension(this.resources.installed, "namespace",
                this.resources.installed.isDependency());
            Assert.fail("Install should have failed");
        } catch (InstallException expected) {
            // expected
        }
    }

    @Test
    public void testStoreExtensionAndInstall() throws ResolveException, LocalExtensionRepositoryException,
        InstallException
    {
        Extension extension = this.repositoryManager.resolve(TestResources.REMOTE_SIMPLE_ID);

        // store

        this.localExtensionRepository.storeExtension(extension);

        LocalExtension localExtension = this.localExtensionRepository.resolve(TestResources.REMOTE_SIMPLE_ID);

        Assert.assertEquals(TestResources.REMOTE_SIMPLE_ID, localExtension.getId());

        // install

        this.installedExtensionRepository.installExtension(localExtension, null, false);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension(
            TestResources.REMOTE_SIMPLE_ID.getId(), null));
        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension(
            TestResources.REMOTE_SIMPLE_ID.getId(), "namespace"));
        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension(
            TestResources.REMOTE_SIMPLE_ID.getId() + "-feature", null));
    }

    @Test
    public void testUninstallExtension() throws UninstallException, InstallException
    {
        // uninstall from root

        this.installedExtensionRepository.uninstallExtension(this.resources.installed, null);
        this.installedExtensionRepository.uninstallExtension(this.resources.installedDependency, null);

        // uninstall from namespace

        this.installedExtensionRepository.installExtension(this.resources.installedDependency, "namespace", false);
        this.installedExtensionRepository.installExtension(this.resources.installed, "namespace", false);
        this.installedExtensionRepository.uninstallExtension(this.resources.installed, "namespace");
        this.installedExtensionRepository.uninstallExtension(this.resources.installedDependency, "namespace");

        // uninstall from namespace with dependency on root

        this.installedExtensionRepository.installExtension(this.resources.installedDependency, null, false);
        this.installedExtensionRepository.installExtension(this.resources.installed, "namespace", false);
        this.installedExtensionRepository.uninstallExtension(this.resources.installed, "namespace");
        this.installedExtensionRepository.uninstallExtension(this.resources.installedDependency, null);
    }

    @Test
    public void testBackwardDependenciesAfterUninstall() throws ResolveException, UninstallException
    {
        this.installedExtensionRepository.uninstallExtension(this.resources.installed, null);

        Assert.assertEquals(Collections.EMPTY_LIST, this.installedExtensionRepository.getBackwardDependencies(
            TestResources.INSTALLED_DEPENDENCY_ID.getId(), null));
    }

    @Test
    public void testBackwardDependenciesWithExtensionAndDepOnRoot() throws ResolveException
    {
        Assert.assertEquals(
            Arrays.asList(this.resources.installed),
            new ArrayList<InstalledExtension>(this.installedExtensionRepository.getBackwardDependencies(
                TestResources.INSTALLED_DEPENDENCY_ID.getId(), null)));

        Assert.assertEquals(
            Arrays.asList(),
            new ArrayList<InstalledExtension>(this.installedExtensionRepository.getBackwardDependencies(
                TestResources.INSTALLED_DEPENDENCY_ID.getId(), "namespace")));

        Assert.assertEquals(
            Arrays.asList(),
            new ArrayList<InstalledExtension>(this.installedExtensionRepository.getBackwardDependencies(
                TestResources.INSTALLED_ID.getId(), null)));

        Map<String, Collection<InstalledExtension>> map = new HashMap<String, Collection<InstalledExtension>>();
        map.put(null, Arrays.asList(this.resources.installed));

        Assert.assertEquals(map,
            this.installedExtensionRepository.getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID));

        Assert.assertEquals(Collections.EMPTY_MAP,
            this.installedExtensionRepository.getBackwardDependencies(TestResources.INSTALLED_ID));
    }

    @Test
    public void testBackwardDependenciesWithExtensionOnNamespaceAndDepOnNamespace() throws InstallException,
        ResolveException, UninstallException
    {
        this.installedExtensionRepository.uninstallExtension(this.resources.installed, null);

        this.installedExtensionRepository.installExtension(this.resources.installed, "namespace", true);

        Assert.assertEquals(Collections.EMPTY_LIST, this.installedExtensionRepository.getBackwardDependencies(
            TestResources.INSTALLED_DEPENDENCY_ID.getId(), null));

        Assert.assertEquals(Arrays.asList(this.resources.installed), this.installedExtensionRepository
            .getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID.getId(), "namespace"));

        Assert.assertEquals(Collections.EMPTY_LIST,
            this.installedExtensionRepository.getBackwardDependencies(TestResources.INSTALLED_ID.getId(), "namespace"));
    }

    @Test
    public void testBackwardDependenciesWithExtensionAndDepOnNamespace() throws InstallException, ResolveException,
        UninstallException
    {
        this.installedExtensionRepository.uninstallExtension(this.resources.installed, null);
        this.installedExtensionRepository.uninstallExtension(this.resources.installedDependency, null);

        this.installedExtensionRepository.installExtension(this.resources.installedDependency, "namespace", true);
        this.installedExtensionRepository.installExtension(this.resources.installed, "namespace", true);

        Assert.assertEquals(Arrays.asList(this.resources.installed), this.installedExtensionRepository
            .getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID.getId(), "namespace"));

        Assert.assertEquals(Collections.EMPTY_LIST,
            this.installedExtensionRepository.getBackwardDependencies(TestResources.INSTALLED_ID.getId(), "namespace"));
    }

    @Test
    public void testSearch() throws SearchException
    {
        CollectionIterableResult<Extension> result =
            (CollectionIterableResult<Extension>) this.installedExtensionRepository.search(null, 0, -1);

        Assert.assertEquals(3, result.getTotalHits());
        Assert.assertEquals(3, result.getSize());
        Assert.assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.installedExtensionRepository.search("", 0, -1);

        Assert.assertEquals(3, result.getTotalHits());
        Assert.assertEquals(3, result.getSize());
        Assert.assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.installedExtensionRepository.search("extension", 0, -1);

        Assert.assertEquals(2, result.getTotalHits());
        Assert.assertEquals(2, result.getSize());
        Assert.assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.installedExtensionRepository.search("dependency", 0, -1);

        Assert.assertEquals(1, result.getTotalHits());
        Assert.assertEquals(1, result.getSize());
        Assert.assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.installedExtensionRepository.search(null, 0, 0);

        Assert.assertEquals(3, result.getTotalHits());
        Assert.assertEquals(0, result.getSize());
        Assert.assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.installedExtensionRepository.search(null, 0, 2);

        Assert.assertEquals(3, result.getTotalHits());
        Assert.assertEquals(2, result.getSize());
        Assert.assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.installedExtensionRepository.search(null, 0, 1);

        Assert.assertEquals(3, result.getTotalHits());
        Assert.assertEquals(1, result.getSize());
        Assert.assertEquals(0, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.installedExtensionRepository.search(null, 1, 2);

        Assert.assertEquals(3, result.getTotalHits());
        Assert.assertEquals(2, result.getSize());
        Assert.assertEquals(1, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.installedExtensionRepository.search(null, 2, 2);

        Assert.assertEquals(3, result.getTotalHits());
        Assert.assertEquals(1, result.getSize());
        Assert.assertEquals(2, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.installedExtensionRepository.search(null, -1, 2);

        Assert.assertEquals(3, result.getTotalHits());
        Assert.assertEquals(2, result.getSize());
        Assert.assertEquals(-1, result.getOffset());

        result = (CollectionIterableResult<Extension>) this.installedExtensionRepository.search(null, -1, 1);

        Assert.assertEquals(3, result.getTotalHits());
        Assert.assertEquals(1, result.getSize());
        Assert.assertEquals(-1, result.getOffset());
    }
}
