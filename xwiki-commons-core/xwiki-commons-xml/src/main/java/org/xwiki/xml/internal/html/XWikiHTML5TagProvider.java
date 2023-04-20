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
package org.xwiki.xml.internal.html;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Singleton;

import org.htmlcleaner.BelongsTo;
import org.htmlcleaner.CloseTag;
import org.htmlcleaner.ContentType;
import org.htmlcleaner.Display;
import org.htmlcleaner.Html5TagProvider;
import org.htmlcleaner.TagInfo;
import org.xwiki.component.annotation.Component;
import org.xwiki.xml.html.HTMLConstants;

/**
 * List the tags allowed in HTML5 with custom bug fixes for &lt;style&gt; and &lt;svg&gt;-tags.
 * <p>
 * See <a href="https://sourceforge.net/p/htmlcleaner/bugs/228/">bug 228</a>
 * and <a href="https://sourceforge.net/p/htmlcleaner/bugs/229/">bug 229</a>
 * <p>
 * This class should be removed once these bugs have been fixed.
 *
 * @version $Id$
 * @since 14.0RC1
 */
@Component(roles = XWikiHTML5TagProvider.class)
@Singleton
public class XWikiHTML5TagProvider extends Html5TagProvider
{
    private static final List<String> TAGS_WITH_EXPLICIT_PHRASING_CHILDREN =
        Arrays.asList(HTMLConstants.TAG_EM, HTMLConstants.TAG_STRONG, "small", HTMLConstants.TAG_S, "wbr", "mark",
            "bdi", "time", HTMLConstants.TAG_DATA, HTMLConstants.TAG_CITE, HTMLConstants.TAG_Q, HTMLConstants.TAG_CODE,
            "bdo", "dfn", HTMLConstants.TAG_KBD, HTMLConstants.TAG_ABBR, HTMLConstants.TAG_VAR, "samp", "sub", "sup",
            HTMLConstants.TAG_B, HTMLConstants.TAG_I, HTMLConstants.TAG_U, "rtc", "rt", "rp", "meter", "legend",
            "progress");

    /**
     * Default constructor, applies our bug fixes.
     */
    public XWikiHTML5TagProvider()
    {
        super();

        // Fix https://sourceforge.net/p/htmlcleaner/bugs/229/, style tags are (wrongly) allowed in the HTML body.
        this.getTagInfo(HTMLConstants.TAG_STYLE).setBelongsTo(BelongsTo.HEAD);

        // Fix https://sourceforge.net/p/htmlcleaner/bugs/228/, SVG is not marked as phrasing content and not allowed
        // where phrasing content is allowed. Also fix the same for the math tag.
        for (String tag : List.of(HTMLConstants.TAG_SVG, HTMLConstants.TAG_MATH)) {
            TagInfo tagInfo = this.getTagInfo(tag);
            // Do not close other tags before except for the same tag.
            tagInfo.setMustCloseTags(Collections.singleton(tag));
            // Do not copy other tags.
            tagInfo.setCopyTags(Collections.emptySet());
        }

        // Fix the embed tag which is set to close all kinds of tags before it.
        TagInfo embedTag = this.getTagInfo(HTMLConstants.TAG_EMBED);
        embedTag.setDisplay(Display.any);
        embedTag.setMustCloseTags(Collections.emptySet());
        embedTag.setCopyTags(Collections.emptySet());

        // Allow missing phrasing content tags where HTML5TagProvider explicitly allows phrasing content.
        // Note: unfortunately, we cannot iterate over the tags, otherwise we could have avoided copying this list of
        // tags that have phrasing children set.
        for (String child : List.of(HTMLConstants.TAG_SVG, HTMLConstants.TAG_IMG, HTMLConstants.TAG_DATA, "object",
            "picture", "video", "iframe", HTMLConstants.TAG_EMBED, HTMLConstants.TAG_MATH, HTMLConstants.TAG_Q)) {
            TAGS_WITH_EXPLICIT_PHRASING_CHILDREN.forEach(tag -> allowChild(tag, child));
        }

        // Fix https://jira.xwiki.org/browse/XCOMMONS-2375 / https://sourceforge.net/p/htmlcleaner/bugs/230/, the dl
        // tag doesn't permit div as child even though it is valid HTML.
        allowChild(HTMLConstants.TAG_DL, HTMLConstants.TAG_DIV);

        // While HTMLCleaner declares the template tag as phrasing and flow content, it doesn't contain a tag info
        // for it, so add one.
        TagInfo templateTag = new TagInfo(HTMLConstants.TAG_TEMPLATE, ContentType.all, BelongsTo.HEAD_AND_BODY, false,
            false, false, CloseTag.required, Display.any);
        put(HTMLConstants.TAG_TEMPLATE, templateTag);
    }

    /**
     * @param tagName the tag for which to allow a child
     * @param childName the name of the child tag to add
     */
    private void allowChild(String tagName, String childName)
    {
        this.getTagInfo(tagName).getChildTags().add(childName);
    }
}
