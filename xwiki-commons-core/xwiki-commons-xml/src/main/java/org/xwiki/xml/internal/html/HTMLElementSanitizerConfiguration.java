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
package org.xwiki.xml.internal.html;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;

/**
 * Provides methods to easily access the configuration options of {@link org.xwiki.xml.html.HTMLElementSanitizer}.
 *
 * @version $Id$
 * @since 14.6RC1
 */
@Component(roles = HTMLElementSanitizerConfiguration.class)
@Singleton
public class HTMLElementSanitizerConfiguration
{
    private static final String EXTRA_ALLOWED_TAGS_CONFIGURATION = "xml.htmlElementSanitizer.extraAllowedTags";

    private static final String EXTRA_ALLOWED_ATTRIBUTES_CONFIGURATION =
        "xml.htmlElementSanitizer.extraAllowedAttributes";

    private static final String EXTRA_URI_SAFE_ATTRIBUTES_CONFIGURATION =
        "xml.htmlElementSanitizer.extraURISafeAttributes";

    private static final String EXTRA_DATA_URI_TAGS_CONFIGURATION = "xml.htmlElementSanitizer.extraDataUriTags";

    private static final String FORBID_TAGS_CONFIGURATION = "xml.htmlElementSanitizer.forbidTags";

    private static final String FORBID_ATTRIBUTES_CONFIGURATION = "xml.htmlElementSanitizer.forbidAttributes";

    private static final String ALLOW_UNKNOWN_PROTOCOLS_CONFIGURATION =
        "xml.htmlElementSanitizer.allowUnknownProtocols";

    private static final String ALLOWED_URI_REGEXP_CONFIGURATION = "xml.htmlElementSanitizer.allowedUriRegexp";

    @Inject
    @Named("restricted")
    private Provider<ConfigurationSource> configurationSourceProvider;

    private <T> T getValue(String key, Class<T> valueType, T defaultValue)
    {
        ConfigurationSource configurationSource = this.configurationSourceProvider.get();

        T result;

        if (configurationSource != null) {
            result = configurationSource.getProperty(key, valueType, defaultValue);
        } else {
            result = defaultValue;
        }

        return result;
    }

    /**
     * @return The list of additionally allowed tags
     */
    public List<String> getExtraAllowedTags()
    {
        return getValue(EXTRA_ALLOWED_TAGS_CONFIGURATION, List.class, Collections.emptyList());
    }

    /**
     * @return the list of additionally allowed attributes
     */
    public List<String> getExtraAllowedAttributes()
    {
        return getValue(EXTRA_ALLOWED_ATTRIBUTES_CONFIGURATION, List.class, Collections.emptyList());
    }

    /**
     * @return the list of additional tags that are safe for all kinds of URIs
     */
    public List<String> getExtraUriSafeAttributes()
    {
        return getValue(EXTRA_URI_SAFE_ATTRIBUTES_CONFIGURATION, List.class, Collections.emptyList());
    }

    /**
     * @return the list of additional tags whose attributes may have data URIs
     */
    public List<String> getExtraDataUriTags()
    {
        return getValue(EXTRA_DATA_URI_TAGS_CONFIGURATION, List.class, Collections.emptyList());
    }

    /**
     * @return the list of forbidden tags
     */
    public List<String> getForbidTags()
    {
        return getValue(FORBID_TAGS_CONFIGURATION, List.class, Collections.emptyList());
    }

    /**
     * @return the list of forbidden attributes
     */
    public List<String> getForbidAttributes()
    {
        return getValue(FORBID_ATTRIBUTES_CONFIGURATION, List.class, Collections.emptyList());
    }

    /**
     * @return if unknown protocols shall be allowed
     */
    public boolean isAllowUnknownProtocols()
    {
        return getValue(ALLOW_UNKNOWN_PROTOCOLS_CONFIGURATION, Boolean.class, Boolean.TRUE);
    }

    /**
     * @return the regular expression for allowed URIs
     */
    public String getAllowedUriRegexp()
    {
        return getValue(ALLOWED_URI_REGEXP_CONFIGURATION, String.class, null);
    }
}
