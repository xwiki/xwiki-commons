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
package org.xwiki.tool.xar;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.artifact.resolver.filter.TypeArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.components.io.fileselectors.FileSelector;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.xwiki.tool.xar.internal.XWikiDocument;

/**
 * Base class for {@code xar} and {@code unxar} mojos.
 * 
 * @version $Id$
 */
abstract class AbstractXARMojo extends AbstractMojo
{
    /**
     * The name of the XAR descriptor file.
     */
    protected static final String PACKAGE_XML = "package.xml";

    /**
     * The name of the tag that marks the list of files in {@code link #PACKAGE_XML}.
     */
    protected static final String FILES_TAG = "files";

    /**
     * The name of the tag that marks a specific file in {@code link #PACKAGE_XML}.
     */
    protected static final String FILE_TAG = "file";

    /**
     * Default encoding to use.
     */
    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * Default excludes.
     * 
     * @todo For now we exclude all files in META-INF even though we would like to keep them. This is because we want
     *       that newly generated XAR be compatible with older versions of XWiki (as otherwise they wouldn't be able to
     *       be imported in those older versions as the Package plugin would fail.
     */
    private static final String[] DEFAULT_EXCLUDES = new String[] {"**/META-INF/**"};

    /**
     * Default includes; only include XML files since XWiki pages are stored in this format and we don't want to include
     * other files.
     */
    private static final String[] DEFAULT_INCLUDES = new String[] {"**/*.xml"};

    /**
     * List of files to include. Specified as fileset patterns.
     */
    @Parameter(property = "includes")
    protected String[] includes;

    /**
     * List of files to exclude. Specified as fileset patterns.
     */
    @Parameter(property = "excludes")
    protected String[] excludes;

    /**
     * The maven project.
     */
    @Parameter(property = "project", required = true, readonly = true)
    protected MavenProject project;

    /**
     * The encoding to use when generating the package summary file and when storing file names.
     */
    @Parameter(property = "project.build.sourceEncoding")
    protected String encoding = DEFAULT_ENCODING;

    /**
     * List of Remote Repositories used by the resolver.
     */
    @Parameter(property = "project.remoteArtifactRepositories", readonly = true, required = true)
    protected List<ArtifactRepository> remoteRepos;

    /**
     * Project builder -- builds a model from a pom.xml.
     */
    @Component
    protected MavenProjectBuilder mavenProjectBuilder;

    /**
     * Used to look up Artifacts in the remote repository.
     */
    @Component
    protected ArtifactFactory factory;

    /**
     * Used to look up Artifacts in the remote repository.
     */
    @Component
    protected ArtifactResolver resolver;

    /**
     * The target directory where to extract XAR pages.
     */
    @Parameter(property = "project.build.outputDirectory", readonly = true, required = true)
    private File outputBuildDirectory;

    /**
     * Location of the local repository.
     */
    @Parameter(property = "localRepository", readonly = true, required = true)
    private ArtifactRepository local;

    /**
     * @since 10.3
     */
    @Parameter(property = "entries", readonly = true, required = false)
    private List<XAREntry> entries;

    private Map<String, XAREntry> entryMap;

    /**
     * @return the includes
     */
    protected String[] getIncludes()
    {
        if (this.includes != null && this.includes.length > 0) {
            return this.includes;
        }

        return DEFAULT_INCLUDES;
    }

    /**
     * @return the excludes
     */
    protected String[] getExcludes()
    {
        if (this.excludes != null && this.excludes.length > 0) {
            return this.excludes;
        }

        return DEFAULT_EXCLUDES;
    }

    /**
     * @return the map containing all the XAR entries
     */
    protected Map<String, XAREntry> getEntryMap()
    {
        if (this.entryMap == null) {
            this.entryMap = new HashMap<>();

            for (XAREntry entry : this.entries) {
                this.entryMap.put(entry.getDocument(), entry);
            }
        }

        return this.entryMap;
    }

    /**
     * Unpacks the XAR file (exclude the package.xml file if it exists).
     * 
     * @param file the file to be unpacked.
     * @param location the location where to put the unpacket files.
     * @param logName the name use with {@link ConsoleLogger}.
     * @param overwrite indicate if extracted files has to overwrite existing ones.
     * @throws MojoExecutionException error when unpacking the file.
     */
    protected void unpack(File file, File location, String logName, boolean overwrite, String[] includes,
        String[] excludes) throws MojoExecutionException
    {
        try {
            ZipUnArchiver unArchiver = new ZipUnArchiver();
            unArchiver.setEncoding(this.encoding);
            unArchiver.enableLogging(new ConsoleLogger(Logger.LEVEL_ERROR, logName));
            unArchiver.setSourceFile(file);
            unArchiver.setDestDirectory(location);

            FileSelector[] selectors;

            IncludeExcludeFileSelector fs = new IncludeExcludeFileSelector();
            fs.setIncludes(includes);
            fs.setExcludes(excludes);

            // Ensure that we don't overwrite XML document files present in this project since
            // we want those to be used and not the ones in the dependent XAR.
            unArchiver.setOverwrite(overwrite);

            if (!overwrite) {
                // Do not unpack any package.xml file in dependent XARs. We'll generate a complete
                // one automatically.
                IncludeExcludeFileSelector fs2 = new IncludeExcludeFileSelector();
                fs2.setExcludes(new String[] {PACKAGE_XML});
                selectors = new FileSelector[] {fs, fs2};
            } else {
                selectors = new FileSelector[] {fs};
            }

            unArchiver.setFileSelectors(selectors);

            unArchiver.extract();
        } catch (Exception e) {
            throw new MojoExecutionException(String.format("Error unpacking file [%s] to [%s]", file, location), e);
        }
    }

    /**
     * Unpacks A XAR artifacts into the build output directory, along with the project's XAR files.
     *
     * @param artifact the XAR artifact to unpack.
     * @throws MojoExecutionException in case of unpack error
     */
    protected void unpackXARToOutputDirectory(Artifact artifact, String[] includes, String[] excludes)
        throws MojoExecutionException
    {
        if (!this.outputBuildDirectory.exists()) {
            this.outputBuildDirectory.mkdirs();
        }

        File file = artifact.getFile();
        unpack(file, this.outputBuildDirectory, "XAR Plugin", false, includes, excludes);
    }


    /**
     * @return Returns the project.
     */
    public MavenProject getProject()
    {
        return this.project;
    }

    /**
     * This method resolves all transitive dependencies of an artifact.
     * 
     * @param artifact the artifact used to retrieve dependencies
     * @return resolved set of dependencies
     * @throws ArtifactResolutionException error
     * @throws ArtifactNotFoundException error
     * @throws ProjectBuildingException error
     * @throws InvalidDependencyVersionException error
     */
    protected Set<Artifact> resolveArtifactDependencies(Artifact artifact) throws ArtifactResolutionException,
        ArtifactNotFoundException, ProjectBuildingException
    {
        Artifact pomArtifact =
            this.factory.createArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), "",
                "pom");

