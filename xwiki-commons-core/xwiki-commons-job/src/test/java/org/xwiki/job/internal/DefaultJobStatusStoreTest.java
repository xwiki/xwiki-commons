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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.inject.Provider;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.internal.MapCache;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.job.AbstractJobStatus;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.DefaultRequest;
import org.xwiki.job.JobManagerConfiguration;
import org.xwiki.job.Request;
import org.xwiki.job.annotation.Serializable;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.job.internal.xstream.SerializableXStreamChecker;
import org.xwiki.job.test.SerializableStandaloneComponent;
import org.xwiki.job.test.StandaloneComponent;
import org.xwiki.job.test.UnserializableJobStatus;
import org.xwiki.logging.LoggerManager;
import org.xwiki.logging.internal.tail.XStreamFileLoggerTail;
import org.xwiki.logging.marker.TranslationMarker;
import org.xwiki.logging.tail.LoggerTail;
import org.xwiki.test.XWikiTempDirUtil;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.xstream.internal.SafeXStream;
import org.xwiki.xstream.internal.XStreamUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultJobStatusStore}.
 *
 * @version $Id$
 */
@ComponentTest
// @formatter:off
@ComponentList({
    SafeXStream.class,
    XStreamUtils.class,
    SerializableXStreamChecker.class,
    JobStatusSerializer.class,
    XStreamFileLoggerTail.class
})
// @formatter:on
class DefaultJobStatusStoreTest
{
    private static final List<String> ID = Arrays.asList("test");

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

    @SuppressWarnings("serial")
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

    @InjectMockComponents
    private DefaultJobStatusStore store;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @MockComponent
    private JobManagerConfiguration jobManagerConfiguration;

    @MockComponent
    private LoggerManager loggerManager;

    @MockComponent
    private CacheManager cacheManager;

    private File storeDirectory;

    @BeforeComponent
    public void before() throws Exception
    {
        this.storeDirectory = XWikiTempDirUtil.createTemporaryDirectory();

        FileUtils.copyDirectory(new File("src/test/resources/jobs/status/"), this.storeDirectory);

        when(this.jobManagerConfiguration.getStorage()).thenReturn(this.storeDirectory);
        when(this.jobManagerConfiguration.getJobStatusCacheSize()).thenReturn(100);

        when(this.cacheManager.createNewCache(any())).thenReturn(new MapCache<>());

        when(this.loggerManager.createLoggerTail(any(), anyBoolean())).then(new Answer<LoggerTail>()
        {
            @Override
            public LoggerTail answer(InvocationOnMock invocation) throws Throwable
            {
                XStreamFileLoggerTail loggerTail = componentManager.getInstance(XStreamFileLoggerTail.class);
                loggerTail.initialize(invocation.getArgument(0), invocation.getArgument(1));

                return loggerTail;
            }
        });
    }

    private DefaultJobStatus<Request> createStatus()
    {
        return createStatus(true);
    }

    private DefaultJobStatus<Request> createStatus(boolean logtail)
    {
        DefaultRequest request = new DefaultRequest();
        request.setId(ID);

        DefaultJobStatus<Request> status = new DefaultJobStatus<>("type", request, null, null, null);

        if (logtail) {
            status.setLoggerTail(this.store.createLoggerTail(ID, false));
        }

        return status;
    }

    private <S extends JobStatus> S getStatus()
    {
        return (S) this.store.getJobStatus(ID);
    }

    private <S extends JobStatus> S storeGet(S status) throws Exception
    {
        if (status instanceof AbstractJobStatus) {
            ((AbstractJobStatus) status).getLoggerTail().close();
        }

        this.store.store(status);

        this.store.flushCache();

        return getStatus();
    }

    // Tests

    @Test
    void getJobStatusWithNullId()
    {
        JobStatus jobStatus = this.store.getJobStatus(null);

        assertNotNull(jobStatus);
        assertNull(jobStatus.getRequest().getId());
        assertEquals(JobStatus.State.FINISHED, jobStatus.getState());

        assertSame(jobStatus, this.store.getJobStatus(null));
    }

    @Test
    void getJobStatusWithMultipleId()
    {
        JobStatus jobStatus = this.store.getJobStatus(Arrays.asList("id1", "id2"));

        assertNotNull(jobStatus);
        assertEquals(Arrays.asList("id1", "id2"), jobStatus.getRequest().getId());
        assertEquals(JobStatus.State.FINISHED, jobStatus.getState());

        assertSame(jobStatus, this.store.getJobStatus(Arrays.asList("id1", "id2")));
    }

