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

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import org.mockito.Mockito;
import org.mockito.internal.util.MockUtil;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.configuration.internal.MemoryConfigurationSource;
import org.xwiki.environment.Environment;
import org.xwiki.extension.handler.ExtensionInitializer;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.version.internal.DefaultVersion;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.mockito.ArgumentMatchers.any;

public class MockitoRepositoryUtils extends RepositoryUtils
{
    protected final MockitoComponentManager componentManager;

    private FileExtensionRepository remoteRepository;

    private ComponentAnnotationLoader componentLoader;

    public MockitoRepositoryUtils(MockitoComponentManager componentManager)
    {
        this.componentManager = componentManager;
    }

    @Override
    public void setup() throws Exception
    {
        Environment environment = null;
        if (this.componentManager.hasComponent(Environment.class)) {
            // Reconfigure repository directories based on existing mocked environment
            environment = this.componentManager.getInstance(Environment.class);
            if (MockUtil.isMock(environment)) {
                initializeDirectories(environment);
            } else {
                // Force mocking environment
                environment = null;
            }
        }

        if (environment == null) {
            environment = this.componentManager.registerMockComponent(Environment.class);
            Mockito.when(environment.getPermanentDirectory()).thenReturn(getPermanentDirectory());
            Mockito.when(environment.getTemporaryDirectory()).thenReturn(getTemporaryDirectory());
            Mockito.when(environment.getResourceAsStream(any())).thenReturn(null);
        }

        super.setup();

        ConfigurationSource configurationSource;
        if (!this.componentManager.hasComponent(ConfigurationSource.class)) {
            configurationSource = this.componentManager.registerMemoryConfigurationSource();
        } else {
            configurationSource = this.componentManager.getInstance(ConfigurationSource.class);
        }
        if (configurationSource instanceof MemoryConfigurationSource) {
            // Disable default repositories
            ((MemoryConfigurationSource) configurationSource).setProperty("extension.repositories", Arrays.asList(""));
        }

        // add default test core extension
        registerComponent(ConfigurableDefaultCoreExtensionRepository.class);
        ((ConfigurableDefaultCoreExtensionRepository) this.componentManager.getInstance(CoreExtensionRepository.class))
            .addExtensions("coreextension", new DefaultVersion("version"));

        // register repositories
        ExtensionRepositoryManager repositoryManager =
            this.componentManager.getInstance(ExtensionRepositoryManager.class);

        // light remote repository

        if (copyResourceFolder(getRemoteRepository(), "repository.remote") > 0) {
            this.remoteRepository = new FileExtensionRepository(getRemoteRepository(), this.componentManager);
            repositoryManager.addRepository(this.remoteRepository);
        }

        // maven repositories

        if (getMavenRepository().exists()) {
            repositoryManager.addRepository(newLocalMavenRepository(MAVENREPOSITORY_ID, getMavenRepository().toURI()));
        }

        if (getMaven2Repository().exists()) {
            repositoryManager
                .addRepository(newLocalMavenRepository(MAVEN2REPOSITORY_ID, getMaven2Repository().toURI()));
        }

        // init

        this.componentManager.<ExtensionInitializer>getInstance(ExtensionInitializer.class);
    }

    private DefaultExtensionRepositoryDescriptor newLocalMavenRepository(String id, URI uri)
    {
        DefaultExtensionRepositoryDescriptor descriptor = new DefaultExtensionRepositoryDescriptor(id, "maven", uri);

        // Disable checksum validation
        descriptor.putProperty("checksumPolicy", "ignore");

        return descriptor;
    }

    public MockitoComponentManager getComponentManager()
    {
        return this.componentManager;
    }

    public ComponentAnnotationLoader getComponentLoader()
    {
        if (this.componentLoader == null) {
            this.componentLoader = new ComponentAnnotationLoader();
        }

        return this.componentLoader;
    }

    private void registerComponent(Class<?> componentClass) throws Exception
    {
        List<ComponentDescriptor<?>> descriptors = getComponentLoader().getComponentsDescriptors(componentClass);

        for (ComponentDescriptor<?> descriptor : descriptors) {
            this.componentManager.registerComponent(descriptor);
        }
    }
}
