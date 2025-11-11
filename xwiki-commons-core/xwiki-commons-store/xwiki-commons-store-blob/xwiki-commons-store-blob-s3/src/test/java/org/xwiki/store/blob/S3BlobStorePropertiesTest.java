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

import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.properties.BeanManager;
import org.xwiki.properties.PropertyException;
import org.xwiki.properties.PropertyMandatoryException;
import org.xwiki.properties.internal.DefaultBeanManager;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.properties.internal.converter.ConvertUtilsConverter;
import org.xwiki.properties.internal.converter.EnumConverter;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link S3BlobStoreProperties} validation.
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
class S3BlobStorePropertiesTest
{
    @MockComponent
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private BeanManager beanManager;

    @BeforeEach
    void setUp(MockitoComponentManager componentManager)
    {
        when(this.componentManagerProvider.get()).thenReturn(componentManager);
    }

    private Map<String, Object> createBaseParameters()
    {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", "test");
        parameters.put("type", "s3");
        parameters.put(S3BlobStoreProperties.BUCKET, "test-bucket");
        parameters.put(S3BlobStoreProperties.MULTIPART_UPLOAD_PART_SIZE, 5L * 1024 * 1024);
        parameters.put(S3BlobStoreProperties.MULTIPART_COPY_PART_SIZE, 10L * 1024 * 1024);

        return parameters;
    }

    private void assertValidationFailsWithMessage(Map<String, Object> parameters, String expectedMessage)
    {
        S3BlobStoreProperties props = new S3BlobStoreProperties();
        PropertyException exception = assertThrows(
            PropertyException.class,
            () -> this.beanManager.populate(props, parameters));
        assertThat(exception.getMessage(), containsString(expectedMessage));
    }

    @Test
    void validateMandatoryProperties()
    {
        S3BlobStoreProperties props = new S3BlobStoreProperties();

        // Should fail validation because bucket is mandatory
        PropertyMandatoryException exception = assertThrows(PropertyMandatoryException.class,
            () -> this.beanManager.populate(props, Map.of("name", "test", "type", "s3")));

        assertEquals("Property [s3.bucket] mandatory", exception.getMessage());
    }

    @Test
    void validateSuccessfulPopulation() throws Exception
    {
        S3BlobStoreProperties props = new S3BlobStoreProperties();

        Map<String, Object> parameters = createBaseParameters();
        parameters.put(S3BlobStoreProperties.KEY_PREFIX, "test-prefix");

        this.beanManager.populate(props, parameters);

        assertEquals("test-bucket", props.getBucket());
        assertEquals("test-prefix", props.getKeyPrefix());
        assertEquals(5L * 1024 * 1024, props.getMultipartUploadPartSize());
        assertEquals(10L * 1024 * 1024, props.getMultipartCopyPartSize());
    }

    @Test
    void validateMultipartUploadSizeBelowMinimum()
    {
        Map<String, Object> parameters = createBaseParameters();
        parameters.put(S3BlobStoreProperties.MULTIPART_UPLOAD_PART_SIZE, 5242879L);

        assertValidationFailsWithMessage(parameters, "must be greater than or equal to 5242880");
    }

    @Test
    void validateMultipartUploadSizeAboveMaximum()
    {
        Map<String, Object> parameters = createBaseParameters();
        parameters.put(S3BlobStoreProperties.MULTIPART_UPLOAD_PART_SIZE, 5368709121L);

        assertValidationFailsWithMessage(parameters, "must be less than or equal to");
    }

    @Test
    void validateMultipartCopySizeBelowMinimum()
    {
        Map<String, Object> parameters = createBaseParameters();
        parameters.put(S3BlobStoreProperties.MULTIPART_COPY_PART_SIZE, 5242879L);

        assertValidationFailsWithMessage(parameters, "must be greater than or equal to 5242880");
    }

    @Test
    void validateMultipartCopySizeAboveMaximum()
    {
        Map<String, Object> parameters = createBaseParameters();
        parameters.put(S3BlobStoreProperties.MULTIPART_COPY_PART_SIZE, 5368709121L);

        assertValidationFailsWithMessage(parameters, "must be less than or equal to");
    }
}