        MavenProject pomProject =
            this.mavenProjectBuilder.buildFromRepository(pomArtifact, this.remoteRepos, this.local);

        return resolveDependencyArtifacts(pomProject);
    }

    /**
     * @param pomProject the project
     * @return set of dependencies
     * @throws ArtifactResolutionException error
     * @throws ArtifactNotFoundException error
     * @throws InvalidDependencyVersionException error
     */
    protected Set<Artifact> resolveDependencyArtifacts(MavenProject pomProject) throws ArtifactResolutionException,
        ArtifactNotFoundException, InvalidDependencyVersionException
    {
        AndArtifactFilter filters = new AndArtifactFilter();

        filters.add(new TypeArtifactFilter("xar"));
        filters.add(new ScopeArtifactFilter(DefaultArtifact.SCOPE_RUNTIME));

        Set<Artifact> artifacts = pomProject.createArtifacts(this.factory, Artifact.SCOPE_TEST, filters);

        for (Artifact artifact : artifacts) {
            // resolve the new artifact
            this.resolver.resolve(artifact, this.remoteRepos, this.local);
        }

        return artifacts;
    }

    /**
     * Load a XWiki document from its XML representation.
     *
     * @param file the file to parse.
     * @return the loaded document object or null if the document cannot be parsed
     */
    protected XWikiDocument getDocFromXML(File file) throws MojoExecutionException
    {
        XWikiDocument doc;

        try {
            doc = new XWikiDocument();
            doc.fromXML(file);
        } catch (Exception e) {
            throw new MojoExecutionException(String.format("Failed to parse [%s].", file.getAbsolutePath()), e);
        }

        return doc;
    }

    /**
     * @return the Maven Resources directory for the project
     */
    protected File getResourcesDirectory()
    {
        String resourcesLocation =
            (this.project.getBasedir().getAbsolutePath() + "/src/main/resources").replace("/", File.separator);
        return new File(resourcesLocation);
    }
}
