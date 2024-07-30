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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.xwiki.environment.Environment;
import org.xwiki.test.TestEnvironment;

/**
 * @version $Id$
 */
public class RepositoryUtils
{
    protected static final String MAVENREPOSITORY_ID = "test-maven";

    protected static final String MAVEN2REPOSITORY_ID = "test-maven2";

    protected static final String MAVENUNKNWONREPOSITORY_ID = "test-mavenunknown";

    protected File extensionDirectory;

    protected File localRepositoryRoot;

    protected File mavenRepositoryRoot;

    protected File maven2RepositoryRoot;

    protected File mavenUnknownRepositoryRoot;

    protected File remoteRepositoryRoot;

    protected ExtensionPackager extensionPackager;

    /**
     * @since 16.6.0RC1
     * @since 16.4.2
     * @since 15.10.12
     */
    protected void initializeDirectories(Environment environment)
    {
        this.extensionDirectory = new File(environment.getPermanentDirectory(), "extension/");
        this.localRepositoryRoot = new File(this.extensionDirectory, "repository/");

        File repositoriesRootFile = new File(environment.getPermanentDirectory(), "repositories/");
        this.mavenRepositoryRoot = new File(repositoriesRootFile, "maven/");
        this.maven2RepositoryRoot = new File(repositoriesRootFile, "maven2/");
        this.mavenUnknownRepositoryRoot = new File(repositoriesRootFile, "mavenunknown/");
        this.remoteRepositoryRoot = new File(repositoriesRootFile, "remote/");

        Map<String, RepositorySerializer> repositories = new HashMap<>();
        repositories.put(null, new DefaultRepositorySerializer(getRemoteRepository()));
        repositories.put("remote", repositories.get(null));
        repositories.put("local", new DefaultRepositorySerializer(getLocalRepository()));
        repositories.put("maven", new MavenRepositorySerializer(getMavenRepository()));
        repositories.put("maven2", new MavenRepositorySerializer(getMaven2Repository()));

        this.extensionPackager = new ExtensionPackager(environment.getPermanentDirectory(), repositories);

        System.setProperty("extension.repository.local", getLocalRepository().getAbsolutePath());
        System.setProperty("extension.repository.maven", getMavenRepository().getAbsolutePath());
        System.setProperty("extension.repository.maven2", getMaven2Repository().getAbsolutePath());
        System.setProperty("extension.repository.remote", getRemoteRepository().getAbsolutePath());
        System.setProperty("extension.repository.mavenunknown", getRemoteRepository().getAbsolutePath());
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

    public File getMaven2Repository()
    {
        return this.maven2RepositoryRoot;
    }

    public File getMavenUnknownRepository()
    {
        return this.mavenUnknownRepositoryRoot;
    }

    public String getMavenRepositoryId()
    {
        return MAVENREPOSITORY_ID;
    }

    public String getMaven2RepositoryId()
    {
        return MAVEN2REPOSITORY_ID;
    }

    public String getMavenUnknown()
    {
        return MAVEN2REPOSITORY_ID;
    }

    public ExtensionPackager getExtensionPackager()
    {
        return this.extensionPackager;
    }

    public void setup() throws Exception
    {
        setup(null);
    }

    public void setup(Environment environment) throws Exception
    {
        // Make sure to have an environment
        if (environment == null) {
            environment = new TestEnvironment();
        }

        // Initialize directories
        initializeDirectories(environment);

        // copy

        copyResourceFolder(getLocalRepository(), "repository.local");
        copyResourceFolder(getMavenRepository(), "repository.maven");
        copyResourceFolder(getMaven2Repository(), "repository.maven2");
        copyResourceFolder(getMavenUnknownRepository(), "repository.mavenunknown");

        // generated extensions

        this.extensionPackager.generateExtensions();
    }

    public int copyResourceFolder(File targetFolder, String resourcePackage) throws IOException
    {
        int nb = 0;

        Collection<URL> urls = ClasspathHelper.forPackage(resourcePackage);

        if (!urls.isEmpty()) {
            String prefix = resourcePackage;
            if (!prefix.endsWith(".")) {
                prefix = prefix + '.';
            }

            Reflections reflections = new Reflections(new ConfigurationBuilder().setScanners(Scanners.Resources)
                .setUrls(urls).filterInputsBy(new FilterBuilder().includePackage(prefix)));

            for (String resource : reflections.getResources(Pattern.compile(".*"))) {
                targetFolder.mkdirs();

                File targetFile = new File(targetFolder, resource.substring(prefix.length()));

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
