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
    private static final String MATH = "math";

    private static final String MENCLOSE = "menclose";

    private static final String MERROR = "merror";

    private static final String MFENCED = "mfenced";

    private static final String MFRAC = "mfrac";

    private static final String MGLYPH = "mglyph";

    private static final String MI = "mi";

    private static final String MLABELEDTR = "mlabeledtr";

    private static final String MMULTISCRIPTS = "mmultiscripts";

    private static final String MN = "mn";

    private static final String MO = "mo";

    private static final String MOVER = "mover";

    private static final String MPADDED = "mpadded";

    private static final String MPHANTOM = "mphantom";

    private static final String MROOT = "mroot";

    private static final String MROW = "mrow";

    private static final String MS = "ms";

    private static final String MSPACE = "mspace";

    private static final String MSQRT = "msqrt";

    private static final String MSTYLE = "mstyle";

    private static final String MSUB = "msub";

    private static final String MSUP = "msup";

    private static final String MSUBSUP = "msubsup";

    private static final String MTABLE = "mtable";

    private static final String MTD = "mtd";

    private static final String MTEXT = "mtext";

    private static final String MTR = "mtr";

    private static final String MUNDER = "munder";

    private static final String MUNDEROVER = "munderover";

    private static final String MACTION = "maction";

    private static final String MALIGNGROUP = "maligngroup";

    private static final String MALIGNMARK = "malignmark";

    private static final String MLONGDIV = "mlongdiv";

    private static final String MSCARRIES = "mscarries";

    private static final String MSCARRY = "mscarry";

    private static final String MSGROUP = "msgroup";

    private static final String MSTACK = "mstack";

    private static final String MSLINE = "msline";

    private static final String MSROW = "msrow";

    private static final String SEMANTICS = "semantics";

    private static final String ANNOTATION = "annotation";

    private static final String ANNOTATION_XML = "annotation-xml";

    private static final String MPRESCRIPTS = "mprescripts";

    private static final String NONE = "none";

    private static final String ACCENT = "accent";

    private static final String ACCENTUNDER = "accentunder";

    private static final String ALIGN = "align";

    private static final String BEVELLED = "bevelled";

    private static final String CLOSE = "close";

    private static final String COLUMNSALIGN = "columnsalign";

    private static final String COLUMNLINES = "columnlines";

    private static final String COLUMNSPAN = "columnspan";

    private static final String DENOMALIGN = "denomalign";

    private static final String DEPTH = "depth";

    private static final String DIR = "dir";

    private static final String DISPLAY = "display";

    private static final String DISPLAYSTYLE = "displaystyle";

    private static final String ENCODING = "encoding";

    private static final String FENCE = "fence";

    private static final String FRAME = "frame";

    private static final String HEIGHT = "height";

    private static final String HREF = "href";

    private static final String ID = "id";

    private static final String LARGEOP = "largeop";

    private static final String LENGTH = "length";

    private static final String LINETHICKNESS = "linethickness";

    private static final String LSPACE = "lspace";

    private static final String LQUOTE = "lquote";

    private static final String MATHBACKGROUND = "mathbackground";

    private static final String MATHCOLOR = "mathcolor";

    private static final String MATHSIZE = "mathsize";

    private static final String MATHVARIANT = "mathvariant";

    private static final String MAXSIZE = "maxsize";

    private static final String MINSIZE = "minsize";

    private static final String MOVABLELIMITS = "movablelimits";

    private static final String NOTATION = "notation";

    private static final String NUMALIGN = "numalign";

    private static final String OPEN = "open";

    private static final String ROWALIGN = "rowalign";

    private static final String ROWLINES = "rowlines";

    private static final String ROWSPACING = "rowspacing";

    private static final String ROWSPAN = "rowspan";

    private static final String RSPACE = "rspace";

    private static final String RQUOTE = "rquote";

    private static final String SCRIPTLEVEL = "scriptlevel";

    private static final String SCRIPTMINSIZE = "scriptminsize";

    private static final String SCRIPTSIZEMULTIPLIER = "scriptsizemultiplier";

    private static final String SELECTION = "selection";

    private static final String SEPARATOR = "separator";

    private static final String SEPARATORS = "separators";

    private static final String STRETCHY = "stretchy";

    private static final String SUBSCRIPTSHIFT = "subscriptshift";

    private static final String SUPSCRIPTSHIFT = "supscriptshift";

    private static final String SYMMETRIC = "symmetric";

    private static final String VOFFSET = "voffset";

    private static final String WIDTH = "width";

    private static final String XMLNS = "xmlns";

    private final Set<String> safeTags;

    private final Set<String> allTags;

    private final Set<String> allowedAttributes;

    private final Set<String> textIntegrationPoints;

    /**
     * Default constructor.
     */
    public MathMLDefinitions()
    {
        this.safeTags = new HashSet<>(Arrays.asList(
            MATH, MENCLOSE, MERROR, MFENCED, MFRAC, MGLYPH, MI, MLABELEDTR, MMULTISCRIPTS, MN, MO, MOVER, MPADDED,
            MPHANTOM, MROOT, MROW, MS, MSPACE, MSQRT, MSTYLE, MSUB, MSUP, MSUBSUP, MTABLE, MTD, MTEXT, MTR, MUNDER,
            MUNDEROVER));

        this.allTags = new HashSet<>(Arrays.asList(
            MACTION, MALIGNGROUP, MALIGNMARK, MLONGDIV, MSCARRIES, MSCARRY, MSGROUP, MSTACK, MSLINE, MSROW, SEMANTICS,
            ANNOTATION, ANNOTATION_XML, MPRESCRIPTS, NONE));

        this.allTags.addAll(this.safeTags);

        this.allowedAttributes = new HashSet<>(Arrays.asList(
            ACCENT, ACCENTUNDER, ALIGN, BEVELLED, CLOSE, COLUMNSALIGN, COLUMNLINES, COLUMNSPAN, DENOMALIGN, DEPTH, DIR,
            DISPLAY, DISPLAYSTYLE, ENCODING, FENCE, FRAME, HEIGHT, HREF, ID, LARGEOP, LENGTH, LINETHICKNESS, LSPACE,
            LQUOTE, MATHBACKGROUND, MATHCOLOR, MATHSIZE, MATHVARIANT, MAXSIZE, MINSIZE, MOVABLELIMITS, NOTATION,
            NUMALIGN, OPEN, ROWALIGN, ROWLINES, ROWSPACING, ROWSPAN, RSPACE, RQUOTE, SCRIPTLEVEL, SCRIPTMINSIZE,
            SCRIPTSIZEMULTIPLIER, SELECTION, SEPARATOR, SEPARATORS, STRETCHY, SUBSCRIPTSHIFT, SUPSCRIPTSHIFT, SYMMETRIC,
            VOFFSET, WIDTH, XMLNS));

        this.textIntegrationPoints = new HashSet<>(Arrays.asList(
            MI, MO, MN, MS, MTEXT, ANNOTATION_XML));
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
