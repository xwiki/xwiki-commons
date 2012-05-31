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
package org.xwiki.diff.display;

import java.util.List;

/**
 * Represents a word from an in-line diff.
 * 
 * @param <E> the character type
 * @version $Id$
 * @since 4.1RC1
 */
public class InlineDiffWord<E>
{
    /**
     * The possible types of words you can find within an in-line diff.
     */
    public static enum WordType
    {
        /** A word that has been added. */
        ADDED,

        /** A word that has been removed. */
        DELETED,

        /** A word that shows the context where a change has been made. */
        CONTEXT;
    }

    /**
     * The word type.
     */
    private final WordType type;

    /**
     * The word characters.
     */
    private final List<E> characters;

    /**
     * Creates a new word with the specified type and characters.
     * 
     * @param type the word type
     * @param characters the word characters
     */
    public InlineDiffWord(WordType type, List<E> characters)
    {
        this.type = type;
        this.characters = characters;
    }

    /**
     * @return the word type
     */
    public WordType getType()
    {
        return type;
    }

    /**
     * @return the word characters
     */
    public List<E> getCharacters()
    {
        return characters;
    }

    /**
     * @return {@code true} if this word was added, {@code false} otherwise
     */
    public boolean isAdded()
    {
        return type == WordType.ADDED;
    }

    /**
     * @return {@code true} if this word was deleted, {@code false} otherwise
     */
    public boolean isDeleted()
    {
        return type == WordType.DELETED;
    }

    @Override
    public String toString()
    {
        StringBuilder word = new StringBuilder(characters.size());
        for (E character : characters) {
            word.append(character);
        }
        return word.toString();
    }
}
