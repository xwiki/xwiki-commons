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
package org.xwiki.job.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.job.internal.xstream.SafeXStream;

import com.thoughtworks.xstream.XStream;

/**
 * Serialize/unserialize tool for job statuses.
 *
 * @version $Id$
 * @since 5.2M2
 */
public class JobStatusSerializer
{
    /**
     * Encoding used for file content and names.
     */
    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * Used to serialize and unserialize status.
     */
    private XStream xstream;

    /**
     * Default constructor.
     *
     * @throws ParserConfigurationException when failing to initialize
     */
    public JobStatusSerializer() throws ParserConfigurationException
    {
        this.xstream = new SafeXStream();
    }

    /**
     * @param status the status to serialize
     * @param file the file to serialize the status to
     * @throws IOException when failing to serialize the status
     */
    public void write(JobStatus status, File file) throws IOException
    {
        File tempFile = File.createTempFile(file.getName(), ".tmp");

        try (FileOutputStream stream = FileUtils.openOutputStream(tempFile)) {
            write(status, stream);
        }

        // Copy the file to its final destination
        file.mkdirs();
        for (int i = 0; i < 10; ++i) {
            try {
                Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Stop the retry loop if it succeeded
                break;
            } catch (FileAlreadyExistsException e) {
                // Yes it sounds pretty weird but it can happen so we try 10 times before giving up
                // We a bit before retrying
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ei) {
                    throw e;
                }
            }
        }
    }

    /**
     * @param status the status to serialize
     * @param stream the stream to serialize the status to
     * @throws IOException when failing to serialize the status
     */
    public void write(JobStatus status, OutputStream stream) throws IOException
    {
        OutputStreamWriter writer = new OutputStreamWriter(stream, DEFAULT_ENCODING);
        writer.write("<?xml version=\"1.0\" encoding=\"" + DEFAULT_ENCODING + "\"?>\n");
        this.xstream.toXML(status, writer);
        writer.flush();
    }

    /**
     * @param file the file to read
     * @return the status
     */
    public JobStatus read(File file)
    {
        return (JobStatus) this.xstream.fromXML(file);
    }

    /**
     * @param stream the stream to read
     * @return the status
     */
    public JobStatus read(InputStream stream)
    {
        return (JobStatus) this.xstream.fromXML(stream);
    }
}