    @Test
    void getJobStatusInOldPlace()
    {
        JobStatus jobStatus = this.store.getJobStatus(Arrays.asList("id1", "id2", "id3"));

        assertNotNull(jobStatus);
        assertEquals(Arrays.asList("id1", "id2", "id3"), jobStatus.getRequest().getId());
        assertEquals(JobStatus.State.FINISHED, jobStatus.getState());
    }

    @Test
    void getJobStatusInWrongPlaceAndWithInvalidLogArgument()
    {
        JobStatus jobStatus = this.store.getJobStatus(Arrays.asList("invalidlogargument"));

        assertNotNull(jobStatus);
        assertEquals(3, jobStatus.getLog().size());
    }

    @Test
    void getJobStatusThatDoesNotExist()
    {
        JobStatus jobStatus = this.store.getJobStatus(Arrays.asList("nostatus"));

        assertNull(jobStatus);

        JobStatusSerializer mockSerializer = mock(JobStatusSerializer.class);
        ReflectionUtils.setFieldValue(this.store, "serializer", mockSerializer);

        jobStatus = this.store.getJobStatus(Arrays.asList("nostatus"));
        assertNull(jobStatus);

        verifyNoMoreInteractions(mockSerializer);
    }

    @Test
    void removeJobStatus()
    {
        List<String> id = null;

        JobStatus jobStatus = this.store.getJobStatus(id);

        assertNotNull(jobStatus);
        assertNull(jobStatus.getRequest().getId());
        assertEquals(JobStatus.State.FINISHED, jobStatus.getState());

        assertSame(jobStatus, this.store.getJobStatus(id));

        this.store.remove(id);

        assertSame(null, this.store.getJobStatus(id));
    }

    @Test
    void removeNotExistingJobStatus()
    {
        this.store.remove(Arrays.asList("notexist"));
    }

    @Test
    void storeNullJobStatus()
    {
        this.store.store(null);
    }

    @Test
    void storeJobStatusWithNullRequest()
    {
        JobStatus jobStatus = new DefaultJobStatus("type", null, null, null, null);

        this.store.store(jobStatus);
    }

    @Test
    void storeJobStatusWithNullId()
    {
        JobStatus jobStatus = new DefaultJobStatus("type", new DefaultRequest(), null, null, null);

        this.store.store(jobStatus);
    }

    @Test
    void storeUnserializableJobStatus()
    {
        List<String> id = Arrays.asList("test");
        DefaultRequest request = new DefaultRequest();
        request.setId(id);
        JobStatus jobStatus = new UnserializableJobStatus("type", request, null, null, null);

        this.store.store(jobStatus);

        // Verify that the status hasn't been serialized, indirectly verifying that isSerializable() has been called and
        // returned true.
        assertFalse(new File(this.storeDirectory, "test/status.xml").exists());
    }

    @Test
    void storeUnserializedJobStatus()
    {
        List<String> id = Arrays.asList("test");
        DefaultRequest request = new DefaultRequest();
        request.setId(id);
        request.setStatusSerialized(false);
        JobStatus jobStatus = new DefaultJobStatus("type", request, null, null, null);

        this.store.store(jobStatus);

        // Verify that the status hasn't been serialized, indirectly verifying that isSerializable() has been called and
        // returned true.
        assertFalse(new File(this.storeDirectory, "test/status.xml").exists());
    }

    @Test
    void storeJobStatusWhenSerializable()
    {
        List<String> id = Arrays.asList("newstatus");

        DefaultRequest request = new DefaultRequest();
        request.setId(id);
        JobStatus jobStatus = new DefaultJobStatus("type", request, null, null, null);

        this.store.store(jobStatus);

        assertSame(jobStatus, this.store.getJobStatus(id));

        // Verify that the status has been serialized, indirectly verifying that isSerializable() has been called and
        // returned true.
        assertTrue(new File(this.storeDirectory, "newstatus/status.xml").exists());
    }

