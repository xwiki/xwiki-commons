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
package org.xwiki.jakartabridge.servlet.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;

import org.xwiki.jakartabridge.internal.AbstractJavaxToJakartaWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @param <T> the exact type of jakarta.servlet.ServletRegistration
 * @version $Id$
 * @since 42.0.0
 */
public class JavaxToJakartaServletRegistration<T extends jakarta.servlet.ServletRegistration>
    extends AbstractJavaxToJakartaWrapper<T> implements ServletRegistration
{
    /**
     * @version $Id$
     */
    public static class Dynamic extends JavaxToJakartaServletRegistration<jakarta.servlet.ServletRegistration.Dynamic>
        implements ServletRegistration.Dynamic
    {
        /**
         * @param jakarta
         */
        public Dynamic(jakarta.servlet.ServletRegistration.Dynamic jakarta)
        {
            super(jakarta);
        }

        @Override
        public void setLoadOnStartup(int loadOnStartup)
        {
            this.jakarta.setLoadOnStartup(loadOnStartup);
        }

        @Override
        public Set<String> setServletSecurity(ServletSecurityElement constraint)
        {
            return this.jakarta.setServletSecurity(JakartaServletBridge.toJakarta(constraint));
        }

        @Override
        public void setMultipartConfig(MultipartConfigElement multipartConfig)
        {
            this.jakarta.setMultipartConfig(JakartaServletBridge.toJakarta(multipartConfig));
        }

        @Override
        public void setRunAsRole(String roleName)
        {
            this.jakarta.setRunAsRole(roleName);
        }

        @Override
        public void setAsyncSupported(boolean isAsyncSupported)
        {
            this.jakarta.setAsyncSupported(isAsyncSupported);
        }
    }

    /**
     * @param jakarta the wrapped version
     */
    public JavaxToJakartaServletRegistration(T jakarta)
    {
        super(jakarta);
    }

    @Override
    public String getName()
    {
        return this.jakarta.getName();
    }

    @Override
    public String getClassName()
    {
        return this.jakarta.getClassName();
    }

    @Override
    public boolean setInitParameter(String name, String value)
    {
        return this.jakarta.setInitParameter(name, value);
    }

    @Override
    public String getInitParameter(String name)
    {
        return this.jakarta.getInitParameter(name);
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters)
    {
        return this.jakarta.setInitParameters(initParameters);
    }

    @Override
    public Map<String, String> getInitParameters()
    {
        return this.jakarta.getInitParameters();
    }

    @Override
    public Set<String> addMapping(String... urlPatterns)
    {
        return this.jakarta.addMapping(urlPatterns);
    }

    @Override
    public Collection<String> getMappings()
    {
        return this.jakarta.getMappings();
    }

    @Override
    public String getRunAsRole()
    {
        return this.jakarta.getRunAsRole();
    }
}
