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
package org.xwiki.job.internal.script.safe;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.xwiki.job.Request;
import org.xwiki.script.internal.safe.AbstractSafeObject;
import org.xwiki.script.internal.safe.ScriptSafeProvider;

/**
 * Provide a public script access to a job status.
 * 
 * @version $Id$
 * @since 14.4RC1
 * @since 13.10.6
 */
public class SafeRequest extends AbstractSafeObject<Request> implements Request
{
    /**
     * @param request the wrapped job request
     * @param safeProvider the provider of instances safe for public scripts
     */
    public SafeRequest(Request request, ScriptSafeProvider<?> safeProvider)
    {
        super(request, safeProvider);
    }

    @Override
    public List<String> getId()
    {
        return getWrapped().getId();
    }

    @Override
    public boolean isRemote()
    {
        return getWrapped().isRemote();
    }

    @Override
    public boolean isInteractive()
    {
        return getWrapped().isInteractive();
    }

    @Override
    public Collection<String> getPropertyNames()
    {
        return getWrapped().getPropertyNames();
    }

    @Override
    public boolean containsProperty(String key)
    {
        return getWrapped().containsProperty(key);
    }

    @Override
    public boolean isVerbose()
    {
        return getWrapped().isVerbose();
    }

    @Override
    public <T> T getProperty(String key)
    {
        return safe(getWrapped().getProperty(key));
    }

    @Override
    public <T> T getProperty(String key, T def)
    {
        return safe(getWrapped().getProperty(key, def));
    }

    @Override
    public Map<String, Serializable> getContext()
    {
        return safe(getWrapped().getContext());
    }

    @Override
    public void setContext(Map<String, Serializable> context)
    {
        throw new UnsupportedOperationException(FORBIDDEN);
    }
}
