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

import jakarta.servlet.ServletContext;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

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
     * @see #getJakartaServletContext()
     */
    private ServletContext jakartaServletContext;

    /**
     * @see #getServletContext()
     */
    private javax.servlet.ServletContext javaxServletContext;

    /**
     * @param servletContext see {@link #getServletContext()}
     */
    public void setServletContext(javax.servlet.ServletContext servletContext)
    {
        this.javaxServletContext = servletContext;
        this.jakartaServletContext = JakartaServletBridge.toJakarta(servletContext);
    }

    /**
     * @param servletContext see {@link #getServletContext()}
     */
    public void setServletContext(ServletContext servletContext)
    {
        this.jakartaServletContext = servletContext;
    }

    /**
     * @return the legacy Javax Servlet Context
     * @deprecated use {@link #getJakartaServletContext()} instead
     */
    @Deprecated(since = "42.0.0")
    public javax.servlet.ServletContext getServletContext()
    {
        if (this.javaxServletContext == null) {
            this.javaxServletContext = JakartaServletBridge.toJavax(getJakartaServletContext());
        }

        return this.javaxServletContext;
    }

    /**
     * @return the Servlet Context
     * @since 42.0.0
     */
    public ServletContext getJakartaServletContext()
    {
        if (this.jakartaServletContext == null) {
            throw new RuntimeException(
                "The Servlet Environment has not been properly initialized (The Servlet Context is not set)");
        }

        return this.jakartaServletContext;
    }

    @Override
    public InputStream getResourceAsStream(String resourceName)
    {
        return getJakartaServletContext().getResourceAsStream(resourceName);
    }

    @Override
    public URL getResource(String resourceName)
    {
        URL url;
        try {
            url = getJakartaServletContext().getResource(resourceName);

            // ensure to normalize the URI, we don't want relative path.
            if (url != null) {
                url = url.toURI().normalize().toURL();
            }
            // We're catching IllegalArgumentException which might be thrown by Tomcat when trying to resolve path such
            // as
            // `templates/../..`
        } catch (MalformedURLException | URISyntaxException | IllegalArgumentException e) {
            url = null;
            this.logger.warn("Error getting resource [{}] because of invalid path format. Reason: [{}]", resourceName,
                e.getMessage());
        }
        return url;
    }

    @Override
    protected String getTemporaryDirectoryName()
    {
        final String tmpDirectory = super.getTemporaryDirectoryName();
        try {
            if (tmpDirectory == null) {
                File tempDir = (File) getJakartaServletContext().getAttribute(ServletContext.TEMPDIR);
                return tempDir == null ? null : tempDir.getCanonicalPath();
            }
        } catch (IOException e) {
            this.logger.warn("Unable to get Servlet temporary directory due to error [{}], "
                + "falling back on the default System temporary directory.", ExceptionUtils.getMessage(e));
        }
        return tmpDirectory;
    }
}
