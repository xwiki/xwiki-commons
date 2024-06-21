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
package org.xwiki.jakartabridge.servlet;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.Collectors;

import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxAsyncContext;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxAsyncListener;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxFilter;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxFilterChain;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxFilterConfig;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxFilterRegistration;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxHttpServletRequest;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxHttpServletResponse;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxHttpSession;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxHttpSessionContext;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxPart;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxReaderListener;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxRequestDispatcher;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServlet;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServletConfig;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServletContext;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServletInputStream;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServletOutputStream;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServletRegistration;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServletRequest;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServletResponse;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxSessionCookieConfig;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxWriteListener;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaAsyncContext;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaAsyncListener;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaFilter;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaFilterChain;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaFilterConfig;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaFilterRegistration;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaHttpServletRequest;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaHttpServletResponse;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaHttpSession;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaHttpSessionContext;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaPart;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaReaderListener;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaRequestDispatcher;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServlet;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServletConfig;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServletContext;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServletInputStream;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServletOutputStream;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServletRegistration;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServletRequest;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServletResponse;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaSessionCookieConfig;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaWriteListener;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ReadListener;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.ServletSecurityElement;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.WriteListener;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionContext;
import jakarta.servlet.http.Part;

/**
 * @version $Id$
 * @since 42.0.0
 */
