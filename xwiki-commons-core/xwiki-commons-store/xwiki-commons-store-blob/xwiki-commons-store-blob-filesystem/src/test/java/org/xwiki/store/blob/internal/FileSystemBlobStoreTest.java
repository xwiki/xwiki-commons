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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.file.PathUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.xwiki.store.blob.Blob;
import org.xwiki.store.blob.BlobAlreadyExistsException;
import org.xwiki.store.blob.BlobNotFoundException;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.store.blob.BlobStore;
import org.xwiki.store.blob.BlobStoreException;
import org.xwiki.store.blob.FileSystemBlobStoreProperties;
import org.xwiki.test.junit5.XWikiTempDir;
import org.xwiki.test.junit5.XWikiTempDirExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FileSystemBlobStore}.
 *
 * @version $Id$
 */
@SuppressWarnings("checkstyle:MultipleStringLiterals")
@ExtendWith({ XWikiTempDirExtension.class, MockitoExtension.class })
class FileSystemBlobStoreTest extends XWikiTempDirExtension
{
    @XWikiTempDir
    private File tmpDir;

    @Mock
    private BlobStore mockSourceStore;

    @Mock
    private Blob mockSourceBlob;

    private FileSystemBlobStore blobStore;

    private Path basePath;

    private FileSystemBlobStoreProperties properties;

    @BeforeEach
    void setUp()
    {
        this.basePath = this.tmpDir.toPath();
        this.properties = new FileSystemBlobStoreProperties();
        this.properties.setRootDirectory(this.basePath);
        this.blobStore = new FileSystemBlobStore("test-store", this.properties);
    }

    @Test
    void constructor()
    {
        assertSame(this.properties, this.blobStore.getProperties());
    }

    @Test
    void getBlobFilePath()
    {
        BlobPath blobPath = BlobPath.of(List.of("dir1", "dir2", "file.txt"));
        Path expected = this.basePath.resolve("dir1").resolve("dir2").resolve("file.txt");

        Path actual = this.blobStore.getBlobFilePath(blobPath);

        assertEquals(expected, actual);
    }

    @Test
    void getBlobFilePathWithSingleSegment()
    {
        BlobPath blobPath = BlobPath.of(List.of("file.txt"));
        Path expected = this.basePath.resolve("file.txt");

        Path actual = this.blobStore.getBlobFilePath(blobPath);

        assertEquals(expected, actual);
    }

    @Test
    void getBlob() throws BlobStoreException
    {
        BlobPath blobPath = BlobPath.of(List.of("test.txt"));

        Blob blob = this.blobStore.getBlob(blobPath);

        assertInstanceOf(FileSystemBlob.class, blob);
        assertEquals(blobPath, blob.getPath());
    }

    @Test
    void listBlobsInNonExistentDirectory() throws BlobStoreException
    {
        BlobPath path = BlobPath.of(List.of("nonexistent"));

        try (Stream<Blob> blobs = this.blobStore.listBlobs(path)) {
            assertEquals(0, blobs.count());
        }
    }

    @Test
    void listBlobsInEmptyDirectory() throws IOException, BlobStoreException
    {
        Path dir = this.basePath.resolve("empty");
        Files.createDirectories(dir);
        BlobPath path = BlobPath.of(List.of("empty"));

        try (Stream<Blob> blobs = this.blobStore.listBlobs(path)) {
            assertEquals(0, blobs.count());
        }
    }

    @Test
    void listBlobsWithFiles() throws IOException, BlobStoreException
    {
        Path dir = this.basePath.resolve("testdir");
        Files.createDirectories(dir);
        Files.createFile(dir.resolve("file1.txt"));
        Files.createFile(dir.resolve("file2.txt"));

        Path subdir = dir.resolve("subdir");
        Files.createDirectories(subdir);
        Files.createFile(subdir.resolve("file3.txt"));

        BlobPath path = BlobPath.of(List.of("testdir"));

        try (Stream<Blob> blobs = this.blobStore.listBlobs(path)) {
            List<Blob> blobList = blobs.toList();
            assertEquals(3, blobList.size());

            // Verify blob paths
            List<String> filenames = blobList.stream()
                .map(blob -> blob.getPath().toString())
                .sorted()
                .toList();
            assertEquals(List.of("testdir/file1.txt", "testdir/file2.txt", "testdir/subdir/file3.txt"), filenames);
        }
    }

    @Test
    void copyBlobSameFile()
    {
        BlobPath path = BlobPath.of(List.of("test.txt"));

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.blobStore.copyBlob(path, path));

