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
package org.xwiki.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class for {@link Request} implementations.
 *
 * @version $Id$
 * @since 4.0M1
 */
public abstract class AbstractRequest implements Request
{
    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see #getId()
     */
    private List<String> id;

    /**
     * The properties.
     */
    private Map<String, Object> properties = new HashMap<>();

    /**
     * @see #isVerbose()
     */
    private boolean verbose = true;

    /**
     * Default constructor.
     */
    public AbstractRequest()
    {

    }

    /**
     * @param request the request to copy
     */
    public AbstractRequest(Request request)
    {
        setId(request.getId());

        for (String key : request.getPropertyNames()) {
            setProperty(key, request.getProperty(key));
        }
    }

    @Override
    public List<String> getId()
    {
        return this.id;
    }

    /**
     * @param id the identifier used to access the job
     * @since 4.1M2
     */
    public void setId(List<String> id)
    {
        this.id = id != null ? new ArrayList<>(id) : null;
    }

    /**
     * @param id the identifier used to access the job
     */
    public void setId(String id)
    {
        setId(Arrays.asList(id));
    }

    /**
     * @param id the id elements
     * @since 8.4RC1
     */
    public void setId(String... id)
    {
        this.id = Arrays.asList(id);
    }

    @Override
    public boolean isRemote()
    {
        return this.<Boolean>getProperty(PROPERTY_REMOTE, false);
    }

    /**
     * @param remote indicate if the job has been triggered by a remote event
     */
    public void setRemote(boolean remote)
    {
        setProperty(PROPERTY_REMOTE, remote);
    }

    @Override
    public boolean isInteractive()
    {
        return this.<Boolean>getProperty(PROPERTY_INTERACTIVE, false);
    }

    /**
     * @param interactive indicate if the job is allowed to ask questions if it it should be fully automated (i.e. use
     *            default answers)
     */
    public void setInteractive(boolean interactive)
    {
        setProperty(PROPERTY_INTERACTIVE, interactive);
    }

    /**
     * @param key the name of the property
     * @param value the value of the property
     */
    public void setProperty(String key, Object value)
    {
        this.properties.put(key, value);
    }

    /**
     * @param key the name of the property
     * @return the previous value associated to the passed key
     * @param <T> the type of the value
     * @since 4.2M2
     */
    public <T> T removeProperty(String key)
    {
        return (T) this.properties.remove(key);
    }

    @Override
    public <T> T getProperty(String key)
    {
        return getProperty(key, null);
    }

    @Override
    public <T> T getProperty(String key, T def)
    {
        Object value = this.properties.get(key);

        return value != null ? (T) value : def;
    }

    @Override
    public Collection<String> getPropertyNames()
    {
        return this.properties.keySet();
    }

    @Override
    public boolean containsProperty(String key)
    {
        return this.properties.containsKey(key);
    }

    @Override
    public boolean isVerbose()
    {
        return this.verbose;
    }

    /**
     * @param verbose true if the job should log informations about what is going on.
     * @since 5.4RC1
     */
    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }
}
