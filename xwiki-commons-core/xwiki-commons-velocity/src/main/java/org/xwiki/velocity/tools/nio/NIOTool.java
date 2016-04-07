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
package org.xwiki.velocity.tools.nio;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Map;

/**
 * Provides access to some of the static NIO2 Files methods.
 *
 * @version $Id$
 * @since 7.4M2
 */
public class NIOTool
{
    /**
     * See {@link Files#newDirectoryStream(Path)}.
     * <p>
     * Velocity Example:
     * </p>
     * <code><pre>
     * #set ($dirStream = $niotool.newDirectoryStream("attach:Sandbox.WebHome@vma.txt.zip/"))
     * #foreach ($entry in $dirStream)
     *   * {{{$entry}}}
     * #end
     * </pre></code>
     *
     * @param dir See {@link Files#newDirectoryStream(Path)}
     * @return See {@link Files#newDirectoryStream(Path)}
     */
    public DirectoryStream<Path> newDirectoryStream(Path dir)
    {
        try {
            return new WrappingDirectoryStream(Files.newDirectoryStream(dir));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * See {@link Files#newDirectoryStream(Path, DirectoryStream.Filter)}.
     *
     * @param dir See {@link Files#newDirectoryStream(Path, DirectoryStream.Filter)}
     * @param filter See {@link Files#newDirectoryStream(Path, DirectoryStream.Filter)}
     * @return See {@link Files#newDirectoryStream(Path, DirectoryStream.Filter)}
     */
    public DirectoryStream<Path> newDirectoryStream(Path dir, DirectoryStream.Filter filter)
    {
        try {
            return new WrappingDirectoryStream(Files.newDirectoryStream(dir, filter));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * See {@link Files#newDirectoryStream(Path, String)}.
     *
     * @param dir See {@link Files#newDirectoryStream(Path, String)}
     * @param glob See {@link Files#newDirectoryStream(Path, String)}
     * @return See {@link Files#newDirectoryStream(Path, String)}
     */
    public DirectoryStream<Path> newDirectoryStream(Path dir, String glob)
    {
        try {
            return new WrappingDirectoryStream(Files.newDirectoryStream(dir, glob));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * See {@link Files#isDirectory(Path, LinkOption...)}.
     *
     * @param path See {@link Files#isDirectory(Path, LinkOption...)}
     * @param options See {@link Files#isDirectory(Path, LinkOption...)}
     * @return See {@link Files#isDirectory(Path, LinkOption...)}
     */
    public boolean isDirectory(Path path, LinkOption... options)
    {
        return Files.isDirectory(path, options);
    }

    /**
     * See {@link Files#isRegularFile(Path, LinkOption...)}.
     *
     * @param path See {@link Files#isRegularFile(Path, LinkOption...)}
     * @param options See {@link Files#isRegularFile(Path, LinkOption...)}
     * @return See {@link Files#isRegularFile(Path, LinkOption...)}
     */
    public boolean isRegularFile(Path path, LinkOption... options)
    {
        return Files.isRegularFile(path, options);
    }

    /**
     * See {@link Files#isReadable(Path)}.
     *
     * @param path See {@link Files#isReadable(Path)}
     * @return See {@link Files#isReadable(Path)}
     */
    public boolean isReadable(Path path)
    {
        return Files.isReadable(path);
    }

    /**
     * See {@link Files#isSymbolicLink(Path)}.
     *
     * @param path See {@link Files#isSymbolicLink(Path)}
     * @return See {@link Files#isSymbolicLink(Path)}
     */
    public boolean isSymbolicLink(Path path)
    {
        return Files.isSymbolicLink(path);
    }

    /**
     * See {@link Files#exists(Path, LinkOption...)}.
     *
     * @param path See {@link Files#exists(Path, LinkOption...)}
     * @param options See {@link Files#exists(Path, LinkOption...)}
     * @return See {@link Files#exists(Path, LinkOption...)}
     */
    public boolean exists(Path path, LinkOption... options)
    {
        return Files.exists(path, options);
    }

    /**
     * See {@link Files#readAllBytes(Path)}.
     * <p>
     * Velocity Example:
     * </p>
     * <code><pre>
     * $stringtool.toString($niotool.readAllBytes("attach:Sandbox.WebHome@vma.txt.zip/vma.txt"), "utf-8")
     * </pre></code>
     *
     * @param path See {@link Files#readAllBytes(Path)}
     * @return See {@link Files#readAllBytes(Path)}
     */
    public byte[] readAllBytes(Path path)
    {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * See {@link Files#readAllLines(Path, Charset)}.
     *
     * @param path See {@link Files#readAllLines(Path, Charset)}
     * @param cs See {@link Files#readAllLines(Path, Charset)}
     * @return See {@link Files#readAllLines(Path, Charset)}
     */
    public List<String> readAllLines(Path path, Charset cs)
    {
        try {
            return Files.readAllLines(path, cs);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * See {@link Files#newBufferedReader(Path, Charset)}.
     *
     * @param path See {@link Files#newBufferedReader(Path, Charset)}
     * @param cs See {@link Files#newBufferedReader(Path, Charset)}
     * @return See {@link Files#newBufferedReader(Path, Charset)}
     */
    public BufferedReader newBufferedReader(Path path, Charset cs)
    {
        try {
            return Files.newBufferedReader(path, cs);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * See {@link Files#size(Path)}.
     *
     * @param path See {@link Files#size(Path)}
     * @return See {@link Files#size(Path)}
     */
    public long size(Path path)
    {
        try {
            return Files.size(path);
        } catch (IOException e) {
            return 0L;
        }
    }

    /**
     * See {@link Files#getAttribute(Path, String, LinkOption...)}.
     *
     * @param path See {@link Files#getAttribute(Path, String, LinkOption...)}
     * @param attribute See {@link Files#getAttribute(Path, String, LinkOption...)}
     * @param options See {@link Files#getAttribute(Path, String, LinkOption...)}
     * @return See {@link Files#getAttribute(Path, String, LinkOption...)}
     */
    public Object getAttribute(Path path, String attribute, LinkOption... options)
    {
        try {
            return Files.getAttribute(path, attribute, options);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * See {@link Files#readAttributes(Path, String, LinkOption...)}.
     *
     * @param path See {@link Files#readAttributes(Path, String, LinkOption...)}
     * @param attributes See {@link Files#readAttributes(Path, String, LinkOption...)}
     * @param options See {@link Files#readAttributes(Path, String, LinkOption...)}
     * @return See {@link Files#readAttributes(Path, String, LinkOption...)}
     */
    public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options)
    {
        try {
            return Files.readAttributes(path, attributes, options);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * See {@link Files#getLastModifiedTime(Path, LinkOption...)}.
     *
     * @param path See {@link Files#getLastModifiedTime(Path, LinkOption...)}
     * @param options See {@link Files#getLastModifiedTime(Path, LinkOption...)}
     * @return See {@link Files#getLastModifiedTime(Path, LinkOption...)}
     */
    public FileTime getLastModifiedTime(Path path, LinkOption... options)
    {
        try {
            return Files.getLastModifiedTime(path, options);
        } catch (IOException e) {
            return null;
        }
    }
}