    @Test
    void serializeUnserializeWhenLogMessage() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message");

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
    }

    @Test
    void serializeUnserializeWhenLogMarker() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error(new TranslationMarker("translation.key"), "error message");

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(new TranslationMarker("translation.key"), status.getLog().peek().getMarker());
    }

    @Test
    void serializeUnserializeWhenLogWithException() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message",
            new TestException("exception message", new Exception("cause"), "custom"));

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals("exception message", status.getLog().peek().getThrowable().getMessage());
        assertEquals("cause", status.getLog().peek().getThrowable().getCause().getMessage());
        assertNull(((TestException) status.getLog().peek().getThrowable()).getCustom(), "exception message");
    }

    @Test
    void serializeUnserializeWhenLogWithArguments() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message", "arg1", "arg2");

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals("arg1", status.getLog().peek().getArgumentArray()[0]);
        assertEquals("arg2", status.getLog().peek().getArgumentArray()[1]);
    }

    @Test
    void serializeUnserializeWhenLogWithNullArguments() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message", "arg1", null, "arg3");

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals("arg1", status.getLog().peek().getArgumentArray()[0]);
        assertNull(status.getLog().peek().getArgumentArray()[1]);
        assertEquals("arg3", status.getLog().peek().getArgumentArray()[2]);
    }

    @Test
    void serializeUnserializeWhenLogWithComponentArgument() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message", new DefaultJobStatusStore());

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(String.class, status.getLog().peek().getArgumentArray()[0].getClass());
    }

    @Test
    void serializeUnserializeWhenLogWithStandaloneComponentArgument() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message", new StandaloneComponent());

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(String.class, status.getLog().peek().getArgumentArray()[0].getClass());
    }

    @Test
    void serializeUnserializeWhenLogWithSerializableStandaloneComponentArgument() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message", new SerializableStandaloneComponent());

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(SerializableStandaloneComponent.class, status.getLog().peek().getArgumentArray()[0].getClass());
    }

    @Test
    void serializeUnserializeWhenLogWithCrossReference() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("message", new SerializableCrossReferenceObject());

        status = storeGet(status);

        assertNotNull(status.getLog());
        SerializableCrossReferenceObject obj =
            (SerializableCrossReferenceObject) status.getLog().peek().getArgumentArray()[0];
        assertSame(obj, obj.field);
    }

    @Test
    void serializeUnserializeWhenLogWithComponentField() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message", new SerializableObjectTest(new DefaultJobStatusStore()));

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertNull(((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field);
    }

    @Test
    void serializeUnserializeWhenLogWithStandaloneComponentField() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message", new SerializableObjectTest(new StandaloneComponent()));

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertNull(((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field);
    }

    @Test
    void serializeUnserializeWhenLogWithLoggerField() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message", new SerializableObjectTest(mock(Logger.class)));

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertNull(((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field);
    }

    @Test
    void serializeUnserializeWhenLogWithProviderField() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message", new SerializableObjectTest(mock(Provider.class)));

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertNull(((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field);
    }

    @Test
    void serializeUnserializeWhenLogWithComponentManagerField() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message", new SerializableObjectTest(mock(ComponentManager.class)));

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertNull(((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field);
    }

    @Test
    void serializeUnserializeWhenLogWithSerializableProviderField() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message", new SerializableObjectTest(new SerializableProvider()));

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(SerializableProvider.class,
            ((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field.getClass());
    }

    @Test
    void serializeUnserializeWhenLogWithSerializableImplementationProviderField() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message",
            new SerializableObjectTest(new SerializableImplementationProvider()));

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(SerializableImplementationProvider.class,
            ((SerializableObjectTest) status.getLog().peek().getArgumentArray()[0]).field.getClass());
    }

    @Test
    void serializeUnserializeWhenLogWithCustomObjectArgument() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message", new CustomSerializableObject("value"));

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(new CustomSerializableObject("value"), status.getLog().peek().getArgumentArray()[0]);
    }

    @Test
    void serializeUnserializeWhenLogWithSerializableCustomObjectArgument() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message", new SerializableCustomObject("value"));

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals(new SerializableCustomObject("value"), status.getLog().peek().getArgumentArray()[0]);
    }

    @Test
    void serializeUnserializeWhenLogWithNotSerializableCustomObjectArgument() throws Exception
    {
        DefaultJobStatus<Request> status = createStatus();

        status.getLoggerTail().error("error message", new NotSerializableCustomObject("value"));

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals("value", status.getLog().peek().getArgumentArray()[0]);
    }

    @Test
    void serializeUnserializeWithLogQueue() throws Exception
    {
        JobStatus status = createStatus(false);

        status.getLog().error("error message", "arg1", "arg2");

        status = storeGet(status);

        assertNotNull(status.getLog());
        assertEquals("error message", status.getLog().peek().getMessage());
        assertEquals("arg1", status.getLog().peek().getArgumentArray()[0]);
        assertEquals("arg2", status.getLog().peek().getArgumentArray()[1]);
    }

    @Test
    void createLoggerTailWithNullId()
    {
        assertNotNull(this.store.createLoggerTail(null, true));
    }
}
