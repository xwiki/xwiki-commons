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

import java.io.IOException;

import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;

import org.xwiki.jakartabridge.servlet.ServletBridge;

/**
 * @version $Id$
 * @since 17-jakarta
 */
public class JavaxToJakartaAsyncListener implements AsyncListener
{
    private final jakarta.servlet.AsyncListener wrapped;

    /**
     * @param wrapped the wrapped version
     */
    public JavaxToJakartaAsyncListener(jakarta.servlet.AsyncListener wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException
    {
        this.wrapped.onComplete(ServletBridge.toJakarta(event));
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException
    {
        this.wrapped.onTimeout(ServletBridge.toJakarta(event));
    }

    @Override
    public void onError(AsyncEvent event) throws IOException
    {
        this.wrapped.onError(ServletBridge.toJakarta(event));
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException
    {
        this.wrapped.onStartAsync(ServletBridge.toJakarta(event));
    }
}
