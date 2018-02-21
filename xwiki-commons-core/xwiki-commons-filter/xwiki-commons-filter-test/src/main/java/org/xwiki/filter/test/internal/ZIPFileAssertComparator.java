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
package org.xwiki.filter.test.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;

/**
 * @version $Id$
 * @since 6.2M1
 */
public class ZIPFileAssertComparator implements FileAssertComparator
{
    public static boolean isZip(File file) throws IOException
    {
        final byte[] signature = new byte[12];

        int signatureLength;
        try (FileInputStream stream = new FileInputStream(file)) {
            stream.mark(signature.length);
            signatureLength = stream.read(signature);
        }

        return ZipArchiveInputStream.matches(signature, signatureLength);
    }

    private static Map<String, byte[]> unzip(File filename) throws IOException
    {
        Map<String, byte[]> zipContent = new HashMap<String, byte[]>();

        ZipFile zipFile = new ZipFile(filename);

        try {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();
            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();

                InputStream inputStream = zipFile.getInputStream(entry);
                try {
                    zipContent.put(entry.getName(), IOUtils.toByteArray(inputStream));
                } finally {
                    inputStream.close();
                }
            }
        } finally {
            zipFile.close();
        }

        return zipContent;
    }

    /**
     * Asserts that two ZIP files are equal. If they are not, an {@link AssertionError} without a message is thrown.
     */
    @Override
    public void assertEquals(String message, File expected, File actual)
    {
        Assertions.assertNotNull(expected);
        Assertions.assertNotNull(actual);

        Assertions.assertTrue(expected.exists(), "Expected file does not exist [" + expected.getAbsolutePath() + "]");
        Assertions.assertTrue(actual.exists(), "Actual file does not exist [" + actual.getAbsolutePath() + "]");

        Assertions.assertTrue(expected.canRead(), "Expected file not readable");
        Assertions.assertTrue(actual.canRead(), "Actual file not readable");

        try {
            Map<String, byte[]> expectedMap = unzip(expected);
            Map<String, byte[]> actualMap = unzip(actual);

            for (Map.Entry<String, byte[]> expectedEntry : expectedMap.entrySet()) {
                byte[] actualContent = actualMap.get(expectedEntry.getKey());

                Assertions.assertNotNull(actualContent, "Entry [" + expectedEntry.getKey() + "] not present");

                FileAssertComparator fileAssertComparator = FileAssert.getComparator(expectedEntry.getKey());

                fileAssertComparator.assertEquals("Entry [" + expectedEntry.getKey() + "] has different content",
                    expectedEntry.getValue(), actualContent);
            }

            Assertions.assertEquals(expectedMap.size(), actualMap.size(), "Too many entries");
        } catch (IOException e) {
            throw new AssertionFailedError(e.toString());
        }
    }

    @Override
    public void assertEquals(String message, byte[] expected, byte[] actual) throws IOException
    {
        File actualFile = File.createTempFile("actual", ".actual");

        try {
            FileUtils.writeByteArrayToFile(actualFile, actual);

            File expectedFile = File.createTempFile("expected", ".expected");

            try {
                FileUtils.writeByteArrayToFile(expectedFile, expected);

                assertEquals(message, expectedFile, actualFile);
            } finally {
                expectedFile.delete();
            }
        } finally {
            actualFile.delete();
        }
    }
}
