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

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.xwiki.store.blob.BlobOption;
import org.xwiki.store.blob.BlobStoreException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link BlobOptionSupport}.
 *
 * @version $Id$
 */
class BlobOptionSupportTest
{
    private static final String MULTIPLE_OPTIONS_ERROR = "Multiple options of type [OptionA] are not supported.";
    private static final String UNSUPPORTED_OPTION_ERROR = "Unsupported option types: [OptionB]";
    private static final String SUPPORTED_TYPES_ERROR = "Supported types are: [OptionA]";

    // Test option implementations
    private static final class OptionA implements BlobOption
    {
        @Override
        public String getDescription()
        {
            return "OptionA description";
        }
    }

    private static final class OptionB implements BlobOption
    {
        @Override
        public String getDescription()
        {
            return "OptionB description";
        }
    }

    private static final class OptionC implements BlobOption
    {
        @Override
        public String getDescription()
        {
            return "OptionC description";
        }
    }

    @Test
    void findSingleOptionWhenPresent() throws Exception
    {
        OptionA optionA = new OptionA();
        OptionB optionB = new OptionB();

        OptionA result = BlobOptionSupport.findSingleOption(OptionA.class, optionA, optionB);

        assertEquals(optionA, result);
    }

    @Test
    void findSingleOptionWhenNotPresent() throws Exception
    {
        OptionA optionA = new OptionA();
        OptionB optionB = new OptionB();

        assertNull(BlobOptionSupport.findSingleOption(OptionC.class, optionA, optionB));
    }

    @Test
    void findSingleOptionWithNullArray() throws Exception
    {
        assertNull(BlobOptionSupport.findSingleOption(OptionA.class, (BlobOption[]) null));
    }

    @Test
    void findSingleOptionThrowsExceptionForMultipleOptions()
    {
        OptionA optionA1 = new OptionA();
        OptionA optionA2 = new OptionA();

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> BlobOptionSupport.findSingleOption(OptionA.class, optionA1, optionA2));

        assertEquals(MULTIPLE_OPTIONS_ERROR, exception.getMessage());
    }

    @Test
    void findSingleOptionIgnoresNullOptions() throws Exception
    {
        OptionA optionA = new OptionA();

        OptionA result = BlobOptionSupport.findSingleOption(OptionA.class, null, optionA, null);

        assertEquals(optionA, result);
    }

    @Test
    void validateSupportedOptionsWithSupportedTypes()
    {
        Set<Class<? extends BlobOption>> supportedTypes = Set.of(OptionA.class, OptionB.class);
        OptionA optionA = new OptionA();
        OptionB optionB = new OptionB();

        assertDoesNotThrow(() -> BlobOptionSupport.validateSupportedOptions(supportedTypes, optionA, optionB));
    }

    @Test
    void validateSupportedOptionsWithNullArray()
    {
        Set<Class<? extends BlobOption>> supportedTypes = Set.of(OptionA.class);

        assertDoesNotThrow(() -> BlobOptionSupport.validateSupportedOptions(supportedTypes, (BlobOption[]) null));
    }

    @Test
    void validateSupportedOptionsWithEmptyArray() throws Exception
    {
        Set<Class<? extends BlobOption>> supportedTypes = Set.of(OptionA.class);

        // Should not throw exception
        BlobOptionSupport.validateSupportedOptions(supportedTypes);
    }

    @Test
    void validateSupportedOptionsThrowsExceptionForUnsupportedType()
    {
        Set<Class<? extends BlobOption>> supportedTypes = Set.of(OptionA.class);
        OptionA optionA = new OptionA();
        OptionB optionB = new OptionB();

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> BlobOptionSupport.validateSupportedOptions(supportedTypes, optionA, optionB));

        assertTrue(exception.getMessage().contains(UNSUPPORTED_OPTION_ERROR));
        assertTrue(exception.getMessage().contains(SUPPORTED_TYPES_ERROR));
    }

    @Test
    void validateSupportedOptionsThrowsExceptionForMultipleUnsupportedTypes()
    {
        Set<Class<? extends BlobOption>> supportedTypes = Set.of(OptionA.class);
        OptionB optionB = new OptionB();
        OptionC optionC = new OptionC();

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> BlobOptionSupport.validateSupportedOptions(supportedTypes, optionB, optionC));

        assertTrue(exception.getMessage().contains("Unsupported option types:"));
        assertTrue(exception.getMessage().contains("OptionB"));
        assertTrue(exception.getMessage().contains("OptionC"));
    }

    @Test
    void validateSupportedOptionsIgnoresNullOptions()
    {
        Set<Class<? extends BlobOption>> supportedTypes = Set.of(OptionA.class);
        OptionA optionA = new OptionA();

        // Should not throw exception
        assertDoesNotThrow(() -> BlobOptionSupport.validateSupportedOptions(supportedTypes, null, optionA, null));
    }

    @Test
    void hasOptionReturnsTrueWhenPresent()
    {
        OptionA optionA = new OptionA();
        OptionB optionB = new OptionB();

        assertTrue(BlobOptionSupport.hasOption(OptionA.class, optionA, optionB));
    }

    @Test
    void hasOptionReturnsFalseWhenNotPresent()
    {
        OptionA optionA = new OptionA();
        OptionB optionB = new OptionB();

        assertFalse(BlobOptionSupport.hasOption(OptionC.class, optionA, optionB));
    }

    @Test
    void hasOptionReturnsFalseForNullArray()
    {
        assertFalse(BlobOptionSupport.hasOption(OptionA.class, (BlobOption[]) null));
    }

    @Test
    void hasOptionReturnsFalseForEmptyArray()
    {
        assertFalse(BlobOptionSupport.hasOption(OptionA.class));
    }
}
