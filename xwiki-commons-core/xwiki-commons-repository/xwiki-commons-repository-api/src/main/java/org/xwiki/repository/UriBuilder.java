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
package org.xwiki.repository;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Generate a URI to use to request a REST server.
 * <p>
 * Support javax.ws.rs.Path style URIs.
 *
 * @version $Id$
 * @since 4.2M1
 */
public class UriBuilder implements Cloneable
{
    private String scheme;

    private String host;

    private Integer port;

    private String userInfo;

    private CharSequence path;

    private CharSequence query;

    private String fragment;

    /**
     * Creates a builder initialized from the passed base URI, then appends the passed path to it.
     *
     * @param base the base URI to start from, providing the scheme, host, port, user information, path, query and
     *  fragment; must not be {@code null}
     * @param path the path to append to the base URI (for example {@code /rest/wikis}); slashes at the junction are
     *  normalized so a single {@code /} separates it from the base path; may be {@code null} or empty to append
     *  nothing, and may contain {@code {variable}} placeholders resolved when {@link #build(Object...)} is called
     * @throws IllegalArgumentException if {@code base} is {@code null}
     */
    public UriBuilder(URI base, String path)
    {
        uri(base);
        path(path);
    }

    /**
     * Creates a builder initialized from the passed base URI, then appends the passed path to it.
     *
     * @param base the base URI as a string to parse and start from (for example
     *  {@code http://localhost:8080/xwiki}); must not be {@code null} and must be a valid URI
     * @param path the path to append to the base URI (for example {@code /rest/wikis}); slashes at the junction are
     *  normalized so a single {@code /} separates it from the base path; may be {@code null} or empty to append
     *  nothing, and may contain {@code {variable}} placeholders resolved when {@link #build(Object...)} is called
     * @throws IllegalArgumentException if {@code base} is {@code null} or is not a valid URI
     */
    public UriBuilder(String base, String path)
    {
        try {
            uri(new URI(base));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid base URI [%s]".formatted(base), e);
        }

        path(path);
    }

    private void uri(URI uri) throws IllegalArgumentException
    {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }

