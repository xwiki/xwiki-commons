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
import org.apache.maven.plugins.annotations.Parameter;
import org.xwiki.tool.extension.util.AbstractExtensionMojo;

/**
 * Generate complete extension descriptor for current artifact.
 *
 * @version $Id$
 * @since 8.4
 */
@Mojo(name = "register", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresProject = true, threadSafe = true)
public class RegisterMojo extends AbstractExtensionMojo
{
    /**
     * The directory where the war is generated.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private File outputDirectory;

    @Override
    public void executeInternal() throws MojoExecutionException
    {
        // Register the WAR
        register();
    }

    private void register() throws MojoExecutionException
    {
        // Make sure "/META-INF/" exists
        File directory = new File(this.outputDirectory, "META-INF");
        directory.mkdirs();

        // Write descriptor
        try {
            this.extensionHelper.serializeExtension(new File(directory, "extension.xed"), this.project.getModel());
        } catch (Exception e) {
            throw new MojoExecutionException("Failed to write WAR descriptor", e);
        }
    }
}
