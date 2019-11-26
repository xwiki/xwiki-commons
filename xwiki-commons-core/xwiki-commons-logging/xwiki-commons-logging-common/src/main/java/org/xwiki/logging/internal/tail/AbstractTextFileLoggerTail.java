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

import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.xwiki.logging.event.LogEvent;

/**
 * Read and write the log in XStream XML format.
 * 
 * @version $Id$
 * @since 11.9RC1
 */
public abstract class AbstractTextFileLoggerTail extends AbstractFileLoggerTail
{
    private Charset charset;

    /**
     * @param path the base path of the log
     * @param charset the charset to use to read/write the log content
     * @param readonly true if the log is readonly
     * @throws IOException when failing to create the log files
     */
    public void initialize(Path path, Charset charset, boolean readonly) throws IOException
    {
        super.initialize(path, readonly);

        this.charset = charset;
    }

    @Override
    protected LogEvent read(InputStream input)
    {
        return read(new InputStreamReader(input, this.charset));
    }

    @Override
    protected void write(LogEvent logEvent, DataOutput output)
    {
        write(logEvent, new OutputStreamWriter(new OutputStreamDataOutput(output), this.charset));
    }

    // Abstracts

    protected abstract LogEvent read(Reader reader);

    protected abstract void write(LogEvent logEvent, Writer writer);
}
