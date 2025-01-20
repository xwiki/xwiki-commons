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

import java.util.Collection;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.Set;
import java.util.stream.Collectors;

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
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.ServletResponseWrapper;
import jakarta.servlet.ServletSecurityElement;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.WriteListener;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionContext;
import jakarta.servlet.http.Part;

import org.xwiki.jakartabridge.JakartaBridge;
import org.xwiki.jakartabridge.JakartaToJavaxWrapper;
import org.xwiki.jakartabridge.JavaxToJakartaWrapper;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxAsyncContext;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxAsyncListener;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxCookie;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxFilter;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxFilterChain;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxFilterConfig;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxFilterRegistration;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxHttpServletRequest;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxHttpServletRequestWrapper;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxHttpServletResponse;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxHttpServletResponseWrapper;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxHttpSession;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxHttpSessionContext;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxPart;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxReadListener;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxRequestDispatcher;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServlet;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServletConfig;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServletContext;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServletInputStream;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServletOutputStream;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServletRegistration;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServletRequest;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServletRequestWrapper;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServletResponse;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxServletResponseWrapper;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxSessionCookieConfig;
import org.xwiki.jakartabridge.servlet.internal.JakartaToJavaxWriteListener;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaAsyncContext;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaAsyncListener;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaCookie;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaFilter;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaFilterChain;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaFilterConfig;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaFilterRegistration;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaHttpServletRequest;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaHttpServletRequestWrapper;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaHttpServletResponse;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaHttpServletResponseWrapper;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaHttpSession;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaHttpSessionContext;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaPart;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaReadListener;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaRequestDispatcher;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServlet;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServletConfig;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServletContext;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServletInputStream;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServletOutputStream;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServletRegistration;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServletRequest;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServletRequestWrapper;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServletResponse;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaServletResponseWrapper;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaSessionCookieConfig;
import org.xwiki.jakartabridge.servlet.internal.JavaxToJakartaWriteListener;

/**
 * @version $Id$
 * @since 17.0.0RC1
 */
public final class JakartaServletBridge
{
    private JakartaServletBridge()
    {
    }

