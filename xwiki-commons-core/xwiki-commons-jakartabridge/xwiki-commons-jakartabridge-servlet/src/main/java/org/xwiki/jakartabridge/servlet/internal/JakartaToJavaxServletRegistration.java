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

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletSecurityElement;

import org.xwiki.jakartabridge.internal.AbstractJakartaToJavaxWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @param <T> the exact type of jakarta.servlet.ServletRegistration
 * @version $Id$
 * @since 42.0.0
 */
public class JakartaToJavaxServletRegistration<T extends javax.servlet.ServletRegistration>
    extends AbstractJakartaToJavaxWrapper<T> implements ServletRegistration
{
    /**
     * @version $Id$
     */
    public static class Dynamic extends JakartaToJavaxServletRegistration<javax.servlet.ServletRegistration.Dynamic>
        implements ServletRegistration.Dynamic
    {
        /**
         * @param javax
         */
        public Dynamic(javax.servlet.ServletRegistration.Dynamic javax)
        {
            super(javax);
        }

        @Override
        public void setLoadOnStartup(int loadOnStartup)
        {
            this.javax.setLoadOnStartup(loadOnStartup);
        }

        @Override
        public Set<String> setServletSecurity(ServletSecurityElement constraint)
        {
            return this.javax.setServletSecurity(JakartaServletBridge.toJavax(constraint));
        }

        @Override
        public void setMultipartConfig(MultipartConfigElement multipartConfig)
        {
            this.javax.setMultipartConfig(JakartaServletBridge.toJavax(multipartConfig));
        }

        @Override
        public void setRunAsRole(String roleName)
        {
            this.javax.setRunAsRole(roleName);
        }

        @Override
        public void setAsyncSupported(boolean isAsyncSupported)
        {
            this.javax.setAsyncSupported(isAsyncSupported);
        }
    }

    /**
     * @param javax the wrapped version
     */
    public JakartaToJavaxServletRegistration(T javax)
    {
        super(javax);
    }

    @Override
    public String getName()
    {
        return this.javax.getName();
    }

    @Override
    public String getClassName()
    {
        return this.javax.getClassName();
    }

    @Override
    public boolean setInitParameter(String name, String value)
    {
        return this.javax.setInitParameter(name, value);
    }

    @Override
    public String getInitParameter(String name)
    {
        return this.javax.getInitParameter(name);
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters)
    {
        return this.javax.setInitParameters(initParameters);
    }

    @Override
    public Map<String, String> getInitParameters()
    {
        return this.javax.getInitParameters();
    }

    @Override
    public Set<String> addMapping(String... urlPatterns)
    {
        return this.javax.addMapping(urlPatterns);
    }

    @Override
    public Collection<String> getMappings()
    {
        return this.javax.getMappings();
    }

    @Override
    public String getRunAsRole()
    {
        return this.javax.getRunAsRole();
    }
}
