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

import org.xwiki.jakartabridge.servlet.ServletBridge;

/**
 * @param <T> the exact type of jakarta.servlet.FilterRegistration
 * @version $Id$
 * @since -1.jakarta
 */
public class JavaxToJakartaFilterRegistration<T extends jakarta.servlet.FilterRegistration>
    implements FilterRegistration
{
    protected final T wrapped;

    /**
     * @version $Id$
     */
    public static class Dynamic extends JavaxToJakartaFilterRegistration<jakarta.servlet.FilterRegistration.Dynamic>
        implements FilterRegistration.Dynamic
    {
        /**
         * @param wrapped the wrapped version
         */
        public Dynamic(jakarta.servlet.FilterRegistration.Dynamic wrapped)
        {
            super(wrapped);
        }

        @Override
        public void setAsyncSupported(boolean isAsyncSupported)
        {
            this.wrapped.setAsyncSupported(isAsyncSupported);
        }
    }

    /**
     * @param wrapped the wrapped version
     */
    public JavaxToJakartaFilterRegistration(T wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public String getName()
    {
        return this.wrapped.getName();
    }

    @Override
    public String getClassName()
    {
        return this.wrapped.getClassName();
    }

    @Override
    public boolean setInitParameter(String name, String value)
    {
        return this.wrapped.setInitParameter(name, value);
    }

    @Override
    public String getInitParameter(String name)
    {
        return this.wrapped.getInitParameter(name);
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters)
    {
        return this.wrapped.setInitParameters(initParameters);
    }

    @Override
    public Map<String, String> getInitParameters()
    {
        return this.wrapped.getInitParameters();
    }

    @Override
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
        String... servletNames)
    {
        this.wrapped.addMappingForServletNames(ServletBridge.toJakarta(dispatcherTypes), isMatchAfter, servletNames);
    }

    @Override
    public Collection<String> getServletNameMappings()
    {
        return this.wrapped.getServletNameMappings();
    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
        String... urlPatterns)
    {
        this.wrapped.addMappingForUrlPatterns(ServletBridge.toJakarta(dispatcherTypes), isMatchAfter, urlPatterns);
    }

    @Override
    public Collection<String> getUrlPatternMappings()
    {
        return this.wrapped.getUrlPatternMappings();
    }
}
