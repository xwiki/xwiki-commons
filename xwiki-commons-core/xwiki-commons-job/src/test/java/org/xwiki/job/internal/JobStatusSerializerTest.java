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
import java.util.Objects;

import javax.inject.Provider;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.DefaultRequest;
import org.xwiki.job.Request;
import org.xwiki.job.annotation.Serializable;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.job.test.SerializableStandaloneComponent;
import org.xwiki.job.test.StandaloneComponent;
import org.xwiki.logging.marker.TranslationMarker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

/**
 * Validate {@link JobStatusSerializer}.
 *
 * @version $Id$
 */
public class JobStatusSerializerTest
{
    private JobStatusSerializer serializer;

    private File testFile = new File("target/test/status.xml");

    @Serializable
    private static class SerializableCrossReferenceObject
    {
        public SerializableCrossReferenceObject field;

        public SerializableCrossReferenceObject()
        {
            this.field = this;
        }
    }

    @Serializable
    private static class SerializableObjectTest
    {
        public Object field;

        public SerializableObjectTest(Object field)
        {
            this.field = field;
        }
    }

    @Serializable
    private static class CustomSerializableObject
    {
        public String field;

        public CustomSerializableObject(String field)
        {
            this.field = field;
        }

        @Override
        public boolean equals(Object obj)
        {
            return Objects.equals(((CustomSerializableObject) obj).field, this.field);
        }
    }

    @Serializable
    private static class SerializableCustomObject
    {
        public String field;

        public SerializableCustomObject(String field)
        {
            this.field = field;
        }

        @Override
        public boolean equals(Object obj)
        {
            return Objects.equals(((SerializableCustomObject) obj).field, this.field);
        }
    }

    @Serializable(false)
    private static class NotSerializableCustomObject
    {
        public String field;

        public NotSerializableCustomObject(String field)
        {
            this.field = field;
        }

        @Override
        public boolean equals(Object obj)
        {
            return Objects.equals(((NotSerializableCustomObject) obj).field, this.field);
        }

        @Override
        public String toString()
        {
            return this.field;
        }
    }

    @Serializable
    private static class SerializableProvider implements Provider<String>
    {
        @Override
        public String get()
        {
            return null;
        }
    }

    private static class SerializableImplementationProvider implements Provider<String>, java.io.Serializable
    {
        private static final long serialVersionUID = 1L;

        @Override
        public String get()
        {
            return null;
        }
    }

    private static class TestException extends Exception
    {
        private Object custom;

        public TestException(String message, Throwable cause, Object custom)
        {
            super(message, cause);

            this.custom = custom;
        }

        public Object getCustom()
        {
            return this.custom;
        }
    }

    @BeforeEach
    public void before() throws ParserConfigurationException
    {
        this.serializer = new JobStatusSerializer();
    }

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

        writeRead(status);
    }

    @Test
    public void serializeUnserializeWhenLogMessage() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message");

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
    }

    @Test
    public void serializeUnserializeWhenLogMarker() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error(new TranslationMarker("translation.key"), "error message");

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(new TranslationMarker("translation.key"), status.getLog().peek().getMarker());
    }

    @Test
    public void serializeUnserializeWhenLogWithException() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message",
            new TestException("exception message", new Exception("cause"), "custom"));

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals("exception message", status.getLog().peek().getThrowable().getMessage());
        assertEquals("cause", status.getLog().peek().getThrowable().getCause().getMessage());
        assertNull(((TestException) status.getLog().peek().getThrowable()).getCustom(), "exception message");
    }

    @Test
    public void serializeUnserializeWhenLogWithArguments() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message", "arg1", "arg2");

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals("arg1", status.getLog().peek().getArgumentArray()[0]);
        assertEquals("arg2", status.getLog().peek().getArgumentArray()[1]);
    }

    @Test
    public void serializeUnserializeWhenLogWithNullArguments() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message", "arg1", null, "arg3");

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals("arg1", status.getLog().peek().getArgumentArray()[0]);
        assertNull(status.getLog().peek().getArgumentArray()[1]);
        assertEquals("arg3", status.getLog().peek().getArgumentArray()[2]);
    }

    @Test
    public void serializeUnserializeWhenLogWithComponentArgument() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new DefaultJobStatusStore());

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(String.class, status.getLog().peek().getArgumentArray()[0].getClass());
    }

    @Test
    public void serializeUnserializeWhenLogWithStandaloneComponentArgument() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new StandaloneComponent());

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(String.class, status.getLog().peek().getArgumentArray()[0].getClass());
    }

    @Test
    public void serializeUnserializeWhenLogWithSerializableStandaloneComponentArgument() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableStandaloneComponent());

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(SerializableStandaloneComponent.class, status.getLog().peek().getArgumentArray()[0].getClass());
    }

    @Test
    public void serializeUnserializeWhenLogWithCrossReference() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("message", new SerializableCrossReferenceObject());

        status = writeRead(status);

        assertNotNull(status.getLog());
        SerializableCrossReferenceObject obj =
            (SerializableCrossReferenceObject) status.getLog().peek().getArgumentArray()[0];
        assertSame(obj, obj.field);
    }

    @Test
    public void serializeUnserializeWhenLogWithComponentField() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableObjectTest(new DefaultJobStatusStore()));

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertNull(((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field);
    }

    @Test
    public void serializeUnserializeWhenLogWithStandaloneComponentField() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableObjectTest(new StandaloneComponent()));

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertNull(((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field);
    }

    @Test
    public void serializeUnserializeWhenLogWithLoggerField() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableObjectTest(mock(Logger.class)));

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertNull(((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field);
    }

    @Test
    public void serializeUnserializeWhenLogWithProviderField() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableObjectTest(mock(Provider.class)));

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertNull(((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field);
    }

    @Test
    public void serializeUnserializeWhenLogWithComponentManagerField() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableObjectTest(mock(ComponentManager.class)));

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertNull(((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field);
    }

    @Test
    public void serializeUnserializeWhenLogWithSerializableProviderField() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableObjectTest(new SerializableProvider()));

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(SerializableProvider.class,
            ((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field.getClass());
    }

    @Test
    public void serializeUnserializeWhenLogWithSerializableImplementationProviderField() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableObjectTest(new SerializableImplementationProvider()));

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(SerializableImplementationProvider.class,
            ((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field.getClass());
    }

    @Test
    public void serializeUnserializeWhenLogWithCustomObjectArgument() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new CustomSerializableObject("value"));

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(new CustomSerializableObject("value"), status.getLog().peek().getArgumentArray()[0]);
    }

    @Test
    public void serializeUnserializeWhenLogWithSerializableCustomObjectArgument() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableCustomObject("value"));

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(new SerializableCustomObject("value"), status.getLog().peek().getArgumentArray()[0]);
    }

    @Test
    public void serializeUnserializeWhenLogWithNotSerializableCustomObjectArgument() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>("type", new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new NotSerializableCustomObject("value"));

        status = writeRead(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals("value", status.getLog().peek().getArgumentArray()[0]);
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
