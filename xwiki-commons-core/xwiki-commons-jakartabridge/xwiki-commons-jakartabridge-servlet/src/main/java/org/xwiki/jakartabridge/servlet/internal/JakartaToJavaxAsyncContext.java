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

import org.xwiki.jakartabridge.servlet.ServletBridge;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * @version $Id$
 * @since -1.jakarta
 */
public class JakartaToJavaxAsyncContext implements AsyncContext
{
    private final javax.servlet.AsyncContext wrapped;

    /**
     * @param wrapped the wrapped version
     */
    public JakartaToJavaxAsyncContext(javax.servlet.AsyncContext wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public ServletRequest getRequest()
    {
        return ServletBridge.toJakarta(this.wrapped.getRequest());
    }

    @Override
    public ServletResponse getResponse()
    {
        return ServletBridge.toJakarta(this.wrapped.getResponse());
    }

    @Override
    public boolean hasOriginalRequestAndResponse()
    {
        return this.wrapped.hasOriginalRequestAndResponse();
    }

    @Override
    public void dispatch()
    {
        this.wrapped.dispatch();
    }

    @Override
    public void dispatch(String path)
    {
        this.wrapped.dispatch(path);
    }

    @Override
    public void dispatch(ServletContext context, String path)
    {
        this.wrapped.dispatch(ServletBridge.toJavax(context), path);
    }

    @Override
    public void complete()
    {
        this.wrapped.complete();
    }

    @Override
    public void start(Runnable run)
    {
        this.wrapped.start(run);
    }

    @Override
    public void addListener(AsyncListener listener)
    {
        this.wrapped.addListener(ServletBridge.toJavax(listener));
    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse)
    {
        this.wrapped.addListener(ServletBridge.toJavax(listener), ServletBridge.toJavax(servletRequest),
            ServletBridge.toJavax(servletResponse));
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
        this.wrapped.setTimeout(timeout);
    }

    @Override
    public long getTimeout()
    {
        return this.wrapped.getTimeout();
    }
}
