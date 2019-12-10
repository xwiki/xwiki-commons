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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.diff.xml.StringSplitter;

/**
 * Default implementation of {@link StringSplitter} that generates a list with a single object, the string itself. Use
 * this when you don't need a detailed report of the changes between two string (i.e. you only care if the strings are
 * different).
 * 
 * @version $Id$
 * @since 11.10.1
 * @since 12.0RC1
 */
@Component
@Singleton
public class DefaultStringSplitter implements StringSplitter
{
    @Override
    public List<Object> split(String text)
    {
        return Collections.singletonList(text);
    }

    @Override
    public String join(List<Object> objects)
    {
        return objects.stream().map(Object::toString).collect(Collectors.joining(""));
    }
}
