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
package org.xwiki.store.blob;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link BlobStorePropertiesBuilder}.
 */
class BlobStorePropertiesBuilderTest
{
    private BlobStorePropertiesBuilder builder;

    @BeforeEach
    void setUp()
    {
        this.builder = new BlobStorePropertiesBuilder("testStore", "file");
    }

    @Test
    void constructorShouldSetNameAndHint()
    {
        assertEquals("testStore", this.builder.getName());
        assertEquals("file", this.builder.getHint());
    }

    @Test
    void getShouldReturnEmptyForNonExistentProperty()
    {
        Optional<Object> result = this.builder.get("nonExistent");
        assertFalse(result.isPresent());
    }

    @ParameterizedTest
    @MethodSource("propertyTestCases")
    void propertyOperations(String key, Object value)
    {
        // Test setting and getting property
        this.builder.set(key, value);
        Optional<Object> result = this.builder.get(key);
        assertTrue(result.isPresent());
        assertEquals(value, result.get());
    }

    private static Stream<Arguments> propertyTestCases()
    {
        return Stream.of(
            Arguments.of("maxSize", 100L),
            Arguments.of("cacheEnabled", true),
            Arguments.of("prop1", "value1"),
            Arguments.of("prop2", 42)
        );
    }

    @Test
    void setShouldUpdateExistingProperty()
    {
        this.builder.set("maxSize", 100L);
        this.builder.set("maxSize", 200L);

        Optional<Object> result = this.builder.get("maxSize");
        assertTrue(result.isPresent());
        assertEquals(200L, result.get());
    }

    @Test
    void removeShouldRemoveExistingProperty()
    {
        String property = "tempProperty";
        this.builder.set(property, "value");
        this.builder.remove(property);

        assertFalse(this.builder.get(property).isPresent());
    }

    @Test
    void getAllPropertiesShouldReturnAllProperties()
    {
        this.builder.set("prop1", "value1");
        this.builder.set("prop2", 42);

        Map<String, Object> options = this.builder.getAllProperties();

        // Only custom properties, not name and hint
        assertEquals(2, options.size());
        assertEquals("value1", options.get("prop1"));
        assertEquals(42, options.get("prop2"));
    }

    @Test
    void builderShouldReturnSelfForMethodChaining()
    {
        BlobStorePropertiesBuilder result = this.builder.set("prop", "value");
        assertSame(this.builder, result);

        result = this.builder.remove("prop");
        assertSame(this.builder, result);
    }
}
