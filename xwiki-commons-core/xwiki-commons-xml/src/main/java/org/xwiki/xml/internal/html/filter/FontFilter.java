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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.xerces.util.DOMUtil;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xwiki.component.annotation.Component;
import org.xwiki.xml.html.filter.AbstractHTMLFilter;
import org.xwiki.xml.internal.html.FontTagTransformation;

/**
 * Replaces invalid &lt;font&gt; tags with equivalent &lt;span&gt; tags using inline css rules.
 *
 * @version $Id$
 * @since 1.8RC2
 */
@Component
@Named("font")
@Singleton
public class FontFilter extends AbstractHTMLFilter
{
    /**
     * {@inheritDoc}
     *
     * <p>The {@link FontFilter} does not use any cleaningParameters passed in.</p>
     */
    @Override
    public void filter(Document document, Map<String, String> cleaningParameters)
    {
        List<Element> fontTags = filterDescendants(document.getDocumentElement(), new String[] { TAG_FONT });
        for (Element fontTag : fontTags) {
            Element span = document.createElement(TAG_SPAN);
            moveChildren(fontTag, span);

            Map<String, String> attributes =
                Arrays.stream(DOMUtil.getAttrs(fontTag)).collect(Collectors.toMap(Attr::getName,
                Attr::getValue));

            (new FontTagTransformation()).applyTagTransformations(attributes).forEach(span::setAttribute);

            fontTag.getParentNode().insertBefore(span, fontTag);
            fontTag.getParentNode().removeChild(fontTag);
        }
    }
}
