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
/*
 * Alternatively, at your choice, the contents of this file may be used under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.xwiki.xml.internal.html;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

/**
 * Provides MathML tag and attribute definitions with a focus on safe tags/attributes.
 * <p>
 * Unless otherwise noted, lists of elements and attributes are copied from DOMPurify by Cure53 and other contributors |
 * Released under the Apache license 2.0 and Mozilla Public License 2.0 -
 * <a href="https://github.com/cure53/DOMPurify/blob/main/LICENSE">LICENSE</a>.
 *
 * @version $Id$
 * @since 14.6RC1
 */
@Component(roles = MathMLDefinitions.class)
@Singleton
public class MathMLDefinitions
{
    private final Set<String> safeTags;

    private final Set<String> allTags;

    private final Set<String> allowedAttributes;

    private final Set<String> textIntegrationPoints;

    /**
     * Default constructor.
     */
    public MathMLDefinitions()
    {
        this.safeTags = new HashSet<>(
            Arrays.asList("math", "menclose", "merror", "mfenced", "mfrac", "mglyph", "mi", "mlabeledtr",
                "mmultiscripts", "mn", "mo", "mover", "mpadded", "mphantom", "mroot", "mrow", "ms", "mspace", "msqrt",
                "mstyle", "msub", "msup", "msubsup", "mtable", "mtd", "mtext", "mtr", "munder", "munderover"));

        this.allTags = new HashSet<>(
            Arrays.asList("maction", "maligngroup", "malignmark", "mlongdiv", "mscarries", "mscarry", "msgroup",
                "mstack", "msline", "msrow", "semantics", "annotation", "annotation-xml", "mprescripts", "none"));

        this.allTags.addAll(this.safeTags);

        this.allowedAttributes = new HashSet<>(
            Arrays.asList("accent", "accentunder", "align", "bevelled", "close", "columnsalign", "columnlines",
                "columnspan", "denomalign", "depth", "dir", "display", "displaystyle", "encoding", "fence", "frame",
                "height", "href", "id", "largeop", "length", "linethickness", "lspace", "lquote", "mathbackground",
                "mathcolor", "mathsize", "mathvariant", "maxsize", "minsize", "movablelimits", "notation", "numalign",
                "open", "rowalign", "rowlines", "rowspacing", "rowspan", "rspace", "rquote", "scriptlevel",
                "scriptminsize", "scriptsizemultiplier", "selection", "separator", "separators", "stretchy",
                "subscriptshift", "supscriptshift", "symmetric", "voffset", "width", "xmlns"));

        this.textIntegrationPoints = new HashSet<>(Arrays.asList("mi", "mo", "mn", "ms", "mtext", "annotation-xml"));
    }

    /**
     * @param tagName the name of the tag to check
     * @return if the tag is considered safe
     */
    public boolean isSafeTag(String tagName)
    {
        return this.safeTags.contains(tagName);
    }

    /**
     * @param tagName the name of the tag to check
     * @return if the tag is a MathML tag
     */
    public boolean isMathMLTag(String tagName)
    {
        return this.allTags.contains(tagName);
    }

    /**
     * @param attributeName the name of the attribute to check
     * @return if the attribute is allowed
     */
    public boolean isAllowedAttribute(String attributeName)
    {
        return this.allowedAttributes.contains(attributeName);
    }

    /**
     * @param tagName the name of the tag to check
     * @return if the tag is a text integration point, i.e., its children can be HTML elements
     */
    public boolean isTextOrHTMLIntegrationPoint(String tagName)
    {
        return this.textIntegrationPoints.contains(tagName);
    }
}
