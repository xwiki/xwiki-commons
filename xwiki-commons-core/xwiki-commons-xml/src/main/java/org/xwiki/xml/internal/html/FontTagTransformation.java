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

import java.util.HashMap;
import java.util.Map;

import org.htmlcleaner.TagTransformation;
import org.xwiki.xml.html.HTMLConstants;

/**
 * Replaces invalid &lt;font&gt; tags with equivalent &lt;span&gt; tags using inline css rules.
 *
 * @version $Id$
 * @since 14.0RC1
 */
public class FontTagTransformation extends TagTransformation
{
    /**
     * A map holding the translation from 'size' attribute of html font tag to 'font-size' css property.
     */
    private static final Map<String, String> FONT_SIZE_MAP;

    static {
        FONT_SIZE_MAP = new HashMap<>();
        FONT_SIZE_MAP.put("1", "0.6em");
        FONT_SIZE_MAP.put("2", "0.8em");
        FONT_SIZE_MAP.put("3", "1.0em");
        FONT_SIZE_MAP.put("4", "1.2em");
        FONT_SIZE_MAP.put("5", "1.4em");
        FONT_SIZE_MAP.put("6", "1.6em");
        FONT_SIZE_MAP.put("7", "1.8em");
        FONT_SIZE_MAP.put("-3", "0.4em");
        FONT_SIZE_MAP.put("-2", FONT_SIZE_MAP.get("1"));
        FONT_SIZE_MAP.put("-1", FONT_SIZE_MAP.get("2"));
        FONT_SIZE_MAP.put("+1", FONT_SIZE_MAP.get("4"));
        FONT_SIZE_MAP.put("+2", FONT_SIZE_MAP.get("5"));
        FONT_SIZE_MAP.put("+3", FONT_SIZE_MAP.get("6"));
    }

    /**
     * Create a transformation from the &lt;font&gt;-tag to the &lt;span&gt;-tag.
     */
    public FontTagTransformation()
    {
        super(HTMLConstants.TAG_FONT, HTMLConstants.TAG_SPAN, false);
    }

    @Override
    public Map<String, String> applyTagTransformations(Map<String, String> attributes)
    {
        Map<String, String> result = super.applyTagTransformations(attributes);

        StringBuilder builder = new StringBuilder();
        if (attributes.containsKey(HTMLConstants.ATTRIBUTE_FONTCOLOR)) {
            builder.append(String.format("color:%s;", attributes.get(HTMLConstants.ATTRIBUTE_FONTCOLOR)));
        }
        if (attributes.containsKey(HTMLConstants.ATTRIBUTE_FONTFACE)) {
            builder.append(String.format("font-family:%s;", attributes.get(HTMLConstants.ATTRIBUTE_FONTFACE)));
        }
        if (attributes.containsKey(HTMLConstants.ATTRIBUTE_FONTSIZE)) {
            String fontSize = attributes.get(HTMLConstants.ATTRIBUTE_FONTSIZE);
            String fontSizeCss = FONT_SIZE_MAP.get(fontSize);
            fontSizeCss = (fontSizeCss != null) ? fontSizeCss : fontSize;
            builder.append(String.format("font-size:%s;", fontSizeCss));
        }
        if (attributes.containsKey(HTMLConstants.ATTRIBUTE_STYLE)
            && attributes.get(HTMLConstants.ATTRIBUTE_STYLE).trim().length() == 0)
        {
            builder.append(attributes.get(HTMLConstants.ATTRIBUTE_STYLE));
        }

        if (builder.length() > 0) {
            result.put(HTMLConstants.ATTRIBUTE_STYLE, builder.toString());
        }

        return result;
    }
}
