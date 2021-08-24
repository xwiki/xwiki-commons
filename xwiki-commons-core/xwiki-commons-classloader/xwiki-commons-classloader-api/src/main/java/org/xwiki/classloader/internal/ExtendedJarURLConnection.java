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
package org.xwiki.classloader.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.net.URLStreamHandler;
import java.security.Permission;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;

/**
 * URL Connection that knows how to get a JAR file with a provided handler.
 * <p>
 * We have to implement {@link JarURLConnection} to be automatically called to release any resource associated to a
 * {@link JarFile} like a temporary file. sun.net.www.protocol.jar.JarURLConnection being internal we have to
 * re-implement most of it, fortunately we only need what's required by {@link URLClassLoader} which is mostly to return
 * a {@link JarFile}.
 *
 * @version $Id$
 * @since 12.7
 * @since 12.10.10
 * @since 13.4.4
 */
public class ExtendedJarURLConnection extends JarURLConnection
{
    private final URLStreamHandler handler;

    private final File jarsDirectory;

    private URL jarFileURL;

    private String entryName;

    private JarFile jarFile;

    private JarEntry jarEntry;

    /**
     * Creates the new ExtendedJarURLConnection to the specified URL.
     * 
     * @param url the specified URL.
     * @param handler the handler to use to manipulate the file this URL contains
     * @param jarsDirectory the folder where to download JAR files
     * @throws MalformedURLException when failing to parse the passed URL
     */
    public ExtendedJarURLConnection(URL url, URLStreamHandler handler, File jarsDirectory) throws MalformedURLException
    {
        // JarURLConnection constructor crash if we pass an URL which contains a custom protocol which unfortunately is
        // the point of ExtendedJarURLConnection
        super(new URL("jar:file:file.jar!/"));

        this.handler = handler;
        this.jarsDirectory = jarsDirectory;

        parseSpecs(url);
    }

    private void createJarFile() throws IOException
    {
        // Make sure the root folder exists
        this.jarsDirectory.mkdirs();

        // Download the data in a temporary local file
        File tempFile = File.createTempFile("jar", ".jar", this.jarsDirectory);
        try (InputStream stream = this.jarFileURL.openStream()) {
            FileUtils.copyInputStreamToFile(stream, tempFile);
        }

        // Create a proper JarFile
        this.jarFile = new JarFile(tempFile)
        {
            @Override
            public void close() throws IOException
            {
                super.close();

                // Get rid of the file when the JarFile as soon as the JarFile instance is not needed anymore
                tempFile.delete();
            }
        };
    }

    @Override
    public void connect() throws IOException
    {
        if (!this.connected) {
            createJarFile();

            if (this.entryName != null) {
                this.jarEntry = (JarEntry) this.jarFile.getEntry(this.entryName);

                if (this.jarEntry == null) {
                    try {
                        this.jarFile.close();
                    } catch (Exception e) {
                    }

                    throw new FileNotFoundException(
                        "JAR entry " + this.entryName + " not found in " + this.jarFile.getName());
                }
            }

            this.connected = true;
        }
    }

    @Override
    public URL getJarFileURL()
    {
        return this.jarFileURL;
    }

    /**
     * Return the JAR file for this connection.
     *
     * @return the JAR file for this connection. If the connection is a connection to an entry of a JAR file, the JAR
     *         file object is returned
     * @exception IOException if an IOException occurs while trying to connect to the JAR file for this connection.
     * @see #connect
     */
    @Override
    public JarFile getJarFile() throws IOException
    {
        connect();

        return this.jarFile;
    }

    @Override
    public InputStream getInputStream() throws IOException
    {
        connect();

        return this.jarFile.getInputStream(this.jarEntry);
    }

    @Override
    public Permission getPermission() throws IOException
    {
        return getJarFileURL().openConnection().getPermission();
    }

    /*
     * Customize the jarFileURL.
     */
    private void parseSpecs(URL url) throws MalformedURLException
    {
        String spec = url.getFile();

        int separator = spec.indexOf("!/");
        /*
         * REMIND: we don't handle nested JAR URLs
         */
        if (separator == -1) {
            throw new MalformedURLException("no !/ found in url spec:" + spec);
        }

        // This is the main difference with standard JarURLConnection: we use a component to handle the actual file
        this.jarFileURL = new URL(null, spec.substring(0, separator++), this.handler);
        this.entryName = null;

        /* if ! is the last letter of the innerURL, entryName is null */
        if (++separator != spec.length()) {
            this.entryName = spec.substring(separator, spec.length());
            try {
                // Note: we decode using UTF8 since it's the W3C recommendation.
                // See http://www.w3.org/TR/html40/appendix/notes.html#non-ascii-chars
                this.entryName = URLDecoder.decode(this.entryName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // Not supporting UTF-8 as a valid encoding for some reasons. We consider XWiki cannot work
                // without that encoding.
                throw new RuntimeException("Failed to URL decode [" + this.entryName + "] using UTF-8.", e);
            }
        }
    }
}
