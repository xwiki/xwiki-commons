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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletRegistration.Dynamic;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;

import org.xwiki.jakartabridge.internal.AbstractJakartaToJavaxWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @version $Id$
 * @since 42.0.0
 */
public class JakartaToJavaxServletContext extends AbstractJakartaToJavaxWrapper<javax.servlet.ServletContext>
    implements ServletContext
{
    /**
     * @param javax the wrapped version
     */
    public JakartaToJavaxServletContext(javax.servlet.ServletContext javax)
    {
        super(javax);
    }

    @Override
    public String getContextPath()
    {
        return this.javax.getContextPath();
    }

    @Override
    public ServletContext getContext(String uripath)
    {
        return new JakartaToJavaxServletContext(this.javax.getContext(uripath));
    }

    @Override
    public int getMajorVersion()
    {
        return this.javax.getMajorVersion();
    }

    @Override
    public int getMinorVersion()
    {
        return this.javax.getMinorVersion();
    }

    @Override
    public int getEffectiveMajorVersion()
    {
        return this.javax.getEffectiveMajorVersion();
    }

    @Override
    public int getEffectiveMinorVersion()
    {
        return this.javax.getEffectiveMinorVersion();
    }

    @Override
    public String getMimeType(String file)
    {
        return this.javax.getMimeType(file);
    }

    @Override
    public Set<String> getResourcePaths(String path)
    {
        return this.javax.getResourcePaths(path);
    }

    @Override
    public URL getResource(String path) throws MalformedURLException
    {
        return this.javax.getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path)
    {
        return this.javax.getResourceAsStream(path);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path)
    {
        return JakartaServletBridge.toJakarta(this.javax.getRequestDispatcher(path));
    }

    @Override
    public RequestDispatcher getNamedDispatcher(String name)
    {
        return JakartaServletBridge.toJakarta(this.javax.getNamedDispatcher(name));
    }

    @Override
    public Servlet getServlet(String name) throws ServletException
    {
        try {
            return JakartaServletBridge.toJakarta(this.javax.getServlet(name));
        } catch (javax.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public Enumeration<Servlet> getServlets()
    {
        return JakartaServletBridge.toJakarta(this.javax.getServlets());
    }

    @Override
    public Enumeration<String> getServletNames()
    {
        return this.javax.getServletNames();
    }

    @Override
    public void log(String msg)
    {
        this.javax.log(msg);
    }

    @Override
    public void log(Exception exception, String msg)
    {
        this.javax.log(exception, msg);
    }

    @Override
    public void log(String message, Throwable throwable)
    {
        this.javax.log(message, throwable);
    }

    @Override
    public String getRealPath(String path)
    {
        return this.javax.getRealPath(path);
    }

    @Override
    public String getServerInfo()
    {
        return this.javax.getServerInfo();
    }

    @Override
    public String getInitParameter(String name)
    {
        return this.javax.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames()
    {
        return this.javax.getInitParameterNames();
    }

    @Override
    public boolean setInitParameter(String name, String value)
    {
        return this.javax.setInitParameter(name, value);
    }

    @Override
    public Object getAttribute(String name)
    {
        return this.javax.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames()
    {
        return this.javax.getAttributeNames();
    }

    @Override
    public void setAttribute(String name, Object object)
    {
        this.javax.setAttribute(name, object);
    }

    @Override
    public void removeAttribute(String name)
    {
        this.javax.removeAttribute(name);
    }

    @Override
    public String getServletContextName()
    {
        return this.javax.getServletContextName();
    }

    @Override
    public Dynamic addServlet(String servletName, String className)
    {
        return JakartaServletBridge.toJakarta(this.javax.addServlet(servletName, className));
    }

    @Override
    public Dynamic addServlet(String servletName, Servlet servlet)
    {
        return JakartaServletBridge
            .toJakarta(this.javax.addServlet(servletName, JakartaServletBridge.toJavax(servlet)));
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
        return JakartaServletBridge.toJakarta(this.javax.getServletRegistration(servletName));
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
        return JakartaServletBridge.toJakarta(this.javax.addFilter(filterName, className));
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter)
    {
        return JakartaServletBridge.toJakarta(this.javax.addFilter(filterName, JakartaServletBridge.toJavax(filter)));
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
        return JakartaServletBridge.toJakarta(this.javax.getFilterRegistration(filterName));
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
        return JakartaServletBridge.toJakarta(this.javax.getSessionCookieConfig());
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
    {
        this.javax.setSessionTrackingModes(JakartaServletBridge.toJavax(sessionTrackingModes));
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
    {
        return JakartaServletBridge.toJakarta(this.javax.getDefaultSessionTrackingModes());
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
    {
        return JakartaServletBridge.toJakarta(this.javax.getEffectiveSessionTrackingModes());
    }

    @Override
    public void addListener(String className)
    {
        this.javax.addListener(className);
    }

    @Override
    public <T extends EventListener> void addListener(T t)
    {
        this.javax.addListener(t);
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass)
    {
        this.javax.addListener(listenerClass);
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException
    {
        try {
            return this.javax.createListener(clazz);
        } catch (javax.servlet.ServletException e) {
            throw new ServletException(e.getMessage(), e);
        }
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor()
    {
        return JakartaServletBridge.toJakarta(this.javax.getJspConfigDescriptor());
    }

    @Override
    public ClassLoader getClassLoader()
    {
        return this.javax.getClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames)
    {
        this.javax.declareRoles(roleNames);
    }

    @Override
    public String getVirtualServerName()
    {
        return this.javax.getVirtualServerName();
    }

    @Override
    public Dynamic addJspFile(String servletName, String jspFile)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSessionTimeout()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSessionTimeout(int sessionTimeout)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestCharacterEncoding()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setRequestCharacterEncoding(String encoding)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getResponseCharacterEncoding()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setResponseCharacterEncoding(String encoding)
    {
        throw new UnsupportedOperationException();
    }
}
