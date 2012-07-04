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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.environment.Environment;
import org.xwiki.extension.handler.ExtensionInitializer;
import org.xwiki.extension.repository.CoreExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryId;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.version.internal.DefaultVersion;

public class RepositoryUtil
{
    private static final String MAVENREPOSITORY_ID = "test-maven";

    private final File permanentDirectory;

    private final File temporaryDirectory;

    private final File extensionDirectory;

    private final File localRepositoryRoot;

    private final File mavenRepositoryRoot;

    private final File remoteRepositoryRoot;

    private final ComponentManager componentManager;

    private final Mockery mockery;

    private final ExtensionPackager extensionPackager;

    private FileExtensionRepository remoteRepository;

    private ComponentAnnotationLoader componentLoader;

    public RepositoryUtil()
    {
        this(null, null);
    }

    public RepositoryUtil(ComponentManager componentManager, Mockery mockery)
    {
        this.componentManager = componentManager;
        this.mockery = mockery;

        File testDirectory = new File("target/test-" + new Date().getTime());

        this.temporaryDirectory = new File(testDirectory, "temporary-dir");

        this.permanentDirectory = new File(testDirectory, "permanent-dir");
        this.extensionDirectory = new File(this.permanentDirectory, "extension/");
        this.localRepositoryRoot = new File(this.extensionDirectory, "repository/");

        this.mavenRepositoryRoot = new File(testDirectory, "maven/");
        this.remoteRepositoryRoot = new File(testDirectory, "remote/");

        this.extensionPackager = new ExtensionPackager(this.permanentDirectory, this.remoteRepositoryRoot);
    }

    public File getPermanentDirectory()
    {
        return this.permanentDirectory;
    }

    public File getTemporaryDirectory()
    {
        return this.temporaryDirectory;
    }

    public File getExtensionDirectory()
    {
        return this.extensionDirectory;
    }

    public File getLocalRepository()
    {
        return this.localRepositoryRoot;
    }

    public File getRemoteRepository()
    {
        return this.remoteRepositoryRoot;
    }

    public File getMavenRepository()
    {
        return this.mavenRepositoryRoot;
    }

    public String getMavenRepositoryId()
    {
        return MAVENREPOSITORY_ID;
    }

    public ExtensionPackager getExtensionPackager()
    {
        return extensionPackager;
    }

    public void setup() throws Exception
    {
        // Mock Environment
        if (this.componentManager != null) {
            final Environment environment = this.mockery.mock(Environment.class);
            this.mockery.checking(new Expectations()
            {
                {
                    allowing(environment).getPermanentDirectory();
                    will(returnValue(getPermanentDirectory()));
                    allowing(environment).getTemporaryDirectory();
                    will(returnValue(getTemporaryDirectory()));
                    allowing(environment).getResourceAsStream(with(any(String.class)));
                    will(returnValue(null));
                }
            });
            DefaultComponentDescriptor<Environment> dcd = new DefaultComponentDescriptor<Environment>();
            dcd.setRoleType(Environment.class);
            this.componentManager.registerComponent(dcd, environment);
        }

        // add default test core extension

        if (this.componentManager != null) {
            registerComponent(ConfigurableDefaultCoreExtensionRepository.class);
            ((ConfigurableDefaultCoreExtensionRepository) this.componentManager
                .getInstance(CoreExtensionRepository.class)).addExtensions("coreextension", new DefaultVersion(
                "version"));
        }

        // copy

        copyResourceFolder(getLocalRepository(), "repository.local");
        boolean mavenRepository = copyResourceFolder(getMavenRepository(), "repository.maven") > 0;

        // remote repositories

        if (this.componentManager != null) {
            ExtensionRepositoryManager repositoryManager =
                this.componentManager.getInstance(ExtensionRepositoryManager.class);

            // light remote repository

            if (copyResourceFolder(getRemoteRepository(), "repository.remote") > 0) {
                this.remoteRepository = new FileExtensionRepository(getRemoteRepository(), this.componentManager);
                repositoryManager.addRepository(remoteRepository);
            }

            // maven repository

            if (mavenRepository) {
                repositoryManager.addRepository(new ExtensionRepositoryId(MAVENREPOSITORY_ID, "maven",
                    getMavenRepository().toURI()));
            }
        }

        // generated extensions

        this.extensionPackager.generateExtensions();

        // init

        if (this.componentManager != null) {
            this.componentManager.<ExtensionInitializer> getInstance(ExtensionInitializer.class).initialize();
        }
    }

    public ComponentAnnotationLoader getComponentLoader()
    {
        if (this.componentLoader == null) {
            this.componentLoader = new ComponentAnnotationLoader();
        }

        return this.componentLoader;
    }

    private void registerComponent(Class< ? > componentClass) throws Exception
    {
        List<ComponentDescriptor> descriptors = getComponentLoader().getComponentsDescriptors(componentClass);

        for (ComponentDescriptor< ? > descriptor : descriptors) {
            this.componentManager.registerComponent(descriptor);
        }
    }

    public int copyResourceFolder(File targetFolder, String resourcePackage) throws IOException
    {
        int nb = 0;

        targetFolder.mkdirs();

        Set<URL> urls = ClasspathHelper.forPackage(resourcePackage);

        if (!urls.isEmpty()) {
            Reflections reflections =
                new Reflections(new ConfigurationBuilder().setScanners(new ResourcesScanner()).setUrls(urls)
                    .filterInputsBy(new FilterBuilder.Include(FilterBuilder.prefix(resourcePackage))));

            for (String resource : reflections.getResources(Pattern.compile(".*"))) {
                File targetFile = new File(targetFolder, resource.substring(resourcePackage.length() + 1));

                InputStream resourceStream = getClass().getResourceAsStream("/" + resource);

                try {
                    FileUtils.copyInputStreamToFile(resourceStream, targetFile);
                    ++nb;
                } finally {
                    resourceStream.close();
                }
            }
        }

        return nb;
    }
}
