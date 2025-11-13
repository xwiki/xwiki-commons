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
package org.xwiki.store.blob.internal;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.xwiki.store.blob.BlobOption;

/**
 * Utility methods to work with {@link BlobOption blob options}.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
public final class BlobOptionSupport
{
    private static final String MULTIPLE_OPTIONS_ERROR = "Multiple options of type [%s] are not supported.";

    private static final String SEPARATOR = ", ";

    private BlobOptionSupport()
    {
        // Utility class
    }

    /**
     * Finds at most one option of the given type in the provided array of options.
     *
     * @param optionType the type of option to look for
     * @param options the options passed to the blob call
     * @param <T> the option type
     * @return the option instance if present, or {@code null} otherwise
     * @throws IllegalArgumentException if multiple options of the same type are provided
     */
    public static <T extends BlobOption> T findSingleOption(Class<T> optionType, BlobOption... options)
    {
        T result = null;
        if (options == null) {
            return null;
        }
        for (BlobOption option : options) {
            if (optionType.isInstance(option)) {
                if (result != null) {
                    throw new IllegalArgumentException(MULTIPLE_OPTIONS_ERROR.formatted(optionType.getSimpleName()));
                }
                result = optionType.cast(option);
            }
        }
        return result;
    }

    /**
     * Validates that all provided options are of supported types.
     *
     * @param supportedTypes the set of option types that are supported
     * @param options the options to validate
     * @throws IllegalArgumentException if any option is not of a supported type
     */
    public static void validateSupportedOptions(Set<Class<? extends BlobOption>> supportedTypes,
        BlobOption... options)
    {
        if (options == null || options.length == 0) {
            return;
        }

        Set<Class<?>> unsupportedTypes = new HashSet<>();
        for (BlobOption option : options) {
            if (option != null && supportedTypes.stream().noneMatch(type -> type.isInstance(option))) {
                unsupportedTypes.add(option.getClass());
            }
        }

        if (!unsupportedTypes.isEmpty()) {
            String unsupportedNames = unsupportedTypes.stream()
                .map(Class::getSimpleName)
                .collect(Collectors.joining(SEPARATOR));
            String supportedNames = supportedTypes.stream()
                .map(Class::getSimpleName)
                .collect(Collectors.joining(SEPARATOR));
            throw new IllegalArgumentException(
                "Unsupported option types: [%s]. Supported types are: [%s]"
                    .formatted(unsupportedNames, supportedNames));
        }
    }
}
