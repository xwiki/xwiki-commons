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

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.diff.display.Splitter;

/**
 * Splits a text into multiple lines.
 *
 * @version $Id$
 * @since 4.1RC1
 */
@Component
@Named("line")
@Singleton
public class LineSplitter implements Splitter<String, String>
{
    @Override
    public List<String> split(String composite)
    {
        List<String> lines;
        if (composite == null) {
            lines = Collections.emptyList();
        } else {
            lines = new ArrayList<>();
            new BufferedReader(new StringReader(composite)).lines().forEach(line -> lines.add(line));
            // This allows to differentiate two contents, one with a trailing new line and one without. Otherwise
            // they would be considered as having the same content.
            if (composite.endsWith("\n")) {
                lines.add("");
            }
        }
        return lines;
    }
}
