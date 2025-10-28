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

import jakarta.inject.Named;
import jakarta.validation.constraints.NotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.phase.Disposable;
import org.xwiki.properties.annotation.PropertyMandatory;
import org.xwiki.properties.internal.DefaultBeanManager;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.properties.internal.converter.ConvertUtilsConverter;
import org.xwiki.properties.internal.converter.EnumConverter;
import org.xwiki.store.blob.BaseBlobStoreProperties;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.BlobStoreFactory;
import org.xwiki.store.blob.BlobStoreProperties;
import org.xwiki.store.blob.BlobStorePropertiesBuilder;
import org.xwiki.store.blob.BlobStorePropertiesCustomizer;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DefaultBlobStoreManager}.
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
@SuppressWarnings({ "checkstyle:MultipleStringLiterals", "checkstyle:ClassFanOutComplexity" })
class DefaultBlobStoreManagerTest
{
    private static final String FILE_HINT = "file";

    private static final String S3_HINT = "s3";

    private static final String TEST_STORE = "testStore";

    private static final String DISPOSABLE_STORE = "disposableStore";

    private static final String REGULAR_STORE = "regularStore";

    private static final String STORE_ERROR = "Store error";

    @InjectMockComponents
    private DefaultBlobStoreManager blobStoreManager;

    @MockComponent
    private BlobStoreConfiguration configuration;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @Mock
    private BlobStore testStore;

    @Mock
    private BlobStore migrationStore;

    @MockComponent
    @Named("file")
    private BlobStoreFactory fileFactory;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    /**
     * Helper method to register and configure a migration factory.
     */
    private BlobStoreFactory registerMigrationFactory(String hint, BlobStore migrationStore) throws Exception
    {
        BlobStoreFactory factory = this.componentManager.registerMockComponent(BlobStoreFactory.class, hint);
        when(factory.newPropertiesBuilder(any())).thenAnswer(
            inv -> new BlobStorePropertiesBuilder(inv.getArgument(0), hint));
        when(factory.create(any())).thenReturn(migrationStore);
        doReturn(BaseBlobStoreProperties.class).when(factory).getPropertiesClass();
        return factory;
    }

    /**
     * Helper method to register a customizer.
     */
    private BlobStorePropertiesCustomizer registerCustomizer(String hint) throws Exception
    {
        return this.componentManager.registerMockComponent(BlobStorePropertiesCustomizer.class, hint);
    }

    @BeforeEach
    void setup() throws Exception
    {
        when(this.configuration.getStoreHint()).thenReturn(FILE_HINT);
        when(this.fileFactory.newPropertiesBuilder(any())).thenAnswer(
            inv -> new BlobStorePropertiesBuilder(inv.getArgument(0), FILE_HINT));
        when(this.fileFactory.create(any())).thenReturn(this.testStore);
        doReturn(BaseBlobStoreProperties.class).when(this.fileFactory).getPropertiesClass();
    }

    @Test
    void getBlobStoreWhenStoreExistsInCache() throws Exception
    {
        BlobStore firstResult = this.blobStoreManager.getBlobStore(TEST_STORE);

        // Second call should use cache.
        BlobStore secondResult = this.blobStoreManager.getBlobStore(TEST_STORE);

        assertSame(firstResult, secondResult);
        assertSame(this.testStore, secondResult);
        // Verify factory was called exactly once, confirming caching behavior
        verify(this.fileFactory, times(1)).create(any());
    }

    @Test
    void getBlobStoreWithMigrationWhenCurrentStoreIsEmpty() throws Exception
    {
        when(this.configuration.getMigrationStoreHint()).thenReturn(S3_HINT);
        BlobStoreFactory s3Factory = registerMigrationFactory(S3_HINT, this.migrationStore);

        when(this.testStore.isEmptyDirectory(BlobPath.ROOT)).thenReturn(true);

        BlobStore result = this.blobStoreManager.getBlobStore(TEST_STORE);

        assertSame(this.testStore, result);
        verify(this.testStore).isEmptyDirectory(BlobPath.ROOT);
        verify(this.testStore).moveDirectory(this.migrationStore, BlobPath.ROOT, BlobPath.ROOT);
        verify(this.configuration).getMigrationStoreHint();
        // Verify both factories were called exactly once
        verify(this.fileFactory, times(1)).create(any());
        verify(s3Factory, times(1)).create(any());
    }

