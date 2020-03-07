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
package org.xwiki.logging.internal.tail;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.logging.event.LogEvent;
import org.xwiki.xstream.internal.SafeXStream;

/**
 * Read and write the log in XStream XML format.
 * 
 * @version $Id$
 * @since 11.9RC1
 */
@Component(roles = XStreamFileLoggerTail.class)
@Singleton
public class XStreamFileLoggerTail extends AbstractTextFileLoggerTail
{
    protected static final String FILE_EXTENSION = ".xml";

    @Inject
    private SafeXStream xstream;

    /**
     * @param path the base path of the log
     * @throws IOException when failing to create the log files
     */
    @Override
    public void initialize(Path path, boolean readonly) throws IOException
    {
        super.initialize(path, StandardCharsets.UTF_8, readonly);
    }

    @Override
    protected String getFileExtension()
    {
        return FILE_EXTENSION;
    }

    /**
     * @param path the base path of the log
     * @return true of a log has been stored at this location
     */
    public static boolean exist(Path path)
    {
        return exist(path, FILE_EXTENSION);
    }

    @Override
    protected LogEvent read(Reader reader)
    {
        return (LogEvent) this.xstream.fromXML(reader);
    }

    @Override
    protected void write(LogEvent logEvent, Writer writer)
    {
        this.xstream.toXML(logEvent, writer);
    }
}
