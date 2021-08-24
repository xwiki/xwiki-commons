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
// In java.net package to be allowed to call URLStreamHandler methods
package org.xwiki.classloader.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.xwiki.classloader.ExtendedURLStreamHandler;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLifecycleException;
import org.xwiki.component.phase.Disposable;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.environment.Environment;

/**
 * Handler for the "jar" protocol. It handle sub-protocols supported by {@link ExtendedURLStreamHandler} components and
 * redirect everything else to the standard JAR handler.
 * <p>
 * We have to implement our own JAR handler because it's not possible to dynamically add new protocol handlers (in our
 * case handlers which are implemented as components) that the standard JAR protocol handler can access.
 *
 * @version $Id$
 * @since 12.7
 * @since 12.10.10
 * @since 13.4.4
 */
@Component
@Named(JarExtendedURLStreamHandler.PROTOCOL)
@Singleton
public class JarExtendedURLStreamHandler extends URLStreamHandler
    implements ExtendedURLStreamHandler, Initializable, Disposable
{
    public static final String PROTOCOL = "jar";

    public static final String JARS_FOLDER = "classloader/jars/";

    @Inject
    private URLStreamHandlerFactory handlerFactory;

    @Inject
    private Environment environment;

    @Inject
    private Logger logger;

    private File jarsDirectory;

    @Override
    public String getProtocol()
    {
        return PROTOCOL;
    }

    private URLStreamHandler getURLStreamHandler(String spec)
    {
        int index = spec.indexOf(':', PROTOCOL.length() + 1);
        if (index != -1) {
            String hint = spec.substring(PROTOCOL.length() + 1, index);

            return this.handlerFactory.createURLStreamHandler(hint);
        }

        return null;
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException
    {
        String spec = u.toExternalForm();

        URLStreamHandler subHandler = getURLStreamHandler(spec);

        // If it's a supported URLStreamHandler created an ExtendedJarURLConnection
        if (subHandler != null) {
            return new ExtendedJarURLConnection(u, subHandler, this.jarsDirectory);
        }

        // For anything else use standard JAR handler
        return new URL(null, spec).openConnection();
    }

    @Override
    public void initialize() throws InitializationException
    {
        this.jarsDirectory = new File(this.environment.getTemporaryDirectory(), JARS_FOLDER);
    }

    @Override
    public void dispose() throws ComponentLifecycleException
    {
        if (this.jarsDirectory.exists()) {
            // Get rid of all the temporary jar files in case it's not already the case
            try {
                FileUtils.deleteDirectory(this.jarsDirectory);
            } catch (IOException e) {
                this.logger.error("Failed to delete folder [{}]", this.jarsDirectory);
            }
        }
    }

    // @formatter:off
    //////////////////////////////////////////////////////////////////////////////////////
    // Begin copying a few sun.net.www.protocol.jar.Handler methods that we cannot reuse
    //////////////////////////////////////////////////////////////////////////////////////

    private static int indexOfBangSlash(String spec) {
        int indexOfBang = spec.length();
        while((indexOfBang = spec.lastIndexOf('!', indexOfBang)) != -1) {
            if ((indexOfBang != (spec.length() - 1)) &&
                (spec.charAt(indexOfBang + 1) == '/')) {
                return indexOfBang + 1;
            } else {
                indexOfBang--;
            }
        }
        return -1;
    }

    public String checkNestedProtocol(String spec) {
        if (spec.regionMatches(true, 0, "jar:", 0, 4)) {
            return "Nested JAR URLs are not supported";
        } else {
            return null;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void parseURL(URL url, String spec,
                            int start, int limit) {
        String file = null;
        String ref = null;
        // first figure out if there is an anchor
        int refPos = spec.indexOf('#', limit);
        boolean refOnly = refPos == start;
        if (refPos > -1) {
            ref = spec.substring(refPos + 1, spec.length());
            if (refOnly) {
                file = url.getFile();
            }
        }
        // then figure out if the spec is
        // 1. absolute (jar:)
        // 2. relative (i.e. url + foo/bar/baz.ext)
        // 3. anchor-only (i.e. url + #foo), which we already did (refOnly)
        boolean absoluteSpec = spec.length() >= 4
                ? spec.regionMatches(true, 0, "jar:", 0, 4)
                : false;
        spec = spec.substring(start, limit);

        String exceptionMessage = checkNestedProtocol(spec);
        if (exceptionMessage != null) {
            // NPE will be transformed into MalformedURLException by the caller
            throw new NullPointerException(exceptionMessage);
        }

        if (absoluteSpec) {
            file = parseAbsoluteSpec(spec);
        } else if (!refOnly) {
            file = parseContextSpec(url, spec);

            // Canonize the result after the bangslash
            int bangSlash = indexOfBangSlash(file);
            String toBangSlash = file.substring(0, bangSlash);
            String afterBangSlash = file.substring(bangSlash);
            afterBangSlash = canonizeString(afterBangSlash);
            file = toBangSlash + afterBangSlash;
        }
        setURL(url, "jar", "", -1, file, ref);
    }

    private String parseAbsoluteSpec(String spec) {
        int index;
        // check for !/
        if ((index = indexOfBangSlash(spec)) == -1) {
            throw new NullPointerException("no !/ in spec");
        }
        // test the inner URL
        try {
            String innerSpec = spec.substring(0, index - 1);
            new URL(innerSpec);
        } catch (MalformedURLException e) {
            throw new NullPointerException("invalid url: " +
                                           spec + " (" + e + ")");
        }
        return spec;
    }

    private String parseContextSpec(URL url, String spec) {
        String ctxFile = url.getFile();
        // if the spec begins with /, chop up the jar back !/
        if (spec.startsWith("/")) {
            int bangSlash = indexOfBangSlash(ctxFile);
            if (bangSlash == -1) {
                throw new NullPointerException("malformed " +
                                               "context url:" +
                                               url +
                                               ": no !/");
            }
            ctxFile = ctxFile.substring(0, bangSlash);
        } else {
            // chop up the last component
            int lastSlash = ctxFile.lastIndexOf('/');
            if (lastSlash == -1) {
                throw new NullPointerException("malformed " +
                                               "context url:" +
                                               url);
            } else if (lastSlash < ctxFile.length() - 1) {
                ctxFile = ctxFile.substring(0, lastSlash + 1);
            }
        }
        return (ctxFile + spec);
    }

    /**
     * Returns a canonical version of the specified string.
     */
    public static String canonizeString(String file) {
        int len = file.length();
        if (len == 0 || (file.indexOf("./") == -1 && file.charAt(len - 1) != '.')) {
            return file;
        } else {
            return doCanonize(file);
        }
    }

    private static String doCanonize(String file) {
        int i, lim;

        // Remove embedded /../
        while ((i = file.indexOf("/../")) >= 0) {
            if ((lim = file.lastIndexOf('/', i - 1)) >= 0) {
                file = file.substring(0, lim) + file.substring(i + 3);
            } else {
                file = file.substring(i + 3);
            }
        }
        // Remove embedded /./
        while ((i = file.indexOf("/./")) >= 0) {
            file = file.substring(0, i) + file.substring(i + 2);
        }
        // Remove trailing ..
        while (file.endsWith("/..")) {
            i = file.indexOf("/..");
            if ((lim = file.lastIndexOf('/', i - 1)) >= 0) {
                file = file.substring(0, lim+1);
            } else {
                file = file.substring(0, i);
            }
        }
        // Remove trailing .
        if (file.endsWith("/."))
            file = file.substring(0, file.length() -1);

        return file;
    }

    //////////////////////////////////////////////////////////////////////////////////////
    // Finish sun.net.www.protocol.jar.Handler copy
    //////////////////////////////////////////////////////////////////////////////////////
    // @formatter:on
}