    //////////////////////////////////////////////////
    // Wrapped

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletRequest toJavax(ServletRequest jakarta)
    {
        javax.servlet.ServletRequest javax;

        if (jakarta == null) {
            javax = null;
        } else if (jakarta instanceof JakartaToJavaxWrapper<?> bridge) {
            javax = (javax.servlet.ServletRequest) bridge.getJavax();
        } else if (jakarta instanceof HttpServletRequest httpjakarta) {
            javax = toJavax(httpjakarta);
        } else if (jakarta instanceof ServletRequestWrapper wrapper) {
            javax = new JavaxToJakartaServletRequestWrapper<jakarta.servlet.ServletRequestWrapper>(wrapper);
        } else {
            javax = new JavaxToJakartaServletRequest<jakarta.servlet.ServletRequest>(jakarta);
        }

        return javax;
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.http.HttpServletRequest toJavax(HttpServletRequest jakarta)
    {
        javax.servlet.http.HttpServletRequest javax;

        if (jakarta == null) {
            javax = null;
        } else if (jakarta instanceof JakartaToJavaxWrapper<?> bridge) {
            javax = (javax.servlet.http.HttpServletRequest) bridge.getJavax();
        } else if (jakarta instanceof HttpServletRequestWrapper wrapper) {
            javax =
                new JavaxToJakartaHttpServletRequestWrapper<jakarta.servlet.http.HttpServletRequestWrapper>(wrapper);
        } else {
            javax = new JavaxToJakartaHttpServletRequest<jakarta.servlet.http.HttpServletRequest>(jakarta);
        }

        return javax;
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static ServletRequest toJakarta(javax.servlet.ServletRequest javax)
    {
        ServletRequest jakarta;

        if (javax == null) {
            jakarta = null;
        } else if (javax instanceof JavaxToJakartaWrapper<?> bridge) {
            jakarta = (ServletRequest) bridge.getJakarta();
        } else if (javax instanceof javax.servlet.http.HttpServletRequest httpjakarta) {
            jakarta = toJakarta(httpjakarta);
        } else if (javax instanceof javax.servlet.ServletRequestWrapper wrapper) {
            jakarta = new JakartaToJavaxServletRequestWrapper<javax.servlet.ServletRequestWrapper>(wrapper);
        } else {
            jakarta = new JakartaToJavaxServletRequest<javax.servlet.ServletRequest>(javax);
        }

        return jakarta;
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static HttpServletRequest toJakarta(javax.servlet.http.HttpServletRequest javax)
    {
        HttpServletRequest jakarta;

        if (javax == null) {
            jakarta = null;
        } else if (javax instanceof JavaxToJakartaWrapper<?> bridge) {
            jakarta = (HttpServletRequest) bridge.getJakarta();
        } else if (javax instanceof javax.servlet.http.HttpServletRequestWrapper wrapper) {
            jakarta =
                new JakartaToJavaxHttpServletRequestWrapper<javax.servlet.http.HttpServletRequestWrapper>(wrapper);
        } else {
            jakarta = new JakartaToJavaxHttpServletRequest<javax.servlet.http.HttpServletRequest>(javax);
        }

        return jakarta;
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletResponse toJavax(ServletResponse jakarta)
    {
        javax.servlet.ServletResponse javax;

        if (jakarta == null) {
            javax = null;
        } else if (jakarta instanceof JakartaToJavaxWrapper<?> bridge) {
            javax = (javax.servlet.ServletResponse) bridge.getJavax();
        } else if (jakarta instanceof HttpServletResponse httpjakarta) {
            javax = toJavax(httpjakarta);
        } else if (jakarta instanceof ServletResponseWrapper wrapper) {
            javax = new JavaxToJakartaServletResponseWrapper<jakarta.servlet.ServletResponseWrapper>(wrapper);
        } else {
            javax = new JavaxToJakartaServletResponse<jakarta.servlet.ServletResponse>(jakarta);
        }

        return javax;
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.http.HttpServletResponse toJavax(HttpServletResponse jakarta)
    {
        javax.servlet.http.HttpServletResponse javax;

        if (jakarta == null) {
            javax = null;
        } else if (jakarta instanceof JakartaToJavaxWrapper<?> bridge) {
            javax = (javax.servlet.http.HttpServletResponse) bridge.getJavax();
        } else if (jakarta instanceof HttpServletResponseWrapper wrapper) {
            javax =
                new JavaxToJakartaHttpServletResponseWrapper<jakarta.servlet.http.HttpServletResponseWrapper>(wrapper);
        } else {
            javax = new JavaxToJakartaHttpServletResponse<jakarta.servlet.http.HttpServletResponse>(jakarta);
        }

        return javax;
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static ServletResponse toJakarta(javax.servlet.ServletResponse javax)
    {
        ServletResponse jakarta;

        if (javax == null) {
            jakarta = null;
        } else if (javax instanceof JavaxToJakartaWrapper<?> bridge) {
            jakarta = (ServletResponse) bridge.getJakarta();
        } else if (javax instanceof javax.servlet.http.HttpServletResponse httpjakarta) {
            jakarta = toJakarta(httpjakarta);
        } else if (javax instanceof javax.servlet.ServletResponseWrapper wrapper) {
            jakarta = new JakartaToJavaxServletResponseWrapper<javax.servlet.ServletResponseWrapper>(wrapper);
        } else {
            jakarta = new JakartaToJavaxServletResponse<javax.servlet.ServletResponse>(javax);
        }

        return jakarta;
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static HttpServletResponse toJakarta(javax.servlet.http.HttpServletResponse javax)
    {
        HttpServletResponse jakarta;

        if (javax == null) {
            jakarta = null;
        } else if (javax instanceof JavaxToJakartaWrapper<?> bridge) {
            jakarta = (HttpServletResponse) bridge.getJakarta();
        } else if (javax instanceof javax.servlet.http.HttpServletResponseWrapper wrapper) {
            jakarta =
                new JakartaToJavaxHttpServletResponseWrapper<javax.servlet.http.HttpServletResponseWrapper>(wrapper);
        } else {
            jakarta = new JakartaToJavaxHttpServletResponse<javax.servlet.http.HttpServletResponse>(javax);
        }

        return jakarta;
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.RequestDispatcher toJavax(RequestDispatcher jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaRequestDispatcher::new);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static RequestDispatcher toJakarta(javax.servlet.RequestDispatcher javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxRequestDispatcher::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletContext toJavax(ServletContext jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaServletContext::new);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static ServletContext toJakarta(javax.servlet.ServletContext javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxServletContext::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.Servlet toJavax(Servlet jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaServlet::new);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static Servlet toJakarta(javax.servlet.Servlet javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxServlet::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.http.HttpSession toJavax(HttpSession jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaHttpSession::new);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static HttpSession toJakarta(javax.servlet.http.HttpSession javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxHttpSession::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.http.Part toJavax(Part jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaPart::new);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static Part toJakarta(javax.servlet.http.Part javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxPart::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ReadListener toJavax(ReadListener jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaReadListener::new);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static ReadListener toJakarta(javax.servlet.ReadListener javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxReadListener::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.WriteListener toJavax(WriteListener jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaWriteListener::new);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static WriteListener toJakarta(javax.servlet.WriteListener javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxWriteListener::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletOutputStream toJavax(ServletOutputStream jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaServletOutputStream::new);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static ServletOutputStream toJakarta(javax.servlet.ServletOutputStream javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxServletOutputStream::new);

    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.http.HttpSessionContext toJavax(HttpSessionContext jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaHttpSessionContext::new);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static HttpSessionContext toJakarta(javax.servlet.http.HttpSessionContext javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxHttpSessionContext::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletConfig toJavax(ServletConfig jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaServletConfig::new);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static ServletConfig toJakarta(javax.servlet.ServletConfig javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxServletConfig::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.AsyncContext toJavax(AsyncContext jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaAsyncContext::new);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static AsyncContext toJakarta(javax.servlet.AsyncContext javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxAsyncContext::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.SessionCookieConfig toJavax(SessionCookieConfig jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaSessionCookieConfig::new);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static SessionCookieConfig toJakarta(javax.servlet.SessionCookieConfig javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxSessionCookieConfig::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.FilterRegistration toJavax(FilterRegistration jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaFilterRegistration::new);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static FilterRegistration toJakarta(javax.servlet.FilterRegistration javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxFilterRegistration::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.FilterRegistration.Dynamic toJavax(FilterRegistration.Dynamic jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaFilterRegistration.Dynamic::new);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static jakarta.servlet.FilterRegistration.Dynamic toJakarta(javax.servlet.FilterRegistration.Dynamic javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxFilterRegistration.Dynamic::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletRegistration toJavax(ServletRegistration jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaServletRegistration::new);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static ServletRegistration toJakarta(javax.servlet.ServletRegistration javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxServletRegistration::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletRegistration.Dynamic toJavax(ServletRegistration.Dynamic jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaServletRegistration.Dynamic::new);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static ServletRegistration.Dynamic toJakarta(javax.servlet.ServletRegistration.Dynamic javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxServletRegistration.Dynamic::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.Filter toJavax(Filter jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaFilter::new);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static Filter toJakarta(javax.servlet.Filter javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxFilter::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.AsyncListener toJavax(AsyncListener jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaAsyncListener::new);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static AsyncListener toJakarta(javax.servlet.AsyncListener javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxAsyncListener::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.ServletInputStream toJavax(ServletInputStream jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaServletInputStream::new);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static ServletInputStream toJakarta(javax.servlet.ServletInputStream javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxServletInputStream::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.FilterChain toJavax(FilterChain jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaFilterChain::new);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static FilterChain toJakarta(javax.servlet.FilterChain javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxFilterChain::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.FilterConfig toJavax(FilterConfig jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaFilterConfig::new);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static FilterConfig toJakarta(javax.servlet.FilterConfig javax)
    {
        return JakartaBridge.toJakarta(javax, JakartaToJavaxFilterConfig::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static javax.servlet.http.Cookie toJavax(Cookie jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaCookie::new);
    }

    /**
     * @param javax the javax version
     * @return the jakarta version
     */
    public static Cookie toJakarta(javax.servlet.http.Cookie javax)
    {
        return JakartaBridge.toJavax(javax, JakartaToJavaxCookie::new);
    }

    //////////////////////////////////////////////////
    // Converted

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

        return jakarta.stream().map(JakartaServletBridge::toJavax).collect(Collectors.toSet());
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

        return javax.stream().map(JakartaServletBridge::toJakarta).collect(Collectors.toSet());
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

    //////////////////////////////////////////////////
    // Multi

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static Collection<javax.servlet.http.Part> toJavax(Collection<Part> jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaPart::new, JakartaToJavaxPart::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static Collection<Part> toJakarta(Collection<javax.servlet.http.Part> jakarta)
    {
        return JakartaBridge.toJakarta(jakarta, JavaxToJakartaPart::new, JakartaToJavaxPart::new);
    }

    /**
     * @param jakarta the jakarta version
     * @return the javax version
     */
    public static Enumeration<javax.servlet.Servlet> toJavax(Enumeration<Servlet> jakarta)
    {
        return JakartaBridge.toJavax(jakarta, JavaxToJakartaServlet::new, JakartaToJavaxServlet::new);
    }

    /**
     * @param javax the javax version
     * @return the javax version
     */
    public static Enumeration<Servlet> toJakarta(Enumeration<javax.servlet.Servlet> javax)
    {
        return JakartaBridge.toJakarta(javax, JavaxToJakartaServlet::new, JakartaToJavaxServlet::new);
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

        return jakarta.stream().map(JakartaServletBridge::toJavax)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(javax.servlet.DispatcherType.class)));
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

        return javax.stream().map(JakartaServletBridge::toJakarta)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(jakarta.servlet.DispatcherType.class)));
    }

    //////////////////////////////////////////////////
    // Unsupported

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
}