    @Test
    void getBlobStoreWithMigrationWhenCurrentStoreIsNotEmpty() throws Exception
    {
        when(this.configuration.getMigrationStoreHint()).thenReturn(S3_HINT);
        BlobStoreFactory s3Factory = registerMigrationFactory(S3_HINT, this.migrationStore);

        when(this.testStore.isEmptyDirectory(BlobPath.ROOT)).thenReturn(false);

        BlobStore result = this.blobStoreManager.getBlobStore(TEST_STORE);

        assertSame(this.testStore, result);
        verify(this.testStore).isEmptyDirectory(BlobPath.ROOT);
        verify(this.testStore, never()).moveDirectory(any(), any(), any());
        verifyNoInteractions(s3Factory);
    }

    @Test
    void getBlobStoreWithSameStoreAndMigrationHint() throws Exception
    {
        when(this.configuration.getMigrationStoreHint()).thenReturn(FILE_HINT);

        BlobStore result = this.blobStoreManager.getBlobStore(TEST_STORE);

        assertSame(this.testStore, result);
        verify(this.testStore, never()).isEmptyDirectory(any());
        verify(this.testStore, never()).moveDirectory(any(), any(), any());
    }

    @Test
    void getBlobStoreWhenNoMigrationHint() throws Exception
    {
        when(this.configuration.getMigrationStoreHint()).thenReturn(null);

        BlobStore result = this.blobStoreManager.getBlobStore(TEST_STORE);

        assertSame(this.testStore, result);
        verify(this.configuration).getMigrationStoreHint();
        verify(this.testStore, never()).isEmptyDirectory(any());
        verify(this.testStore, never()).moveDirectory(any(), any(), any());
    }

    @Test
    void getBlobStoreThrowsComponentLookupException()
    {
        when(this.configuration.getStoreHint()).thenReturn("foo");
        // No factory registered for "foo"

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.blobStoreManager.getBlobStore(TEST_STORE));

