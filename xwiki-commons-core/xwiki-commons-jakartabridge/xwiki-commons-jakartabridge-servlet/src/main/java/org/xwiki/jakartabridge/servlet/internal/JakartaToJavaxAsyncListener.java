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

import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;

import org.xwiki.jakartabridge.internal.AbstractJakartaToJavaxWrapper;
import org.xwiki.jakartabridge.servlet.JakartaServletBridge;

/**
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JakartaToJavaxAsyncListener extends AbstractJakartaToJavaxWrapper<javax.servlet.AsyncListener>
    implements AsyncListener
{
    /**
     * @param javax the wrapped version
     */
    public JakartaToJavaxAsyncListener(javax.servlet.AsyncListener javax)
    {
        super(javax);
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException
    {
        this.javax.onComplete(JakartaServletBridge.toJavax(event));
    }

    @Override
    public void onTimeout(AsyncEvent event) throws IOException
    {
        this.javax.onTimeout(JakartaServletBridge.toJavax(event));
    }

    @Override
    public void onError(AsyncEvent event) throws IOException
    {
        this.javax.onError(JakartaServletBridge.toJavax(event));
    }

    @Override
    public void onStartAsync(AsyncEvent event) throws IOException
    {
        this.javax.onStartAsync(JakartaServletBridge.toJavax(event));
    }
}
