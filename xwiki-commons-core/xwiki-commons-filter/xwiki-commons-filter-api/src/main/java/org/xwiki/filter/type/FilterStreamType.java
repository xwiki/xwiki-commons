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
package org.xwiki.filter.type;

import java.util.Objects;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Combination of supported system and their data types.
 *
 * @version $Id$
 * @since 6.2M1
 */
public class FilterStreamType implements Comparable<FilterStreamType>
{
    /**
     * Data format identifier for XML.
     * <p>
     * Main content is based on XML.
     */
    public static final String DATA_XML = "xml";

    /**
     * Data format identifier for XAR.
     * <p>
     * Main content is based on XAR format.
     */

    public static final String DATA_XAR = "xar";

    /**
     * Data format identifier for TEXT.
     * <p>
     * Main content is textual content which does not belong to another well know and more specific textual content.
     * 
     * @since 9.5RC1
     */
    public static final String DATA_TEXT = "text";

    /**
     * Generic WIKI XML Syntax.
     */
    public static final FilterStreamType FILTER_XML = new FilterStreamType(SystemType.FILTER, DATA_XML);

    /**
     * The XAR format in version 1.1.
     *
     * @since 6.2M1
     */
    public static final FilterStreamType XWIKI_XAR_11 = new FilterStreamType(SystemType.XWIKI, DATA_XAR, "1.1");

    /**
     * The XAR format in version 1.2.
     *
     * @since 7.2M1
     */
    public static final FilterStreamType XWIKI_XAR_12 = new FilterStreamType(SystemType.XWIKI, DATA_XAR, "1.2");

    /**
     * The XAR format in version 1.3.
     *
     * @since 9.0RC1
     */
    public static final FilterStreamType XWIKI_XAR_13 = new FilterStreamType(SystemType.XWIKI, DATA_XAR, "1.3");

    /**
     * The XAR format in version 1.4.
     *
     * @since 14.0RC1
     */
    public static final FilterStreamType XWIKI_XAR_14 = new FilterStreamType(SystemType.XWIKI, DATA_XAR, "1.4");

    /**
     * The XAR format in version 1.5.
     *
     * @since 14.0RC1
     */
    public static final FilterStreamType XWIKI_XAR_15 = new FilterStreamType(SystemType.XWIKI, DATA_XAR, "1.5");

    /**
     * The XAR format in the current version.
     *
     * @since 7.2M1
     */
    public static final FilterStreamType XWIKI_XAR_CURRENT = XWIKI_XAR_15;

    /**
     * The database stream based on oldcore APIs.
     */
    public static final FilterStreamType XWIKI_INSTANCE = new FilterStreamType(SystemType.XWIKI, "instance");

    /**
     * The Confluence XML format.
     */
    public static final FilterStreamType CONFLUENCE_XML = new FilterStreamType(SystemType.CONFLUENCE, DATA_XML);

    /**
     * The MediaWiki XML format.
     */
    public static final FilterStreamType MEDIAWIKI_XML = new FilterStreamType(SystemType.MEDIAWIKI, DATA_XML);

    /**
     * The DokuWiki data format.
     * 
     * @since 9.5RC1
     */
    public static final FilterStreamType DOKUWIKI_TEXT = new FilterStreamType(SystemType.DOKUWIKI, DATA_TEXT);

    /**
     * Wiki type.
     */
    private SystemType type;

    /**
     * Export data format.
     */
    private String dataFormat;

    /**
     * The version.
     */
    private String version;

    /**
     * @param type the type of Wiki
     * @param dataFormat the export data format
     */
    public FilterStreamType(SystemType type, String dataFormat)
    {
        this(type, dataFormat, null);
    }

    /**
     * @param type the type of Wiki
     * @param dataFormat the export data format
     * @param version the version
     */
    public FilterStreamType(SystemType type, String dataFormat, String version)
    {
        this.type = type;
        this.dataFormat = dataFormat != null ? dataFormat.toLowerCase() : null;
        this.version = version;
    }

    /**
     * @return the wiki
     */
    public SystemType getType()
    {
        return this.type;
    }

    /**
     * @return the export data format
     */
    public String getDataFormat()
    {
        return this.dataFormat;
    }

    /**
     * @return the version
     */
    public String getVersion()
    {
        return this.version;
    }

    /**
     * @return a {@link String} representation of the {@link FilterStreamType}
     */
    public String serialize()
    {
        StringBuilder builder = new StringBuilder();

        builder.append(getType().serialize());

        if (getDataFormat() != null) {
            builder.append('+');
            builder.append(getDataFormat());
        }

        if (getVersion() != null) {
            builder.append('/');
            builder.append(getVersion());
        }

        return builder.toString();
    }

    /**
     * Create a new {@link FilterStreamType} from a {@link String}.
     *
     * @param str the {@link String} to parse
     * @return a {@link FilterStreamType}
     */
    public static FilterStreamType unserialize(String str)
    {
        if (str == null) {
            return null;
        }

        String wikiType = str;
        String data = null;
        String version = null;

        // Version

        int versionIndex = str.lastIndexOf('/');

        if (versionIndex == 0) {
            throw new IllegalArgumentException("'/' is invalid as first character: " + str);
        }

        if (versionIndex != -1) {
            version = wikiType.substring(versionIndex + 1);
            wikiType = wikiType.substring(0, versionIndex);
        }

        // Data

        int dataIndex = str.indexOf('+');

        if (dataIndex == 0) {
            throw new IllegalArgumentException("'+' is invalid as first character: " + str);
        }

        if (dataIndex != -1) {
            data = wikiType.substring(dataIndex + 1);
            wikiType = wikiType.substring(0, dataIndex);
        }

        return new FilterStreamType(SystemType.unserialize(wikiType), data, version);
    }

    @Override
    public String toString()
    {
        return serialize();
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(getType()).append(getDataFormat()).append(getVersion()).toHashCode();
    }

    @Override
    public boolean equals(Object object)
    {
        boolean result;

        if (this == object) {
            result = true;
        } else {
            if (object instanceof FilterStreamType) {
                result = Objects.equals(getType(), ((FilterStreamType) object).getType())
                    && Objects.equals(getDataFormat(), ((FilterStreamType) object).getDataFormat())
                    && Objects.equals(getVersion(), ((FilterStreamType) object).getVersion());
            } else {
                result = false;
            }
        }

        return result;
    }

    @Override
    public int compareTo(FilterStreamType o)
    {
        CompareToBuilder builder = new CompareToBuilder();

        builder.append(getType().toString(), o.getType().toString());
        builder.append(getDataFormat(), o.getDataFormat());
        builder.append(getVersion(), o.getVersion());

        return builder.toComparison();
    }
}