        assertEquals("Failed to get or create blob store with name [" + TEST_STORE + "]", exception.getMessage());
        assertInstanceOf(ComponentLookupException.class, exception.getCause());
    }

    @Test
    void getBlobStoreThrowsBlobStoreException() throws Exception
    {
        when(this.fileFactory.create(any())).thenThrow(new BlobStoreException(STORE_ERROR));

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.blobStoreManager.getBlobStore(TEST_STORE));

        assertEquals(STORE_ERROR, exception.getMessage());
    }

    @Test
    void getBlobStoreAppliesCustomizers() throws Exception
    {
        BlobStorePropertiesCustomizer customizer1 = registerCustomizer("customizer1");
        BlobStorePropertiesCustomizer customizer2 = registerCustomizer("customizer2");

        this.blobStoreManager.getBlobStore(TEST_STORE);

        verify(customizer1).customize(any(BlobStorePropertiesBuilder.class));
        verify(customizer2).customize(any(BlobStorePropertiesBuilder.class));
    }

    @Test
    void getBlobStoreWithMigrationThrowsExceptionWhenMigrationFails() throws Exception
    {
        when(this.configuration.getMigrationStoreHint()).thenReturn(S3_HINT);
        registerMigrationFactory(S3_HINT, this.migrationStore);

        when(this.testStore.isEmptyDirectory(BlobPath.ROOT)).thenReturn(true);
        doThrow(new BlobStoreException("Migration failed")).when(this.testStore)
            .moveDirectory(any(), any(), any());

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.blobStoreManager.getBlobStore(TEST_STORE));

        assertEquals("Migration failed", exception.getMessage());
    }

    @Test
    void disposeWithDisposableBlobStores() throws Exception
    {
        DisposableBlobStore disposableBlobStore = mock();
        when(this.fileFactory.create(argThat(p -> p != null
            && DISPOSABLE_STORE.equals(p.getName())))).thenReturn(disposableBlobStore);
        when(this.fileFactory.create(argThat(p -> p != null
            && REGULAR_STORE.equals(p.getName())))).thenReturn(this.testStore);

        this.blobStoreManager.getBlobStore(DISPOSABLE_STORE);
        this.blobStoreManager.getBlobStore(REGULAR_STORE);

        this.blobStoreManager.dispose();

        verify(disposableBlobStore).dispose();
    }

    @Test
    void disposeHandlesExceptionsFromDisposableStores() throws Exception
    {
        DisposableBlobStore disposableStore1 = mock();
        BlobStoreProperties p1 = mock();
        when(p1.getName()).thenReturn("store1");
        when(disposableStore1.getProperties()).thenReturn(p1);

        DisposableBlobStore disposableStore2 = mock();
        BlobStoreProperties p2 = mock();
        when(p2.getName()).thenReturn("store2");
        when(disposableStore2.getProperties()).thenReturn(p2);

        when(this.fileFactory.create(argThat(p -> p != null
            && "store1".equals(p.getName())))).thenReturn(disposableStore1);
        when(this.fileFactory.create(argThat(p -> p != null
            && "store2".equals(p.getName())))).thenReturn(disposableStore2);

        ComponentLifecycleException expectedCause = new ComponentLifecycleException("Dispose failed for store1");
        // We explicitly dispose the component first, then the test setup will dispose the component again. In the
        // second case, don't throw an exception to allow the test to complete without errors.
        doThrow(expectedCause).doNothing().when(disposableStore1).dispose();

        this.blobStoreManager.getBlobStore("store1");
        this.blobStoreManager.getBlobStore("store2");

        // Dispose should not throw despite one store failing to dispose, and should attempt to dispose both stores.
        assertDoesNotThrow(() -> this.blobStoreManager.dispose());

        verify(disposableStore1).dispose();
        verify(disposableStore2).dispose();
        // Check that a warning was logged for the failure
        assertEquals(1, this.logCapture.size());
        assertTrue(this.logCapture.getMessage(0).contains("Failed to dispose blob store [store1]"));
        assertTrue(this.logCapture.getMessage(0).contains("ComponentLifecycleException"));
    }

    @Test
    void disposeWithNoStores()
    {
        assertDoesNotThrow(() -> this.blobStoreManager.dispose());
    }

    @Test
    void getBlobStorePopulatesPropertiesFromBuilder() throws Exception
    {
        BlobStoreFactory mockFactory = mock();
        when(mockFactory.newPropertiesBuilder(any())).thenAnswer(
            inv -> new BlobStorePropertiesBuilder(inv.getArgument(0), "filesystem"));
        doReturn(BaseBlobStoreProperties.class).when(mockFactory).getPropertiesClass();
        when(mockFactory.create(any())).thenReturn(mock());
        this.componentManager.registerComponent(BlobStoreFactory.class, FILE_HINT, mockFactory);

        this.blobStoreManager.getBlobStore(TEST_STORE);

        ArgumentCaptor<BlobStoreProperties> propertiesCaptor = ArgumentCaptor.forClass(BlobStoreProperties.class);
        verify(mockFactory).create(propertiesCaptor.capture());

        BlobStoreProperties capturedProperties = propertiesCaptor.getValue();
        assertEquals(TEST_STORE, capturedProperties.getName());
        assertEquals("filesystem", capturedProperties.getType());
    }

    @Test
    void getBlobStorePopulatesCustomPropertiesFromCustomizer() throws Exception
    {
        BlobStoreFactory mockFactory = mock();
        when(mockFactory.newPropertiesBuilder(any())).thenAnswer(
            inv -> new BlobStorePropertiesBuilder(inv.getArgument(0), "filesystem"));
        doReturn(ExtendedBlobStoreProperties.class).when(mockFactory).getPropertiesClass();
        when(mockFactory.create(any())).thenReturn(mock());
        this.componentManager.registerComponent(BlobStoreFactory.class, FILE_HINT, mockFactory);

        BlobStorePropertiesCustomizer customizer = registerCustomizer("customizer");
        doAnswer(inv -> {
            BlobStorePropertiesBuilder builder = inv.getArgument(0);
            builder.set("customOption", "customValue");
            return null;
        }).when(customizer).customize(any());

        this.blobStoreManager.getBlobStore(TEST_STORE);

        ArgumentCaptor<BlobStoreProperties> propertiesCaptor = ArgumentCaptor.forClass(BlobStoreProperties.class);
        verify(mockFactory).create(propertiesCaptor.capture());

        BlobStoreProperties capturedProperties = propertiesCaptor.getValue();
        assertInstanceOf(ExtendedBlobStoreProperties.class, capturedProperties);
        ExtendedBlobStoreProperties extendedProps = (ExtendedBlobStoreProperties) capturedProperties;
        assertEquals("customValue", extendedProps.getCustomOption());
    }

    @Test
    void getBlobStoreThrowsExceptionWhenMandatoryPropertyMissing() throws Exception
    {
        BlobStoreFactory mockFactory = mock();
        when(mockFactory.newPropertiesBuilder(any())).thenAnswer(
            inv -> new BlobStorePropertiesBuilder(inv.getArgument(0), "filesystem"));
        doReturn(ValidatingBlobStoreProperties.class).when(mockFactory).getPropertiesClass();
        when(mockFactory.create(any())).thenReturn(mock());
        this.componentManager.registerComponent(BlobStoreFactory.class, FILE_HINT, mockFactory);

        // Don't set the mandatory property via customizer
        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.blobStoreManager.getBlobStore(TEST_STORE));

        assertTrue(exception.getMessage().contains("Failed to populate blob store properties"));
    }

    @Test
    void getBlobStoreSucceedsWhenMandatoryPropertySetByCustomizer() throws Exception
    {
        BlobStoreFactory mockFactory = mock();
        when(mockFactory.newPropertiesBuilder(any())).thenAnswer(
            inv -> new BlobStorePropertiesBuilder(inv.getArgument(0), "filesystem"));
        doReturn(ValidatingBlobStoreProperties.class).when(mockFactory).getPropertiesClass();
        when(mockFactory.create(any())).thenReturn(mock());
        this.componentManager.registerComponent(BlobStoreFactory.class, FILE_HINT, mockFactory);

        BlobStorePropertiesCustomizer customizer = registerCustomizer("customizer");
        doAnswer(inv -> {
            BlobStorePropertiesBuilder builder = inv.getArgument(0);
            builder.set("mandatoryField", "requiredValue");
            return null;
        }).when(customizer).customize(any());

        this.blobStoreManager.getBlobStore(TEST_STORE);

        ArgumentCaptor<BlobStoreProperties> propertiesCaptor = ArgumentCaptor.forClass(BlobStoreProperties.class);
        verify(mockFactory).create(propertiesCaptor.capture());

        BlobStoreProperties capturedProperties = propertiesCaptor.getValue();
        assertInstanceOf(ValidatingBlobStoreProperties.class, capturedProperties);
        ValidatingBlobStoreProperties validatingProps = (ValidatingBlobStoreProperties) capturedProperties;
        assertEquals("requiredValue", validatingProps.getMandatoryField());
    }

    // Helper interface to create a mock that implements both BlobStore and Disposable
    private interface DisposableBlobStore extends BlobStore, Disposable
    {
    }

    /**
     * Extended properties class with a custom option.
     */
    public static final class ExtendedBlobStoreProperties extends BaseBlobStoreProperties
    {
        private String customOption;

        public String getCustomOption()
        {
            return this.customOption;
        }

        public void setCustomOption(String customOption)
        {
            this.customOption = customOption;
        }
    }

    /**
     * Properties class with a mandatory field for validation testing.
     */
    public static final class ValidatingBlobStoreProperties extends BaseBlobStoreProperties
    {
        private String mandatoryField;

        @NotNull
        public String getMandatoryField()
        {
            return this.mandatoryField;
        }

        @PropertyMandatory
        public void setMandatoryField(String mandatoryField)
        {
            this.mandatoryField = mandatoryField;
        }
    }
}
