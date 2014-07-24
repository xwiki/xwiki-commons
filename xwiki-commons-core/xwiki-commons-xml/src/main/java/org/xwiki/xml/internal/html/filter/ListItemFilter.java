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

import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xwiki.component.annotation.Component;
import org.xwiki.xml.html.filter.AbstractHTMLFilter;

/**
 * If &lt;li&gt; elements have no &lt;ul&gt; or &lt;ol&gt; parent then add a &lt;ul&gt; parent to make it valid.
 *
 * @version $Id$
 * @since 3.2RC1
 */
@Component
@Named("listitem")
@Singleton
public class ListItemFilter extends AbstractHTMLFilter
{
    @Override
    public void filter(Document document, Map<String, String> cleaningParameters)
    {
        // Iterate all lists and fix them.
        for (Element listItem : filterDescendants(document.getDocumentElement(), new String[] { TAG_LI })) {
            Node parent = listItem.getParentNode();
            if (parent != null && (!parent.getNodeName().equalsIgnoreCase(TAG_UL)
                && !parent.getNodeName().equalsIgnoreCase(TAG_OL)))
            {
                // Add a UL parent
                Element newUL = listItem.getOwnerDocument().createElement(TAG_UL);
                parent.replaceChild(newUL, listItem);
                newUL.appendChild(listItem);

                // If the parent is a <p> then remove it. HTMLCleaner will clean "<li></li>" by wrapping it in a
                // paragraph: "<p><li></li></p>". Since this isn't valid XHTML we need to remove the <p>.
                if (parent.getNodeName().equalsIgnoreCase(TAG_P)) {
                    Node parentOfParent = parent.getParentNode();
                    if (parentOfParent != null) {
                        parentOfParent.replaceChild(newUL, parent);
                    }
                }
            }
        }
    }
}
