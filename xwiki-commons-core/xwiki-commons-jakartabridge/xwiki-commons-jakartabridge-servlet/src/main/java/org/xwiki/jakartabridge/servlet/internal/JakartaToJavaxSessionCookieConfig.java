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
package org.xwiki.jakartabridge.servlet.internal;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import jakarta.servlet.SessionCookieConfig;

import org.xwiki.jakartabridge.internal.AbstractJakartaToJavaxWrapper;

/**
 * @version $Id$
 * @since 17.0.0RC1
 */
public class JakartaToJavaxSessionCookieConfig extends AbstractJakartaToJavaxWrapper<javax.servlet.SessionCookieConfig>
    implements SessionCookieConfig
{
    private static final String ATTRIBUTE_DOMAIN = "Domain";

    private static final String ATTRIBUTE_PATH = "Path";

    private static final String ATTRIBUTE_COMMENT = "Comment";

    private static final String ATTRIBUTE_HTTP_ONLY = "HttpOnly";

    private static final String ATTRIBUTE_SECURE = "Secure";

    private static final String ATTRIBUTE_MAX_AGE = "Max-Age";

    /**
     * The attributes which don't have any equivalent in Servlet 3.1.
     */
    private final Map<String, String> customAttributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    /**
     * @param javax the wrapped version
     */
    public JakartaToJavaxSessionCookieConfig(javax.servlet.SessionCookieConfig javax)
    {
        super(javax);
    }

    @Override
    public void setName(String name)
    {
        this.javax.setName(name);
    }

    @Override
    public String getName()
    {
        return this.javax.getName();
    }

    @Override
    public void setDomain(String domain)
    {
        this.javax.setDomain(domain);
    }

    @Override
    public String getDomain()
    {
        return this.javax.getDomain();
    }

    @Override
    public void setPath(String path)
    {
        this.javax.setPath(path);
    }

    @Override
    public String getPath()
    {
        return this.javax.getPath();
    }

    @Override
    public void setComment(String comment)
    {
        this.javax.setComment(comment);
    }

    @Override
    public String getComment()
    {
        return this.javax.getComment();
    }

    @Override
    public void setHttpOnly(boolean httpOnly)
    {
        this.javax.setHttpOnly(httpOnly);
    }

    @Override
    public boolean isHttpOnly()
    {
        return this.javax.isHttpOnly();
    }

    @Override
    public void setSecure(boolean secure)
    {
        this.javax.setSecure(secure);
    }

    @Override
    public boolean isSecure()
    {
        return this.javax.isSecure();
    }

    @Override
    public void setMaxAge(int maxAge)
    {
        this.javax.setMaxAge(maxAge);
    }

    @Override
    public int getMaxAge()
    {
        return this.javax.getMaxAge();
    }

    @Override
    public void setAttribute(String name, String value)
    {
        // Servlet 3.1 only supports a fixed set of attributes
        if (!setStandardAttribute(name, value)) {
            if (value != null) {
                this.customAttributes.put(name, value);
            } else {
                this.customAttributes.remove(name);
            }
        }
    }

    private boolean setStandardAttribute(String name, String value)
    {
        if (ATTRIBUTE_DOMAIN.equalsIgnoreCase(name)) {
            setDomain(value);
        } else if (ATTRIBUTE_PATH.equalsIgnoreCase(name)) {
            setPath(value);
        } else if (ATTRIBUTE_COMMENT.equalsIgnoreCase(name)) {
            setComment(value);
        } else if (ATTRIBUTE_HTTP_ONLY.equalsIgnoreCase(name)) {
            setHttpOnly(toBoolean(value));
        } else if (ATTRIBUTE_SECURE.equalsIgnoreCase(name)) {
            setSecure(toBoolean(value));
        } else if (ATTRIBUTE_MAX_AGE.equalsIgnoreCase(name)) {
            setMaxAge(value != null ? Integer.parseInt(value) : -1);
        } else {
            return false;
        }

        return true;
    }

    private static boolean toBoolean(String value)
    {
        // A boolean cookie attribute is enabled when it's present without value
        return value != null && (value.isEmpty() || Boolean.parseBoolean(value));
    }

    @Override
    public String getAttribute(String name)
    {
        return getAttributes().get(name);
    }

    @Override
    public Map<String, String> getAttributes()
    {
        Map<String, String> attributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        putAttribute(attributes, ATTRIBUTE_DOMAIN, getDomain());
        putAttribute(attributes, ATTRIBUTE_PATH, getPath());
        putAttribute(attributes, ATTRIBUTE_COMMENT, getComment());
        if (isHttpOnly()) {
            attributes.put(ATTRIBUTE_HTTP_ONLY, "");
        }
        if (isSecure()) {
            attributes.put(ATTRIBUTE_SECURE, "");
        }
        if (getMaxAge() != -1) {
            attributes.put(ATTRIBUTE_MAX_AGE, String.valueOf(getMaxAge()));
        }
        attributes.putAll(this.customAttributes);

        return Collections.unmodifiableMap(attributes);
    }

    private void putAttribute(Map<String, String> attributes, String name, String value)
    {
        if (value != null) {
            attributes.put(name, value);
        }
    }
}
