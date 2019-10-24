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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xwiki.component.annotation.Component;
import org.xwiki.xml.html.filter.AbstractHTMLFilter;

/**
 * Get rid of control characters missed by HTMLCleaner.
 *
 * @version $Id$
 * @since 10.11.10
 * @since 11.3.6
 * @since 11.9
 */
// TODO: remove when upgrading to HTMLCleaner 2.23
@Component
@Named("controlcharacters")
@Singleton
public class ControlCharactersFilter extends AbstractHTMLFilter
{
    @Override
    public void filter(Document document, Map<String, String> cleaningParameters)
    {
        filterTextNodes(document);
    }

    private void filterTestNode(Text textNode)
    {
        String text = textNode.getTextContent();
        StringBuilder cleanedText = null;

        int cleanedIndex = 0;
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);

            // Convert invalid XML characters to spaces or the UTF replacement character.
            // Copied from org.htmlcleaner.HtmlTokenizer#readIfNeeded
            if (c >= 1 && c < 32 && c != 10 && c != 13) {
                if (cleanedText == null) {
                    cleanedText = new StringBuilder();
                }

                cleanedText.append(text.substring(cleanedIndex, i));
                cleanedText.append(' ');

                cleanedIndex = i + 1;
            } else if (c == 0) {
                if (cleanedText == null) {
                    cleanedText = new StringBuilder();
                }

                cleanedText.append(text.substring(cleanedIndex, i));
                cleanedText.append('\uFFFD');

                cleanedIndex = i + 1;
            }
        }

        if (cleanedText != null) {
            // Replace the text
            textNode.setData(cleanedText.toString());
        }
    }

    private void filterTextNodes(Node parentNode)
    {
        NodeList children = parentNode.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);

            if (child instanceof Text) {
                filterTestNode((Text) child);
            } else {
                filterTextNodes(child);
            }
        }
    }
}
