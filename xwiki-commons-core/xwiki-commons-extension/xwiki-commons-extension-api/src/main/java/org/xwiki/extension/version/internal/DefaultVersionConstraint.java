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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.xwiki.extension.version.IncompatibleVersionConstraintException;
import org.xwiki.extension.version.InvalidVersionConstraintException;
import org.xwiki.extension.version.InvalidVersionRangeException;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.VersionConstraint;
import org.xwiki.extension.version.VersionRange;
import org.xwiki.extension.version.VersionRangeCollection;

/**
 * Default implementation of {@link VersionConstraint}.
 * <p>
 * Mostly based on AETHER implementation which is itself based on Maven specifications. The main difference is that it
 * can contains a list of ranges OR collection instead of just a OR collection to allow combining several constraints in
 * one.
 * <p>
 * {(,1.0],[2.0,)},{[3.0)}
 *
 * @see org.sonatype.aether.util.version.GenericVersionConstraint
 * @version $Id$
 * @since 4.0M1
 */
public class DefaultVersionConstraint implements VersionConstraint
{
    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The character used to separated version ranges.
     */
    private static final char RANGE_SEPARATOR = ',';

    /**
     * @see #getRanges()
     */
    private List<VersionRangeCollection> ranges;

    /**
     * @see #getVersion()
     */
    private Version version;

    /**
     * @see #getValue()
     */
    private String value;

    private transient int hashCode;

    /**
     * @param rawConstraint the version range to parse
     */
    public DefaultVersionConstraint(String rawConstraint)
    {
        this.value = rawConstraint;
    }

    /**
     * Created a new {@link DefaultVersionConstraint} by cloning the provided version constraint.
     *
     * @param versionConstraint the version constrain to copy
     */
    public DefaultVersionConstraint(VersionConstraint versionConstraint)
    {
        this(versionConstraint.getRanges(), versionConstraint.getVersion());
    }

    /**
     * @param ranges the ranges of versions
     * @param version the recommended version
     */
    public DefaultVersionConstraint(Collection<? extends VersionRangeCollection> ranges, Version version)
    {
        this.version = version;

        setRanges(ranges);
    }

    /**
     * @param version the recommended version
     */
    public DefaultVersionConstraint(Version version)
    {
        this.version = version;

        this.ranges = Collections.emptyList();
    }

    private VersionRangeCollection getStrictVersion(Collection<? extends VersionRangeCollection> ranges)
    {
        for (VersionRangeCollection collection : ranges) {
            if (collection.getRanges().size() == 1) {
                VersionRange range = collection.getRanges().iterator().next();
                if (range instanceof DefaultVersionRange && ((DefaultVersionRange) range).getLowerBound() != null
                    && ((DefaultVersionRange) range).getLowerBound()
                        .equals(((DefaultVersionRange) range).getUpperBound())) {
                    return collection;
                }

            }
        }

        return null;
    }

    private void setRanges(Collection<? extends VersionRangeCollection> ranges)
    {
        if (ranges != null && !ranges.isEmpty()) {
            // Optimize ranges in case there is range collection with a strict version
            VersionRangeCollection strictVersion = getStrictVersion(ranges);
            if (strictVersion != null) {
                this.ranges = Collections.singletonList(strictVersion);
            } else {
                this.ranges = new ArrayList<>(ranges);
            }
        } else {
            this.ranges = Collections.emptyList();
        }
    }

    private void init()
    {
        if (this.ranges == null && this.value != null) {
            // Parse

            List<VersionRangeCollection> newRanges = null;
            try {
                newRanges = parseRanges(this.value);
            } catch (InvalidVersionConstraintException e) {
                // Invalid range syntax, lets use it as version
            }

            // Version

            if (newRanges == null || newRanges.isEmpty()) {
                this.version = new DefaultVersion(this.value);
                this.ranges = Collections.emptyList();
            } else {
                setRanges(newRanges);
            }
        }
    }

    /**
     * @param rawConstraint the constraint to parse
     * @return the list of version ranges
     * @throws InvalidVersionConstraintException invalid constraint range syntax
     */
    private List<VersionRangeCollection> parseRanges(String rawConstraint) throws InvalidVersionConstraintException
    {
        String constraint = rawConstraint;

        List<VersionRangeCollection> newRanges = new ArrayList<>();

        while (VersionUtils.startsWith(constraint, '{')) {
            int index = constraint.indexOf('}');

            if (index < 0) {
                throw new InvalidVersionConstraintException(
                    String.format("Unbounded version range [{%s}]", rawConstraint));
            }

            String range = constraint.substring(1, index);
            try {
                newRanges.add(new DefaultVersionRangeCollection(range));
            } catch (InvalidVersionRangeException e) {
                throw new InvalidVersionConstraintException(
                    String.format("Failed to parse version range [%s] in constraint [%s]", range, rawConstraint), e);
            }

            constraint = constraint.substring(index + 1).trim();

            if (VersionUtils.startsWith(constraint, RANGE_SEPARATOR)) {
                constraint = constraint.substring(1).trim();
            }
        }

        if (!constraint.isEmpty()) {
            if (newRanges.isEmpty()) {
                try {
                    newRanges.add(new DefaultVersionRangeCollection(constraint));
                } catch (InvalidVersionRangeException e) {
                    throw new InvalidVersionConstraintException(
                        String.format("Failed to parse version range [{%s}]", constraint), e);
                }
            } else {
                throw new InvalidVersionConstraintException(String
                    .format("Invalid version range [{%s}], expected [ or ( but got [{%s}]", rawConstraint, constraint));
            }
        }

        return newRanges;
    }

