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
package org.xwiki.xml.internal.html.filter;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xwiki.component.annotation.Component;
import org.xwiki.xml.html.HTMLConstants;
import org.xwiki.xml.html.filter.AbstractHTMLFilter;

/**
 * Filters attributes that are used by many different elements. Writing transformations for these attributes is tedious
 * because tag transformation don't support matching all tags (e.g. using a wildcard character like '*') and so we need
 * to duplicate the transformation for each element.
 * <p>
 * The following changes are made by this filter:
 * <ul>
 * <li>{@code align="value"} is replaced with {@code style="text-align:value"}</li>
 * <li>{@code valign="value"} is replaced with {@code style="vertical-align:value"}</li>
 * </ul>
 * 
 * @version $Id$
 * @since 4.3M1
 */
@Component
@Named("attribute")
@Singleton
public class AttributeFilter extends AbstractHTMLFilter
{
    /**
     * The map between HTML attribute names and the corresponding CSS property name.
     */
    private static final Map<String, String> ATTRIBUTE_TO_CSS_PROPERTY = new LinkedHashMap<>();

    /**
     * The 'vertical-align' CSS property.
     */
    private static final String VERTICAL_ALIGN = "vertical-align";

    static {
        ATTRIBUTE_TO_CSS_PROPERTY.put("align", "text-align");
        ATTRIBUTE_TO_CSS_PROPERTY.put("bgcolor", "background-color");
        ATTRIBUTE_TO_CSS_PROPERTY.put("valign", VERTICAL_ALIGN);
    }

    @Override
    public void filter(Document document, Map<String, String> cleaningParameters)
    {
        NodeList nodeList = document.getElementsByTagName("*");
        for (int i = 0, len = nodeList.getLength(); i < len; i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                filterElement((Element) node);
            }
        }
    }

    private void filterElement(Element element)
    {
        if (element.hasAttributes()) {
            for (String attrName : ATTRIBUTE_TO_CSS_PROPERTY.keySet()) {
                Attr attribute = element.getAttributeNode(attrName);
                if (attribute != null) {
                    filterAttribute(attribute);
                }
            }
        }
    }

    private void filterAttribute(Attr attribute)
    {
        Element element = attribute.getOwnerElement();
        String property = ATTRIBUTE_TO_CSS_PROPERTY.get(attribute.getName());
        String value = attribute.getValue();
        if (HTMLConstants.TAG_IMG.equals(element.getTagName())
            && HTMLConstants.ATTRIBUTE_ALIGN.equals(attribute.getName())) {
            // We need to transform the align attribute differently when it is used on an image element.
            property = "left".equals(value) || "right".equals(value) ? "float" : VERTICAL_ALIGN;
        }
        StringBuilder style = new StringBuilder(element.getAttribute(ATTRIBUTE_STYLE).trim());
        if (style.length() > 0 && style.charAt(style.length() - 1) != ';') {
            style.append(';');
        }
        style.append(property).append(':').append(value);
        element.setAttribute(ATTRIBUTE_STYLE, style.toString());
        element.removeAttributeNode(attribute);
    }
}
