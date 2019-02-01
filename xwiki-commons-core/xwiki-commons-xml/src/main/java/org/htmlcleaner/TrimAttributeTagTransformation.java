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

import java.util.HashSet;
import java.util.Map;

import org.xwiki.stability.Unstable;

/**
 * This class allows to transform all tags attribute to trim their value from leading space, except for input value.
 * It applies the original tag transformations, and then iterates over the attributes to remove the leading spaces.
 * This class aims at being deleted once HtmlCleaner offers a way to have a better control over trimAttribute flag,
 * see: https://sourceforge.net/p/htmlcleaner/bugs/213/.
 *
 * Note: Even though in a public package this code is not meant to be a public API. We've had to put in under the {@code
 * org.htmlcleaner} package because we use the following package protected API: TagTransformation#getSourceTag().
 *
 * @version $Id$
 * @since 11.1RC1
 */
@Unstable
public class TrimAttributeTagTransformation extends TagTransformation
{
    /**
     * Create a {@link TagTransformation} from source tag to target tag. This kind of transformation always preserve
     * the attributes.
     * @param sourceTag Name of the tag to be transformed.
     * @param destTag Name of tag to which source tag is to be transformed.
     */
    public TrimAttributeTagTransformation(String sourceTag, String destTag) {
        super(sourceTag, destTag);
    }

    @Override
    public Map<String, String> applyTagTransformations(Map<String, String> attributes) {
        Map<String, String> result = super.applyTagTransformations(attributes);

        for (Map.Entry<String, String> attributesEntry : new HashSet<>(result.entrySet())) {
            String attrName = attributesEntry.getKey();
            String attrValue = attributesEntry.getValue();

            // we don't want to trim spaces for input value attribute
            // this is the only reason of this class existence.
            if (!(getSourceTag().equals("input") && attrName.equals("value"))) {
                result.put(attrName, attrValue.trim());
            }
        }

        return result;
    }
}