        assertEquals("source and target paths are the same", exception.getMessage());
    }

    @Test
    void copyBlobSourceNotFound()
    {
        BlobPath sourcePath = BlobPath.of(List.of("nonexistent.txt"));
        BlobPath targetPath = BlobPath.of(List.of("target.txt"));

        BlobNotFoundException blobNotFoundException = assertThrows(BlobNotFoundException.class,
            () -> this.blobStore.copyBlob(sourcePath, targetPath));
        assertEquals(sourcePath, blobNotFoundException.getBlobPath());
    }

    @ParameterizedTest
    @CsvSource({
        "'file.txt', 'copy_of_file.txt'",
        "'dir1/dir2/file.txt', 'dir1/dir2/copy_of_file.txt'",
        "'dir1/file.txt', 'dir1/copy_of_file.txt'"
    })
    void copyBlobSuccess(String source, String target) throws IOException, BlobStoreException
    {
        // Create source file
        Path sourceFile = this.basePath.resolve(source);
        Files.createDirectories(sourceFile.getParent());
        Files.createFile(sourceFile);
        Files.writeString(sourceFile, "test content");

        BlobPath sourcePath = BlobPath.from(source);
        BlobPath targetPath = BlobPath.from(target);

        Blob result = this.blobStore.copyBlob(sourcePath, targetPath);

        // Verify both files exist
        assertTrue(Files.exists(sourceFile));
        Path targetFile = this.basePath.resolve(target);
        assertTrue(Files.exists(targetFile));

        // Verify content
        assertEquals("test content", Files.readString(targetFile));
        assertEquals(targetPath, result.getPath());
    }

    @Test
    void copyBlobTargetAlreadyExists() throws IOException
    {
        // Create both source and target files
        Path sourceFile = this.basePath.resolve("source.txt");
        Path targetFile = this.basePath.resolve("target.txt");
        Files.createFile(sourceFile);
        Files.createFile(targetFile);

        BlobPath sourcePath = BlobPath.of(List.of("source.txt"));
        BlobPath targetPath = BlobPath.of(List.of("target.txt"));

        assertThrows(BlobAlreadyExistsException.class,
            () -> this.blobStore.copyBlob(sourcePath, targetPath));
    }

    @Test
    void copyBlobTargetDirectoryExists() throws IOException, BlobStoreException
    {
        // Create source file and target directory structure
        Path sourceFile = this.basePath.resolve("docs/manual.pdf");
        Files.createDirectories(sourceFile.getParent());
        Files.createFile(sourceFile);
        Files.writeString(sourceFile, "PDF content");

        // Create target directory but not the file
        Path targetDir = this.basePath.resolve("backup/docs");
        Files.createDirectories(targetDir);

        BlobPath sourcePath = BlobPath.of(List.of("docs", "manual.pdf"));
        BlobPath targetPath = BlobPath.of(List.of("backup", "docs", "manual.pdf"));

        Blob result = this.blobStore.copyBlob(sourcePath, targetPath);

        // Verify copy succeeded even though parent directories existed
        Path targetFile = this.basePath.resolve("backup/docs/manual.pdf");
        assertTrue(Files.exists(targetFile));
        assertEquals("PDF content", Files.readString(targetFile));
        assertEquals(targetPath, result.getPath());
    }

    @Test
    void copyBlobFromSameStore() throws IOException, BlobStoreException
    {
        // Create source file
        Path sourceFile = this.basePath.resolve("source.txt");
        Files.createFile(sourceFile);
        Files.writeString(sourceFile, "test content");

        BlobPath sourcePath = BlobPath.of(List.of("source.txt"));
        BlobPath targetPath = BlobPath.of(List.of("target.txt"));

        FileSystemBlobStoreProperties sourceProps = new FileSystemBlobStoreProperties();
        sourceProps.setRootDirectory(this.basePath);
        FileSystemBlobStore sourceStore = new FileSystemBlobStore("source-store", sourceProps);

        Blob result = this.blobStore.copyBlob(sourceStore, sourcePath, targetPath);

        // Verify target file exists
        Path targetFile = this.basePath.resolve("target.txt");
        assertTrue(Files.exists(targetFile));
        assertEquals("test content", Files.readString(targetFile));
        assertEquals(targetPath, result.getPath());
    }

    @Test
    void copyBlobFromDifferentStore() throws Exception
    {
        BlobPath sourcePath = BlobPath.of(List.of("source.txt"));
        BlobPath targetPath = BlobPath.of(List.of("target.txt"));
        String expectedContent = "test content for copy operation";

        when(this.mockSourceStore.getBlob(sourcePath)).thenReturn(this.mockSourceBlob);
        when(this.mockSourceBlob.getStream()).thenReturn(new ByteArrayInputStream(expectedContent.getBytes()));

        Blob result = this.blobStore.copyBlob(this.mockSourceStore, sourcePath, targetPath);

        assertEquals(targetPath, result.getPath());
        verify(this.mockSourceStore).getBlob(sourcePath);
        verify(this.mockSourceBlob).getStream();
        // Verify that no delete method was called on the source store (this is copy, not move)
        verify(this.mockSourceStore, never()).deleteBlob(any());
        verify(this.mockSourceStore, never()).deleteBlobs(any());
    }

    @ParameterizedTest
    @CsvSource({
        "'temp/file1.txt', 'archive/file1.txt', 'Temporary content 1'",
        "'data/logs/app.log', 'backup/logs/2023/app.log', 'Log entry: Application started'",
        "'images/photos/vacation.jpg', 'gallery/2023/vacation.jpg', 'JPEG binary data'"
    })
    void moveBlobWithNestedDirectories(String sourcePath, String targetPath, String content)
        throws IOException, BlobStoreException
    {
        // Create source file with nested directory structure
        BlobPath source = BlobPath.from(sourcePath);
        Path sourceFile = this.basePath.resolve(sourcePath);
        Files.createDirectories(sourceFile.getParent());
        Files.createFile(sourceFile);
        Files.writeString(sourceFile, content);

        BlobPath target = BlobPath.from(targetPath);

        Blob result = this.blobStore.moveBlob(source, target);

        // Verify source file is gone
        assertFalse(Files.exists(sourceFile), "Source file should be deleted: " + sourcePath);

        // Verify target file exists and has correct content
        Path targetFile = this.basePath.resolve(targetPath);
        assertTrue(Files.exists(targetFile), "Target file should exist: " + targetPath);
        assertEquals(content, Files.readString(targetFile), "Content should match");

        // Verify returned blob
        assertEquals(target, result.getPath());
    }

    @Test
    void moveBlobWithDirectoryCleanup() throws IOException, BlobStoreException
    {
        // Create multiple files in nested directories
        String[] filePaths = {
            "temp/data/file1.txt",
            "temp/data/file2.txt",
            "temp/logs/app.log"
        };

        // Create all files
        for (String filePath : filePaths) {
            Path file = this.basePath.resolve(filePath);
            Files.createDirectories(file.getParent());
            Files.createFile(file);
            Files.writeString(file, "Content for " + filePath);
        }

        // Move one file - directories should not be cleaned up yet
        BlobPath sourcePath1 = BlobPath.of(List.of("temp", "data", "file1.txt"));
        BlobPath targetPath1 = BlobPath.of(List.of("archive", "file1.txt"));

        this.blobStore.moveBlob(sourcePath1, targetPath1);

        // Verify first file moved
        assertFalse(Files.exists(this.basePath.resolve("temp/data/file1.txt")));
        assertTrue(Files.exists(this.basePath.resolve("archive/file1.txt")));

        // Verify other files and directories still exist
        assertTrue(Files.exists(this.basePath.resolve("temp/data/file2.txt")));
        assertTrue(Files.exists(this.basePath.resolve("temp/logs/app.log")));
        assertTrue(Files.exists(this.basePath.resolve("temp/data")));
        assertTrue(Files.exists(this.basePath.resolve("temp")));

        // Move second file from data directory
        BlobPath sourcePath2 = BlobPath.of(List.of("temp", "data", "file2.txt"));
        BlobPath targetPath2 = BlobPath.of(List.of("archive", "file2.txt"));

        this.blobStore.moveBlob(sourcePath2, targetPath2);

        // Now the data directory should be cleaned up, but not temp (logs still there)
        assertFalse(Files.exists(this.basePath.resolve("temp/data")), "Empty data directory should be cleaned up");
        assertTrue(Files.exists(this.basePath.resolve("temp")), "Temp directory should still exist (contains logs)");
        assertTrue(Files.exists(this.basePath.resolve("temp/logs/app.log")));
    }

    @Test
    void moveDirectory() throws IOException, BlobStoreException
    {
        // Create source directory structure
        Path sourceDir = this.basePath.resolve("sourceDir");
        Files.createDirectories(sourceDir);
        Files.createFile(sourceDir.resolve("file.txt"));

        BlobPath sourcePath = BlobPath.of(List.of("sourceDir"));
        BlobPath targetPath = BlobPath.of(List.of("targetDir"));

        this.blobStore.moveDirectory(sourcePath, targetPath);

        // Verify source directory is gone
        assertFalse(Files.exists(sourceDir));

        // Verify target directory exists
        Path targetDir = this.basePath.resolve("targetDir");
        assertTrue(Files.exists(targetDir));
        assertTrue(Files.exists(targetDir.resolve("file.txt")));
    }

    @ParameterizedTest
    @CsvSource({
        "'docs', 'archive/docs'",
        "'src/main', 'src-main'",
        "'data/cache', 'data/old-cache'"
    })
    void moveDirectoryWithNestedStructure(String sourcePath, String targetPath) throws IOException, BlobStoreException
    {
        // Create source directory with nested files
        BlobPath source = BlobPath.from(sourcePath);
        Path sourceDir = this.basePath.resolve(sourcePath);
        Files.createDirectories(sourceDir);

        // Create multiple files in the directory
        Path file1 = sourceDir.resolve("file1.txt");
        Path file2 = sourceDir.resolve("file2.txt");
        Path subDir = sourceDir.resolve("subdir");
        Files.createFile(file1);
        Files.createFile(file2);
        Files.createDirectories(subDir);
        Files.createFile(subDir.resolve("nested.txt"));

        Files.writeString(file1, "Content 1");
        Files.writeString(file2, "Content 2");
        Files.writeString(subDir.resolve("nested.txt"), "Nested content");

        BlobPath target = BlobPath.from(targetPath);

        this.blobStore.moveDirectory(source, target);

        // Verify source directory is completely gone
        assertFalse(Files.exists(sourceDir), "Source directory should be deleted: " + sourcePath);

        // Verify target directory and all its contents exist
        Path targetDir = this.basePath.resolve(targetPath);
        assertTrue(Files.exists(targetDir), "Target directory should exist: " + targetPath);
        assertTrue(Files.exists(targetDir.resolve("file1.txt")));
        assertTrue(Files.exists(targetDir.resolve("file2.txt")));
        assertTrue(Files.exists(targetDir.resolve("subdir")));
        assertTrue(Files.exists(targetDir.resolve("subdir/nested.txt")));

        // Verify content preservation
        assertEquals("Content 1", Files.readString(targetDir.resolve("file1.txt")));
        assertEquals("Content 2", Files.readString(targetDir.resolve("file2.txt")));
        assertEquals("Nested content", Files.readString(targetDir.resolve("subdir/nested.txt")));
    }

    @Test
    void moveDirectorySameSourceAndTarget()
    {
        BlobPath path = BlobPath.of(List.of("dir"));

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.blobStore.moveDirectory(path, path));

        assertEquals("source and target paths are the same", exception.getMessage());
    }

    @Test
    void moveDirectoryTargetExists() throws IOException
    {
        // Create source and target directories
        Path sourceDir = this.basePath.resolve("source");
        Path targetDir = this.basePath.resolve("target");
        Files.createDirectories(sourceDir);
        Files.createDirectories(targetDir);
        Files.createFile(sourceDir.resolve("file.txt"));

        BlobPath sourcePath = BlobPath.of(List.of("source"));
        BlobPath targetPath = BlobPath.of(List.of("target"));

        // Should throw exception when target already exists
        assertThrows(BlobAlreadyExistsException.class,
            () -> this.blobStore.moveDirectory(sourcePath, targetPath));

        // Verify source directory still exists
        assertTrue(Files.exists(sourceDir));
        assertTrue(Files.exists(sourceDir.resolve("file.txt")));
    }

    @Test
    void isEmptyDirectoryTrue() throws IOException, BlobStoreException
    {
        Path dir = this.basePath.resolve("empty");
        Files.createDirectories(dir);
        BlobPath path = BlobPath.of(List.of("empty"));

        assertTrue(this.blobStore.isEmptyDirectory(path));
    }

    @Test
    void isEmptyDirectoryFalse() throws IOException, BlobStoreException
    {
        Path dir = this.basePath.resolve("nonempty");
        Files.createDirectories(dir);
        Files.createFile(dir.resolve("file.txt"));
        BlobPath path = BlobPath.of(List.of("nonempty"));

        assertFalse(this.blobStore.isEmptyDirectory(path));
    }

    @Test
    void isEmptyDirectoryWithNestedEmptyDirectories() throws IOException, BlobStoreException
    {
        // Create nested empty directories
        Path dir = this.basePath.resolve("parent/child/grandchild");
        Files.createDirectories(dir);
        BlobPath path = BlobPath.of(List.of("parent"));

        // Directory with only empty subdirectories should be considered empty
        assertTrue(this.blobStore.isEmptyDirectory(path));
    }

    @Test
    void moveBlobSameSourceAndTarget()
    {
        BlobPath path = BlobPath.of(List.of("test.txt"));

        BlobStoreException exception = assertThrows(BlobStoreException.class,
            () -> this.blobStore.moveBlob(path, path));

        assertEquals("source and target paths are the same", exception.getMessage());
    }

    @Test
    void moveBlobSourceNotFound()
    {
        BlobPath sourcePath = BlobPath.of(List.of("nonexistent", "file.txt"));
        BlobPath targetPath = BlobPath.of(List.of("target", "target.txt"));

        BlobNotFoundException exception = assertThrows(BlobNotFoundException.class,
            () -> this.blobStore.moveBlob(sourcePath, targetPath));
        assertEquals(sourcePath, exception.getBlobPath());
        // Verify that the target directory has been cleaned up if it was created
        Path targetDir = this.basePath.resolve("target");
        assertFalse(Files.exists(targetDir), "Target directory should not exist after failed move");
    }

    @Test
    void moveBlobTargetAlreadyExists() throws IOException
    {
        // Create source and target files
        Path sourceFile = this.basePath.resolve("docs/source.txt");
        Path targetFile = this.basePath.resolve("backup/target.txt");
        Files.createDirectories(sourceFile.getParent());
        Files.createDirectories(targetFile.getParent());
        Files.createFile(sourceFile);
        Files.createFile(targetFile);

        BlobPath sourcePath = BlobPath.of(List.of("docs", "source.txt"));
        BlobPath targetPath = BlobPath.of(List.of("backup", "target.txt"));

        assertThrows(BlobAlreadyExistsException.class,
            () -> this.blobStore.moveBlob(sourcePath, targetPath));

        // Verify source file still exists after failed move
        assertTrue(Files.exists(sourceFile));
    }

    @Test
    void moveBlobFromDifferentStore() throws Exception
    {
        BlobPath sourcePath = BlobPath.of(List.of("source.txt"));
        BlobPath targetPath = BlobPath.of(List.of("target.txt"));
        String expectedContent = "test content for move operation";

        when(this.mockSourceStore.getBlob(sourcePath)).thenReturn(this.mockSourceBlob);
        when(this.mockSourceBlob.getStream()).thenReturn(new ByteArrayInputStream(expectedContent.getBytes()));

        Blob result = this.blobStore.moveBlob(this.mockSourceStore, sourcePath, targetPath);

        assertEquals(targetPath, result.getPath());
        verify(this.mockSourceStore).getBlob(sourcePath);
        verify(this.mockSourceStore).deleteBlob(sourcePath);

        // Verify that the content has been written to the target location
        Path targetFile = this.basePath.resolve("target.txt");
        assertTrue(Files.exists(targetFile));
        assertEquals(expectedContent, Files.readString(targetFile));
    }

    @Test
    void moveBlobFromDifferentStoreTargetExists() throws Exception
    {
        // Create target file that will cause the move to fail
        Path targetFile = this.basePath.resolve("target.txt");
        Files.createFile(targetFile);

        BlobPath sourcePath = BlobPath.of(List.of("source.txt"));
        BlobPath targetPath = BlobPath.of(List.of("target.txt"));

        when(this.mockSourceStore.getBlob(sourcePath)).thenReturn(this.mockSourceBlob);
        when(this.mockSourceBlob.getStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

        assertThrows(BlobAlreadyExistsException.class,
            () -> this.blobStore.moveBlob(this.mockSourceStore, sourcePath, targetPath));

        // Verify that delete was not called on the source store
        verify(this.mockSourceStore, never()).deleteBlob(sourcePath);

        // Verify that the target file still exists and is unchanged
        assertTrue(Files.exists(targetFile));
        assertEquals(0, Files.size(targetFile));
    }

    @Test
    void moveBlobFromDifferentFileSystemStore() throws IOException, BlobStoreException
    {
        // Create a second FileSystemBlobStore with different base path.
        Path sourceBasePath = this.tmpDir.toPath().resolve("source-store");
        Files.createDirectories(sourceBasePath);
        FileSystemBlobStoreProperties sourceStoreProps = new FileSystemBlobStoreProperties();
        sourceStoreProps.setRootDirectory(sourceBasePath);
        FileSystemBlobStore sourceStore = new FileSystemBlobStore("source-store", sourceStoreProps);

        // Create source file in the source store
        BlobPath sourcePath = BlobPath.of(List.of("documents", "file.txt"));
        Path sourceFile = sourceBasePath.resolve("documents/file.txt");
        Files.createDirectories(sourceFile.getParent());
        Files.createFile(sourceFile);
        String expectedContent = "Content from different FileSystemBlobStore";
        Files.writeString(sourceFile, expectedContent);

        BlobPath targetPath = BlobPath.of(List.of("moved", "file.txt"));

        Blob result = this.blobStore.moveBlob(sourceStore, sourcePath, targetPath);

        // Verify source file is gone
        assertFalse(Files.exists(sourceFile));
        // Verify source directory is cleaned up
        assertFalse(Files.exists(sourceBasePath.resolve("documents")));

        // Verify target file exists in this store
        Path targetFile = this.basePath.resolve("moved/file.txt");
        assertTrue(Files.exists(targetFile));
        assertEquals(expectedContent, Files.readString(targetFile));
        assertEquals(targetPath, result.getPath());
    }

    @Test
    void moveDirectoryFromDifferentFileSystemStore() throws IOException, BlobStoreException
    {
        // Create a second FileSystemBlobStore with different base path
        Path sourceBasePath = this.tmpDir.toPath().resolve("source-store");
        Files.createDirectories(sourceBasePath);
        FileSystemBlobStoreProperties sourceStoreProps = new FileSystemBlobStoreProperties();
        sourceStoreProps.setRootDirectory(sourceBasePath);
        FileSystemBlobStore sourceStore = new FileSystemBlobStore("source-store", sourceStoreProps);

        // Create source directory structure in the source store
        BlobPath sourcePath = BlobPath.of(List.of("project"));
        Path sourceDir = sourceBasePath.resolve("project");
        Files.createDirectories(sourceDir);

        // Create multiple files in the source directory
        Path file1 = sourceDir.resolve("app.java");
        Path file2 = sourceDir.resolve("config.xml");
        Path subDir = sourceDir.resolve("lib");
        Files.createFile(file1);
        Files.createFile(file2);
        Files.createDirectories(subDir);
        Files.createFile(subDir.resolve("library.jar"));

        Files.writeString(file1, "Java application code");
        Files.writeString(file2, "XML configuration");
        Files.writeString(subDir.resolve("library.jar"), "JAR content");

        BlobPath targetPath = BlobPath.of(List.of("migrated-project"));

        this.blobStore.moveDirectory(sourceStore, sourcePath, targetPath);

        // Verify source directory is completely gone
        assertFalse(Files.exists(sourceDir));

        // Verify target directory and all its contents exist in this store
        Path targetDir = this.basePath.resolve("migrated-project");
        assertTrue(Files.exists(targetDir));
        assertTrue(Files.exists(targetDir.resolve("app.java")));
        assertTrue(Files.exists(targetDir.resolve("config.xml")));
        assertTrue(Files.exists(targetDir.resolve("lib")));
        assertTrue(Files.exists(targetDir.resolve("lib/library.jar")));

        // Verify content preservation
        assertEquals("Java application code", Files.readString(targetDir.resolve("app.java")));
        assertEquals("XML configuration", Files.readString(targetDir.resolve("config.xml")));
        assertEquals("JAR content", Files.readString(targetDir.resolve("lib/library.jar")));
    }

    @Test
    void moveDirectoryFromDifferentStoreType() throws Exception
    {
        BlobPath sourcePath = BlobPath.of(List.of("docs"));
        BlobPath targetPath = BlobPath.of(List.of("archived-docs"));

        // Create mock blobs for the directory contents
        List<Blob> blobs = List.of(mock(), mock(), mock());
        List<BlobPath> blobPaths = List.of(
            BlobPath.of(List.of("docs", "readme.txt")),
            BlobPath.of(List.of("docs", "guide.md")),
            BlobPath.of(List.of("docs", "assets", "logo.png"))
        );
        List<String> contents = List.of("README content", "Guide markdown content", "PNG image data");

        for (int i = 0; i < blobs.size(); i++) {
            when(blobs.get(i).getPath()).thenReturn(blobPaths.get(i));
            when(blobs.get(i).getStream()).thenReturn(new ByteArrayInputStream(contents.get(i).getBytes()));
            when(this.mockSourceStore.getBlob(blobPaths.get(i))).thenReturn(blobs.get(i));
        }

        // Mock the listBlobs method to return our test blobs
        when(this.mockSourceStore.listBlobs(sourcePath)).thenReturn(blobs.stream());

        this.blobStore.moveDirectory(this.mockSourceStore, sourcePath, targetPath);

        // Verify all blobs were retrieved and deleted from source
        verify(this.mockSourceStore).listBlobs(sourcePath);
        for (BlobPath blobPath : blobPaths) {
            verify(this.mockSourceStore).getBlob(blobPath);
            verify(this.mockSourceStore).deleteBlob(blobPath);
        }

        // Verify target files exist in this store with correct content
        Path targetDir = this.basePath.resolve("archived-docs");
        List<Path> targetFiles = List.of(
            targetDir.resolve("readme.txt"),
            targetDir.resolve("guide.md"),
            targetDir.resolve("assets/logo.png")
        );
        for (int i = 0; i < targetFiles.size(); i++) {
            Path file = targetFiles.get(i);
            String content = contents.get(i);
            assertTrue(Files.exists(file), "Target file should exist: " + file);
            assertEquals(content, Files.readString(file), "Content should match for file: " + file);
        }
    }

    @Test
    void deleteBlob() throws IOException, BlobStoreException
    {
        // Create test file in nested directory
        Path dir = this.basePath.resolve("dir1").resolve("dir2");
        Files.createDirectories(dir);
        Path file = dir.resolve("test.txt");
        Files.createFile(file);

        BlobPath blobPath = BlobPath.of(List.of("dir1", "dir2", "test.txt"));

        this.blobStore.deleteBlob(blobPath);

        // Verify file is deleted
        assertFalse(Files.exists(file));

        // Parent directories should be cleaned up if empty
        assertFalse(Files.exists(dir));
        assertFalse(Files.exists(this.basePath.resolve("dir1")));
    }

    @Test
    void deleteBlobNonExistent()
    {
        BlobPath blobPath = BlobPath.of(List.of("nonexistent.txt"));

        assertDoesNotThrow(() -> this.blobStore.deleteBlob(blobPath));
    }

    @Test
    void deleteBlobs() throws IOException, BlobStoreException
    {
        // Create directory with files
        Path dir = this.basePath.resolve("testdir");
        Files.createDirectories(dir);
        Files.createFile(dir.resolve("file1.txt"));
        Files.createFile(dir.resolve("file2.txt"));

        BlobPath blobPath = BlobPath.of(List.of("testdir"));

        this.blobStore.deleteBlobs(blobPath);

        // Verify directory and all files are deleted
        assertFalse(Files.exists(dir));
    }

    @Test
    void deleteBlobsNonExistentDirectory()
    {
        BlobPath blobPath = BlobPath.of(List.of("nonexistent"));

        // Should not throw exception
        assertDoesNotThrow(() -> this.blobStore.deleteBlobs(blobPath));
    }

    @Test
    void createParents() throws IOException
    {
        Path directory = this.basePath.resolve("dir1").resolve("dir2");
        Path targetPath = directory.resolve("file.txt");

        this.blobStore.createParents(targetPath);

        assertTrue(Files.exists(directory));
        assertTrue(Files.isDirectory(directory));
    }

    @Test
    void cleanUpParents() throws IOException
    {
        // Create nested empty directories
        Path nestedDir = this.basePath.resolve("dir1").resolve("dir2").resolve("dir3");
        Files.createDirectories(nestedDir);

        Path filePath = nestedDir.resolve("file.txt");

        this.blobStore.cleanUpParents(filePath);

        // Empty directories should be cleaned up, but not the base path
        assertTrue(Files.exists(this.basePath));
        assertFalse(Files.exists(nestedDir));
        assertFalse(Files.exists(this.basePath.resolve("dir1").resolve("dir2")));
        assertFalse(Files.exists(this.basePath.resolve("dir1")));
    }

    @Test
    void cleanUpParentsWithNonEmptyDirectory() throws IOException
    {
        // Create nested directories with a file in the middle
        Path dir1 = this.basePath.resolve("dir1");
        Path dir2 = dir1.resolve("dir2");
        Path dir3 = dir2.resolve("dir3");
        Files.createDirectories(dir3);

        // Add a file to dir2 to make it non-empty
        Files.createFile(dir2.resolve("keepme.txt"));

        Path filePath = dir3.resolve("file.txt");

        this.blobStore.cleanUpParents(filePath);

        // Only empty dir3 should be cleaned up
        assertTrue(Files.exists(dir2));
        assertTrue(Files.exists(dir2.resolve("keepme.txt")));
        assertFalse(Files.exists(dir3));
    }

    @Test
    void equalsAndHashCode()
    {
        FileSystemBlobStoreProperties props1 = new FileSystemBlobStoreProperties();
        props1.setRootDirectory(this.basePath);
        FileSystemBlobStore store1 = new FileSystemBlobStore("store1", props1);
        FileSystemBlobStoreProperties props2 = new FileSystemBlobStoreProperties();
        props2.setRootDirectory(this.basePath);
        FileSystemBlobStore store2 = new FileSystemBlobStore("store2", props2);
        FileSystemBlobStoreProperties props3 = new FileSystemBlobStoreProperties();
        props3.setRootDirectory(this.tmpDir.toPath().resolve("different"));
        FileSystemBlobStore store3 = new FileSystemBlobStore("store3", props3);

        // Equals based on base path only
        assertEquals(store1, store2);
        assertNotEquals(store1, store3);

        // Hash code based on base path only
        assertEquals(store1.hashCode(), store2.hashCode());
        assertNotEquals(store1.hashCode(), store3.hashCode());

        // Not equals to null or different type
        assertNotEquals(store1, null);
        assertNotEquals(store1, "string");

        // Self equality
        assertEquals(store1, store1);
    }

    // The following tests use Mockito to mock static Files methods to simulate failures and retries. We only do this
    // for the move blob operation as the other operations use the same underlying methods and would be redundant to
    // test.

    @Test
    void moveBlobRetriesOnNoSuchFileExceptionForParentDirectory() throws BlobStoreException
    {
        BlobPath sourcePath = BlobPath.of(List.of("source.txt"));
        BlobPath targetPath = BlobPath.of(List.of("nested", "dir", "target.txt"));
        Path absoluteSourcePath = this.basePath.resolve("source.txt");
        Path absoluteTargetPath = this.basePath.resolve("nested/dir/target.txt");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            // Mock that source file exists to avoid that we abort because the source is missing.
            filesMock.when(() -> Files.exists(absoluteSourcePath)).thenReturn(true);

            // First 2 attempts throw NoSuchFileException, third succeeds
            filesMock.when(() -> Files.move(any(Path.class), any(Path.class)))
                .thenThrow(new NoSuchFileException("nested/dir"))
                .thenThrow(new NoSuchFileException("nested/dir"))
                .thenReturn(absoluteTargetPath);

            // Should succeed after retries
            Blob result = this.blobStore.moveBlob(sourcePath, targetPath);

            assertEquals(targetPath, result.getPath());

            // Verify move was attempted 3 times.
            filesMock.verify(() -> Files.move(absoluteSourcePath, absoluteTargetPath), times(3));
            // Verify that on each retry, the parent directories were created.
            filesMock.verify(() -> Files.createDirectories(absoluteTargetPath.getParent()), times(3));
        }
    }

    @Test
    void moveBlobFailsAfterMaxRetries()
    {
        BlobPath sourcePath = BlobPath.of(List.of("source.txt"));
        BlobPath targetPath = BlobPath.of(List.of("nested", "target.txt"));

        try (MockedStatic<Files> filesMock = mockStatic(Files.class);
             MockedStatic<PathUtils> pathUtilsMock = mockStatic(PathUtils.class))
        {
            // Mock that all directories exist and are empty to trigger cleanup.
            filesMock.when(() -> Files.exists(any())).thenReturn(true);
            filesMock.when(() -> Files.isDirectory(any())).thenReturn(true);
            pathUtilsMock.when(() -> PathUtils.isEmptyDirectory(any())).thenReturn(true);

            // Always throw NoSuchFileException to exceed retry limit
            filesMock.when(() -> Files.move(any(Path.class), any(Path.class)))
                .thenThrow(new NoSuchFileException("nested"));

            BlobStoreException exception = assertThrows(BlobStoreException.class,
                () -> this.blobStore.moveBlob(sourcePath, targetPath));

            assertEquals("move blob failed after 5 attempts", exception.getMessage());
            assertInstanceOf(NoSuchFileException.class, exception.getCause());
            // Verify cleanup attempted on target directory.
            filesMock.verify(() -> Files.deleteIfExists(this.basePath.resolve("nested")));
        }
    }

    @Test
    void moveDirectoryWithIOExceptionDuringMoveTriggersCleanup()
    {
        BlobPath sourcePath = BlobPath.of(List.of("sourceDir"));
        BlobPath targetPath = BlobPath.of(List.of("nested", "targetDir"));
        Path absoluteTargetPath = this.basePath.resolve("nested/targetDir");

        try (MockedStatic<Files> filesMock = mockStatic(Files.class);
             MockedStatic<PathUtils> pathUtilsMock = mockStatic(PathUtils.class))
        {
            // Mock that all directories exist and are empty to trigger cleanup.
            filesMock.when(() -> Files.exists(any())).thenReturn(true);
            filesMock.when(() -> Files.isDirectory(any())).thenReturn(true);
            pathUtilsMock.when(() -> PathUtils.isEmptyDirectory(any())).thenReturn(true);

            // Mock Files.move throws IOException to simulate I/O error during directory move
            filesMock.when(() -> Files.move(any(Path.class), any(Path.class)))
                .thenThrow(new IOException("I/O error during directory move"));

            BlobStoreException exception = assertThrows(BlobStoreException.class,
                () -> this.blobStore.moveDirectory(sourcePath, targetPath));

            assertEquals("move blob failed", exception.getMessage());
            assertInstanceOf(IOException.class, exception.getCause());

            // We shouldn't have deleted the target directory as we never do that.
            filesMock.verify(() -> Files.deleteIfExists(absoluteTargetPath), never());
            // But we should have tried to clean up the parent directories if they were empty.
            filesMock.verify(() -> Files.deleteIfExists(absoluteTargetPath.getParent()));
            // Cleaning should stop at the base path, so it should not be deleted.
            filesMock.verify(() -> Files.deleteIfExists(this.basePath), never());
        }
    }
}
