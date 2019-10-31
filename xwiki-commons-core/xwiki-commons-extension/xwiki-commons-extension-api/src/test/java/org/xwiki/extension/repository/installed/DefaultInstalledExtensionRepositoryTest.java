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
package org.xwiki.extension.repository.installed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.TestResources;
import org.xwiki.extension.UninstallException;
import org.xwiki.extension.handler.ExtensionHandler;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.InstalledExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepository;
import org.xwiki.extension.repository.LocalExtensionRepositoryException;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.ExtensionQuery;
import org.xwiki.extension.repository.search.ExtensionQuery.COMPARISON;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.test.MockitoRepositoryUtilsRule;
import org.xwiki.extension.test.TestExtensionHandler;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;
import org.xwiki.test.annotation.AllComponents;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@AllComponents
public class DefaultInstalledExtensionRepositoryTest
{
    @Rule
    public MockitoRepositoryUtilsRule repositoryUtil = new MockitoRepositoryUtilsRule();

    private InstalledExtensionRepository installedExtensionRepository;

    private LocalExtensionRepository localExtensionRepository;

    private ExtensionRepositoryManager repositoryManager;

    private TestResources resources;

    private TestExtensionHandler handler;

    @Before
    public void setUp() throws Exception
    {
        // lookup

        this.installedExtensionRepository =
            this.repositoryUtil.getComponentManager().getInstance(InstalledExtensionRepository.class);
        this.localExtensionRepository =
            this.repositoryUtil.getComponentManager().getInstance(LocalExtensionRepository.class);
        this.repositoryManager =
            this.repositoryUtil.getComponentManager().getInstance(ExtensionRepositoryManager.class);
        this.handler = this.repositoryUtil.getComponentManager().getInstance(ExtensionHandler.class, "test");

        // resources

        this.resources = new TestResources();
        this.resources.init(this.installedExtensionRepository);
    }

    private void install(ExtensionId extensionId)
        throws ResolveException, LocalExtensionRepositoryException, InstallException
    {
        Extension extension = this.repositoryManager.resolve(extensionId);

        // store

        this.localExtensionRepository.storeExtension(extension);

        LocalExtension localExtension = this.localExtensionRepository.resolve(extensionId);

        assertEquals(extensionId, localExtension.getId());

        // install

        this.installedExtensionRepository.installExtension(localExtension, null, false);
    }

    // Tests

    @Test
    public void testInit()
    {
        assertTrue(this.installedExtensionRepository.countExtensions() > 0);

        // installedonnamespace
        InstalledExtension extension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ONNAMESPACE_ID);

        assertNotNull(extension);
        assertEquals(TestResources.INSTALLED_ONNAMESPACE_ID, extension.getId());

