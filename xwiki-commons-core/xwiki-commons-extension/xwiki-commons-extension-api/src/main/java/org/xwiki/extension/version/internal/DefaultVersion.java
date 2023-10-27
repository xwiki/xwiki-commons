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
package org.xwiki.extension.version.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.internal.DefaultVersion.Element.ElementType;

/**
 * Default implementation of {@link Version}. Note each repositories generally provide their own implementation based on
 * their own version standard.
 * <p>
 * Based on Maven Resolver logic but also adds Maven SNAPSHOT timestamp handling.
 *
 * @version $Id$
 * @since 4.0M1
 */
public class DefaultVersion implements Version
{
    /**
     * The format of a timestamped SNAPSHOT version.
     * 
     * @since 15.6RC1
     */
    public static final Pattern SNAPSHOT_TIMESTAMP = Pattern.compile("-\\d{8}\\.\\d{6}-\\d+$");

    /**
     * The format of a timestamped SNAPSHOT version.
     * 
     * @since 15.6RC1
     */
    public static final String SNAPSHOT_TIMESTAMP_FORMAT = "yyyyMMdd.HHmmss";

    private static final Pattern LOCAL_SNAPSHOT_VERSION = Pattern.compile("-SNAPSHOT$");

    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    private static final String MAX_INTEGER_STRING = String.valueOf(Integer.MAX_VALUE);

