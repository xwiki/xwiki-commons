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
package org.xwiki.environment.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

/**
 * Defines what an Environment means in Java SE.
 *
 * @version $Id$
 * @since 3.5M1
 */
@Component
@Singleton
public class StandardEnvironment extends AbstractEnvironment
{
    /**
     * @see #setResourceDirectory(java.io.File)
     */
    private File resourceDirectory;

    /**
     * @see #setResourceClassLoader(ClassLoader)
     */
    private ClassLoader resourceClassLoader;

    /**
     * @param resourceDirectory the directory where resources such as configuration files (actually any content that
     *                          is neither temporary nor permanent data) which will point to the permanent directory
     *                          if not set
     */
    public void setResourceDirectory(File resourceDirectory)
    {
        this.resourceDirectory = resourceDirectory;
    }

    /**
     * @param classLoader The Class Loader used to load resources when they're not found in the Resources directory
     *        (or if it's not set). Defaults to the Class Loader used to load this class if not set.
     */
    public void setResourceClassLoader(ClassLoader classLoader)
    {
        this.resourceClassLoader = classLoader;
    }

    @Override
    public InputStream getResourceAsStream(String resourceName)
    {
        InputStream resourceStream = null;

        URL resourceURL = getResource(resourceName);
        if (resourceURL != null) {
            try {
                resourceStream = resourceURL.openStream();
            } catch (IOException e) {
                this.logger.debug("Failed to get Input stream for resource [{}]", resourceName, e);
            }
        }

        return resourceStream;
    }

    @Override
    public URL getResource(String resourceName)
    {
        URL resourceURL = null;

        // Try to find the resource in the Resources directory (if set)
        if (this.resourceDirectory != null) {
            try {
                // Verify if the File exists
                File resourceFile = new File(this.resourceDirectory, resourceName);
                if (resourceFile.exists()) {
                    resourceURL = resourceFile.toURI().toURL();
                }
            } catch (MalformedURLException e) {
                this.logger.debug("Failed to access resource [{}]", resourceName, e);
            }
        }

        // If not found, try in the Resource Class Loader
        if (resourceURL == null) {
            resourceURL = (this.resourceClassLoader == null ? Thread.currentThread().getContextClassLoader()
                : this.resourceClassLoader).getResource(resourceName);
        }

        return resourceURL;
    }
}
