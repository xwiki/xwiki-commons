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
package org.xwiki.tool.extension;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.archiver.util.DefaultFileSet;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.xwiki.tool.extension.util.AbstractExtensionMojo;

/**
 * @version $Id$
 * @since 9.4RC1
 */
@Mojo(name = "xip", defaultPhase = LifecyclePhase.GENERATE_RESOURCES,
    requiresDependencyResolution = ResolutionScope.COMPILE, requiresProject = true, threadSafe = true)
public class XIPMojo extends AbstractExtensionMojo
{
    @Override
    public void executeInternal() throws MojoExecutionException
    {
        packageExtensions();
    }

    private void packageExtensions() throws MojoExecutionException
    {
        // Store extensions
        // Use Extension Manager to resolve dependencies instead of Maven since there is some differences
        this.extensionHelper.storeExtensionDependencies(true);

        // Generate package
        createPackage();
    }

    private void createPackage() throws MojoExecutionException
    {
        File xipFile =
            new File(this.project.getBuild().getDirectory(), this.project.getBuild().getFinalName() + ".xip");

        ZipArchiver archiver = new ZipArchiver();
        archiver.setDestFile(xipFile);
        archiver.setIncludeEmptyDirs(false);
        archiver.setCompress(true);

        archiver.addFileSet(
            new DefaultFileSet(new File(this.extensionHelper.getPermanentDirectory(), "extension/repository")));

        try {
            archiver.createArchive();
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to create archive", e);
        }

        this.project.getArtifact().setFile(xipFile);
    }
}
