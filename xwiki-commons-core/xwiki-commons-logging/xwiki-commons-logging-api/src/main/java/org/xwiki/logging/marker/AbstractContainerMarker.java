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
package org.xwiki.logging.marker;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Marker;

/**
 * Base class to use for custom {@link Marker}s which contains values (so which are a bit more than {@link Marker} as
 * defined by SLF4J). For "real" marker use {@link org.slf4j.MarkerFactory#getMarker(String)}.
 *
 * @version $Id$
 * @since 5.4M1
 */
public abstract class AbstractContainerMarker implements ContainerMarker
{
    private static final long serialVersionUID = 1L;

    private static final String OPEN = "[ ";

    private static final String CLOSE = " ]";

    private static final String SEP = ", ";

    private final String name;

    private Set<Marker> references;

    /**
     * @param name the name of the {@link Marker}
     * @param references the other associated markers
     */
    public AbstractContainerMarker(String name, Marker... references)
    {
        if (name == null) {
            throw new IllegalArgumentException("A marker name cannot be null");
        }

        this.name = name;

        for (Marker reference : references) {
            add(reference);
        }
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public <M extends Marker> M get(String name)
    {
        if (this.name.equals(name)) {
            return (M) this;
        }

        if (this.references != null) {
            for (Marker marker : this.references) {
                if (marker.getName().equals(name)) {
                    return (M) marker;
                }

                if (marker instanceof ContainerMarker) {
                    Marker targetMarker = ((ContainerMarker) marker).get(name);

                    if (targetMarker != null) {
                        return (M) targetMarker;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public synchronized void add(Marker reference)
    {
        if (reference == null) {
            throw new IllegalArgumentException("A null value cannot be added to a Marker as reference.");
        }

        if (!reference.contains(this)) {
            if (this.references == null) {
                this.references = new LinkedHashSet<>();
            }

            this.references.add(reference);
        }

    }

    @Override
    public synchronized boolean hasReferences()
    {
        return this.references != null && this.references.size() > 0;
    }

    @Override
    public boolean hasChildren()
    {
        return hasReferences();
    }

    @Override
    public synchronized Iterator<Marker> iterator()
    {
        if (this.references != null) {
            return this.references.iterator();
        } else {
            return Collections.<Marker>emptyList().iterator();
        }
    }

    @Override
    public synchronized boolean remove(Marker referenceToRemove)
    {
        if (this.references == null) {
            return false;
        }

        return this.references.remove(referenceToRemove);
    }

    @Override
    public boolean contains(Marker other)
    {
        if (equals(other)) {
            return true;
        }

        if (this.references != null) {
            for (Marker marker : this.references) {
                if (marker.contains(other)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean contains(String name)
    {
        if (this.name.equals(name)) {
            return true;
        }

        if (this.references != null) {
            for (Marker marker : this.references) {
                if (marker.contains(name)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Marker)) {
            return false;
        }

        final Marker other = (Marker) obj;
        return this.name.equals(other.getName());
    }

    @Override
    public int hashCode()
    {
        return this.name.hashCode();
    }

    @Override
    public String toString()
    {
        if (!this.hasReferences()) {
            return this.getName();
        }

        StringBuilder sb = new StringBuilder(this.getName());
        sb.append(' ').append(OPEN);

        Iterator<Marker> it = this.iterator();
        while (it.hasNext()) {
            Marker reference = it.next();
            sb.append(reference.getName());
            if (it.hasNext()) {
                sb.append(SEP);
            }
        }
        sb.append(CLOSE);

        return sb.toString();
    }
}
