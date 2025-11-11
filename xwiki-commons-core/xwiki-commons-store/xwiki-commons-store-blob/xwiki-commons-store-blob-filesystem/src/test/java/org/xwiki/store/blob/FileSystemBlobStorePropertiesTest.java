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

import java.nio.file.Path;
import java.util.Map;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;
import org.xwiki.properties.BeanManager;
import org.xwiki.properties.PropertyMandatoryException;
import org.xwiki.properties.internal.DefaultBeanManager;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.properties.internal.converter.ConvertUtilsConverter;
import org.xwiki.properties.internal.converter.EnumConverter;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link FileSystemBlobStoreProperties} validation.
 *
 * @version $Id$
 */
@ComponentTest
@ComponentList({
    DefaultBeanManager.class,
    DefaultConverterManager.class,
    EnumConverter.class,
    ConvertUtilsConverter.class
})
class FileSystemBlobStorePropertiesTest
{
    @Inject
    private BeanManager beanManager;

    @Test
    void validateMandatoryProperties()
    {
        FileSystemBlobStoreProperties props = new FileSystemBlobStoreProperties();

        // Should fail validation because rootDir is mandatory
        PropertyMandatoryException exception = assertThrows(PropertyMandatoryException.class,
            () -> this.beanManager.populate(props, Map.of()));

        assertEquals("Property [filesystem.rootdirectory] mandatory", exception.getMessage());
    }

    @Test
    void validateSuccessfulPopulation() throws Exception
    {
        FileSystemBlobStoreProperties props = new FileSystemBlobStoreProperties();

        Path rootDir = Path.of("/tmp/test");
        Map<String, Object> parameters = Map.of(
            FileSystemBlobStoreProperties.ROOT_DIRECTORY, rootDir,
            "name", "test",
            "type", "filesystem"
        );

        this.beanManager.populate(props, parameters);

        assertEquals(rootDir, props.getRootDirectory());
    }
}
