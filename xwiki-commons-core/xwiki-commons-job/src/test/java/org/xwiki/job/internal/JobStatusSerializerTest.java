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
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.DefaultRequest;
import org.xwiki.job.Request;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.xstream.internal.SafeXStream;
import org.xwiki.xstream.internal.XStreamUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Validate {@link JobStatusSerializer}.
 *
 * @version $Id$
 */
@ComponentTest
@ComponentList({ SafeXStream.class, XStreamUtils.class })
public class JobStatusSerializerTest
{
    @InjectMockComponents
    private JobStatusSerializer serializer;

    private File testFile = new File("target/test/status.xml");

    private JobStatus writeRead(JobStatus status) throws IOException
    {
        this.serializer.write(status, this.testFile);

        return this.serializer.read(this.testFile);
    }

    // Tests

    @Test
    public void serializeUnserialize() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status = writeRead(status);

        assertEquals("type", status.getJobType());
    }

    @Test
    public void serializeUnserializeProgress() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status = writeRead(status);

        assertNotNull(status.getProgress());
        assertEquals(0.0d, status.getProgress().getOffset(), 0.1d);
        assertEquals(0.0d, status.getProgress().getCurrentLevelOffset(), 0.1d);
        assertEquals("Progress with name [{}]", status.getProgress().getRootStep().getMessage().getMessage());
    }
}