    private List<VersionRangeCollection> getRangesInternal()
    {
        init();

        return this.ranges;
    }

    @Override
    public Collection<VersionRangeCollection> getRanges()
    {
        return getRangesInternal();
    }

    @Override
    public Version getVersion()
    {
        init();

        return this.version;
    }

    @Override
    public boolean containsVersion(Version version)
    {
        if (getRangesInternal().isEmpty()) {
            return getVersion() != null && getVersion().equals(version);
        } else {
            for (VersionRange range : getRangesInternal()) {
                if (!range.containsVersion(version)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public boolean isCompatible(Version version)
    {
        boolean compatible;
        if (getVersion() == null) {
            compatible = containsVersion(version);
        } else {
            compatible = version.compareTo(getVersion()) >= 0;
        }

        return compatible;
    }

    @Override
    public VersionConstraint merge(VersionConstraint versionConstraint) throws IncompatibleVersionConstraintException
    {
        if (equals(versionConstraint)) {
            return this;
        } else {
            VersionConstraint mergedConstraint = mergeVersions(versionConstraint);

            return mergedConstraint == null ? mergeRanges(versionConstraint) : mergedConstraint;
        }
    }

    /**
     * @param versionConstraint the version constraint to merge
     * @return the version constraint with the upper version or null if both are ranges based
     */
    private VersionConstraint mergeVersions(VersionConstraint versionConstraint)
    {
        if (getVersion() != null && versionConstraint.getVersion() != null) {
            return getVersion().compareTo(versionConstraint.getVersion()) >= 0 ? this : versionConstraint;
        }

        return null;
    }

    /**
     * Create a new {@link DefaultVersionConstraint} instance which is the combination of the provided version ranges
     * and this version ranges.
     *
     * @param otherConstraint the version ranges to merge with this version ranges
     * @return the new {@link DefaultVersionConstraint}
     * @throws IncompatibleVersionConstraintException the provided version and version ranges are not compatible with
     *             this version constraint
     */
    private DefaultVersionConstraint mergeRanges(VersionConstraint otherConstraint)
        throws IncompatibleVersionConstraintException
    {
        Collection<VersionRangeCollection> resolvedRanges = resolveRanges(this);
        Collection<VersionRangeCollection> otherResolvedRanges = resolveRanges(otherConstraint);

        return mergeRanges(resolvedRanges, otherResolvedRanges);
    }

    private DefaultVersionConstraint mergeRanges(Collection<VersionRangeCollection> ranges1,
        Collection<VersionRangeCollection> ranges2) throws IncompatibleVersionConstraintException
    {
        // Validate

        validateCompatibility(ranges1, ranges2);

        // Merge

        Collection<VersionRangeCollection> newRanges = new ArrayList<>(ranges1.size() + ranges2.size());
        newRanges.addAll(ranges1);
        newRanges.addAll(ranges2);

        return new DefaultVersionConstraint(newRanges, null);
    }

    private Collection<VersionRangeCollection> resolveRanges(VersionConstraint constraint)
    {
        Collection<VersionRangeCollection> resolvedRanges = constraint.getRanges();

        // If no range is provided convert the recommended version into a range
        if (constraint.getVersion() != null) {
            resolvedRanges = Collections.singleton(new DefaultVersionRangeCollection(
                Collections.singleton(new DefaultVersionRange(constraint.getVersion(), true, null, true))));
        }

        return resolvedRanges;
    }

    private void validateCompatibility(Collection<VersionRangeCollection> ranges1,
        Collection<VersionRangeCollection> ranges2) throws IncompatibleVersionConstraintException
    {
        for (VersionRange range2 : ranges2) {
            for (VersionRange range1 : ranges1) {
                if (!range1.isCompatible(range2)) {
                    throw new IncompatibleVersionConstraintException(
                        "Ranges [" + range1 + "] and [" + range2 + "] are incompatibles");
                }
            }
        }
    }

    @Override
    public String getValue()
    {
        if (this.value == null) {
            StringBuilder builder = new StringBuilder();

            if (getVersion() != null) {
                builder.append(getVersion());
            } else {
                if (getRangesInternal().size() == 1) {
                    builder.append(getRangesInternal().get(0).getValue());
                } else {
                    for (VersionRange range : getRangesInternal()) {
                        if (builder.length() > 0) {
                            builder.append(RANGE_SEPARATOR);
                        }
                        builder.append('{');
                        builder.append(range.getValue());
                        builder.append('}');
                    }
                }
            }

            this.value = builder.toString();
        }

        return this.value;
    }

    // Object

    @Override
    public String toString()
    {
        return getValue();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof VersionConstraint)) {
            return false;
        }

        init();

        VersionConstraint versionConstraint = (VersionConstraint) obj;

        return getRangesInternal().equals(versionConstraint.getRanges())
            && Objects.equals(getVersion(), versionConstraint.getVersion());
    }

    @Override
    public int hashCode()
    {
        if (this.hashCode == 0) {
            HashCodeBuilder builder = new HashCodeBuilder(17, 31);
            builder.append(getRangesInternal());
            builder.append(getVersion());

            this.hashCode = builder.toHashCode();
        }

        return this.hashCode;
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
        this.value = (String) in.readObject();
    }
}