public final class ServletBridge
{
    private ServletBridge()
    {
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletRequest toJavax(ServletRequest jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return jakarta instanceof HttpServletRequest httpjakarta ? toJavax(httpjakarta)
            : new JavaxToJakartaServletRequest<ServletRequest>(jakarta);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.http.HttpServletRequest toJavax(HttpServletRequest jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaHttpServletRequest(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static ServletRequest toJakarta(javax.servlet.ServletRequest javax)
    {
        if (javax == null) {
            return null;
        }

        return javax instanceof javax.servlet.http.HttpServletRequest httpjakarta ? toJakarta(httpjakarta)
            : new JakartaToJavaxServletRequest<javax.servlet.ServletRequest>(javax);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static HttpServletRequest toJakarta(javax.servlet.http.HttpServletRequest javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxHttpServletRequest(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletResponse toJavax(ServletResponse jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return jakarta instanceof HttpServletResponse httpjakarta ? toJavax(httpjakarta)
            : new JavaxToJakartaServletResponse<jakarta.servlet.ServletResponse>(jakarta);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.http.HttpServletResponse toJavax(HttpServletResponse jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaHttpServletResponse(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static ServletResponse toJakarta(javax.servlet.ServletResponse javax)
    {
        if (javax == null) {
            return null;
        }

        return javax instanceof javax.servlet.http.HttpServletResponse httpjakarta ? toJakarta(httpjakarta)
            : new JakartaToJavaxServletResponse<javax.servlet.ServletResponse>(javax);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static HttpServletResponse toJakarta(javax.servlet.http.HttpServletResponse javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxHttpServletResponse(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.RequestDispatcher toJavax(RequestDispatcher jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaRequestDispatcher(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static RequestDispatcher toJakarta(javax.servlet.RequestDispatcher javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxRequestDispatcher(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletContext toJavax(ServletContext jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaServletContext(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static ServletContext toJakarta(javax.servlet.ServletContext javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxServletContext(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.Servlet toJavax(Servlet jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaServlet(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static Servlet toJakarta(javax.servlet.Servlet javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxServlet(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.http.HttpSession toJavax(HttpSession jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaHttpSession(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static HttpSession toJakarta(javax.servlet.http.HttpSession javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxHttpSession(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.http.Part toJavax(Part jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaPart(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static Part toJakarta(javax.servlet.http.Part javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxPart(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.http.Cookie toJavax(Cookie jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new javax.servlet.http.Cookie(jakarta.getName(), jakarta.getValue());
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static Cookie toJakarta(javax.servlet.http.Cookie javax)
    {
        if (javax == null) {
            return null;
        }

        return new Cookie(javax.getName(), javax.getValue());
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ReadListener toJavax(ReadListener jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaReaderListener(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static ReadListener toJakarta(javax.servlet.ReadListener javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxReaderListener(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.WriteListener toJavax(WriteListener jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaWriteListener(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static WriteListener toJakarta(javax.servlet.WriteListener javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxWriteListener(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.DispatcherType toJavax(DispatcherType jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return javax.servlet.DispatcherType.valueOf(jakarta.name());
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static DispatcherType toJakarta(javax.servlet.DispatcherType javax)
    {
        if (javax == null) {
            return null;
        }

        return DispatcherType.valueOf(javax.name());
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletOutputStream toJavax(ServletOutputStream jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaServletOutputStream(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static ServletOutputStream toJakarta(javax.servlet.ServletOutputStream javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxServletOutputStream(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.http.HttpSessionContext toJavax(HttpSessionContext jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaHttpSessionContext(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static HttpSessionContext toJakarta(javax.servlet.http.HttpSessionContext javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxHttpSessionContext(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletConfig toJavax(ServletConfig jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaServletConfig(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static ServletConfig toJakarta(javax.servlet.ServletConfig javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxServletConfig(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.AsyncContext toJavax(AsyncContext jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaAsyncContext(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static AsyncContext toJakarta(javax.servlet.AsyncContext javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxAsyncContext(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.descriptor.JspConfigDescriptor toJavax(JspConfigDescriptor jakarta)
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static JspConfigDescriptor toJakarta(javax.servlet.descriptor.JspConfigDescriptor javax)
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.SessionTrackingMode toJavax(SessionTrackingMode jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return javax.servlet.SessionTrackingMode.valueOf(jakarta.name());
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static SessionTrackingMode toJakarta(javax.servlet.SessionTrackingMode javax)
    {
        if (javax == null) {
            return null;
        }

        return SessionTrackingMode.valueOf(javax.name());
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static Set<javax.servlet.SessionTrackingMode> toJavax(Set<SessionTrackingMode> jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return jakarta.stream().map(ServletBridge::toJavax).collect(Collectors.toSet());
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static Set<SessionTrackingMode> toJakarta(Set<javax.servlet.SessionTrackingMode> javax)
    {
        if (javax == null) {
            return null;
        }

        return javax.stream().map(ServletBridge::toJakarta).collect(Collectors.toSet());
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.SessionCookieConfig toJavax(SessionCookieConfig jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaSessionCookieConfig(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static SessionCookieConfig toJakarta(javax.servlet.SessionCookieConfig javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxSessionCookieConfig(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.FilterRegistration toJavax(FilterRegistration jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaFilterRegistration(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static FilterRegistration toJakarta(javax.servlet.FilterRegistration javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxFilterRegistration(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.FilterRegistration.Dynamic toJavax(FilterRegistration.Dynamic jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaFilterRegistration.Dynamic(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static jakarta.servlet.FilterRegistration.Dynamic toJakarta(javax.servlet.FilterRegistration.Dynamic javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxFilterRegistration.Dynamic(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletRegistration toJavax(ServletRegistration jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaServletRegistration(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static ServletRegistration toJakarta(javax.servlet.ServletRegistration javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxServletRegistration(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletRegistration.Dynamic toJavax(ServletRegistration.Dynamic jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaServletRegistration.Dynamic(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static ServletRegistration.Dynamic toJakarta(javax.servlet.ServletRegistration.Dynamic javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxServletRegistration.Dynamic(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.Filter toJavax(Filter jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaFilter(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static Filter toJakarta(javax.servlet.Filter javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxFilter(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.AsyncListener toJavax(AsyncListener jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaAsyncListener(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static AsyncListener toJakarta(javax.servlet.AsyncListener javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxAsyncListener(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletInputStream toJavax(ServletInputStream jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaServletInputStream(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static ServletInputStream toJakarta(javax.servlet.ServletInputStream javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxServletInputStream(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.AsyncEvent toJavax(AsyncEvent jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new javax.servlet.AsyncEvent(toJavax(jakarta.getAsyncContext()), toJavax(jakarta.getSuppliedRequest()),
            toJavax(jakarta.getSuppliedResponse()), jakarta.getThrowable());
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static AsyncEvent toJakarta(javax.servlet.AsyncEvent javax)
    {
        if (javax == null) {
            return null;
        }

        return new AsyncEvent(toJakarta(javax.getAsyncContext()), toJakarta(javax.getSuppliedRequest()),
            toJakarta(javax.getSuppliedResponse()), javax.getThrowable());
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.FilterChain toJavax(FilterChain jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaFilterChain(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static FilterChain toJakarta(javax.servlet.FilterChain javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxFilterChain(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.FilterConfig toJavax(FilterConfig jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new JavaxToJakartaFilterConfig(jakarta);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static FilterConfig toJakarta(javax.servlet.FilterConfig javax)
    {
        if (javax == null) {
            return null;
        }

        return new JakartaToJavaxFilterConfig(javax);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.MultipartConfigElement toJavax(MultipartConfigElement jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return new javax.servlet.MultipartConfigElement(jakarta.getLocation(), jakarta.getMaxFileSize(),
            jakarta.getMaxRequestSize(), jakarta.getFileSizeThreshold());
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static MultipartConfigElement toJakarta(javax.servlet.MultipartConfigElement javax)
    {
        if (javax == null) {
            return null;
        }

        return new MultipartConfigElement(javax.getLocation(), javax.getMaxFileSize(), javax.getMaxRequestSize(),
            javax.getFileSizeThreshold());
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletSecurityElement toJavax(ServletSecurityElement jakarta)
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static ServletSecurityElement toJakarta(javax.servlet.ServletSecurityElement javax)
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static Enumeration<javax.servlet.Servlet> toJavax(Enumeration<Servlet> jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return Collections.enumeration(Collections.list(jakarta).stream().map(ServletBridge::toJavax).toList());
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static Enumeration<Servlet> toJakarta(Enumeration<javax.servlet.Servlet> javax)
    {
        if (javax == null) {
            return null;
        }

        return Collections.enumeration(Collections.list(javax).stream().map(ServletBridge::toJakarta).toList());
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.http.Cookie[] toJavax(Cookie[] jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        javax.servlet.http.Cookie[] javax = new javax.servlet.http.Cookie[jakarta.length];

        for (int i = 0; i < jakarta.length; ++i) {
            javax[i] = toJavax(jakarta[i]);
        }

        return javax;
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static Cookie[] toJakarta(javax.servlet.http.Cookie[] javax)
    {
        if (javax == null) {
            return null;
        }

        Cookie[] jakarta = new Cookie[javax.length];

        for (int i = 0; i < javax.length; ++i) {
            jakarta[i] = toJakarta(javax[i]);
        }

        return jakarta;
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static EnumSet<javax.servlet.DispatcherType> toJavax(EnumSet<DispatcherType> jakarta)
    {
        if (jakarta == null) {
            return null;
        }

        return EnumSet.copyOf(jakarta.stream().map(ServletBridge::toJavax).collect(Collectors.toList()));
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static EnumSet<DispatcherType> toJakarta(EnumSet<javax.servlet.DispatcherType> javax)
    {
        if (javax == null) {
            return null;
        }

        return EnumSet.copyOf(javax.stream().map(ServletBridge::toJakarta).collect(Collectors.toList()));
    }
}
