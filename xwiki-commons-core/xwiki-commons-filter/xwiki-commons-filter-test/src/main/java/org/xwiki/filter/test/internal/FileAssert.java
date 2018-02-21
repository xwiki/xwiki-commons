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

/**
 * Files related test tools.
 * 
 * @version $Id$
 * @since 6.2M1
 */
public final class FileAssert
{
    private final static Map<String, FileAssertComparator> COMPARATORS = new HashMap<String, FileAssertComparator>();

    private final static DefaultFileAssertComparator DEFAULT_COMPARATOR = new DefaultFileAssertComparator();

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

    public static void setComparator(String extension, FileAssertComparator comparator)
    {
        COMPARATORS.put(extension, comparator);
    }

    public static void setStringComparator(String extension)
    {
        setComparator(extension, new StringFileAssertComparator());
    }

    public static void setZIPComparator(String extension)
    {
        setComparator(extension, new ZIPFileAssertComparator());
    }

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
     */
    public static void assertEquals(File expected, byte[] actual) throws IOException
    {
        File actualFile = File.createTempFile(expected.getName(), ".actual");

        try {
            FileUtils.writeByteArrayToFile(actualFile, actual);

            assertEquals(expected, actualFile);
        } finally {
            actualFile.delete();
        }
    }

    /**
     * Asserts that two ZIP files are equal. If they are not, an {@link AssertionError} without a message is thrown.
     */
    public static void assertEquals(File expected, File actual) throws IOException
    {
        getComparator(expected.getName()).assertEquals(null, expected, actual);
    }
}