        this.scheme = selectNonNull(uri.getScheme(), this.scheme);
        this.host = selectNonNull(uri.getHost(), this.host);
        if (uri.getPort() > 0) {
            this.port = uri.getPort();
        }
        this.userInfo = selectNonNull(uri.getRawUserInfo(), this.userInfo);
        this.path = selectNonNull(uri.getRawPath(), this.path);
        this.query = selectNonNull(uri.getRawQuery(), this.query);
        this.fragment = selectNonNull(uri.getRawFragment(), this.fragment);
    }

    private static <T> T selectNonNull(T value, T fallback)
    {
        return value != null ? value : fallback;
    }

    private void path(String path)
    {
        if (path != null && !path.isEmpty()) {
            StringBuilder stringBuilder;
            if (this.path != null) {
                if (this.path instanceof String) {
                    stringBuilder = new StringBuilder(this.path);
                    this.path = stringBuilder;
                } else {
                    stringBuilder = (StringBuilder) this.path;
                }
            } else {
                stringBuilder = new StringBuilder();
                this.path = stringBuilder;
            }

            appendPath(stringBuilder, path);
        }
    }

    private void appendPath(StringBuilder stringBuilder, String path)
    {
        if (this.path.length() == 0 || this.path.charAt(this.path.length() - 1) != '/') {
            if (path.charAt(0) != '/') {
                stringBuilder.append('/');
            }

            stringBuilder.append(path);
        } else {
            int i = 0;
            for (; i < path.length() && path.charAt(i) == '/'; ++i) {
                // Nothing to do, the for loop already has all the required code
            }

            if (i > 0) {
                stringBuilder.append(path.substring(i));
            } else {
                stringBuilder.append(path);
            }
        }
    }

    /**
     * URL-encodes the passed value using the {@code UTF-8} charset, as done by {@link java.net.URLEncoder}.
     *
     * @param toEncode the value to encode; may be {@code null}
     * @return the URL-encoded value, or {@code null} if the passed value was {@code null}
     */
    public static String encode(String toEncode)
    {
        String result = null;

        if (toEncode != null) {
            try {
                result = java.net.URLEncoder.encode(toEncode, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // Should never happen
            }
        }

        return result;
    }

    /**
     * Adds a query parameter to the URI being built, appending one {@code name=value} pair for each passed value
     * (both name and values are URL-encoded). Successive calls accumulate parameters.
     *
     * @param name the name of the query parameter (for example {@code media})
     * @param values the values to associate with the parameter name, one {@code name=value} pair being added per
     *  value; each value is converted through its {@link Object#toString()}
     * @return this builder, to allow chaining calls
     * @throws IllegalArgumentException if the passed values array is {@code null}
     */
    public UriBuilder queryParam(String name, Object... values) throws IllegalArgumentException
    {
        if (values == null) {
            throw new IllegalArgumentException("The values must not be null");
        }

        String encodedName = encode(name);

        StringBuilder queryBuilder;
        if (this.query == null) {
            queryBuilder = new StringBuilder();
            this.query = queryBuilder;
        } else if (this.query instanceof StringBuilder) {
            queryBuilder = (StringBuilder) this.query;
        } else {
            queryBuilder = new StringBuilder(this.query);
            this.query = queryBuilder;
        }

        for (Object value : values) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append('&');
            }
            queryBuilder.append(encodedName);
            queryBuilder.append('=');
            queryBuilder.append(encode(value.toString()));
        }

        return this;
    }

    /**
     * Builds the final URI, resolving in order each {@code {variable}} placeholder found in the path with the passed
     * values.
     *
     * @param values the values used to resolve the path variables, in the order the {@code {variable}} placeholders
     *  appear in the path; each value is URL-encoded
     * @return the built URI, assembled from the scheme, authority, path, query and fragment
     * @throws IllegalArgumentException if the resulting string is not a valid URI
     */
    public URI build(Object... values)
    {
        final StringBuilder stb = new StringBuilder();

        appendSchemeAndAuthority(stb);
        appendPathValues(stb, values);
        appendQueryAndFragment(stb);

        try {
            return new URI(stb.toString());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Failed to build the URI", e);
        }
    }

    private void appendSchemeAndAuthority(StringBuilder stb)
    {
        if (this.scheme != null) {
            stb.append(this.scheme);
            stb.append("://");
        }
        if (this.userInfo != null) {
            stb.append(this.userInfo);
            stb.append('@');
        }
        if (this.host != null) {
            stb.append(this.host);
        }
        if (this.port != null) {
            stb.append(':');
            stb.append(this.port);
        }
    }

    private void appendPathValues(StringBuilder stb, Object... values)
    {
        String resolvePath = formatPath(values);
        if (resolvePath != null) {
            if (stb.length() > 0 && (resolvePath.length() == 0 || resolvePath.charAt(0) != '/')) {
                stb.append('/');
            }
            stb.append(resolvePath);
        }
    }

    private void appendQueryAndFragment(StringBuilder stb)
    {
        String queryString = this.query != null ? this.query.toString() : null;
        if (queryString != null) {
            stb.append('?');
            stb.append(queryString);
        }
        if (this.fragment != null) {
            stb.append('#');
            stb.append(this.fragment);
        }
    }

    /**
     * Indicates whether the passed character is an <em>unreserved</em> character as defined by
     * <a href="https://www.rfc-editor.org/rfc/rfc3986#section-2.3">RFC 3986</a>, that is a letter, a digit or one
     * of {@code -}, {@code .}, {@code _} and {@code ~}.
     *
     * @param character the character to check
     * @return {@code true} if the passed character is unreserved and therefore does not need to be percent-encoded
     *  in a URI, {@code false} otherwise
     */
    public static boolean isUnreserved(char character)
    {
        return Character.isLetter(character) || Character.isDigit(character) || isUnreservedSymbol(character);
    }

    private static boolean isUnreservedSymbol(char character)
    {
        return character == '-' || character == '.' || character == '_' || character == '~';
    }

    private String formatPath(Object[] values)
    {
        final StringBuilder result = new StringBuilder();

        StringBuilder varBuffer = null;
        char c;
        boolean inVariable = false;
        final int patternLength = this.path.length();
        int valueId = 0;
        for (int i = 0; i < patternLength; i++) {
            c = this.path.charAt(i);

            if (inVariable) {
                if (isUnreserved(c)) {
                    // Append to the variable name
                    varBuffer.append(c);
                } else if (c == '}') {
                    // End of variable detected
                    if (varBuffer.length() == 0) {
                        // TODO: log ?
                    } else {
                        Object varValue = values[valueId++];

                        String varValueString = (varValue == null) ? null : varValue.toString();

                        result.append(encode(varValueString));

                        // Reset the variable name buffer
                        varBuffer = new StringBuilder();
                    }
                    inVariable = false;
                } else {
                    // TODO: log ?
                }
            } else {
                if (c == '{') {
                    inVariable = true;
                    varBuffer = new StringBuilder();
                } else if (c == '}') {
                    // TODO: log ?
                } else {
                    result.append(c);
                }
            }
        }

        return result.toString();
    }

    // Object

    @Override
    public String toString()
    {
        return build().toString();
    }

    @Override
    public UriBuilder clone()
    {
        UriBuilder clone = null;

        try {
            clone = (UriBuilder) super.clone();
        } catch (CloneNotSupportedException e) {
            // Should never happen
        }

        return clone;
    }
}