    private static final int MAX_INTEGER_LENGTH = MAX_INTEGER_STRING.length();

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultVersion.class);

    private static final VarHandle ELEMENTS_HANDLE;

    static {
        try {
            ELEMENTS_HANDLE = MethodHandles.lookup()
                .findVarHandle(DefaultVersion.class, "elements", List.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * The original version string representation.
     */
    private String rawVersion;

    private String baseVersion;

    private Version sourceVersion;

    private boolean timestampSNAPSHOT;

    /**
     * The version cut in peaces for easier comparison.
     */
    private List<Element> elements;

    /**
     * @see #getType()
     */
    private Type type = Type.STABLE;

    private transient int hashCode;

    /**
     * Used to parse the string representation of the version.
     *
     * @version $Id$
     */
    static final class Tokenizer
    {
        /**
         * The string representation of the version.
         */
        private final String rawVersion;

        /**
         * The current index in the parsed version.
         */
        private int index;

        /**
         * @see #isNumber()
         */
        private boolean number;

        /**
         * @see #getToken()
         */
        private String token;

        /**
         * @param rawVersion the string representation of the version
         */
        public Tokenizer(String rawVersion)
        {
            this.rawVersion = (rawVersion.length() > 0) ? rawVersion : "0";
        }

        /**
         * @return the token
         */
        public String getToken()
        {
            return this.token;
        }

        /**
         * @return indicate if the token is a number
         */
        public boolean isNumber()
        {
            return this.number;
        }

        /**
         * @return move to the next token
         */
        public boolean next()
        {
            final int n = this.rawVersion.length();
            if (this.index >= n) {
                return false;
            }

            int state = -2;

            int start = this.index;
            int end = n;

            for (; this.index < n; this.index++) {
                char c = this.rawVersion.charAt(this.index);

                if (c == '.' || c == '-') {
                    end = this.index;
                    this.index++;
                    break;
                } else {
                    int digit = Character.digit(c, 10);
                    if (digit >= 0) {
                        if (state == -1) {
                            end = this.index;
                            break;
                        }
                        if (state == 0) {
                            // normalize numbers and strip leading zeros
                            start++;
                        }
                        state = (state > 0 || digit > 0) ? 1 : 0;
                    } else {
                        if (state >= 0) {
                            end = this.index;
                            break;
                        }
                        state = -1;
                    }
                }
            }

            if (start < end) {
                this.token = this.rawVersion.substring(start, end);
                this.number = state >= 0;
            } else {
                this.token = "0";
                this.number = true;
            }

            return true;
        }

        @Override
        public String toString()
        {
            return this.token;
        }
    }

    /**
     * A peace of the version.
     *
     * @version $Id$
     */
    static final class Element implements Comparable<Element>
    {
        /**
         * Message used in the exception produced when one of the {@link ElementType} is unknown.
         */
        private static final String ERROR_UNKNOWNKIND = "Unknown version element kind ";

        /**
         * The kind of element.
         *
         * @version $Id$
         */
        enum ElementType
        {
            /**
             * A known qualifier id.
             */
            QUALIFIER,

            /**
             * An integer.
             */
            INT,

            /**
             * An unknown literal string.
             */
            STRING
        }

        /**
         * The list of known qualifiers.
         */
        private static final Map<String, Integer> QUALIFIERS;

        static {
            QUALIFIERS = new HashMap<>();
            QUALIFIERS.put("alpha", Integer.valueOf(-6));
            QUALIFIERS.put("a", Integer.valueOf(-6));
            QUALIFIERS.put("beta", Integer.valueOf(-5));
            QUALIFIERS.put("b", Integer.valueOf(-5));
            QUALIFIERS.put("milestone", Integer.valueOf(-4));
            QUALIFIERS.put("m", Integer.valueOf(-4));
            QUALIFIERS.put("cr", Integer.valueOf(-3));
            QUALIFIERS.put("rc", Integer.valueOf(-3));
            // -2 is the SNAPSHOT timestamp (we want the undefined SNAPSHOT to represent the current SNAPSHOT so great
            // than a specific SNAPSHOT)
            QUALIFIERS.put("snapshot", Integer.valueOf(-1));
            QUALIFIERS.put("ga", Integer.valueOf(0));
            QUALIFIERS.put("final", Integer.valueOf(0));
            QUALIFIERS.put("", Integer.valueOf(0));
            QUALIFIERS.put("sp", Integer.valueOf(1));
        }

        /**
         * The kind of element.
         */
        private final ElementType elementType;

        /**
         * The value of the element.
         */
        private final Object value;

        /**
         * A secondary value to use when the value is equals.
         */
        private Object secondaryValue;

        /**
         * @see #getVersionType()
         */
        private Type versionType = Type.STABLE;

        private boolean isInteger(String number)
        {
            return number.length() < MAX_INTEGER_LENGTH
                || (number.length() == MAX_INTEGER_LENGTH && MAX_INTEGER_STRING.compareTo(number) >= 0);
        }

        public Element(String token)
        {
            this(ElementType.STRING, token, null);
        }

        public Element(ElementType elementType, Object value, Object secondaryValue)
        {
            this.elementType = elementType;
            this.value = value;
            this.secondaryValue = secondaryValue;
        }

        /**
         * @param tokenizer the token from which to create the version element
         */
        public Element(Tokenizer tokenizer)
        {
            String token = tokenizer.getToken();
            if (tokenizer.isNumber()) {
                if (isInteger(token)) {
                    try {
                        this.elementType = ElementType.INT;
                        this.value = Integer.valueOf(token);
                    } catch (NumberFormatException e) {
                        throw new IllegalStateException(e);
                    }
                } else {
                    this.elementType = ElementType.STRING;
                    this.value = token;
                }
            } else {
                String lowerCaseToken = token.toLowerCase(Locale.ENGLISH);
                Integer qualifier = QUALIFIERS.get(lowerCaseToken);
                if (qualifier != null) {
                    this.elementType = ElementType.QUALIFIER;
                    this.value = qualifier;
                    if (qualifier.intValue() == -1) {
                        this.versionType = Type.SNAPSHOT;
                    } else if (qualifier < 0) {
                        this.versionType = Type.BETA;
                    }
                } else {
                    this.elementType = ElementType.STRING;
                    this.value = lowerCaseToken;
                }
            }
        }

        /**
         * @return indicate of the element is a number
         */
        public boolean isNumber()
        {
            return this.elementType == ElementType.INT || this.elementType == ElementType.QUALIFIER;
        }

        /**
         * @return the type of the version element
         */
        public Type getVersionType()
        {
            return this.versionType;
        }

        @Override
        public int compareTo(Element that)
        {
            int rel;

            if (that == null) {
                // null in this context denotes the pad element (0 or "ga")
                switch (this.elementType) {
                    case STRING:
                        rel = 1;
                        break;
                    case INT:
                    case QUALIFIER:
                        rel = (Integer) this.value;
                        break;
                    default:
                        throw new IllegalStateException(ERROR_UNKNOWNKIND + this.elementType);
                }
            } else {
                rel = this.elementType.compareTo(that.elementType);
                if (rel == 0) {
                    switch (this.elementType) {
                        case INT:
                        case QUALIFIER:
                            rel = (Integer) this.value - (Integer) that.value;
                            break;
                        case STRING:
                            rel = ((String) this.value).compareToIgnoreCase((String) that.value);
                            break;
                        default:
                            throw new IllegalStateException(ERROR_UNKNOWNKIND + this.elementType);
                    }

                    if (rel == 0 && this.secondaryValue != null && that.secondaryValue != null) {
                        rel = this.secondaryValue.toString().compareToIgnoreCase(that.secondaryValue.toString());
                    }
                }
            }

            return rel;
        }

        @Override
        public boolean equals(Object obj)
        {
            return (obj instanceof Element) && compareTo((Element) obj) == 0;
        }

        @Override
        public int hashCode()
        {
            HashCodeBuilder builder = new HashCodeBuilder();

            builder.append(this.value);
            builder.append(this.elementType);

            return builder.toHashCode();
        }

        @Override
        public String toString()
        {
            return this.value.toString();
        }

    }

    /**
     * @param version the version for which to resolve the deploy version
     * @return the deploy version of the passed version
     * @since 15.6RC1
     */
    public static String resolveSNAPSHOT(String version)
    {
        String resolvedVersion = version;
        Matcher snapshotMatcher = LOCAL_SNAPSHOT_VERSION.matcher(resolvedVersion);
        if (snapshotMatcher.find()) {
            SimpleDateFormat formatter = new SimpleDateFormat(SNAPSHOT_TIMESTAMP_FORMAT);
            resolvedVersion =
                resolvedVersion.substring(0, snapshotMatcher.start()) + '-' + formatter.format(new Date()) + "-0";
        }

        return resolvedVersion;
    }

    /**
     * @param version the version to check
     * @return true if the passed version is a wildcard snapshot version
     * @since 15.6RC1
     */
    public static boolean isWildcardSNAPSHOT(Version version)
    {
        return version.getType() == Type.SNAPSHOT && version instanceof DefaultVersion
            && !((DefaultVersion) version).isTimestampSNAPSHOT();
    }

    /**
     * @param rawVersion the original string representation of the version
     */
    public DefaultVersion(String rawVersion)
    {
        setVersion(rawVersion);
    }

    /**
     * Create a new {@link DefaultVersion} by cloning the provided version.
     *
     * @param version the version to copy
     */
    public DefaultVersion(Version version)
    {
        this(version.getValue());
    }

    /**
     * Make sure the version has been parsed.
     */
    private void initElements()
    {
        // Make sure that no loads of, e.g., type, are re-ordered before checking if elements is null as otherwise it
        // could happen that the type is loaded before it has been set by another thread.
        if (ELEMENTS_HANDLE.getAcquire(this) == null) {
            parse();
        }
    }

    /**
     * @param rawVersion the string representation to parse
     */
    private void setVersion(String rawVersion)
    {
        this.rawVersion = rawVersion;
    }

    /**
     * @return true if the version if a timestamp SNAPSHOT version
     * @since 15.6RC1
     */
    public boolean isTimestampSNAPSHOT()
    {
        this.initElements();

        return this.timestampSNAPSHOT;
    }

    /**
     * @return the base version
     * @since 15.6RC1
     */
    public String getBaseVersion()
    {
        this.initElements();

        return this.baseVersion;
    }

    /**
     * Parse the string representation of the version into separated elements. Also initializes the type,
     * sourceVersion (if different from this version), and baseVersion fields and sets the timestampSNAPSHOT flag.
     * <p>
     * All set values are set once to their final value. When the {@code elements} field has been set, all other
     * values are guaranteed to be set as well.
     */
    private void parse()
    {
        List<Element> newElements = new ArrayList<>();

        try {
            Matcher matcher = SNAPSHOT_TIMESTAMP.matcher(this.rawVersion);
            if (matcher.find()) {
                // We need to match special snapshot style timestamp version as a SNAPSHOT and as a single element
                this.timestampSNAPSHOT = true;
                this.baseVersion = this.rawVersion.substring(0, matcher.start());
                this.sourceVersion = new DefaultVersion(this.baseVersion + "-SNAPSHOT");
                parseElements(newElements, this.baseVersion);
                newElements.add(new Element(ElementType.QUALIFIER, -2, this.rawVersion.substring(matcher.start())));

                this.type = Type.SNAPSHOT;
            } else {
                Type calculatedType = parseElements(newElements, this.rawVersion);
                this.type = calculatedType;
                if (calculatedType == Type.SNAPSHOT) {
                    int index = this.rawVersion.lastIndexOf("-SNAPSHOT");
                    if (index > -1) {
                        this.baseVersion = this.rawVersion.substring(0, index);
                    } else {
                        this.baseVersion = this.rawVersion;
                    }
                } else {
                    this.baseVersion = this.rawVersion;
                }
            }

            trimPadding(newElements);
        } catch (Exception e) {
            // Make sure to never fail no matter what
            LOGGER.error("Failed to parse version [" + this.rawVersion + "]", e);
            newElements.add(new Element(this.rawVersion));
        } finally {
            // Ensure that all previous writes are visible to other threads after elements has been written.
            ELEMENTS_HANDLE.setRelease(this, newElements);
        }
    }

    private Type parseElements(List<Element> elements, String versionToParse)
    {
        Type resultType = Type.STABLE;

        for (Tokenizer tokenizer = new Tokenizer(versionToParse); tokenizer.next();) {
            Element element = new Element(tokenizer);
            elements.add(element);
            if (element.getVersionType() != Type.STABLE) {
                resultType = element.getVersionType();
            }
        }

        return resultType;
    }

    /**
     * Remove empty elements.
     *
     * @param elements the list of clean
     */
    private static void trimPadding(List<Element> elements)
    {
        for (ListIterator<Element> it = elements.listIterator(elements.size()); it.hasPrevious();) {
            Element element = it.previous();

            if (element.compareTo(null) == 0) {
                it.remove();
            } else {
                break;
            }
        }
    }

    // Version

    @Override
    public Type getType()
    {
        initElements();

        return this.type;
    }

    @Override
    public String getValue()
    {
        return this.rawVersion;
    }

    @Override
    public Version getSourceVersion()
    {
        initElements();

        return this.sourceVersion != null ? this.sourceVersion : this;
    }

    // Object

    @Override
    public String toString()
    {
        return getValue();
    }

    @Override
    public int hashCode()
    {
        if (this.hashCode == 0) {
            initElements();

            this.hashCode = this.elements.hashCode();
        }

        return this.hashCode;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {
            return true;
        }

        boolean equals;

        if (obj instanceof DefaultVersion) {
            equals = equals((DefaultVersion) obj);
        } else if (obj instanceof Version) {
            equals = equals(new DefaultVersion(((Version) obj).getValue()));
        } else {
            equals = false;
        }

        return equals;
    }

    /**
     * @param version the version
     * @return true if the provided version is equals to this version
     */
    public boolean equals(DefaultVersion version)
    {
        if (this == version) {
            return true;
        }

        if (version == null) {
            return false;
        }

        return compareTo(version) == 0;
    }

    @Override
    public int compareTo(Version version)
    {
        if (version == this) {
            return 0;
        }

        if (version instanceof DefaultVersion) {
            return compareTo((DefaultVersion) version);
        } else {
            return compareTo(new DefaultVersion(version.getValue()));
        }
    }

    /**
     * @param version the version to compare as a String
     * @return a negative integer, zero, or a positive integer as this version is less than, equal to, or greater than
     *         the specified version
     * @since 7.4.2
     * @since 8.0M2
     */
    public int compareTo(String version)
    {
        return compareTo(new DefaultVersion(version));
    }

    /**
     * @param version the version to compare
     * @return a negative integer, zero, or a positive integer as this version is less than, equal to, or greater than
     *         the specified version
     */
    public int compareTo(DefaultVersion version)
    {
        initElements();
        version.initElements();

        final List<Element> otherElements = version.elements;

        boolean number = true;

        int rel;

        for (int index = 0;; index++) {
            if (index >= this.elements.size() && index >= otherElements.size()) {
                return 0;
            } else if (index >= this.elements.size()) {
                return -comparePadding(otherElements, index, null);
            } else if (index >= otherElements.size()) {
                return comparePadding(this.elements, index, null);
            }

            Element thisElement = this.elements.get(index);
            Element thatElement = otherElements.get(index);

            if (thisElement.isNumber() != thatElement.isNumber()) {
                if (number == thisElement.isNumber()) {
                    rel = comparePadding(this.elements, index, Boolean.valueOf(number));
                } else {
                    rel = -comparePadding(otherElements, index, Boolean.valueOf(number));
                }

                break;
            } else {
                rel = thisElement.compareTo(thatElement);
                if (rel != 0) {
                    break;
                }
                number = thisElement.isNumber();
            }
        }

        return rel;
    }

    /**
     * Compare the end of the version with 0.
     *
     * @param elements the elements to compare to 0
     * @param index the index where to start comparing with 0
     * @param number indicate of the previous element is a number
     * @return the comparison result
     */
    private static int comparePadding(List<Element> elements, int index, Boolean number)
    {
        int rel = 0;

        for (Iterator<Element> it = elements.listIterator(index); it.hasNext();) {
            Element element = it.next();
            if (number != null && number.booleanValue() != element.isNumber()) {
                break;
            }

            rel = element.compareTo(null);
            if (rel != 0) {
                break;
            }
        }

        return rel;
    }

    // Serializable

    /**
     * @param out the stream
     * @throws IOException error when serializing the version
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
        out.writeObject(getValue());
    }

    /**
     * @param in the stream
     * @throws IOException error when unserializing the version
     * @throws ClassNotFoundException error when unserializing the version
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        setVersion((String) in.readObject());
    }
}
