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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import javax.inject.Provider;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
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

    @Before
    public void before() throws ParserConfigurationException
    {
        this.serializer = new JobStatusSerializer();
    }

    private JobStatus writeread(JobStatus status) throws IOException
    {
        this.serializer.write(status, this.testFile);

        return this.serializer.read(this.testFile);
    }

    // Tests

    @Test
    public void test() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        writeread(status);
    }

    @Test
    public void testLogMessage() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message");

        status = writeread(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
    }

    @Test
    public void testLogMarker() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error(new TranslationMarker("translation.key"), "error message");

        status = writeread(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(new TranslationMarker("translation.key"), status.getLog().peek().getMarker());
    }

    @Test
    public void testLogWithException() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new TestException("exception message", new Exception("cause"), "custom"));

        status = writeread(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals("exception message", status.getLog().peek().getThrowable().getMessage());
        assertEquals("cause", status.getLog().peek().getThrowable().getCause().getMessage());
        assertNull("exception message", ((TestException)status.getLog().peek().getThrowable()).getCustom());
    }

    @Test
    public void testLogWithArguments() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message", "arg1", "arg2");

        status = writeread(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals("arg1", status.getLog().peek().getArgumentArray()[0]);
        assertEquals("arg2", status.getLog().peek().getArgumentArray()[1]);
    }

    @Test
    public void testLogWithNullArguments() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message", "arg1", null, "arg3");

        status = writeread(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals("arg1", status.getLog().peek().getArgumentArray()[0]);
        assertEquals(null, status.getLog().peek().getArgumentArray()[1]);
        assertEquals("arg3", status.getLog().peek().getArgumentArray()[2]);
    }

    @Test
    public void testLogWithComponentArgument() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new DefaultJobStatusStore());

        status = writeread(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(String.class, status.getLog().peek().getArgumentArray()[0].getClass());
    }

    @Test
    public void testLogWithStandaloneComponentArgument() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new StandaloneComponent());

        status = writeread(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(String.class, status.getLog().peek().getArgumentArray()[0].getClass());
    }

    @Test
    public void testLogWithSerializableStandaloneComponentArgument() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableStandaloneComponent());

        status = writeread(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(SerializableStandaloneComponent.class, status.getLog().peek().getArgumentArray()[0].getClass());
    }

    @Test
    public void testLogWithCrossReference() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("message", new SerializableCrossReferenceObject());

        status = writeread(status);

        assertNotNull(status.getLog());
        SerializableCrossReferenceObject obj =
            (SerializableCrossReferenceObject) status.getLog().peek().getArgumentArray()[0];
        assertSame(obj, obj.field);
    }

    @Test
    public void testLogWithComponentField() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableObjectTest(new DefaultJobStatusStore()));

        status = writeread(status);

        assertNotNull(status.getLog());
        assertNull(((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field);
    }

    @Test
    public void testLogWithStandaloneComponentField() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableObjectTest(new StandaloneComponent()));

        status = writeread(status);

        assertNotNull(status.getLog());
        assertNull(((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field);
    }

    @Test
    public void testLogWithLoggerField() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableObjectTest(mock(Logger.class)));

        status = writeread(status);

        assertNotNull(status.getLog());
        assertNull(((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field);
    }

    @Test
    public void testLogWithProviderField() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableObjectTest(mock(Provider.class)));

        status = writeread(status);

        assertNotNull(status.getLog());
        assertNull(((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field);
    }

    @Test
    public void testLogWithComponentManagerField() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableObjectTest(mock(ComponentManager.class)));

        status = writeread(status);

        assertNotNull(status.getLog());
        assertNull(((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field);
    }

    @Test
    public void testLogWithSerializableProviderField() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableObjectTest(new SerializableProvider()));

        status = writeread(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(SerializableProvider.class,
            ((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field.getClass());
    }

    @Test
    public void testLogWithSerializableImplementationProviderField() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableObjectTest(new SerializableImplementationProvider()));

        status = writeread(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(SerializableImplementationProvider.class,
            ((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field.getClass());
    }

    @Test
    public void testLogWithCustomObjectArgument() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new CustomSerializableObject("value"));

        status = writeread(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(new CustomSerializableObject("value"), status.getLog().peek().getArgumentArray()[0]);
    }

    @Test
    public void testLogWithSerializableCustomObjectArgument() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new SerializableCustomObject("value"));

        status = writeread(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(new SerializableCustomObject("value"), status.getLog().peek().getArgumentArray()[0]);
    }

    @Test
    public void testLogWithNotSerializableCustomObjectArgument() throws IOException
    {
        JobStatus status = new DefaultJobStatus<Request>(new DefaultRequest(), null, null, null);

        status.getLog().error("error message", new NotSerializableCustomObject("value"));

        status = writeread(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals("value", status.getLog().peek().getArgumentArray()[0]);
    }
}
