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
package org.xwiki.diff.xml.internal;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.diff.xml.StringSplitter;

/**
 * Splits a string into its characters. Use this when you need a very detailed report of the changes between strings.
 * 
 * @version $Id$
 * @since 11.10.1
 * @since 12.0RC1
 */
@Component
@Singleton
@Named("character")
public class CharacterStringSplitter implements StringSplitter
{
    @Override
    public List<Object> split(String text)
    {
        return text.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
    }

    @Override
    public String join(List<Object> characters)
    {
        StringBuilder stringBuilder = new StringBuilder();
        characters.forEach(stringBuilder::append);
        return stringBuilder.toString();
    }
}
