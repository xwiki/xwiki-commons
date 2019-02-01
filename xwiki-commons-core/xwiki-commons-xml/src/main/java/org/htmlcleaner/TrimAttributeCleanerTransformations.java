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
package org.htmlcleaner;

import org.xwiki.stability.Unstable;

/**
 * This class allows to create on the fly a new tag transformation to trim leading space.
 * See {@link TrimAttributeTagTransformation} for more information.
 *
 * @version $Id$
 * @since 11.1RC1
 */
@Unstable
public class TrimAttributeCleanerTransformations extends CleanerTransformations
{
    @Override
    public TagTransformation getTransformation(String tagName) {
        TagTransformation transfo = super.getTransformation(tagName);
        if (transfo == null) {

            // we only create the transformation if it doesn't exist yet
            // and we keep it to avoid creating multiple objects for the same tag over and over.
            transfo = new TrimAttributeTagTransformation(tagName, tagName, true);
            this.addTransformation(transfo);
        }
        return transfo;
    }
}
