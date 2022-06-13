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

import java.util.List;
import java.util.Map;

import org.xwiki.stability.Unstable;
import org.xwiki.xml.html.filter.HTMLFilter;

/**
 * Represents a configuration for the {@link HTMLCleaner} and allows to configure the cleaning parameters and cleaning
 * filters to apply.
 *
 * @version $Id$
 * @since 1.8.1
 */
public interface HTMLCleanerConfiguration
{
    /**
     * Cleaning property identifier which decides if parsing should omit/keep namespace information.
     */
    String NAMESPACES_AWARE = "namespacesAware";

    /**
     * Cleaning in restricted mode ({@code &lt;script&gt;} and {@code &lt;style&gt;} should not be allowed for example).
     */
    String RESTRICTED = "restricted";

    /**
     * Cleaning property that controls whether the output HTML should use character references rather than entity
     * references. This is useful if you need to parse the clean HTML as XML later.
     */
    String USE_CHARACTER_REFERENCES = "useCharacterReferences";
    /**
     * Cleaning property that transform special HTML entities to be recognized by an XML parser.
     * @since 12.3RC1
     */
    String TRANSLATE_SPECIAL_ENTITIES = "translateSpecialEntities";

    /**
     * The HTML (major) version. Should be "5" for HTML5 and "4" (default) otherwise for the default implementation.
     * @since 14.0RC1
     */
    @Unstable
    String HTML_VERSION = "htmlVersion";

    /**
     * @return the ordered list of filters to use for cleaning the HTML content
     */
    List<HTMLFilter> getFilters();

    /**
     * @param filters the ordered list of filters to use for cleaning the HTML content
     */
    void setFilters(List<HTMLFilter> filters);

    /**
     * @return the list of cleaning parameters that will be used to clean the HTML content. Note that these parameters
     *         are implementation dependent and thus depend on the HTML cleaner implementation used.
     */
    Map<String, String> getParameters();

    /**
     * @param cleaningParameters the list of cleaning parameters that will be used to clean the HTML content
     */
    void setParameters(Map<String, String> cleaningParameters);
}
