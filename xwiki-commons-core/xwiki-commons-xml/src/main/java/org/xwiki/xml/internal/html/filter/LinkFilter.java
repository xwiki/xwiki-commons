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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xwiki.component.annotation.Component;
import org.xwiki.text.StringUtils;
import org.xwiki.xml.html.filter.AbstractHTMLFilter;

/**
 * When a link is open in an other window or in an other frame, the loaded page has some restricted access to the
 * parent window. Among other things, it has the ability to redirect it to an other page, which can lead to
 * dangerous phishing attacks.
 *
 * See: https://mathiasbynens.github.io/rel-noopener/ or https://dev.to/phishing or
 * https://jira.xwiki.org/browse/XRENDERING-462
 *
 * To avoid this vulnerability, we automatically add the "noopener" value to the "rel" attribute of the anchor.
 * But because Firefox does not handle it, we also need to add the "noreferer" value.
 *
 * @version $Id$
 * @since 7.4.5
 * @since 8.2.2
 * @since 8.3M2
 */
@Component
@Named("link")
@Singleton
public class LinkFilter extends AbstractHTMLFilter
{
    private static final String NOOPENER = "noopener";

    private static final String NOREFERRER = "noreferrer";

    @Override
    public void filter(Document document, Map<String, String> cleaningParameters)
    {
        // Iterate all links and fix them.
        for (Element link : filterDescendants(document.getDocumentElement(), new String[] { TAG_A })) {
            filter(link);
        }
    }

    private void filter(Element link)
    {
        // Do not handle internal link
        if (!isExternalLink(link.getAttribute(ATTRIBUTE_HREF))) {
            return;
        }

        String target = link.getAttribute(ATTRIBUTE_TARGET);
        // Target can have these values:
        //
        // "_blank" which opens the link in a new window
        // "_self" which opens the link in the same window (default)
        // "_top" and "_parent" which control the top or the parent window of some frame
        // "frame-name" (it could be anything) which opens the link in the frame called "frame-name".
        //
        // "_self", "_top" and "_parent" are the only safe values. So we need to handle any other value...
        if (StringUtils.isNotBlank(target)
                && !"_self".equals(target) && !"_parent".equals(target) && !"_top".equals(target)) {
            List<String> relAttributes = new ArrayList<>();

            // Parse the current values
            String relAttribute = link.getAttribute(ATTRIBUTE_REL);
            if (relAttribute != null) {
                relAttributes.addAll(Arrays.asList(relAttribute.split(" ")));
            }

            // Add the "noopener" attribute
            if (!relAttributes.contains(NOOPENER)) {
                relAttributes.add(NOOPENER);
            }

            // Add the "noreferrer" attribute
            if (!relAttributes.contains(NOREFERRER)) {
                relAttributes.add(NOREFERRER);
            }

            // Serialize the attributes
            if (!relAttributes.isEmpty()) {
                link.setAttribute(ATTRIBUTE_REL, String.join(" ", relAttributes));
            }
        }
    }

    private boolean isExternalLink(String href)
    {
        try {
            URI uri = new URI(href);
            return uri.isAbsolute();
        } catch (URISyntaxException e) {
            return false;
        }
    }

}
