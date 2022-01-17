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
package org.xwiki.xml.html;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.htmlcleaner.BelongsTo;
import org.htmlcleaner.Html5TagProvider;
import org.htmlcleaner.TagInfo;
import org.xwiki.stability.Unstable;

/**
 * List the tags allowed in HTML5 with custom bug fixes for &lt;style&gt; and &lt;svg&gt;-tags.
 *
 * See https://sourceforge.net/p/htmlcleaner/bugs/228/ and https://sourceforge.net/p/htmlcleaner/bugs/229/
 *
 * This class should be removed once these bugs have been fixed.
 *
 * @version $Id$
 * @since 14.0RC1
 */
@Unstable
public class XWikiHTML5TagProvider extends Html5TagProvider
{
    private static final List<String> TAGS_WITH_EXPLICIT_PHRASING_CHILDREN =
        Arrays.asList(HTMLConstants.TAG_EM, HTMLConstants.TAG_STRONG, "small", HTMLConstants.TAG_S, "wbr", "mark",
            "bdi", "time", "data", HTMLConstants.TAG_CITE, HTMLConstants.TAG_Q, HTMLConstants.TAG_CODE, "bdo", "dfn",
            HTMLConstants.TAG_KBD, HTMLConstants.TAG_ABBR, HTMLConstants.TAG_VAR, "samp", "sub", "sup",
            HTMLConstants.TAG_B, HTMLConstants.TAG_I, HTMLConstants.TAG_U, "rtc", "rt", "rp", "meter", "legend",
            "progress");

    /**
     * Default constructor, applies our bug fixes.
     */
    public XWikiHTML5TagProvider()
    {
        super();

        // Fix https://sourceforge.net/p/htmlcleaner/bugs/229/.
        this.getTagInfo(HTMLConstants.TAG_STYLE).setBelongsTo(BelongsTo.HEAD);

        // Fix https://sourceforge.net/p/htmlcleaner/bugs/228/.
        TagInfo svgTag = this.getTagInfo(HTMLConstants.TAG_SVG);
        // Do not close other tags before SVG apart from svg.
        svgTag.setMustCloseTags(Collections.singleton(HTMLConstants.TAG_SVG));
        // Do not copy other tags inside SVG.
        svgTag.setCopyTags(Collections.emptySet());

        // Allow the SVG tag as child everywhere, where HTML5TagProvider explicitly allows phrasing content.
        // Note: unfortunately, we cannot iterate over the tags, otherwise we could have avoided copying this list.
        TAGS_WITH_EXPLICIT_PHRASING_CHILDREN.forEach(this::allowSVGChild);
    }

    /**
     * @param tagName Tag for which the &lt;svg&gt;-tag shall be added to the allowed children.
     */
    private void allowSVGChild(String tagName)
    {
        this.getTagInfo(tagName).getChildTags().add(HTMLConstants.TAG_SVG);
    }
}
