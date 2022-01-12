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
import java.net.URISyntaxException;
import java.net.URL;

import javax.inject.Singleton;
import javax.servlet.ServletContext;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.xwiki.component.annotation.Component;

/**
 * Defines what an Environment means in a Servlet environment.
 *
 * @version $Id$
 * @since 3.5M1
 */
@Component
@Singleton
public class ServletEnvironment extends AbstractEnvironment
{
    /**
     * @see #getServletContext()
     */
    private ServletContext servletContext;

    /**
     * @param servletContext see {@link #getServletContext()}
     */
    public void setServletContext(ServletContext servletContext)
    {
        this.servletContext = servletContext;
    }

    /**
     * @return the Servlet Context
     */
    public ServletContext getServletContext()
    {
        if (this.servletContext == null) {
            throw new RuntimeException("The Servlet Environment has not been properly initialized "
                + "(The Servlet Context is not set)");
        }
        return this.servletContext;
    }

    @Override
    public InputStream getResourceAsStream(String resourceName)
    {
        return getServletContext().getResourceAsStream(resourceName);
    }

    @Override
    public URL getResource(String resourceName)
    {
        URL url;
        try {
            url = getServletContext().getResource(resourceName);

            // ensure to normalize the URI, we don't want relative path.
            if (url != null) {
                url = url.toURI().normalize().toURL();
            }
        } catch (MalformedURLException | URISyntaxException e) {
            url = null;
            this.logger.warn("Error getting resource [{}] because of invalid path format. Reason: [{}]",
                resourceName, e.getMessage());
        }
        return url;
    }

    @Override
    protected String getTemporaryDirectoryName()
    {
        final String tmpDirectory = super.getTemporaryDirectoryName();
        try {
            if (tmpDirectory == null) {
                File tempDir = (File) this.getServletContext().getAttribute(ServletContext.TEMPDIR);
                return tempDir == null ? null : tempDir.getCanonicalPath();
            }
        } catch (IOException e) {
            this.logger.warn("Unable to get Servlet temporary directory due to error [{}], "
                + "falling back on the default System temporary directory.", ExceptionUtils.getMessage(e));
        }
        return tmpDirectory;
    }
}
