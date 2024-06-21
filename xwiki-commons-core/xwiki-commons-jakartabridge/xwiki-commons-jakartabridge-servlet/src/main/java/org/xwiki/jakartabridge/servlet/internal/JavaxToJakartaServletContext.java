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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

import org.xwiki.jakartabridge.servlet.ServletBridge;

/**
 * @version $Id$
 * @since 42.0.0
 */
public class JavaxToJakartaServletContext implements ServletContext
{
    private final jakarta.servlet.ServletContext wrapped;

    /**
     * @param wrapped the wrapped version
     */
    public JavaxToJakartaServletContext(jakarta.servlet.ServletContext wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public String getContextPath()
    {
        return this.wrapped.getContextPath();
    }

    @Override
    public ServletContext getContext(String uripath)
    {
        return ServletBridge.toJavax(this.wrapped.getContext(uripath));
    }

    @Override
    public int getMajorVersion()
    {
        return this.wrapped.getMajorVersion();
    }

    @Override
    public int getMinorVersion()
    {
        return this.wrapped.getMinorVersion();
    }

    @Override
    public int getEffectiveMajorVersion()
    {
        return this.wrapped.getEffectiveMajorVersion();
    }

    @Override
    public int getEffectiveMinorVersion()
    {
        return this.wrapped.getEffectiveMinorVersion();
    }

    @Override
    public String getMimeType(String file)
    {
        return this.wrapped.getMimeType(file);
    }

    @Override
    public Set<String> getResourcePaths(String path)
    {
        return this.wrapped.getResourcePaths(path);
    }

    @Override
    public URL getResource(String path) throws MalformedURLException
    {
        return this.wrapped.getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path)
    {
        return this.wrapped.getResourceAsStream(path);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path)
    {
        return ServletBridge.toJavax(this.wrapped.getRequestDispatcher(path));
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name)
    {
        return new JavaxToJakartaRequestDispatcher(this.wrapped.getNamedDispatcher(name));
    }

    @Override
    public Servlet getServlet(String name) throws ServletException
    {
        try {
            return ServletBridge.toJavax(this.wrapped.getServlet(name));
        } catch (jakarta.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public Enumeration<Servlet> getServlets()
    {
        return ServletBridge.toJavax(this.wrapped.getServlets());
    }

    @Override
    public Enumeration<String> getServletNames()
    {
        return this.wrapped.getServletNames();
    }

    @Override
    public void log(String msg)
    {
        this.wrapped.log(msg);
    }

    @Override
    public void log(Exception exception, String msg)
    {
        // Since deprecated APIs where removed in Servlet 6, it's safer to use alternatives
        this.wrapped.log(msg, exception);
    }

    @Override
    public void log(String message, Throwable throwable)
    {
        this.wrapped.log(message, throwable);
    }

    @Override
    public String getRealPath(String path)
    {
        return this.wrapped.getRealPath(path);
    }

    @Override
    public String getServerInfo()
    {
        return this.wrapped.getServerInfo();
    }

    @Override
    public String getInitParameter(String name)
    {
        return this.wrapped.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames()
    {
        return this.wrapped.getInitParameterNames();
    }

    @Override
    public boolean setInitParameter(String name, String value)
    {
        return this.wrapped.setInitParameter(name, value);
    }

    @Override
    public Object getAttribute(String name)
    {
        return this.wrapped.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames()
    {
        return this.wrapped.getAttributeNames();
    }

    @Override
    public void setAttribute(String name, Object object)
    {
        this.wrapped.setAttribute(name, object);
    }

    @Override
    public void removeAttribute(String name)
    {
        this.wrapped.removeAttribute(name);
    }

    @Override
    public String getServletContextName()
    {
        return this.wrapped.getServletContextName();
    }

    @Override
    public Dynamic addServlet(String servletName, String className)
    {
        return ServletBridge.toJavax(this.wrapped.addServlet(servletName, className));
    }

    @Override
    public Dynamic addServlet(String servletName, Servlet servlet)
    {
        return ServletBridge.toJavax(this.wrapped.addServlet(servletName, ServletBridge.toJakarta(servlet)));
    }

    @Override
    public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass)
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName)
    {
        return ServletBridge.toJavax(this.wrapped.getServletRegistration(servletName));
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations()
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className)
    {
        return ServletBridge.toJavax(this.wrapped.addFilter(filterName, className));
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter)
    {
        return ServletBridge.toJavax(this.wrapped.addFilter(filterName, ServletBridge.toJakarta(filter)));
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass)
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName)
    {
        return ServletBridge.toJavax(this.wrapped.getFilterRegistration(filterName));
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations()
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig()
    {
        return ServletBridge.toJavax(this.wrapped.getSessionCookieConfig());
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
    {
        this.wrapped.setSessionTrackingModes(ServletBridge.toJakarta(sessionTrackingModes));
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
    {
        return ServletBridge.toJavax(this.wrapped.getDefaultSessionTrackingModes());
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
    {
        return ServletBridge.toJavax(this.wrapped.getEffectiveSessionTrackingModes());
    }

    @Override
    public void addListener(String className)
    {
        this.wrapped.addListener(className);
    }

    @Override
    public <T extends EventListener> void addListener(T t)
    {
        this.wrapped.addListener(t);
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass)
    {
        this.wrapped.addListener(listenerClass);
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException
    {
        try {
            return this.wrapped.createListener(clazz);
        } catch (jakarta.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor()
    {
        return ServletBridge.toJavax(this.wrapped.getJspConfigDescriptor());
    }

    @Override
    public ClassLoader getClassLoader()
    {
        return this.wrapped.getClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames)
    {
        this.wrapped.declareRoles(roleNames);
    }

    @Override
    public String getVirtualServerName()
    {
        return this.wrapped.getVirtualServerName();
    }
}
