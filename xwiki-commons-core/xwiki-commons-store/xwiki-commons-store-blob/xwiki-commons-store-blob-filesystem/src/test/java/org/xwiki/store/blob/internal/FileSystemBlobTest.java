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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xwiki.store.blob.BlobDoesNotExistCondition;
import org.xwiki.store.blob.BlobNotFoundException;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.WriteConditionFailedException;
import org.xwiki.test.junit5.XWikiTempDir;
import org.xwiki.test.junit5.XWikiTempDirExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link FileSystemBlob}.
 *
 * @version $Id$
 */
@ExtendWith({ MockitoExtension.class, XWikiTempDirExtension.class })
class FileSystemBlobTest
{
    @XWikiTempDir
    private File tempDir;

    @Mock
    private BlobPath blobPath;

    private Path absolutePath;

    @Mock
    private FileSystemBlobStore store;

    private FileSystemBlob blob;

    @BeforeEach
    void setUp()
    {
        this.absolutePath = this.tempDir.toPath().resolve("testblob.dat");
        this.blob = new FileSystemBlob(this.blobPath, this.absolutePath, this.store);
    }

    @Test
    void constructor()
    {
        // Verify that the blob is properly initialized
        assertEquals(this.blobPath, this.blob.getPath());
        assertEquals(this.store, this.blob.getStore());
    }

    @Test
    void existsWhenFileExists() throws Exception
    {
        Files.createFile(this.absolutePath);
        assertTrue(this.blob.exists());
    }

    @Test
    void existsWhenFileDoesNotExist()
    {
        assertFalse(this.blob.exists());
    }

    @Test
    void getSizeWhenFileExists() throws Exception
    {
        Files.write(this.absolutePath, new byte[1024]);
        assertEquals(1024L, this.blob.getSize());
    }

    @Test
    void getSizeWhenFileDoesNotExist() throws Exception
    {
        assertEquals(-1L, this.blob.getSize());
    }

    @Test
    void getSizeThrowsExceptionOnIOError()
    {
        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.exists(this.absolutePath, LinkOption.NOFOLLOW_LINKS))
                .thenReturn(true);
            filesMock.when(() -> Files.size(this.absolutePath))
                .thenThrow(new IOException("IO Error"));

            BlobStoreException exception = assertThrows(BlobStoreException.class, () -> this.blob.getSize());
            assertEquals("Error getting file size.", exception.getMessage());
            assertInstanceOf(IOException.class, exception.getCause());
        }
    }

    @Test
    void getOutputStreamWithoutConditions() throws Exception
    {
        try (OutputStream result = this.blob.getOutputStream()) {
            assertNotNull(result);
            result.write("test data".getBytes());
        }

        assertTrue(Files.exists(this.absolutePath));
        assertEquals("test data", Files.readString(this.absolutePath));
        verify(this.store).createParents(this.absolutePath);
    }

    @Test
    void getOutputStreamWithCreateNewCondition() throws Exception
    {
        try (OutputStream result = this.blob.getOutputStream(BlobDoesNotExistCondition.INSTANCE)) {
            assertNotNull(result);
            result.write("test data".getBytes());
        }

        assertTrue(Files.exists(this.absolutePath));
        assertEquals("test data", Files.readString(this.absolutePath));
        verify(this.store).createParents(this.absolutePath);
    }

    @Test
    void getOutputStreamThrowsWriteConditionFailedOnFileExists() throws Exception
    {
        // Create the file first so it exists
        Files.createFile(this.absolutePath);

        WriteConditionFailedException exception = assertThrows(WriteConditionFailedException.class,
            () -> this.blob.getOutputStream(BlobDoesNotExistCondition.INSTANCE));

        assertEquals(this.blobPath, exception.getBlobPath());
        assertInstanceOf(FileAlreadyExistsException.class, exception.getCause());
    }

    @Test
    void getOutputStreamRetriesOnNoSuchFileException() throws Exception
    {
        OutputStream mockOutputStream = mock();
        NoSuchFileException noSuchFileException = new NoSuchFileException("No parent");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // First attempt fails with NoSuchFileException, second succeeds
            filesMock.when(() -> Files.newOutputStream(this.absolutePath))
                .thenThrow(noSuchFileException)
                .thenReturn(mockOutputStream);

            OutputStream result = this.blob.getOutputStream();

            assertSame(mockOutputStream, result);
            verify(this.store, times(2)).createParents(this.absolutePath);
        }
    }

    @Test
    void getOutputStreamThrowsExceptionAfterMaxRetries() throws Exception
    {
        NoSuchFileException noSuchFileException = new NoSuchFileException("No parent");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // All attempts fail with NoSuchFileException
            filesMock.when(() -> Files.newOutputStream(this.absolutePath))
                .thenThrow(noSuchFileException);

            BlobStoreException exception = assertThrows(BlobStoreException.class,
                () -> this.blob.getOutputStream());

            assertTrue(exception.getMessage().contains("Error creating parent directories"));
            assertTrue(exception.getMessage().contains("attempts"));
            assertSame(noSuchFileException, exception.getCause());
            verify(this.store, times(FileSystemBlobStore.NUM_ATTEMPTS)).createParents(this.absolutePath);
        }
    }

    @Test
    void getOutputStreamCleansUpOnIOException() throws Exception
    {
        IOException ioException = new IOException("Generic IO Error");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.newOutputStream(this.absolutePath))
                .thenThrow(ioException);

            BlobStoreException exception = assertThrows(BlobStoreException.class,
                () -> this.blob.getOutputStream());

            assertEquals("Error getting output stream.", exception.getMessage());
            assertSame(ioException, exception.getCause());
            verify(this.store).createParents(this.absolutePath);
            verify(this.store).cleanUpParents(this.absolutePath);
        }
    }

    @Test
    void getStreamSuccess() throws Exception
    {
        String expectedContent = "test content";
        Files.write(this.absolutePath, expectedContent.getBytes());

        String content;
        try (InputStream result = this.blob.getStream()) {
            assertNotNull(result);
            content = IOUtils.toString(result, StandardCharsets.UTF_8);
        }
        assertEquals(expectedContent, content);
    }

    @Test
    void getStreamThrowsBlobNotFoundOnNoSuchFileException()
    {
        BlobNotFoundException exception = assertThrows(BlobNotFoundException.class,
            () -> this.blob.getStream());

        assertEquals(this.blobPath, exception.getBlobPath());
        assertInstanceOf(NoSuchFileException.class, exception.getCause());
    }

    @Test
    void getStreamThrowsBlobStoreExceptionOnIOException()
    {
        IOException ioException = new IOException("Generic IO Error");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.newInputStream(this.absolutePath, LinkOption.NOFOLLOW_LINKS))
                .thenThrow(ioException);

            BlobStoreException exception = assertThrows(BlobStoreException.class,
                () -> this.blob.getStream());

            assertEquals("Error getting input stream.", exception.getMessage());
            assertSame(ioException, exception.getCause());
        }
    }
}
