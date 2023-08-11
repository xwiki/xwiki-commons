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

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.xwiki.jakartabridge.internal.AbstractJavaxToJakartaWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @param <T> the exact type of jakarta.servlet.FilterRegistration
 * @version $Id$
 * @since 42.0.0
 */
public class JavaxToJakartaFilterRegistration<T extends jakarta.servlet.FilterRegistration>
    extends AbstractJavaxToJakartaWrapper<T> implements FilterRegistration
{
    /**
     * @version $Id$
     */
    public static class Dynamic extends JavaxToJakartaFilterRegistration<jakarta.servlet.FilterRegistration.Dynamic>
        implements FilterRegistration.Dynamic
    {
        /**
         * @param jakarta the wrapped version
         */
        public Dynamic(jakarta.servlet.FilterRegistration.Dynamic jakarta)
        {
            super(jakarta);
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
    public JavaxToJakartaFilterRegistration(T jakarta)
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
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
        String... servletNames)
    {
        this.jakarta.addMappingForServletNames(JakartaServletBridge.toJakarta(dispatcherTypes), isMatchAfter,
            servletNames);
    }

    @Override
    public Collection<String> getServletNameMappings()
    {
        return this.jakarta.getServletNameMappings();
    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
        String... urlPatterns)
    {
        this.jakarta.addMappingForUrlPatterns(JakartaServletBridge.toJakarta(dispatcherTypes), isMatchAfter,
            urlPatterns);
    }

    @Override
    public Collection<String> getUrlPatternMappings()
    {
        return this.jakarta.getUrlPatternMappings();
    }
}
