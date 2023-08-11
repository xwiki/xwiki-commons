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

import org.xwiki.jakartabridge.internal.AbstractJavaxToJakartaWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @version $Id$
 * @since 42.0.0
 */
public class JavaxToJakartaServletContext extends AbstractJavaxToJakartaWrapper<jakarta.servlet.ServletContext>
    implements ServletContext
{
    /**
     * @param jakarta the wrapped version
     */
    public JavaxToJakartaServletContext(jakarta.servlet.ServletContext jakarta)
    {
        super(jakarta);
    }

    @Override
    public String getContextPath()
    {
        return this.jakarta.getContextPath();
    }

    @Override
    public ServletContext getContext(String uripath)
    {
        return JakartaServletBridge.toJavax(this.jakarta.getContext(uripath));
    }

    @Override
    public int getMajorVersion()
    {
        return this.jakarta.getMajorVersion();
    }

    @Override
    public int getMinorVersion()
    {
        return this.jakarta.getMinorVersion();
    }

    @Override
    public int getEffectiveMajorVersion()
    {
        return this.jakarta.getEffectiveMajorVersion();
    }

    @Override
    public int getEffectiveMinorVersion()
    {
        return this.jakarta.getEffectiveMinorVersion();
    }

    @Override
    public String getMimeType(String file)
    {
        return this.jakarta.getMimeType(file);
    }

    @Override
    public Set<String> getResourcePaths(String path)
    {
        return this.jakarta.getResourcePaths(path);
    }

    @Override
    public URL getResource(String path) throws MalformedURLException
    {
        return this.jakarta.getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path)
    {
        return this.jakarta.getResourceAsStream(path);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path)
    {
        return JakartaServletBridge.toJavax(this.jakarta.getRequestDispatcher(path));
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name)
    {
        return new JavaxToJakartaRequestDispatcher(this.jakarta.getNamedDispatcher(name));
    }

    @Override
    public Servlet getServlet(String name) throws ServletException
    {
        try {
            return JakartaServletBridge.toJavax(this.jakarta.getServlet(name));
        } catch (jakarta.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public Enumeration<Servlet> getServlets()
    {
        return JakartaServletBridge.toJavax(this.jakarta.getServlets());
    }

    @Override
    public Enumeration<String> getServletNames()
    {
        return this.jakarta.getServletNames();
    }

    @Override
    public void log(String msg)
    {
        this.jakarta.log(msg);
    }

    @Override
    public void log(Exception exception, String msg)
    {
        // Since deprecated APIs where removed in Servlet 6, it's safer to use alternatives
        this.jakarta.log(msg, exception);
    }

    @Override
    public void log(String message, Throwable throwable)
    {
        this.jakarta.log(message, throwable);
    }

    @Override
    public String getRealPath(String path)
    {
        return this.jakarta.getRealPath(path);
    }

    @Override
    public String getServerInfo()
    {
        return this.jakarta.getServerInfo();
    }

    @Override
    public String getInitParameter(String name)
    {
        return this.jakarta.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames()
    {
        return this.jakarta.getInitParameterNames();
    }

    @Override
    public boolean setInitParameter(String name, String value)
    {
        return this.jakarta.setInitParameter(name, value);
    }

    @Override
    public Object getAttribute(String name)
    {
        return this.jakarta.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames()
    {
        return this.jakarta.getAttributeNames();
    }

    @Override
    public void setAttribute(String name, Object object)
    {
        this.jakarta.setAttribute(name, object);
    }

    @Override
    public void removeAttribute(String name)
    {
        this.jakarta.removeAttribute(name);
    }

    @Override
    public String getServletContextName()
    {
        return this.jakarta.getServletContextName();
    }

    @Override
    public Dynamic addServlet(String servletName, String className)
    {
        return JakartaServletBridge.toJavax(this.jakarta.addServlet(servletName, className));
    }

    @Override
    public Dynamic addServlet(String servletName, Servlet servlet)
    {
        return JakartaServletBridge.toJavax(this.jakarta.addServlet(servletName, JakartaServletBridge.toJakarta(servlet)));
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
        return JakartaServletBridge.toJavax(this.jakarta.getServletRegistration(servletName));
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
        return JakartaServletBridge.toJavax(this.jakarta.addFilter(filterName, className));
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter)
    {
        return JakartaServletBridge.toJavax(this.jakarta.addFilter(filterName, JakartaServletBridge.toJakarta(filter)));
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
        return JakartaServletBridge.toJavax(this.jakarta.getFilterRegistration(filterName));
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
        return JakartaServletBridge.toJavax(this.jakarta.getSessionCookieConfig());
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
    {
        this.jakarta.setSessionTrackingModes(JakartaServletBridge.toJakarta(sessionTrackingModes));
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
    {
        return JakartaServletBridge.toJavax(this.jakarta.getDefaultSessionTrackingModes());
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
    {
        return JakartaServletBridge.toJavax(this.jakarta.getEffectiveSessionTrackingModes());
    }

    @Override
    public void addListener(String className)
    {
        this.jakarta.addListener(className);
    }

    @Override
    public <T extends EventListener> void addListener(T t)
    {
        this.jakarta.addListener(t);
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass)
    {
        this.jakarta.addListener(listenerClass);
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException
    {
        try {
            return this.jakarta.createListener(clazz);
        } catch (jakarta.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor()
    {
        return JakartaServletBridge.toJavax(this.jakarta.getJspConfigDescriptor());
    }

    @Override
    public ClassLoader getClassLoader()
    {
        return this.jakarta.getClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames)
    {
        this.jakarta.declareRoles(roleNames);
    }

    @Override
    public String getVirtualServerName()
    {
        return this.jakarta.getVirtualServerName();
    }
}
