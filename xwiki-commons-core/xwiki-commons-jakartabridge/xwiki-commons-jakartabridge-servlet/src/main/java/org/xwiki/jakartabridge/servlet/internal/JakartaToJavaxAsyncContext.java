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

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.xwiki.jakartabridge.internal.AbstractJakartaToJavaxWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JakartaToJavaxAsyncContext extends AbstractJakartaToJavaxWrapper<javax.servlet.AsyncContext>
    implements AsyncContext
{
    /**
     * @param javax the wrapped version
     */
    public JakartaToJavaxAsyncContext(javax.servlet.AsyncContext javax)
    {
        super(javax);
    }

    @Override
    public ServletRequest getRequest()
    {
        return JakartaServletBridge.toJakarta(this.javax.getRequest());
    }

    @Override
    public ServletResponse getResponse()
    {
        return JakartaServletBridge.toJakarta(this.javax.getResponse());
    }

    @Override
    public boolean hasOriginalRequestAndResponse()
    {
        return this.javax.hasOriginalRequestAndResponse();
    }

    @Override
    public void dispatch()
    {
        this.javax.dispatch();
    }

    @Override
    public void dispatch(String path)
    {
        this.javax.dispatch(path);
    }

    @Override
    public void dispatch(ServletContext context, String path)
    {
        this.javax.dispatch(JakartaServletBridge.toJavax(context), path);
    }

    @Override
    public void complete()
    {
        this.javax.complete();
    }

    @Override
    public void start(Runnable run)
    {
        this.javax.start(run);
    }

    @Override
    public void addListener(AsyncListener listener)
    {
        this.javax.addListener(JakartaServletBridge.toJavax(listener));
    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse)
    {
        this.javax.addListener(JakartaServletBridge.toJavax(listener), JakartaServletBridge.toJavax(servletRequest),
            JakartaServletBridge.toJavax(servletResponse));
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
        this.javax.setTimeout(timeout);
    }

    @Override
    public long getTimeout()
    {
        return this.javax.getTimeout();
    }
}
