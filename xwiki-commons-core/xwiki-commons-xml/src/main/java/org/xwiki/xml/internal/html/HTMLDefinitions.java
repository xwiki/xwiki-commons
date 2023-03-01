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
 * Provides definitions of safe HTML attributes and tags.
 * <p>
 * Unless otherwise noted, lists of elements and attributes are copied from DOMPurify by Cure53 and other contributors |
 * Released under the Apache license 2.0 and Mozilla Public License 2.0 -
 * <a href="https://github.com/cure53/DOMPurify/blob/main/LICENSE">LICENSE</a>.
 *
 * @version $Id$
 * @since 14.6RC1
 */
@Component(roles = HTMLDefinitions.class)
@Singleton
public class HTMLDefinitions
{
    /**
     * Allowed HTML elements.
     */
    private final Set<String> htmlTags;

    /**
     * Allowed attributes.
     */
    private final Set<String> htmlAttributes;

    /**
     * Default constructor.
     */
    public HTMLDefinitions()
    {
        // Compared to DOMPurify, this disallows form-related tags as they can be dangerous in the context of XWiki.
        this.htmlTags = new HashSet<>(
            Arrays.asList("a", "abbr", "acronym", "address", "area", "article", "aside", "audio", "b", "bdi", "bdo",
                "big", "blink", "blockquote", "body", "br", "canvas", "caption", "center", "cite", "code",
                "col", "colgroup", "content", "data", "datalist", "dd", "decorator", "del", "details", "dfn", "dialog",
                "dir", "div", "dl", "dt", "element", "em", "fieldset", "figcaption", "figure", "font", "footer",
                "h1", "h2", "h3", "h4", "h5", "h6", "head", "header", "hgroup", "hr", "html", "i", "img",
                "ins", "kbd", "label", "legend", "li", "main", "map", "mark", "marquee", "menu", "menuitem", "meter",
                "nav", "nobr", "ol", "optgroup", "option", "output", "p", "picture", "pre", "progress", "q", "rp", "rt",
                "ruby", "s", "samp", "section", "shadow", "small", "source", "spacer", "span", "strike",
                "strong", "style", "sub", "summary", "sup", "table", "tbody", "td", "template", "tfoot",
                "th", "thead", "time", "tr", "track", "tt", "u", "ul", "var", "video", "wbr"));

        // Attributes that are in general allowed. Note that "target" is not generally safe, but XWiki contains code
        // that already adds the necessary attributes to make it safe both in HTMLCleaner and in XHTML rendering.
        this.htmlAttributes = new HashSet<>(
            Arrays.asList("accept", "action", "align", "alt", "autocapitalize", "autocomplete", "autopictureinpicture",
                "autoplay", "background", "bgcolor", "border", "capture", "cellpadding", "cellspacing", "checked",
                "cite", "class", "clear", "color", "cols", "colspan", "controls", "controlslist", "coords",
                "crossorigin", "datetime", "decoding", "default", "dir", "disabled", "disablepictureinpicture",
                "disableremoteplayback", "download", "draggable", "enctype", "enterkeyhint", "face", "for", "headers",
                "height", "hidden", "high", "href", "hreflang", "id", "inputmode", "integrity", "ismap", "kind",
                "label", "lang", "list", "loading", "loop", "low", "max", "maxlength", "media", "method", "min",
                "minlength", "multiple", "muted", "name", "nonce", "noshade", "novalidate", "nowrap", "open", "optimum",
                "pattern", "placeholder", "playsinline", "poster", "preload", "pubdate", "radiogroup", "readonly",
                "rel", "required", "rev", "reversed", "role", "rows", "rowspan", "spellcheck", "scope", "selected",
                "shape", "size", "sizes", "span", "srclang", "start", "src", "srcset", "step", "style", "summary",
                "tabindex", "title", "translate", "type", "usemap", "valign", "value", "width", "xmlns", "slot",
                "target"));
    }

    /**
     * @param tagName the name of the tag to check
     * @return if the tag is considered safe
     */
    public boolean isSafeTag(String tagName)
    {
        return this.htmlTags.contains(tagName);
    }

    /**
     * @param attributeName the name of the attribute to check
     * @return if the attribute is allowed
     */
    public boolean isAllowedAttribute(String attributeName)
    {
        return this.htmlAttributes.contains(attributeName);
    }
}
