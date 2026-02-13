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
package org.xwiki.properties.internal.converter;

import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Validate {@link OptionalConverter} component.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class OptionalConverterTest
{
    @InjectMockComponents
    private DefaultConverterManager converterManager;

    private ParameterizedType optionalBooleanType = new DefaultParameterizedType(null, Optional.class, Boolean.class);

    private ParameterizedType listOfBooleanType = new DefaultParameterizedType(null, List.class, Boolean.class);

    private ParameterizedType optionalListOfBooleanType =
        new DefaultParameterizedType(null, Optional.class, listOfBooleanType);

    @Test
    void xToOptionalY()
    {
        // String to Optional<Boolean>
        assertEquals(Optional.of(true), this.converterManager.convert(optionalBooleanType, "true"));
        assertEquals(Optional.empty(), this.converterManager.convert(optionalBooleanType, null));

        // String to Optional<List<Boolean>>
        assertEquals(Optional.of(List.of(true, false)),
            this.converterManager.convert(optionalListOfBooleanType, "true, false"));
        assertEquals(Optional.of(List.of()), this.converterManager.convert(optionalListOfBooleanType, ""));
        assertEquals(Optional.empty(), this.converterManager.convert(optionalListOfBooleanType, null));
    }

    @Test
    void optionalXToOptionalY()
    {
        // Optional<String> to Optional<Boolean>
        assertEquals(Optional.of(true), this.converterManager.convert(optionalBooleanType, Optional.of("true")));
        assertEquals(Optional.empty(), this.converterManager.convert(optionalBooleanType, Optional.empty()));

        // Optional<String> to Optional<List<Boolean>>
        assertEquals(Optional.of(List.of(true, false)),
            this.converterManager.convert(optionalListOfBooleanType, Optional.of("true, false")));
        assertEquals(Optional.of(List.of()), this.converterManager.convert(optionalListOfBooleanType, Optional.of("")));
        assertEquals(Optional.empty(), this.converterManager.convert(optionalListOfBooleanType, Optional.empty()));
    }

    @Test
    void convertToString()
    {
        assertEquals("true", this.converterManager.convert(String.class, Optional.of(true)));
        assertNull(this.converterManager.convert(String.class, Optional.empty()));
    }
}
