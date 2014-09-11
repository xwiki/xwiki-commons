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

import org.slf4j.Marker;

/**
 * Allows associating a translation key to a log.
 *
 * @version $Id$
 * @since 5.4M1
 */
public class TranslationMarker extends AbstractContainerMarker
{
    /**
     * The marker name.
     */
    public static final String NAME = "xwiki.translation";

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see #getTranslationKey()
     */
    private String translationKey;

    /**
     * @param translationKey the translation key to associate to the log
     */
    public TranslationMarker(String translationKey)
    {
        super(NAME);

        this.translationKey = translationKey;
    }

    /**
     * @param translationKey the translation key to associate to the log
     * @param references the other associated markers
     */
    public TranslationMarker(String translationKey, Marker... references)
    {
        super(translationKey, references);

        this.translationKey = translationKey;
    }

    /**
     * @return the translation key associate to the log
     */
    public String getTranslationKey()
    {
        return this.translationKey;
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