        extension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.INSTALLED_ONNAMESPACE_ID.getId(), "namespace");

        assertNotNull(extension);
        assertEquals(TestResources.INSTALLED_ONNAMESPACE_ID, extension.getId());
        assertTrue(extension.isValid("namespace"));

        // installedextension
        extension = this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID);

        assertNotNull(extension);
        assertEquals(TestResources.INSTALLED_ID, extension.getId());

        extension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), "namespace");

        assertNotNull(extension);
        assertEquals(TestResources.INSTALLED_ID, extension.getId());
        assertTrue(this.handler.getExtensions().get(null).contains(extension));
        assertFalse(this.handler.getExtensions().get("namespace").contains(extension));
        assertSame(extension,
            this.installedExtensionRepository.getInstalledExtension("installedextension", "namespace"));

        // installedonnemspacewithrootdependency
        extension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.INSTALLED_ONNAMESPACEWITHROOTDEPENDENCY_ID);

        assertNotNull(extension);
        assertEquals(TestResources.INSTALLED_ONNAMESPACEWITHROOTDEPENDENCY_ID, extension.getId());

        extension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.INSTALLED_ONNAMESPACEWITHROOTDEPENDENCY_ID.getId(), "namespace");

        assertNotNull(extension);
        assertEquals(TestResources.INSTALLED_ONNAMESPACEWITHROOTDEPENDENCY_ID, extension.getId());
        assertFalse(this.handler.getExtensions().get(null).contains(extension));
        assertTrue(this.handler.getExtensions().get("namespace").contains(extension));

        // installedwithfeatureasdependency
        extension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_WITHFEATUREASDEPENDENCY_ID);

        assertNotNull(extension);
        assertEquals(TestResources.INSTALLED_WITHFEATUREASDEPENDENCY_ID, extension.getId());

        extension = this.installedExtensionRepository
            .getInstalledExtension(TestResources.INSTALLED_WITHFEATUREASDEPENDENCY_ID.getId(), "namespace");

        assertNotNull(extension);
        assertEquals(TestResources.INSTALLED_WITHFEATUREASDEPENDENCY_ID, extension.getId());
        assertTrue(this.handler.getExtensions().get(null).contains(extension));
        assertFalse(this.handler.getExtensions().get("namespace").contains(extension));

        // invalidextension
        extension = this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_INVALID_ID);

        assertNotNull(extension);
        assertEquals(TestResources.INSTALLED_INVALID_ID, extension.getId());
        assertFalse(extension.isValid(null));
        assertFalse(this.handler.getExtensions().get(null).contains(extension));
        assertFalse(this.handler.getExtensions().get("namespace").contains(extension));

        // installedmanagedextension
        extension = this.installedExtensionRepository.getInstalledExtension("installedmanagedextension", null);

        assertNotNull(extension);
        assertTrue(extension.isValid(null));
        assertEquals("2.0", extension.getId().getVersion().toString());

        extension = this.installedExtensionRepository.getInstalledExtension("installedmanagedextension", "namespace");

        assertNotNull(extension);
        assertTrue(extension.isValid(null));
        assertEquals("2.0", extension.getId().getVersion().toString());

        extension = this.installedExtensionRepository.getInstalledExtension("installedmanageddependency", null);

        assertNotNull(extension);
        assertTrue(extension.isValid(null));
        assertEquals("2.0", extension.getId().getVersion().toString());

        extension =
            this.installedExtensionRepository.getInstalledExtension("installedmanagedtransitivedependency", null);

        assertNotNull(extension);
        assertTrue(extension.isValid(null));
        assertEquals("1.0", extension.getId().getVersion().toString());

        // installedorphaneddependency

        extension = this.installedExtensionRepository.getInstalledExtension("installedorphaneddependency", null);

        assertNotNull(extension);
        assertTrue(extension.isValid(null));
        assertEquals("version", extension.getId().getVersion().toString());

        // install on several namespaces

        extension = this.installedExtensionRepository.getInstalledExtension("installedonseveralnamespaces", null);

        assertNull(extension);

        extension =
            this.installedExtensionRepository.getInstalledExtension("installedonseveralnamespaces", "namespace1");

        assertNotNull(extension);
        assertTrue(extension.isValid("namespace1"));
        assertTrue(extension.isInstalled("namespace1"));
        assertEquals("version", extension.getId().getVersion().toString());

        extension =
            this.installedExtensionRepository.getInstalledExtension("installedonseveralnamespaces", "namespace2");

        assertNotNull(extension);
        assertTrue(extension.isValid("namespace2"));
        assertTrue(extension.isInstalled("namespace2"));
        assertEquals("version", extension.getId().getVersion().toString());

        // Missing optional dependency

        extension =
            this.installedExtensionRepository.getInstalledExtension("installedwithmissingoptionaldependency", null);

        assertNotNull(extension);
        assertTrue(extension.isValid(null));
        assertTrue(extension.isInstalled(null));
        assertEquals("version", extension.getId().getVersion().toString());

        extension =
            this.installedExtensionRepository.getInstalledExtension("installedwithnotoptionalmanaged", null);

        assertNotNull(extension);
        assertTrue(extension.isValid(null));
        assertTrue(extension.isInstalled(null));
        assertEquals("version", extension.getId().getVersion().toString());
    }

    @Test
    public void testGetLocalExtension()
    {
        assertNull(this.installedExtensionRepository.getInstalledExtension("unexistingextension", null));

        Extension extension =
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), null);

        assertNotNull(extension);
        assertEquals(TestResources.INSTALLED_ID, extension.getId());
        assertEquals("test", extension.getType());
        assertEquals(Arrays.asList(TestResources.INSTALLED_ID.getId() + "-feature"),
            new ArrayList<String>(extension.getFeatures()));

        ExtensionDependency dependency = extension.getDependencies().iterator().next();
        assertEquals(TestResources.INSTALLED_DEPENDENCY_ID.getId(), dependency.getId());
        assertEquals(TestResources.INSTALLED_DEPENDENCY_ID.getVersion(),
            dependency.getVersionConstraint().getVersion());
    }

    @Test
    public void testResolve() throws ResolveException
    {
        try {
            this.installedExtensionRepository.resolve(new ExtensionId("unexistingextension", "version"));

            fail("Resolve should have failed");
        } catch (ResolveException expected) {
            // expected
        }

        try {
            this.installedExtensionRepository
                .resolve(new ExtensionId(TestResources.INSTALLED_ID.getId(), "wrongversion"));

            fail("Resolve should have failed");
        } catch (ResolveException expected) {
            // expected
        }

        Extension extension = this.installedExtensionRepository.resolve(TestResources.INSTALLED_ID);

        assertNotNull(extension);
        assertEquals(TestResources.INSTALLED_ID, extension.getId());
    }

    @Test
    public void testResolveDependency() throws ResolveException
    {
        try {
            this.installedExtensionRepository.resolve(
                new DefaultExtensionDependency("unexistingextension", new DefaultVersionConstraint("version")));

            fail("Resolve should have failed");
        } catch (ResolveException expected) {
            // expected
        }

        try {
            this.installedExtensionRepository.resolve(new DefaultExtensionDependency(TestResources.INSTALLED_ID.getId(),
                new DefaultVersionConstraint("wrongversion")));

            fail("Resolve should have failed");
        } catch (ResolveException expected) {
            // expected
        }

        Extension extension =
            this.installedExtensionRepository.resolve(new DefaultExtensionDependency(TestResources.INSTALLED_ID.getId(),
                new DefaultVersionConstraint(TestResources.INSTALLED_ID.getVersion().getValue())));

        assertNotNull(extension);
        assertEquals(TestResources.INSTALLED_ID, extension.getId());
    }

    @Test
    public void testInstallTwice() throws InstallException
    {
        // Change status
        this.installedExtensionRepository.installExtension(this.resources.installed, "namespace",
            !this.resources.installed.isDependency("namespace"));

        // Try to install again with the same status
        try {
            this.installedExtensionRepository.installExtension(this.resources.installed, "namespace",
                this.resources.installed.isDependency("namespace"));
            fail("Install should have failed");
        } catch (InstallException expected) {
            // expected
        }
    }

    @Test
    public void testStoreExtensionAndInstall()
        throws ResolveException, LocalExtensionRepositoryException, InstallException
    {
        install(TestResources.REMOTE_SIMPLE_ID);

        assertNotNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(), null));
        assertNotNull(this.installedExtensionRepository.getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId(),
            "namespace"));
        assertNotNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.REMOTE_SIMPLE_ID.getId() + "-feature", null));
    }

    @Test
    public void testUninstallExtension() throws UninstallException, InstallException
    {
        // uninstall from root

        this.installedExtensionRepository.uninstallExtension(this.resources.installed, null);

        assertNull(this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), null));
        assertNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), "namespace"));
        assertNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.INSTALLED_ID.getId() + "-feature", null));
        assertNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.INSTALLED_ID.getId() + "-feature", "namespace"));

        this.installedExtensionRepository.uninstallExtension(this.resources.installedDependency, null);

        assertNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.INSTALLED_DEPENDENCY_ID.getId(), null));

        // uninstall from namespace

        this.installedExtensionRepository.installExtension(this.resources.installedDependency, "namespace", false);
        this.installedExtensionRepository.installExtension(this.resources.installed, "namespace", false);

        this.installedExtensionRepository.uninstallExtension(this.resources.installed, "namespace");

        assertNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), "namespace"));
        assertNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.INSTALLED_ID.getId() + "-feature", "namespace"));

        this.installedExtensionRepository.uninstallExtension(this.resources.installedDependency, "namespace");

        assertNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.INSTALLED_DEPENDENCY_ID.getId(), "namespace"));

        // uninstall from namespace with dependency on root

        this.installedExtensionRepository.installExtension(this.resources.installedDependency, null, false);
        this.installedExtensionRepository.installExtension(this.resources.installed, "namespace", false);

        this.installedExtensionRepository.uninstallExtension(this.resources.installed, "namespace");

        assertNull(
            this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(), "namespace"));
        assertNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.INSTALLED_ID.getId() + "-feature", "namespace"));

        this.installedExtensionRepository.uninstallExtension(this.resources.installedDependency, null);

        assertNull(this.installedExtensionRepository
            .getInstalledExtension(TestResources.INSTALLED_DEPENDENCY_ID.getId(), null));
    }

    @Test
    public void testBackwardDependenciesAfterInit() throws ResolveException
    {
        // installedextension
        assertEquals(Arrays.asList(this.resources.installedwithfeatureasdependency),
            this.installedExtensionRepository.getBackwardDependencies(TestResources.INSTALLED_ID.getId(), null));
        assertEquals(new HashMap<String, Collection<InstalledExtension>>()
        {
            {
                put(null, Arrays
                    .asList(DefaultInstalledExtensionRepositoryTest.this.resources.installedwithfeatureasdependency));
                put("namespace", Arrays.asList(
                    DefaultInstalledExtensionRepositoryTest.this.resources.installedonnemspacewithrootdependency));
            }
        }, this.installedExtensionRepository.getBackwardDependencies(TestResources.INSTALLED_ID));

        // installedextensiondependency
        assertEquals(Arrays.asList(this.resources.installed), this.installedExtensionRepository
            .getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID.getId(), null));
        assertEquals(Collections.singletonMap(null, Arrays.asList(this.resources.installed)),
            this.installedExtensionRepository.getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID));

        // installedonnemspacewithrootdependency
        assertEquals(Collections.EMPTY_MAP, this.installedExtensionRepository
            .getBackwardDependencies(TestResources.INSTALLED_ONNAMESPACEWITHROOTDEPENDENCY_ID));
        assertEquals(Collections.EMPTY_LIST, this.installedExtensionRepository
            .getBackwardDependencies(TestResources.INSTALLED_ONNAMESPACEWITHROOTDEPENDENCY_ID.getId(), "namespace"));
    }

    @Test
    public void testBackwardDependenciesAfterUninstall() throws ResolveException, UninstallException
    {
        this.installedExtensionRepository.uninstallExtension(this.resources.installed, null);

        assertEquals(Collections.EMPTY_LIST, this.installedExtensionRepository
            .getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID.getId(), null));
    }

    @Test
    public void testBackwardDependenciesWithExtensionAndDepOnRoot() throws ResolveException
    {
        assertEquals(Arrays.asList(this.resources.installed),
            new ArrayList<InstalledExtension>(this.installedExtensionRepository
                .getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID.getId(), null)));

        assertEquals(Arrays.asList(), new ArrayList<InstalledExtension>(this.installedExtensionRepository
            .getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID.getId(), "namespace")));

        assertEquals(Arrays.asList(this.resources.installedwithfeatureasdependency), new ArrayList<InstalledExtension>(
            this.installedExtensionRepository.getBackwardDependencies(TestResources.INSTALLED_ID.getId(), null)));

        Map<String, Collection<InstalledExtension>> map = new HashMap<String, Collection<InstalledExtension>>();
        map.put(null, Arrays.asList(this.resources.installed));

        assertEquals(map,
            this.installedExtensionRepository.getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID));
    }

    @Test
    public void testBackwardDependenciesWithExtensionOnNamespaceAndDepOnNamespace()
        throws InstallException, ResolveException, UninstallException
    {
        this.installedExtensionRepository.uninstallExtension(this.resources.installed, null);

        this.installedExtensionRepository.installExtension(this.resources.installed, "namespace", true);

        assertEquals(Collections.EMPTY_LIST, this.installedExtensionRepository
            .getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID.getId(), null));

        assertEquals(Arrays.asList(this.resources.installed), this.installedExtensionRepository
            .getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID.getId(), "namespace"));

        assertEquals(Arrays.asList(this.resources.installedonnemspacewithrootdependency),
            this.installedExtensionRepository.getBackwardDependencies(TestResources.INSTALLED_ID.getId(), "namespace"));
    }

    @Test
    public void testBackwardDependenciesWithExtensionAndDepOnNamespace()
        throws InstallException, ResolveException, UninstallException
    {
        this.installedExtensionRepository.uninstallExtension(this.resources.installed, null);
        this.installedExtensionRepository.uninstallExtension(this.resources.installedDependency, null);

        this.installedExtensionRepository.installExtension(this.resources.installedDependency, "namespace", true);
        this.installedExtensionRepository.installExtension(this.resources.installed, "namespace", true);

        assertEquals(Arrays.asList(this.resources.installed), this.installedExtensionRepository
            .getBackwardDependencies(TestResources.INSTALLED_DEPENDENCY_ID.getId(), "namespace"));

        assertEquals(Arrays.asList(this.resources.installedonnemspacewithrootdependency),
            this.installedExtensionRepository.getBackwardDependencies(TestResources.INSTALLED_ID.getId(), "namespace"));
    }

    @Test
    public void testSearch() throws SearchException
    {
        IterableResult<Extension> result = this.installedExtensionRepository.search(null, 0, -1);

        assertEquals(17, result.getTotalHits());
        assertEquals(17, result.getSize());
        assertEquals(0, result.getOffset());

        result = this.installedExtensionRepository.search("", 0, -1);

        assertEquals(17, result.getTotalHits());
        assertEquals(17, result.getSize());
        assertEquals(0, result.getOffset());

        result = this.installedExtensionRepository.search("extension", 0, -1);

        assertEquals(5, result.getTotalHits());
        assertEquals(5, result.getSize());
        assertEquals(0, result.getOffset());

        result = this.installedExtensionRepository.search("Extension", 0, -1);

        assertEquals(5, result.getTotalHits());
        assertEquals(5, result.getSize());
        assertEquals(0, result.getOffset());

        result = this.installedExtensionRepository.search("dependency", 0, -1);

        assertEquals(9, result.getTotalHits());
        assertEquals(9, result.getSize());
        assertEquals(0, result.getOffset());

        result = this.installedExtensionRepository.search(null, 0, 0);

        assertEquals(17, result.getTotalHits());
        assertEquals(0, result.getSize());
        assertEquals(0, result.getOffset());

        result = this.installedExtensionRepository.search(null, 0, 2);

        assertEquals(17, result.getTotalHits());
        assertEquals(2, result.getSize());
        assertEquals(0, result.getOffset());

        result = this.installedExtensionRepository.search(null, 0, 1);

        assertEquals(17, result.getTotalHits());
        assertEquals(1, result.getSize());
        assertEquals(0, result.getOffset());

        result = this.installedExtensionRepository.search(null, 1, 2);

        assertEquals(17, result.getTotalHits());
        assertEquals(2, result.getSize());
        assertEquals(1, result.getOffset());

        result = this.installedExtensionRepository.search(null, 2, 2);

        assertEquals(17, result.getTotalHits());
        assertEquals(2, result.getSize());
        assertEquals(2, result.getOffset());

        result = this.installedExtensionRepository.search(null, -1, 2);

        assertEquals(17, result.getTotalHits());
        assertEquals(2, result.getSize());
        assertEquals(-1, result.getOffset());

        result = this.installedExtensionRepository.search(null, -1, 1);

        assertEquals(17, result.getTotalHits());
        assertEquals(1, result.getSize());
        assertEquals(-1, result.getOffset());
    }

    @Test
    public void testAdvancedSearchWithNullQuery() throws SearchException
    {
        ExtensionQuery query = new ExtensionQuery();

        query.addFilter(Extension.FIELD_ID, TestResources.INSTALLED_ID.getId(), COMPARISON.EQUAL);

        IterableResult<Extension> result = this.installedExtensionRepository.search(query);

        assertEquals(1, result.getTotalHits());
        assertEquals(1, result.getSize());
        assertEquals(0, result.getOffset());
        assertEquals(TestResources.INSTALLED_ID, result.iterator().next().getId());
    }

    @Test
    public void testAdvancedSearchInstalledWithNullQuery() throws SearchException
    {
        ExtensionQuery query = new ExtensionQuery();

        query.addFilter(Extension.FIELD_ID, TestResources.INSTALLED_ID.getId(), COMPARISON.EQUAL);

        IterableResult<InstalledExtension> result =
            this.installedExtensionRepository.searchInstalledExtensions((String) null, query);

        assertEquals(1, result.getTotalHits());
        assertEquals(1, result.getSize());
        assertEquals(0, result.getOffset());
        assertEquals(TestResources.INSTALLED_ID, result.iterator().next().getId());
    }

    @Test
    public void testSearchInstalledExtensions() throws SearchException
    {
        // Root namespace
        IterableResult<InstalledExtension> result =
            this.installedExtensionRepository.searchInstalledExtensions(null, null, 0, -1);

        assertEquals(11, result.getTotalHits());
        assertEquals(11, result.getSize());
        assertEquals(0, result.getOffset());

        // Namespace "namespace" + "root"
        result = this.installedExtensionRepository.searchInstalledExtensions(null, "namespace", 0, -1);

        assertEquals(15, result.getTotalHits());
        assertEquals(15, result.getSize());
        assertEquals(0, result.getOffset());

        // This namespace does not exist so same as root
        result = this.installedExtensionRepository.searchInstalledExtensions(null, "notnamespace", 0, -1);

        assertEquals(11, result.getTotalHits());
        assertEquals(11, result.getSize());
        assertEquals(0, result.getOffset());
    }

    @Test
    public void testgetInstalledExtensionFeatureNamespace()
    {
        InstalledExtension extension;

        // installed extension

        extension = this.installedExtensionRepository.getInstalledExtension(TestResources.INSTALLED_ID.getId(),
            TestResources.INSTALLED_ID.getVersion().toString());

        assertNotNull(extension);
        assertEquals(TestResources.INSTALLED_ID, extension.getId());

        // installed feature

        extension = this.installedExtensionRepository.getInstalledExtension(
            TestResources.INSTALLED_ID.getId() + "-feature", TestResources.INSTALLED_ID.getVersion().toString());

        assertNotNull(extension);
        assertEquals(TestResources.INSTALLED_ID, extension.getId());
    }
}
