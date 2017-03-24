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
package org.xwiki.extension.jar;

import java.io.File;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.junit.Assert;
import org.junit.Test;
import org.xwiki.classloader.ClassLoaderManager;
import org.xwiki.component.internal.StackingComponentEventManager;
import org.xwiki.component.internal.multi.ComponentManagerManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.InstallException;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.repository.internal.installed.DefaultInstalledExtension;
import org.xwiki.extension.test.AbstractExtensionHandlerTest;
import org.xwiki.logging.LogLevel;
import org.xwiki.logging.LoggerManager;
import org.xwiki.observation.ObservationManager;
import org.xwiki.test.annotation.AfterComponent;

import packagefile.jarextension.DefaultTestComponent;
import packagefile.jarextension.TestComponent;
import packagefile.jarextensionwithdeps.DefaultTestComponentWithDeps;
import packagefile.jarextensionwithdeps.TestComponentWithDeps;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class JarExtensionHandlerTest extends AbstractExtensionHandlerTest
{
    private ComponentManagerManager componentManagerManager;

    private ClassLoader testApplicationClassloader;

    private ClassLoaderManager jarExtensionClassLoader;

    private static final String NAMESPACE = "namespace";

    @AfterComponent
    public void registerComponents() throws Exception
    {
        // Override the system ClassLoader to isolate class loading of extensions from the current ClassLoader
        // (which already contains the extensions)
        this.mocker.registerComponent(TestJarExtensionClassLoaderManager.class);

        // Make sure to fully enable ObservationManager to test EventListener live registration
        StackingComponentEventManager componentEventManager = new StackingComponentEventManager();
        componentEventManager.shouldStack(false);
        this.mocker.setComponentEventManager(componentEventManager);

        // Ignore warning log during setup
        ((LoggerManager) this.mocker.getInstance(LoggerManager.class)).pushLogListener(null);
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        this.testApplicationClassloader = Thread.currentThread().getContextClassLoader();

        // lookup
        this.componentManagerManager = this.mocker.getInstance(ComponentManagerManager.class);
        this.jarExtensionClassLoader = this.mocker.getInstance(ClassLoaderManager.class);

        // Make sure to fully enable ObservationManager to test EventListener live registration
        StackingComponentEventManager componentEventManager =
            (StackingComponentEventManager) this.mocker.getComponentEventManager();
        ObservationManager manager = this.mocker.getInstance(ObservationManager.class);
        componentEventManager.setObservationManager(manager);

        ((LoggerManager) this.mocker.getInstance(LoggerManager.class)).popLogListener();
    }

    private void assertNotEquals(Type type1, Type type2)
    {
        if (type1.equals(type2)) {
            Assert.fail("expected not equals");
        }
    }

    /**
     * @param namespace the namespace to be used
     * @return the extension class loader for the current namespace
     */
    private ClassLoader getExtensionClassloader(String namespace)
    {
        ClassLoader extensionLoader = this.jarExtensionClassLoader.getURLClassLoader(namespace, false);
        if (extensionLoader == null) {
            extensionLoader =
                ((TestJarExtensionClassLoaderManager) this.jarExtensionClassLoader).testExtensionClassLoader;
        }

        return extensionLoader;
    }

    /**
     * @param namespace the namespace to be used
     * @return the extension ComponentManager for the current namespace
     */
    private ComponentManager getExtensionComponentManager(String namespace)
    {
        ComponentManager extensionComponentManager = this.componentManagerManager.getComponentManager(namespace, false);
        if (extensionComponentManager == null) {
            try {
                extensionComponentManager = this.mocker.getInstance(ComponentManager.class);
            } catch (Exception e) {
                // Should never happen
            }
        }

        return extensionComponentManager;
    }

    /**
     * Check that the extension is properly reported to be installed in all namespaces.
     *
     * @param installedExtension the local extension to check
     */
    private void checkInstallStatus(InstalledExtension installedExtension)
    {
        checkInstallStatus(installedExtension, null);
    }

    /**
     * Check that the extension is properly reported to be installed in the given namespace.
     *
     * @param installedExtension the local extension to check
     * @param namespace the namespace where it has been installed
     */
    private void checkInstallStatus(InstalledExtension installedExtension, String namespace)
    {
        // check extension status
        Assert.assertNotNull(installedExtension);
        Assert.assertNotNull(installedExtension.getFile());
        Assert.assertTrue(new File(installedExtension.getFile().getAbsolutePath()).exists());
        Assert.assertTrue(installedExtension.isInstalled(namespace));
        if (namespace != null) {
            Assert.assertFalse(installedExtension.isInstalled(null));
        }

        // check repository status
        Assert.assertNotNull(
            this.installedExtensionRepository.getInstalledExtension(installedExtension.getId().getId(), namespace));
        if (namespace != null) {
            Assert.assertNull(
                this.installedExtensionRepository.getInstalledExtension(installedExtension.getId().getId(), null));
        }
    }

    private Type getLoadedType(Type role, ClassLoader extensionLoader) throws ClassNotFoundException
    {
        if (role instanceof Class) {
            return Class.forName(((Class<?>) role).getName(), true, extensionLoader);
        } else if (role instanceof ParameterizedType) {
            Class<?> rawType =
                Class.forName(((Class<?>) ((ParameterizedType) role).getRawType()).getName(), true, extensionLoader);
            return new DefaultParameterizedType(((ParameterizedType) role).getOwnerType(), rawType,
                ((ParameterizedType) role).getActualTypeArguments());
        }

        return null;
    }

    /**
     * Check that an extension is effectively available in all namespace and that the global component manager provide
     * the expected default implementation.
     *
     * @param role the role expected to be provided by the extension
     * @param implementation the implementation expected for the given role
     * @param <T> the role class
     * @return the effective role class in the extension class loader
     * @throws Exception on error
     */
    private <T> Type checkJarExtensionAvailability(Type role, Class<? extends T> implementation) throws Exception
    {
        return checkJarExtensionAvailability(role, implementation, null);
    }

    /**
     * Check that an extension is effectively available in the given namespace and that the corresponding component
     * manager provide the expected default implementation.
     *
     * @param role the role expected to be provided by the extension
     * @param implementation the implementation expected for the given role
     * @param namespace the namespace where the extension is expected to installed
     * @param <T> the role class
     * @return the effective role class in the extension class loader
     * @throws Exception on error
     */
    private <T> Type checkJarExtensionAvailability(Type role, Class<? extends T> implementation, String namespace)
        throws Exception
    {
        ClassLoader extensionLoader = getExtensionClassloader(namespace);
        Assert.assertNotNull(extensionLoader);
        Assert.assertNotSame(this.testApplicationClassloader, extensionLoader);

        Type loadedRole = getLoadedType(role, extensionLoader);
        // Ensure the loaded role does not came from the application classloader (a check to validate the test)
        Assert.assertFalse(loadedRole.equals(role));

        if (namespace != null) {
            try {
                this.jarExtensionClassLoader.getURLClassLoader(null, false)
                    .loadClass(ReflectionUtils.getTypeClass(loadedRole).getName());
                Assert.fail("the interface should not be in the root class loader");
            } catch (ClassNotFoundException expected) {
                // expected
            }
        }

        // check components managers
        Class<?> componentInstanceClass = null;
        if (namespace != null) {
            componentInstanceClass = getExtensionComponentManager(namespace).getInstance(loadedRole).getClass();

            try {
                this.mocker.getInstance(loadedRole);
                Assert.fail("the component should not be in the root component manager");
            } catch (ComponentLookupException expected) {
                // expected
            }
        } else {
            componentInstanceClass = this.mocker.getInstance(loadedRole).getClass();
        }
        Assert.assertEquals(implementation.getName(), componentInstanceClass.getName());
        Assert.assertNotSame(implementation, componentInstanceClass);

        return loadedRole;
    }

    /**
     * Check that the extension is properly reported to be not installed in all namespace.
     *
     * @param localExtension the local extension to check
     */
    private void ckeckUninstallStatus(LocalExtension localExtension)
    {
        ckeckUninstallStatus(localExtension, null);
    }

    /**
     * Check that the extension is properly reported to be not installed in the given namespace.
     *
     * @param localExtension the local extension to check
     * @param namespace the namespace where it should not be installed
     */
    private void ckeckUninstallStatus(LocalExtension localExtension, String namespace)
    {
        // check extension status
        Assert.assertFalse(DefaultInstalledExtension.isInstalled(localExtension, namespace));
        Assert.assertFalse(DefaultInstalledExtension.isInstalled(localExtension, null));

        // check repository status
        InstalledExtension installedExtension =
            this.installedExtensionRepository.getInstalledExtension(localExtension.getId().getId(), namespace);
        if (installedExtension != null) {
            Assert.assertNotEquals(localExtension.getId(), installedExtension.getId());
        }
    }

    /**
     * Check that an extension is effectively not available in all namespace and that the corresponding component
     * manager does not provide an implementation.
     *
     * @param role the role expected to not be provide
     * @throws Exception on error
     */
    private void checkJarExtensionUnavailability(Type role) throws Exception
    {
        checkJarExtensionUnavailability(role, null);
    }

    /**
     * Check that an extension is effectively not available in the given namespace and that the corresponding component
     * manager does not provide an implementation.
     *
     * @param role the role expected to not be provide
     * @param namespace the namespace where the extension is not expected to be installed
     * @throws Exception on error
     */
    private void checkJarExtensionUnavailability(Type role, String namespace) throws Exception
    {
        try {
            ClassLoader extensionLoader = getExtensionClassloader(namespace);
            Type loadedRole = getLoadedType(role, extensionLoader);

            // check components managers
            this.mocker.getInstance(loadedRole);
            Assert.fail("the extension has not been uninstalled, component found!");
        } catch (ComponentLookupException unexpected) {
            Assert.fail("the extension has not been uninstalled, role found!");
        } catch (ClassNotFoundException expected) {
            // expected
        }
    }

    @Test
    public void testInstallAndUninstallExtension() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("org.xwiki.test:test-extension", "test");

        // actual install test
        InstalledExtension installedExtension = install(extensionId);

        checkInstallStatus(installedExtension);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature", null));

        Type extensionRole1 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);

        // try to install again
        try {
            install(extensionId);
            Assert.fail("installExtension should have failed");
        } catch (InstallException expected) {
            // expected
        }

        // actual uninstall test
        LocalExtension localExtension = uninstall(extensionId, null);

        ckeckUninstallStatus(localExtension);

        checkJarExtensionUnavailability(TestComponent.TYPE_STRING);

        // actual reinstall test
        installedExtension = install(extensionId);

        checkInstallStatus(installedExtension);

        Type extensionRole2 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);
        assertNotEquals(extensionRole1, extensionRole2);
    }

    @Test
    public void testInstallAndUninstallWebjarExtension() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("webjar", "test");

        // actual install test
        InstalledExtension installedExtension = install(extensionId);

        checkInstallStatus(installedExtension);

        // actual uninstall test
        LocalExtension localExtension = uninstall(extensionId, null);

        ckeckUninstallStatus(localExtension);

        // actual reinstall test
        installedExtension = install(extensionId);

        checkInstallStatus(installedExtension);
    }

    @Test
    public void testUpgradeAndDowngradeExtensionOnNamespace() throws Throwable
    {
        final ExtensionId extensionId1 = new ExtensionId("test", "1.0");
        final ExtensionId extensionId2 = new ExtensionId("test", "2.0");

        // install 1.0
        InstalledExtension installedExtension = install(extensionId1, NAMESPACE);

        checkInstallStatus(installedExtension, NAMESPACE);

        Assert.assertSame(installedExtension,
            this.installedExtensionRepository.getInstalledExtension(extensionId1.getId(), NAMESPACE));
        Assert.assertNull(this.installedExtensionRepository.getInstalledExtension(extensionId1.getId(), null));

        Class<?> clazz = Class.forName("test.TestClass", true, getExtensionClassloader(NAMESPACE));
        assertNotNull(MethodUtils.getAccessibleMethod(clazz, "method1"));
        assertNull(MethodUtils.getAccessibleMethod(clazz, "method2"));

        // upgrade to 2.0
        installedExtension = install(extensionId2, NAMESPACE);

        checkInstallStatus(installedExtension, NAMESPACE);

        Assert.assertSame(installedExtension,
            this.installedExtensionRepository.getInstalledExtension(extensionId2.getId(), NAMESPACE));
        Assert.assertNull(this.installedExtensionRepository.getInstalledExtension(extensionId2.getId(), null));

        clazz = Class.forName("test.TestClass", true, getExtensionClassloader(NAMESPACE));
        assertNotNull(MethodUtils.getAccessibleMethod(clazz, "method1"));
        assertNotNull(MethodUtils.getAccessibleMethod(clazz, "method2"));

        // downgrade to 1.0
        installedExtension = install(extensionId1, NAMESPACE);

        checkInstallStatus(installedExtension, NAMESPACE);

        Assert.assertSame(installedExtension,
            this.installedExtensionRepository.getInstalledExtension(extensionId1.getId(), NAMESPACE));
        Assert.assertNull(this.installedExtensionRepository.getInstalledExtension(extensionId1.getId(), null));

        clazz = Class.forName("test.TestClass", true, getExtensionClassloader(NAMESPACE));
        assertNotNull(MethodUtils.getAccessibleMethod(clazz, "method1"));
        assertNull(MethodUtils.getAccessibleMethod(clazz, "method2"));
    }

    @Test
    public void testInstallAndUninstallExtensionOnNamespace() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("org.xwiki.test:test-extension", "test");

        // actual install test
        InstalledExtension installedExtension = install(extensionId, NAMESPACE);

        checkInstallStatus(installedExtension, NAMESPACE);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature", NAMESPACE));
        Assert.assertNull(this.installedExtensionRepository.getInstalledExtension("feature", null));

        checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, NAMESPACE);

        try {
            install(extensionId, NAMESPACE);
            Assert.fail("installExtension should have failed");
        } catch (InstallException expected) {
            // expected
        }

        // actual uninstall test
        LocalExtension localExtension = uninstall(extensionId, null);

        ckeckUninstallStatus(localExtension, NAMESPACE);

        checkJarExtensionUnavailability(TestComponent.TYPE_STRING, NAMESPACE);
    }

    @Test
    public void testInstallAndUninstallExtensionWithoutComponents() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("simplejar", "test");

        // actual install test
        InstalledExtension installedExtension = install(extensionId);

        checkInstallStatus(installedExtension);

        // actual uninstall test
        LocalExtension localExtension = uninstall(extensionId, null);

        ckeckUninstallStatus(localExtension);

        // actual reinstall test
        installedExtension = install(extensionId);

        checkInstallStatus(installedExtension);
    }

    @Test
    public void testInstallAndUninstallExtensionWithDependency() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("org.xwiki.test:test-extension-with-deps", "test");

        // actual install test
        InstalledExtension installedExtension = install(extensionId);

        checkInstallStatus(installedExtension);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", null));
        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature", null));

        Type extensionRole1 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class);
        Type extensionDep1 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);

        // actual uninstall test
        LocalExtension localExtension = uninstall(extensionId, null);

        ckeckUninstallStatus(localExtension);

        checkJarExtensionUnavailability(TestComponentWithDeps.class);
        Type extensionDep2 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);
        assertNotEquals(extensionDep1, extensionDep2);

        // actual reinstall test
        installedExtension = install(extensionId);

        checkInstallStatus(installedExtension);

        Type extensionRole2 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class);
        Type extensionDep3 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);
        assertNotEquals(extensionRole1, extensionRole2);
        Assert.assertEquals(extensionDep2, extensionDep3);
    }

    @Test
    public void testInstallAndUninstallExtensionWithDependencyOnANamespace() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("org.xwiki.test:test-extension-with-deps", "test");
        final String namespace = NAMESPACE;

        // actual install test
        InstalledExtension installExtension = install(extensionId, namespace);

        checkInstallStatus(installExtension, namespace);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", namespace));
        Assert.assertNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", null));

        checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace);
        checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace);

        // actual uninstall test
        LocalExtension localExtension = uninstall(extensionId, null);

        ckeckUninstallStatus(localExtension, namespace);

        checkJarExtensionUnavailability(TestComponentWithDeps.class, namespace);
        checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace);
    }

    @Test
    public void testInstallEntensionAndUninstallDependency() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("org.xwiki.test:test-extension-with-deps", "test");
        final ExtensionId dependencyId = new ExtensionId("org.xwiki.test:test-extension", "test");

        // actual install test
        InstalledExtension installedExtension = install(extensionId);

        checkInstallStatus(installedExtension);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", null));
        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature", null));

        Type extensionRole1 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class);
        Type extensionDep1 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);

        // actual uninstall test
        LocalExtension localExtension = uninstall(dependencyId, null);

        ckeckUninstallStatus(localExtension);

        checkJarExtensionUnavailability(TestComponent.TYPE_STRING);
        checkJarExtensionUnavailability(TestComponentWithDeps.class);

        // actual reinstall test
        installedExtension = install(extensionId);

        checkInstallStatus(installedExtension);

        Type extensionRole2 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class);
        Type extensionDep2 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);
        assertNotEquals(extensionRole1, extensionRole2);
        assertNotEquals(extensionDep1, extensionDep2);
    }

    @Test
    public void testInstallExtensionAndUninstallDependencyOnANamespace() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("org.xwiki.test:test-extension-with-deps", "test");
        final ExtensionId dependencyId = new ExtensionId("org.xwiki.test:test-extension", "test");
        final String namespace = NAMESPACE;

        // actual install test
        InstalledExtension installedExtension = install(extensionId, namespace);

        checkInstallStatus(installedExtension, namespace);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", namespace));
        Assert.assertNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", null));

        checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace);
        checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace);

        // actual uninstall test
        LocalExtension localExtension = uninstall(dependencyId, null);

        ckeckUninstallStatus(localExtension, namespace);

        checkJarExtensionUnavailability(TestComponentWithDeps.class, namespace);
        checkJarExtensionUnavailability(TestComponentWithDeps.class);
    }

    @Test
    public void testInstallDependencyInstallExtensionOnANamespaceAndUninstallExtension() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("org.xwiki.test:test-extension-with-deps", "test");
        final ExtensionId dependencyId = new ExtensionId("org.xwiki.test:test-extension", "test");
        final String namespace = NAMESPACE;

        // actual install test
        InstalledExtension installedExtension = install(dependencyId);

        checkInstallStatus(installedExtension);
        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature", null));
        Type extensionDep1 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);

        // actual install test
        installedExtension = install(extensionId, namespace);

        checkInstallStatus(installedExtension, namespace);
        checkInstallStatus(this.installedExtensionRepository.resolve(dependencyId));

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", namespace));
        Type extensionRole1 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace);
        Type extensionDep2 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);
        Assert.assertEquals(extensionDep1, extensionDep2);

        // actual uninstall test
        LocalExtension localExtension = uninstall(extensionId, namespace);

        ckeckUninstallStatus(localExtension, namespace);
        checkInstallStatus(this.installedExtensionRepository.resolve(dependencyId));

        checkJarExtensionUnavailability(TestComponentWithDeps.class, namespace);
        Type extensionDep3 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);
        Assert.assertEquals(extensionDep1, extensionDep3);

        // actual reinstall test
        installedExtension = install(extensionId, namespace);

        checkInstallStatus(installedExtension, namespace);

        Type extensionRole2 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace);
        Type extensionDep4 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);
        assertNotEquals(extensionRole1, extensionRole2);
        Assert.assertEquals(extensionDep1, extensionDep4);
    }

    @Test
    public void testInstallDependencyInstallExtensionOnANamespaceAndUninstallDependency() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("org.xwiki.test:test-extension-with-deps", "test");
        final ExtensionId dependencyId = new ExtensionId("org.xwiki.test:test-extension", "test");
        final String namespace = NAMESPACE;

        // actual install test
        InstalledExtension installedExtension = install(dependencyId);

        checkInstallStatus(installedExtension);
        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature", null));
        Type extensionDep1 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);

        // actual install test
        installedExtension = install(extensionId, namespace);

        checkInstallStatus(installedExtension, namespace);
        checkInstallStatus(this.installedExtensionRepository.resolve(dependencyId));

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", namespace));
        Type extensionRole1 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace);
        Type extensionDep2 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);
        Assert.assertEquals(extensionDep1, extensionDep2);

        // actual uninstall test
        LocalExtension localExtension = uninstall(dependencyId, null);

        ckeckUninstallStatus(localExtension);
        ckeckUninstallStatus(this.localExtensionRepository.resolve(extensionId), namespace);

        checkJarExtensionUnavailability(TestComponent.TYPE_STRING);
        checkJarExtensionUnavailability(TestComponentWithDeps.class, namespace);

        // actual reinstall test
        installedExtension = install(extensionId, namespace);

        checkInstallStatus(installedExtension, namespace);
        checkInstallStatus(this.installedExtensionRepository.resolve(dependencyId), namespace);

        Type extensionRole2 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace);
        Type extensionDep3 =
            checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace);
        assertNotEquals(extensionRole1, extensionRole2);
        assertNotEquals(extensionDep1, extensionDep3);
    }

    @Test
    public void testMultipleInstallOnANamespaceAndUninstall() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("org.xwiki.test:test-extension-with-deps", "test");
        final String namespace1 = "namespace1";
        final String namespace2 = "namespace2";

        // actual install test
        InstalledExtension installedExtension = install(extensionId, namespace1);

        checkInstallStatus(installedExtension, namespace1);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", namespace1));
        Type extensionRole1 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace1);
        Type extensionDep1 =
            checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace1);

        // actual install test
        // TODO: go back to LogLevel.WARN when https://jira.xwiki.org/browse/XCOMMONS-213 is fixed
        installedExtension = install(extensionId, namespace2, LogLevel.ERROR);

        checkInstallStatus(installedExtension, namespace2);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", namespace2));
        Type extensionRole2 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace2);
        Type extensionDep2 =
            checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace2);
        assertNotEquals(extensionRole1, extensionRole2);
        assertNotEquals(extensionDep1, extensionDep2);

        // actual uninstall test
        // FIXME: Ignore warning because of https://jira.xwiki.org/browse/XCOMMONS-213
        LocalExtension localExtension = uninstall(extensionId, namespace1, LogLevel.ERROR);

        ckeckUninstallStatus(localExtension);

        checkJarExtensionUnavailability(TestComponentWithDeps.class, namespace1);
        Type extensionDep3 =
            checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace1);
        assertNotEquals(extensionDep1, extensionDep3);

        Type extensionRole3 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace2);
        Type extensionDep4 =
            checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace2);
        Assert.assertEquals(extensionRole2, extensionRole3);
        Assert.assertEquals(extensionDep2, extensionDep4);

        // actual uninstall test
        // FIXME: Ignore warning because of https://jira.xwiki.org/browse/XCOMMONS-213
        localExtension = uninstall(extensionId, namespace2, LogLevel.ERROR);

        ckeckUninstallStatus(localExtension);

        checkJarExtensionUnavailability(TestComponentWithDeps.class, namespace2);
        Type extensionDep5 =
            checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace2);
        assertNotEquals(extensionDep2, extensionDep5);

        Type extensionDep6 =
            checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace1);
        Assert.assertEquals(extensionDep3, extensionDep6);
    }

    @Test
    public void testMultipleInstallOnANamespaceAndUninstallDependency() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("org.xwiki.test:test-extension-with-deps", "test");
        final ExtensionId dependencyId = new ExtensionId("org.xwiki.test:test-extension", "test");
        final String namespace1 = "namespace1";
        final String namespace2 = "namespace2";

        InstalledExtension installedExtension = install(extensionId, namespace1);

        checkInstallStatus(installedExtension, namespace1);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", namespace1));
        Type extensionRole1 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace1);
        Type extensionDep1 =
            checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace1);

        // TODO: go back to LogLevel.WARN when https://jira.xwiki.org/browse/XCOMMONS-213 is fixed
        installedExtension = install(extensionId, namespace2, LogLevel.ERROR);

        checkInstallStatus(installedExtension, namespace2);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", namespace2));
        Type extensionRole2 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace2);
        Type extensionDep2 =
            checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace2);
        assertNotEquals(extensionRole1, extensionRole2);
        assertNotEquals(extensionDep1, extensionDep2);

        // actual uninstall test
        LocalExtension localExtension = uninstall(dependencyId, namespace1);

        ckeckUninstallStatus(localExtension);

        checkJarExtensionUnavailability(TestComponent.TYPE_STRING, namespace1);
        checkJarExtensionUnavailability(TestComponentWithDeps.class, namespace1);

        Type extensionRole3 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace2);
        Type extensionDep3 =
            checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace2);
        Assert.assertEquals(extensionRole2, extensionRole3);
        Assert.assertEquals(extensionDep2, extensionDep3);

        // actual uninstall test
        localExtension = uninstall(dependencyId, namespace2);

        ckeckUninstallStatus(localExtension);

        checkJarExtensionUnavailability(TestComponent.TYPE_STRING, namespace2);
        checkJarExtensionUnavailability(TestComponentWithDeps.class, namespace2);
    }

    @Test
    public void testMultipleInstallOnANamespaceAndUninstallAll() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("org.xwiki.test:test-extension-with-deps", "test");
        final String namespace1 = "namespace1";
        final String namespace2 = "namespace2";

        InstalledExtension installedExtension = install(extensionId, namespace1);

        checkInstallStatus(installedExtension, namespace1);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", namespace1));
        Type extensionRole1 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace1);
        Type extensionDep1 =
            checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace1);

        // TODO: go back to LogLevel.WARN when https://jira.xwiki.org/browse/XCOMMONS-213 is fixed
        installedExtension = install(extensionId, namespace2, LogLevel.ERROR);

        checkInstallStatus(installedExtension, namespace2);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", namespace2));
        Type extensionRole2 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace2);
        Type extensionDep2 =
            checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace2);
        assertNotEquals(extensionRole1, extensionRole2);
        assertNotEquals(extensionDep1, extensionDep2);

        // actual uninstall test
        // FIXME: Ignore warning because of https://jira.xwiki.org/browse/XCOMMONS-213
        LocalExtension localExtension = uninstall(extensionId, null, LogLevel.ERROR);

        ckeckUninstallStatus(localExtension);

        checkJarExtensionUnavailability(TestComponentWithDeps.class, namespace1);
        Type extensionDep3 =
            checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace1);
        assertNotEquals(extensionDep1, extensionDep3);
        Type extensionDep4 =
            checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace2);
        assertNotEquals(extensionDep2, extensionDep4);
    }

    @Test
    public void testMultipleInstallOnANamespaceAndUninstallDependencyAll() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("org.xwiki.test:test-extension-with-deps", "test");
        final ExtensionId dependencyId = new ExtensionId("org.xwiki.test:test-extension", "test");
        final String namespace1 = "namespace1";
        final String namespace2 = "namespace2";

        InstalledExtension installedExtension = install(extensionId, namespace1);

        checkInstallStatus(installedExtension, namespace1);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", namespace1));
        Type extensionRole1 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace1);
        Type extensionDep1 =
            checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace1);

        // TODO: go back to LogLevel.WARN when https://jira.xwiki.org/browse/XCOMMONS-213 is fixed
        installedExtension = install(extensionId, namespace2, LogLevel.ERROR);

        checkInstallStatus(installedExtension, namespace2);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", namespace2));
        Type extensionRole2 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace2);
        Type extensionDep2 =
            checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, namespace2);
        assertNotEquals(extensionRole1, extensionRole2);
        assertNotEquals(extensionDep1, extensionDep2);

        // actual uninstall test
        LocalExtension localExtension = uninstall(dependencyId, null);

        ckeckUninstallStatus(localExtension);

        checkJarExtensionUnavailability(TestComponent.TYPE_STRING, namespace1);
        checkJarExtensionUnavailability(TestComponentWithDeps.class, namespace1);
        checkJarExtensionUnavailability(TestComponent.TYPE_STRING, namespace2);
        checkJarExtensionUnavailability(TestComponentWithDeps.class, namespace2);
    }

    @Test
    public void testMultipleInstallOnANamespaceWithGlobalDependencyAndUninstall() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("org.xwiki.test:test-extension-with-deps", "test");
        final ExtensionId dependencyId = new ExtensionId("org.xwiki.test:test-extension", "test");
        final String namespace1 = "namespace1";
        final String namespace2 = "namespace2";

        // install global deps
        InstalledExtension installedExtension = install(dependencyId);

        checkInstallStatus(installedExtension);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature", null));
        Type extensionDep1 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);

        // actual install test
        installedExtension = install(extensionId, namespace1);

        checkInstallStatus(installedExtension, namespace1);
        checkInstallStatus(this.installedExtensionRepository.resolve(dependencyId));

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", namespace1));
        Type extensionRole1 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace1);
        Type extensionDep2 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);
        Assert.assertEquals(extensionDep1, extensionDep2);

        // actual install test
        installedExtension = install(extensionId, namespace2);

        checkInstallStatus(installedExtension, namespace2);
        checkInstallStatus(this.installedExtensionRepository.resolve(dependencyId));

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", namespace2));
        Type extensionRole2 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace2);
        Type extensionDep3 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);
        assertNotEquals(extensionRole1, extensionRole2);
        Assert.assertEquals(extensionDep1, extensionDep3);

        // actual uninstall test
        LocalExtension localExtension = uninstall(extensionId, namespace1);

        ckeckUninstallStatus(localExtension);
        checkInstallStatus(this.installedExtensionRepository.resolve(dependencyId));

        checkJarExtensionUnavailability(TestComponentWithDeps.class, namespace1);

        Type extensionRole3 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace2);
        Type extensionDep4 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);
        Assert.assertEquals(extensionRole2, extensionRole3);
        Assert.assertEquals(extensionDep1, extensionDep4);

        // actual uninstall test
        localExtension = uninstall(extensionId, namespace2);

        ckeckUninstallStatus(localExtension);
        checkInstallStatus(this.installedExtensionRepository.resolve(dependencyId));

        checkJarExtensionUnavailability(TestComponentWithDeps.class, namespace2);
        Type extensionDep5 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);
        Assert.assertEquals(extensionDep1, extensionDep5);
    }

    @Test
    public void testMultipleInstallOnANamespaceWithGlobalDependencyAndUninstallDependency() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("org.xwiki.test:test-extension-with-deps", "test");
        final ExtensionId dependencyId = new ExtensionId("org.xwiki.test:test-extension", "test");
        final String namespace1 = "namespace1";
        final String namespace2 = "namespace2";

        // install global deps
        InstalledExtension installedExtension = install(dependencyId);

        checkInstallStatus(installedExtension);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature", null));
        Type extensionDep1 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);

        installedExtension = install(extensionId, namespace1);

        checkInstallStatus(installedExtension, namespace1);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", namespace1));
        Type extensionRole1 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace1);
        Type extensionDep2 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);
        Assert.assertEquals(extensionDep1, extensionDep2);

        installedExtension = install(extensionId, namespace2);

        checkInstallStatus(installedExtension, namespace2);

        Assert.assertNotNull(this.installedExtensionRepository.getInstalledExtension("feature-with-deps", namespace2));
        Type extensionRole2 =
            checkJarExtensionAvailability(TestComponentWithDeps.class, DefaultTestComponentWithDeps.class, namespace2);
        Type extensionDep3 = checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);
        assertNotEquals(extensionRole1, extensionRole2);
        Assert.assertEquals(extensionDep1, extensionDep3);

        // actual uninstall test
        LocalExtension localExtension = uninstall(dependencyId, null);

        ckeckUninstallStatus(localExtension);
        ckeckUninstallStatus(this.localExtensionRepository.resolve(extensionId), namespace1);
        ckeckUninstallStatus(this.localExtensionRepository.resolve(extensionId), namespace2);

        checkJarExtensionUnavailability(TestComponent.TYPE_STRING);
        checkJarExtensionUnavailability(TestComponentWithDeps.class, namespace1);
        checkJarExtensionUnavailability(TestComponentWithDeps.class, namespace2);
    }

    @Test
    public void testUninstallInvalidExtensionFromNamespace() throws Throwable
    {
        ExtensionId extensionId = new ExtensionId("invalidextensiononnamespace", "version");

        uninstall(extensionId, "namespaceofinvalidextension");
    }

    @Test
    public void testUninstallInvalidExtensionFromRoot() throws Throwable
    {
        ExtensionId extensionId = new ExtensionId("invalidextensiononroot", "version");

        uninstall(extensionId, null);
    }

    @Test
    public void testExtensionInstalledOnNamespaceAtInit() throws Throwable
    {
        ExtensionId extensionId = new ExtensionId("installedextensiononnamespace", "1.0");

        InstalledExtension installedExtension = this.installedExtensionRepository.resolve(extensionId);

        checkInstallStatus(installedExtension, NAMESPACE);

        checkJarExtensionAvailability(packagefile.installedextensiononnamespace.TestInstalledComponent.TYPE_STRING,
            packagefile.installedextensiononnamespace.DefaultTestInstalledComponent.class, NAMESPACE);

        uninstall(extensionId, NAMESPACE);
    }

    @Test
    public void testExtensionInstalledOnRootAtInit() throws Throwable
    {
        ExtensionId extensionId = new ExtensionId("installedextensiononroot", "1.0");

        InstalledExtension installedExtension = this.installedExtensionRepository.resolve(extensionId);

        checkInstallStatus(installedExtension, null);

        checkJarExtensionAvailability(packagefile.installedextensiononroot.TestInstalledComponent.TYPE_STRING,
            packagefile.installedextensiononroot.DefaultTestInstalledComponent.class, null);

        uninstall(extensionId, null);
    }

    @Test
    public void testInstallOnNamespaceThenOnRoot() throws Throwable
    {
        final ExtensionId extensionId = new ExtensionId("org.xwiki.test:test-extension", "test");

        // install on namespace
        InstalledExtension installedExtension = install(extensionId, NAMESPACE);

        checkInstallStatus(installedExtension, NAMESPACE);

        Assert.assertSame(installedExtension,
            this.installedExtensionRepository.getInstalledExtension(extensionId.getId(), NAMESPACE));
        Assert.assertNull(this.installedExtensionRepository.getInstalledExtension(extensionId.getId(), null));

        checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class, NAMESPACE);

        // install on root
        installedExtension = install(extensionId);

        checkInstallStatus(installedExtension);

        checkJarExtensionAvailability(TestComponent.TYPE_STRING, DefaultTestComponent.class);
    }

    @Test
    public void testInstallOnNamespaceThenUpgradeOnRoot() throws Throwable
    {
        final ExtensionId extensionId1 = new ExtensionId("jarupgrade", "1.0");
        final ExtensionId extensionId2 = new ExtensionId("jarupgrade", "2.0");

        // install on namespace
        InstalledExtension installedExtension1 = install(extensionId1, NAMESPACE);

        checkInstallStatus(installedExtension1, NAMESPACE);

        Assert.assertSame(installedExtension1,
            this.installedExtensionRepository.getInstalledExtension(extensionId1.getId(), NAMESPACE));
        Assert.assertNull(this.installedExtensionRepository.getInstalledExtension(extensionId1.getId(), null));

        checkJarExtensionAvailability(packagefile.jarupgrade1.TestComponent.class,
            packagefile.jarupgrade1.DefaultTestComponent.class, NAMESPACE);

        // install on root
        InstalledExtension installedExtension2 = install(extensionId2);

        LocalExtension localExtension = this.localExtensionRepository.getLocalExtension(extensionId1);

        ckeckUninstallStatus(localExtension, NAMESPACE);
        checkInstallStatus(installedExtension2);

        checkJarExtensionUnavailability(packagefile.jarupgrade1.TestComponent.class, NAMESPACE);
        checkJarExtensionAvailability(packagefile.jarupgrade2.TestComponent.class,
            packagefile.jarupgrade2.DefaultTestComponent.class);
    }
}
