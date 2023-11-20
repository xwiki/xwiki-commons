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
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Named;
import javax.inject.Singleton;

import org.w3c.dom.Document;
import org.xwiki.component.annotation.Component;
import org.xwiki.xml.html.HTMLConstants;

/**
 * A filter that doesn't filter anything but detects if restricted mode affects the document.
 * <p>
 * Use this filter with restricted mode disabled and retrieve the result from the
 * {@link SanitizerDetectorFilter#ATTRIBUTE_FILTERED} attribute on the document element.
 * </p>
 *
 * @version $Id$
 * @since 15.10RC1
 */
@Component
@Singleton
@Named(SanitizerDetectorFilter.ID)
public class SanitizerDetectorFilter extends SanitizerFilter
{
    /**
     * The attribute on the document element where a boolean string is stored if the document is affected by restricted
     * filtering.
     */
    public static final String ATTRIBUTE_FILTERED = "restrictedFiltering";

    /**
     * The id of this filter.
     */
    public static final String ID = "restrictedFilterDetector";

    @Override
    public void filter(Document document, Map<String, String> cleaningParameters)
    {
        // Use an atomic boolean such that the value can be modified from within a lambda.
        AtomicBoolean filtered = new AtomicBoolean();

        traverseWithNamespace(document.getDocumentElement(), (element, currentNamespace) -> {
            filtered.set(filtered.get()
                // Invalid namespace, i.e., element used in the wrong namespace.
                || currentNamespace == TagInformation.INVALID
                // Element isn't allowed.
                || !this.htmlElementSanitizer.isElementAllowed(element.getTagName())
                // Attribute isn't allowed.
                || getAttributes(element).stream()
                .anyMatch(
                    attr -> !this.htmlElementSanitizer.isAttributeAllowed(element.getTagName(), attr.getName(),
                        attr.getValue())
                ));

            // Directly check for script and style elements as they are removed unconditionally in restricted filtering.
            filtered.set(filtered.get()
                || HTMLConstants.TAG_SCRIPT.equals(element.getTagName())
                || HTMLConstants.TAG_STYLE.equals(element.getTagName())
            );

            // Skip children to make the traversal faster when we found the result.
            return filtered.get();
        }, commentNode -> {
            // Comments are filtered, so any comments also trigger a difference in restricted mode.
            filtered.set(true);
            return true;
        });

        document.getDocumentElement().setAttribute(ATTRIBUTE_FILTERED, String.valueOf(filtered.get()));
    }

}
