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
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;

import org.xwiki.jakartabridge.internal.AbstractJakartaToJavaxWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @param <T> the exact type of jakarta.servlet.FilterRegistration
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JakartaToJavaxFilterRegistration<T extends javax.servlet.FilterRegistration>
    extends AbstractJakartaToJavaxWrapper<T> implements FilterRegistration
{
    /**
     * @version $Id$
     */
    public static class Dynamic extends JakartaToJavaxFilterRegistration<javax.servlet.FilterRegistration.Dynamic>
        implements FilterRegistration.Dynamic
    {
        /**
         * @param javax the wrapped version
         */
        public Dynamic(javax.servlet.FilterRegistration.Dynamic javax)
        {
            super(javax);
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
    public JakartaToJavaxFilterRegistration(T javax)
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
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
        String... servletNames)
    {
        this.javax.addMappingForServletNames(JakartaServletBridge.toJavax(dispatcherTypes), isMatchAfter,
            servletNames);
    }

    @Override
    public Collection<String> getServletNameMappings()
    {
        return this.javax.getServletNameMappings();
    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
        String... urlPatterns)
    {
        this.javax.addMappingForUrlPatterns(JakartaServletBridge.toJavax(dispatcherTypes), isMatchAfter, urlPatterns);
    }

    @Override
    public Collection<String> getUrlPatternMappings()
    {
        return this.javax.getUrlPatternMappings();
    }
}
