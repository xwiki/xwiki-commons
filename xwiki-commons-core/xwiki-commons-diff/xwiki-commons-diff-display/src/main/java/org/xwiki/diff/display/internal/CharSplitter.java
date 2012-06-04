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
package org.xwiki.diff.display.internal;

import java.util.Arrays;
import java.util.List;

import javax.inject.Singleton;

import org.apache.commons.lang3.ArrayUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.diff.display.Splitter;

/**
 * Splits a string into its characters.
 * 
 * @version $Id$
 * @since 4.1RC1
 */
@Component
@Singleton
public class CharSplitter implements Splitter<String, Character>
{
    @Override
    public List<Character> split(String composite)
    {
        return Arrays.asList(ArrayUtils.toObject(composite.toCharArray()));
    }
}
