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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
    private static final String FILE_TYPE = "file";

    private static final String S3_TYPE = "s3";

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
    private BlobStoreMigrator blobStoreMigrator;

    private BlobStoreFactory<ExtendedBlobStoreProperties> fileFactory;

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    // Define interfaces with explicitly set type parameters so they can be detected at runtime.
    private interface ExtendedBlobStoreFactory extends BlobStoreFactory<ExtendedBlobStoreProperties>
    {
    }

    private interface ValidatingBlobStoreFactory extends BlobStoreFactory<ValidatingBlobStoreProperties>
    {
    }

    private <T extends BlobStoreProperties> void registerMockFactory(String type,
        BlobStore blobStore, BlobStoreFactory<T> factory) throws Exception
    {
        this.componentManager.registerComponent(BlobStoreFactory.class, type, factory);
        when(factory.newPropertiesBuilder(any())).thenAnswer(
            inv -> new BlobStorePropertiesBuilder(inv.getArgument(0), type));
        when(factory.create(any(), any())).thenReturn(blobStore);
        when(factory.getType()).thenReturn(type);
    }


    private BlobStoreFactory<ExtendedBlobStoreProperties> registerExtendedBlobStoreFactory(String type,
        BlobStore blobStore) throws Exception
    {
        ExtendedBlobStoreFactory factory = mock();
        registerMockFactory(type, blobStore, factory);
        return factory;
    }

    private BlobStoreFactory<ValidatingBlobStoreProperties> registerValidatingBlobStoreFactory(String type,
        BlobStore blobStore) throws Exception
    {
        ValidatingBlobStoreFactory factory = mock();
        registerMockFactory(type, blobStore, factory);
        return factory;
    }

    /**
     * Helper method to register a customizer.
     */
    private BlobStorePropertiesCustomizer registerCustomizer(String type) throws Exception
    {
        return this.componentManager.registerMockComponent(BlobStorePropertiesCustomizer.class, type);
    }

    @BeforeEach
    void setup() throws Exception
    {
        this.fileFactory = registerExtendedBlobStoreFactory(FILE_TYPE, this.testStore);
        when(this.configuration.getStoreType()).thenReturn(FILE_TYPE);
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
        verify(this.fileFactory, times(1)).create(any(), any());
    }

    @Test
    void getBlobStoreWithMigrationWhenCurrentStoreIsEmpty() throws Exception
    {
        when(this.configuration.getMigrationStoreType()).thenReturn(S3_TYPE);
        BlobStoreFactory<ExtendedBlobStoreProperties> s3Factory =
            registerExtendedBlobStoreFactory(S3_TYPE, this.migrationStore);

        when(this.testStore.hasDescendants(BlobPath.root())).thenReturn(false);

        BlobStore result = this.blobStoreManager.getBlobStore(TEST_STORE);

        assertSame(this.testStore, result);
        verify(this.blobStoreMigrator).isMigrationInProgress(this.testStore);
        verify(this.testStore).hasDescendants(BlobPath.root());
        verify(this.blobStoreMigrator).migrate(this.testStore, this.migrationStore);
        verify(this.configuration).getMigrationStoreType();
        // Verify both factories were called exactly once
        verify(this.fileFactory, times(1)).create(any(), any());
        verify(s3Factory, times(1)).create(any(), any());
    }

    @Test
    void getBlobStoreWithMigrationWhenCurrentStoreIsNotEmpty() throws Exception
    {
        when(this.configuration.getMigrationStoreType()).thenReturn(S3_TYPE);
        BlobStoreFactory<ExtendedBlobStoreProperties> s3Factory =
            registerExtendedBlobStoreFactory(S3_TYPE, this.migrationStore);

        when(this.testStore.hasDescendants(BlobPath.root())).thenReturn(true);

        BlobStore result = this.blobStoreManager.getBlobStore(TEST_STORE);

        assertSame(this.testStore, result);
        verify(this.blobStoreMigrator).isMigrationInProgress(this.testStore);
        verify(this.testStore).hasDescendants(BlobPath.root());
        verifyNoMoreInteractions(this.blobStoreMigrator);
        verifyNoInteractions(s3Factory);
    }

    @Test
    void getBlobStoreResumesMigrationWhenMarkerPresent() throws Exception
    {
        when(this.configuration.getMigrationStoreType()).thenReturn(S3_TYPE);
        BlobStoreFactory<ExtendedBlobStoreProperties> s3Factory =
            registerExtendedBlobStoreFactory(S3_TYPE, this.migrationStore);

        when(this.testStore.hasDescendants(BlobPath.root())).thenReturn(true);
        when(this.blobStoreMigrator.isMigrationInProgress(this.testStore)).thenReturn(true);

        BlobStore result = this.blobStoreManager.getBlobStore(TEST_STORE);

        assertSame(this.testStore, result);
        verify(this.blobStoreMigrator).isMigrationInProgress(this.testStore);
        verify(this.testStore, never()).hasDescendants(BlobPath.root());
        verify(this.blobStoreMigrator).migrate(this.testStore, this.migrationStore);
        verifyNoMoreInteractions(this.blobStoreMigrator);
        verify(s3Factory, times(1)).create(any(), any());
    }

    @Test
    void getBlobStoreWithSameStoreAndMigrationType() throws Exception
    {
        when(this.configuration.getMigrationStoreType()).thenReturn(FILE_TYPE);

        BlobStore result = this.blobStoreManager.getBlobStore(TEST_STORE);

        assertSame(this.testStore, result);
        verify(this.testStore, never()).hasDescendants(any());
        verifyNoInteractions(this.blobStoreMigrator);
    }

    @Test
    void getBlobStoreWhenNoMigrationType() throws Exception
    {
        when(this.configuration.getMigrationStoreType()).thenReturn(null);

        BlobStore result = this.blobStoreManager.getBlobStore(TEST_STORE);

        assertSame(this.testStore, result);
        verify(this.configuration).getMigrationStoreType();
        verify(this.testStore, never()).hasDescendants(any());
        verifyNoInteractions(this.blobStoreMigrator);
    }

    @Test
    void getBlobStoreThrowsComponentLookupException()
    {
        when(this.configuration.getStoreType()).thenReturn("foo");
        // No factory registered for "foo"

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.blobStoreManager.getBlobStore(TEST_STORE));

        assertEquals("Failed to get or create blob store with name [" + TEST_STORE + "]", exception.getMessage());
        assertInstanceOf(ComponentLookupException.class, exception.getCause());
    }

    @Test
    void getBlobStoreThrowsBlobStoreException() throws Exception
    {
        when(this.fileFactory.create(any(), any())).thenThrow(new BlobStoreException(STORE_ERROR));

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
        when(this.configuration.getMigrationStoreType()).thenReturn(S3_TYPE);
        registerExtendedBlobStoreFactory(S3_TYPE, this.migrationStore);

        when(this.testStore.hasDescendants(BlobPath.root())).thenReturn(false);
        doThrow(new BlobStoreException("Migration failed"))
            .when(this.blobStoreMigrator).migrate(this.testStore, this.migrationStore);

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.blobStoreManager.getBlobStore(TEST_STORE));

        assertEquals("Migration failed", exception.getMessage());
        verify(this.blobStoreMigrator).isMigrationInProgress(this.testStore);
    }

    @Test
    void disposeWithDisposableBlobStores() throws Exception
    {
        DisposableBlobStore disposableBlobStore = mock();
        when(this.fileFactory.create(eq(DISPOSABLE_STORE), any())).thenReturn(disposableBlobStore);
        when(this.fileFactory.create(eq(REGULAR_STORE), any())).thenReturn(this.testStore);

        this.blobStoreManager.getBlobStore(DISPOSABLE_STORE);
        this.blobStoreManager.getBlobStore(REGULAR_STORE);

        this.blobStoreManager.dispose();

        verify(disposableBlobStore).dispose();
    }

    @Test
    void disposeHandlesExceptionsFromDisposableStores() throws Exception
    {
        DisposableBlobStore disposableStore1 = mock();
        when(disposableStore1.getName()).thenReturn("store1");

        DisposableBlobStore disposableStore2 = mock();
        when(disposableStore2.getName()).thenReturn("store2");

        when(this.fileFactory.create(eq("store1"), any())).thenReturn(disposableStore1);
        when(this.fileFactory.create(eq("store2"), any())).thenReturn(disposableStore2);

        ComponentLifecycleException expectedCause = new ComponentLifecycleException("Dispose failed for store1");
        doThrow(expectedCause).when(disposableStore1).dispose();

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
    void getBlobStoreThrowsExceptionWhenMandatoryPropertyMissing() throws Exception
    {
        when(this.configuration.getStoreType()).thenReturn("validating");
        registerValidatingBlobStoreFactory("validating", this.testStore);

        // Don't set the mandatory property via customizer
        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.blobStoreManager.getBlobStore(TEST_STORE));

        assertTrue(exception.getMessage().contains("Failed to populate blob store properties"));
    }

    @Test
    void getBlobStoreSucceedsWhenMandatoryPropertySetByCustomizer() throws Exception
    {
        when(this.configuration.getStoreType()).thenReturn("validating");
        BlobStoreFactory<ValidatingBlobStoreProperties> mockFactory =
            registerValidatingBlobStoreFactory("validating", this.testStore);

        BlobStorePropertiesCustomizer customizer = registerCustomizer("customizer");
        doAnswer(inv -> {
            BlobStorePropertiesBuilder builder = inv.getArgument(0);
            builder.set("mandatoryField", "requiredValue");
            return null;
        }).when(customizer).customize(any());

        this.blobStoreManager.getBlobStore(TEST_STORE);

        ArgumentCaptor<ValidatingBlobStoreProperties> propertiesCaptor = ArgumentCaptor.captor();
        verify(mockFactory).create(eq(TEST_STORE), propertiesCaptor.capture());

        ValidatingBlobStoreProperties capturedProperties = propertiesCaptor.getValue();
        assertInstanceOf(ValidatingBlobStoreProperties.class, capturedProperties);
        assertEquals("requiredValue", capturedProperties.getMandatoryField());
    }

    // Helper interface to create a mock that implements both BlobStore and Disposable
    private interface DisposableBlobStore extends BlobStore, Disposable
    {
    }

    /**
     * Extended properties class with a custom option.
     */
    public static final class ExtendedBlobStoreProperties implements BlobStoreProperties
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
    public static final class ValidatingBlobStoreProperties implements BlobStoreProperties
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
