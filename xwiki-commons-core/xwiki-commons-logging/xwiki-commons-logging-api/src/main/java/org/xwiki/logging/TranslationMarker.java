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
package org.xwiki.logging;

import java.util.Collections;
import java.util.Iterator;

import org.slf4j.Marker;

/**
 * Allow associate a log to a translation key.
 * 
 * @version $Id$
 * @since 5.0M2
 */
public class TranslationMarker implements Marker
{
    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The marker name.
     */
    private static final String NAME = TranslationMarker.class.getName();

    /**
     * @see #getTranslationKey()
     */
    private String translationKey;

    /**
     * @param translationKey the translation key to associate to the log
     */
    public TranslationMarker(String translationKey)
    {
        this.translationKey = translationKey;
    }

    /**
     * @return the translation key associate to the log
     */
    public String getTranslationKey()
    {
        return this.translationKey;
    }

    // Marker

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public void add(Marker reference)
    {
        // Not supported
    }

    @Override
    public boolean remove(Marker reference)
    {
        // Not supported
        return false;
    }

    @Override
    public boolean hasChildren()
    {
        // Not supported
        return false;
    }

    @Override
    public boolean hasReferences()
    {
        // Not supported
        return false;
    }

    @Override
    public Iterator iterator()
    {
        // Not supported
        return Collections.EMPTY_LIST.iterator();
    }

    @Override
    public boolean contains(Marker other)
    {
        if (equals(other)) {
            return true;
        }

        // Not supported
        return false;
    }

    @Override
    public boolean contains(String name)
    {
        if (NAME.equals(name)) {
            return true;
        }

        // Not supported
        return false;
    }

    // Object

    @Override
    public int hashCode()
    {
        return this.translationKey.hashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other) {
            return true;
        }

        return other instanceof TranslationMarker
            && this.translationKey.equals(((TranslationMarker) other).getTranslationKey());
    }
}
