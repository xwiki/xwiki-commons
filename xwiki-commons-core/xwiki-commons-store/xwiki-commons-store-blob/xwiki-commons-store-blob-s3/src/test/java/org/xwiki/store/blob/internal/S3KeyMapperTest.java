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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.xwiki.store.blob.BlobPath;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link S3KeyMapper}.
 *
 * @version $Id$
 */
class S3KeyMapperTest
{
    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @ParameterizedTest
    @CsvSource({
        ",path/to/blob,path/to/blob",
        "'',path/to/blob,path/to/blob",
        "'   ',path/to/blob,path/to/blob",
        "my-prefix,path/to/blob,my-prefix/path/to/blob",
        "my/nested/prefix,path/to/blob,my/nested/prefix/path/to/blob",
        "prefix,blob,prefix/blob",
        "prefix/,blob,prefix/blob",
        "' //prefix// ',blob,prefix/blob"
    })
    void buildS3Key(String keyPrefix, String blobPathStr, String expectedS3Key)
    {
        S3KeyMapper mapper = new S3KeyMapper(keyPrefix);
        BlobPath path = BlobPath.from(blobPathStr);

        String s3Key = mapper.buildS3Key(path);

        assertEquals(expectedS3Key, s3Key);
    }

    @ParameterizedTest
    @CsvSource({
        "prefix,path/to,prefix/path/to/",
        "prefix,path/to/,prefix/path/to/",
        ",path/to,path/to/",
        "prefix,'',prefix/",
        "prefix/,blob/,prefix/blob/"
    })
    void getS3KeyPrefix(String keyPrefix, String blobPathStr, String expectedPrefix)
    {
        S3KeyMapper mapper = new S3KeyMapper(keyPrefix);
        BlobPath path = BlobPath.from(blobPathStr);

        String prefix = mapper.getS3KeyPrefix(path);

        assertEquals(expectedPrefix, prefix);
    }

    @ParameterizedTest
    @CsvSource({
        ",path/to/blob,path/to/blob",
        "'',path/to/blob,path/to/blob",
        "my-prefix,my-prefix/path/to/blob,path/to/blob",
        "my/nested/prefix,my/nested/prefix/path/to/blob,path/to/blob"
    })
    void s3KeyToBlobPathValid(String keyPrefix, String s3Key, String expectedBlobPath)
    {
        S3KeyMapper mapper = new S3KeyMapper(keyPrefix);

        BlobPath path = mapper.s3KeyToBlobPath(s3Key);

        assertNotNull(path);
        assertEquals(expectedBlobPath, path.toString());
    }

    @ParameterizedTest
    @CsvSource({
        "expected-prefix,different-prefix/path/to/blob",
        "prefix,prefix-extended/path/to/blob"
    })
    void s3KeyToBlobPathWithMismatchedPrefix(String keyPrefix, String s3Key)
    {
        S3KeyMapper mapper = new S3KeyMapper(keyPrefix);

        BlobPath path = mapper.s3KeyToBlobPath(s3Key);

        assertNull(path);
    }

    @Test
    void s3KeyToBlobPathWithInvalidPath()
    {
        S3KeyMapper mapper = new S3KeyMapper("prefix");
        String s3Key = "prefix/invalid/../path";

        BlobPath path = mapper.s3KeyToBlobPath(s3Key);

        assertNull(path);
        assertEquals("Invalid blob path from S3 key: prefix/invalid/../path", this.logCapture.getMessage(0));
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"prefix", "my/nested/prefix"})
    void s3KeyToBlobPathRoundTrip(String keyPrefix)
    {
        S3KeyMapper mapper = new S3KeyMapper(keyPrefix);
        BlobPath originalPath = BlobPath.from("path/to/blob");

        String s3Key = mapper.buildS3Key(originalPath);
        BlobPath reconstructedPath = mapper.s3KeyToBlobPath(s3Key);

        assertNotNull(reconstructedPath);
        assertEquals(originalPath.toString(), reconstructedPath.toString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"prefix", "my/nested/prefix"})
    @NullAndEmptySource
    void equalsWithSamePrefix(String prefix)
    {
        S3KeyMapper mapper1 = new S3KeyMapper(prefix);
        S3KeyMapper mapper2 = new S3KeyMapper(prefix);

        assertEquals(mapper1, mapper2);
        assertEquals(mapper1.hashCode(), mapper2.hashCode());
    }

    @ParameterizedTest
    @ValueSource(strings = {" prefix ", " /prefix/  ", " //prefix/", "/prefix/"})
    void equalsWithEquivalentPrefixes(String prefix)
    {
        S3KeyMapper mapper1 = new S3KeyMapper(prefix);
        S3KeyMapper mapper2 = new S3KeyMapper("prefix");

        assertEquals(mapper1, mapper2);
        assertEquals(mapper1.hashCode(), mapper2.hashCode());
    }

    @Test
    void equalsWithDifferentPrefix()
    {
        S3KeyMapper mapper1 = new S3KeyMapper("prefix1");
        S3KeyMapper mapper2 = new S3KeyMapper("prefix2");

        assertNotEquals(mapper1, mapper2);
    }

    @Test
    void equalsWithSameInstance()
    {
        S3KeyMapper mapper = new S3KeyMapper("prefix");

        assertEquals(mapper, mapper);
    }

    @Test
    void equalsWithNull()
    {
        S3KeyMapper mapper = new S3KeyMapper("prefix");

        assertNotEquals(null, mapper);
    }

    @Test
    void equalsWithDifferentClass()
    {
        S3KeyMapper mapper = new S3KeyMapper("prefix");
        String other = "not a mapper";

        assertNotEquals(mapper, other);
    }

    @Test
    void hashCodeConsistency()
    {
        S3KeyMapper mapper = new S3KeyMapper("prefix");

        int hashCode1 = mapper.hashCode();
        int hashCode2 = mapper.hashCode();

        assertEquals(hashCode1, hashCode2);
    }
}
