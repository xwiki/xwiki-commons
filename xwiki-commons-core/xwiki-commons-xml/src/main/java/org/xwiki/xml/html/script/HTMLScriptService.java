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
package org.xwiki.xml.html.script;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;
import org.xwiki.xml.html.HTMLElementSanitizer;
import org.xwiki.xml.internal.html.SecureHTMLElementSanitizer;

/**
 * Script service dedicated to perform operations on html scripts.
 *
 * @version $Id$
 * @since 14.10.4
 * @since 15.0RC1
 */
@Unstable
@Component
@Named("html")
@Singleton
public class HTMLScriptService implements ScriptService
{
    @Inject
    private HTMLElementSanitizer sanitizer;

    /**
     * Check if the given html element can be considered safe or not.
     * The definition of safe is based on the implementation of the configured {@link HTMLElementSanitizer}.
     * By default it should use {@link SecureHTMLElementSanitizer} which itself relies partly on the configuration of
     * the wiki (see {@link org.xwiki.xml.internal.html.HTMLElementSanitizerConfiguration}).
     *
     * @param elementName the name of the HTML element
     * @return {@code true} if the given element is allowed in principle (given appropriate attributes)
     */
    public boolean isElementSafe(String elementName)
    {
        return this.sanitizer.isElementAllowed(elementName);
    }

    /**
     * Check if the attribute and its value can be considered safe or not.
     * The definition of safe is based on the implementation of the configured {@link HTMLElementSanitizer}.
     * By default it should use {@link SecureHTMLElementSanitizer} which itself relies partly on the configuration of
     * the wiki (see {@link org.xwiki.xml.internal.html.HTMLElementSanitizerConfiguration}).
     *
     * @param elementName the element for which the attributes shall be checked
     * @param attributeName the attributes to check
     * @param value the value of the attribute
     * @return {@code true} if the attribute with this value is considered safe
     */
    public boolean isAttributeSafe(String elementName, String attributeName, String value)
    {
        return this.sanitizer.isAttributeAllowed(elementName, attributeName, value);
    }
}
