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

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;

/**
 * Provides methods to check if HTML elements and attributes/attribute values are considered safe.
 * <p>
 * This also includes SVG and MathML elements and attributes.
 *
 * @version $Id$
 * @since 14.6RC1
 */
@Role
@Unstable
public interface HTMLElementSanitizer
{
    /**
     * The key under which a hint can be stored that will be used by the default implementation.
     */
    String EXECUTION_CONTEXT_HINT_KEY = "xml.html.htmlElementSanitizerHint";

    /**
     * @param elementName the name of the HTML element
     * @return {@code true} if the given element is allowed in principle (given appropriate attributes)
     */
    boolean isElementAllowed(String elementName);

    /**
     * @param elementName the element for which the attributes shall be checked
     * @param attributeName the attributes to check
     * @param value the value of the attribute
     * @return {@code true} if the attribute with this value is considered safe
     */
    boolean isAttributeAllowed(String elementName, String attributeName, String value);
}
