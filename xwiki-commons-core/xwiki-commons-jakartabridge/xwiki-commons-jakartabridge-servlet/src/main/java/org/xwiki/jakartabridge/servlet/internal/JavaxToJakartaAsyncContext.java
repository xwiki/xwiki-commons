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

import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.xwiki.jakartabridge.internal.AbstractJavaxToJakartaWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JavaxToJakartaAsyncContext extends AbstractJavaxToJakartaWrapper<jakarta.servlet.AsyncContext>
    implements AsyncContext
{
    /**
     * @param jakarta the wrapped version
     */
    public JavaxToJakartaAsyncContext(jakarta.servlet.AsyncContext jakarta)
    {
        super(jakarta);
    }

    @Override
    public ServletRequest getRequest()
    {
        return JakartaServletBridge.toJavax(this.jakarta.getRequest());
    }

    @Override
    public ServletResponse getResponse()
    {
        return JakartaServletBridge.toJavax(this.jakarta.getResponse());
    }

    @Override
    public boolean hasOriginalRequestAndResponse()
    {
        return this.jakarta.hasOriginalRequestAndResponse();
    }

    @Override
    public void dispatch()
    {
        this.jakarta.dispatch();
    }

    @Override
    public void dispatch(String path)
    {
        this.jakarta.dispatch(path);
    }

    @Override
    public void dispatch(ServletContext context, String path)
    {
        this.jakarta.dispatch(JakartaServletBridge.toJakarta(context), path);
    }

    @Override
    public void complete()
    {
        this.jakarta.complete();
    }

    @Override
    public void start(Runnable run)
    {
        this.jakarta.start(run);
    }

    @Override
    public void addListener(AsyncListener listener)
    {
        this.jakarta.addListener(JakartaServletBridge.toJakarta(listener));
    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse)
    {
        this.jakarta.addListener(JakartaServletBridge.toJakarta(listener),
            JakartaServletBridge.toJakarta(servletRequest), JakartaServletBridge.toJakarta(servletResponse));
    }

    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException
    {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public void setTimeout(long timeout)
    {
        this.jakarta.setTimeout(timeout);
    }

    @Override
    public long getTimeout()
    {
        return this.jakarta.getTimeout();
    }
}
