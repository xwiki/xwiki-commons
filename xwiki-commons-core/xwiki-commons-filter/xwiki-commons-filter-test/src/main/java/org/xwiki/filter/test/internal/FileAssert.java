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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.xwiki.test.XWikiTempDirUtil;

/**
 * Files related test tools.
 * 
 * @version $Id$
 * @since 6.2M1
 */
public final class FileAssert
{
    /** The temporary directory in which actual files are generated before being compared. */
    public static final File TEMPORARY_DIRECTORY = XWikiTempDirUtil.createTemporaryDirectory();

    private static final Map<String, FileAssertComparator> COMPARATORS = new HashMap<>();

    private static final DefaultFileAssertComparator DEFAULT_COMPARATOR = new DefaultFileAssertComparator();

    static {
        setStringComparator("txt");
        setStringComparator("xml");
        setStringComparator("properties");

        setZIPComparator("xar");
        setZIPComparator("jar");
        setZIPComparator("zip");
    }

    private FileAssert()
    {
    }

    /**
     * @param extension the file extension to associate the comparator with
     * @param comparator the comparator to use for files with the passed extension
     */
    public static void setComparator(String extension, FileAssertComparator comparator)
    {
        COMPARATORS.put(extension, comparator);
    }

    /**
     * @param extension the file extension to compare as text
     */
    public static void setStringComparator(String extension)
    {
        setComparator(extension, new StringFileAssertComparator());
    }

    /**
     * @param extension the file extension to compare as a ZIP archive
     */
    public static void setZIPComparator(String extension)
    {
        setComparator(extension, new ZIPFileAssertComparator());
    }

    /**
     * @param filename the name of the file for which to find a comparator
     * @return the comparator associated with the file extension, or a default one when none is registered
     */
    public static FileAssertComparator getComparator(String filename)
    {
        String extension = FilenameUtils.getExtension(filename);

        FileAssertComparator comparator = COMPARATORS.get(extension);
        if (comparator == null) {
            comparator = DEFAULT_COMPARATOR;
        }

        return comparator;
    }

    /**
     * Asserts that two files are equal. If they are not, an {@link AssertionError} without a message is thrown.
     *
     * @param expected the expected file
     * @param actual the actual content
     * @throws IOException when failing to compare the files
     */
    public static void assertEquals(File expected, byte[] actual) throws IOException
    {
        File actualFile = File.createTempFile(expected.getName(), ".actual", TEMPORARY_DIRECTORY);

        try {
            FileUtils.writeByteArrayToFile(actualFile, actual);

            assertEquals(expected, actualFile);
        } finally {
            actualFile.delete();
        }
    }

    /**
     * Asserts that two ZIP files are equal. If they are not, an {@link AssertionError} without a message is thrown.
     *
     * @param expected the expected file
     * @param actual the actual file
     * @throws IOException when failing to compare the files
     */
    public static void assertEquals(File expected, File actual) throws IOException
    {
        getComparator(expected.getName()).assertEquals(null, expected, actual);
    }
}
