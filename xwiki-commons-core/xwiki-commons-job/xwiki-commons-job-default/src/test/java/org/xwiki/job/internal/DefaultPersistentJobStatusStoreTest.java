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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.inject.Provider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
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
import org.xwiki.logging.event.LogEvent;
import org.xwiki.logging.internal.tail.XStreamFileLoggerTail;
import org.xwiki.logging.marker.TranslationMarker;
import org.xwiki.logging.tail.LoggerTail;
import org.xwiki.test.TestEnvironment;
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
 * Unit tests for {@link DefaultPersistentJobStatusStore}.
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
    XStreamFileLoggerTail.class,
    TestEnvironment.class,
    // Add all components to ensure that the priority works as expected.
    Version1JobStatusFolderResolver.class,
    Version2JobStatusFolderResolver.class,
    Version3JobStatusFolderResolver.class
})
// @formatter:on
class DefaultPersistentJobStatusStoreTest
{
    private static final List<String> ID = Arrays.asList("test");

    private static final String STATUS_XML_ZIP = "status.xml.zip";

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
    private DefaultPersistentJobStatusStore store;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @MockComponent
    private JobManagerConfiguration jobManagerConfiguration;

    @MockComponent
    private LoggerManager loggerManager;

    private File storeDirectory;

    private File wrongDirectory;

    private File wrongDirectory2;

    private File correctDirectory;

    private File wrongStatusInCorrectDirectory;

    @BeforeComponent
    void before() throws Exception
    {
        this.storeDirectory = XWikiTempDirUtil.createTemporaryDirectory();

        FileUtils.copyDirectory(new File("src/test/resources/jobs/status/"), this.storeDirectory);

        this.wrongDirectory = new File(this.storeDirectory, "wrong/location");
        this.wrongDirectory2 = new File(this.storeDirectory, "wrong/location2");
        this.correctDirectory = new File(this.storeDirectory, "3/correct/location");
        assertTrue(this.wrongDirectory.isDirectory(), "Directory copied from resources doesn't exist.");

        File mostRecentFile = new File(this.wrongDirectory, STATUS_XML_ZIP);
        File oldestFile = new File(this.wrongDirectory2, STATUS_XML_ZIP);
        File middleFile = new File(this.correctDirectory, "status.xml");
        this.wrongStatusInCorrectDirectory = middleFile;

        assertTrue(mostRecentFile.exists());
        assertTrue(oldestFile.exists());
        assertTrue(middleFile.exists());

        assertTrue(mostRecentFile.setLastModified(System.currentTimeMillis()));
        assertTrue(oldestFile.setLastModified(System.currentTimeMillis() - 4000));
        assertTrue(middleFile.setLastModified(System.currentTimeMillis() - 2000));

        when(this.jobManagerConfiguration.getStorage()).thenReturn(this.storeDirectory);

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

    private <S extends JobStatus> S getStatus() throws IOException
    {
        return (S) this.store.loadJobStatus(ID);
    }

    private <S extends JobStatus> S storeGet(S status) throws Exception
    {
        if (status instanceof AbstractJobStatus<?> abstractStatus) {
            abstractStatus.getLoggerTail().close();
        }

        this.store.saveJobStatus(status);

        return getStatus();
    }

    @Test
    void jobStatusAndLogMoved() throws IOException
    {
        JobStatus status = this.store.loadJobStatus(List.of("correct", "location"));
        assertNotNull(status);
        LogEvent lastLogEvent = status.getLogTail().getLastLogEvent();
        assertNotNull(lastLogEvent);
        // Verify that we got the correct job.
        assertEquals("Finished correct job of type [{}] with identifier [{}]", lastLogEvent.getMessage());

        assertFalse(this.wrongDirectory.exists());
        assertFalse(this.wrongDirectory.getParentFile().exists());
        assertFalse(this.wrongDirectory2.exists());
        assertFalse(this.wrongStatusInCorrectDirectory.exists());
        assertTrue(new File(this.correctDirectory, STATUS_XML_ZIP).exists());
        assertTrue(new File(this.correctDirectory, "log.index").exists());
        assertTrue(new File(this.correctDirectory, "log.xml").exists());
    }

    @Test
    void getJobStatusWithNullId() throws IOException
    {
        JobStatus jobStatus = this.store.loadJobStatus(null);

        assertNotNull(jobStatus);
        assertNull(jobStatus.getRequest().getId());
        assertEquals(JobStatus.State.FINISHED, jobStatus.getState());
    }

    @Test
    void getJobStatusWithMultipleId() throws IOException
    {
        JobStatus jobStatus = this.store.loadJobStatus(Arrays.asList("id1", "id2"));

        assertNotNull(jobStatus);
        assertEquals(Arrays.asList("id1", "id2"), jobStatus.getRequest().getId());
        assertEquals(JobStatus.State.FINISHED, jobStatus.getState());

        JobStatus secondJobStatus = this.store.loadJobStatus(Arrays.asList("id1", "id2"));
        assertEquals(jobStatus.getRequest(), secondJobStatus.getRequest());
        assertEquals(jobStatus.getState(), secondJobStatus.getState());
        assertEquals(jobStatus.getJobType(), secondJobStatus.getJobType());
    }

    @Test
    void getJobStatusInOldPlace() throws IOException
    {
        JobStatus jobStatus = this.store.loadJobStatus(Arrays.asList("id1", "id2", "id3"));

        assertNotNull(jobStatus);
        assertEquals(Arrays.asList("id1", "id2", "id3"), jobStatus.getRequest().getId());
        assertEquals(JobStatus.State.FINISHED, jobStatus.getState());
    }

    @Test
    void getJobStatusInWrongPlaceAndWithInvalidLogArgument() throws IOException
    {
        JobStatus jobStatus = this.store.loadJobStatus(Arrays.asList("invalidlogargument"));

        assertNotNull(jobStatus);
        assertEquals(3, jobStatus.getLog().size());
    }

    @Test
    void getJobStatusThatDoesNotExist() throws IOException
    {
        JobStatus jobStatus = this.store.loadJobStatus(Arrays.asList("nostatus"));

        assertNull(jobStatus);

        JobStatusSerializer mockSerializer = mock(JobStatusSerializer.class);
        ReflectionUtils.setFieldValue(this.store, "serializer", mockSerializer);

        jobStatus = this.store.loadJobStatus(Arrays.asList("nostatus"));
        assertNull(jobStatus);

        verifyNoMoreInteractions(mockSerializer);
    }

    @Test
    void removeJobStatus() throws IOException
    {
        List<String> id = null;

        JobStatus jobStatus = this.store.loadJobStatus(id);

        assertNotNull(jobStatus);
        assertNull(jobStatus.getRequest().getId());
        assertEquals(JobStatus.State.FINISHED, jobStatus.getState());

        this.store.removeJobStatus(id);

        assertNull(this.store.loadJobStatus(id));
    }

    @Test
    void removeNotExistingJobStatus() throws IOException
    {
        this.store.removeJobStatus(Arrays.asList("notexist"));
    }

    @Test
    void storeNullJobStatus() throws IOException
    {
        this.store.saveJobStatus(null);
    }

    @Test
    void storeJobStatusWithNullRequest() throws IOException
    {
        JobStatus jobStatus = new DefaultJobStatus("type", null, null, null, null);

        this.store.saveJobStatus(jobStatus);
    }

    @Test
    void storeJobStatusWithNullId() throws IOException
    {
        JobStatus jobStatus = new DefaultJobStatus("type", new DefaultRequest(), null, null, null);

        this.store.saveJobStatus(jobStatus);
    }

    @Test
    void storeJobStatusWithBigId() throws IOException
    {
        DefaultRequest request = new DefaultRequest();
        request.setId(StringUtils.repeat('a', 768));

        JobStatus jobStatus = new DefaultJobStatus<>("type", request, null, null, null);

        this.store.saveJobStatus(jobStatus);

        String longAName = StringUtils.repeat('a', 250);

        assertTrue(new File(this.storeDirectory,
            "3/%s/%s/%s/aaaaaaaaaaaaaaaaaa/status.xml.zip".formatted(longAName, longAName, longAName)).exists());
    }

    @Test
    void storeJobStatusWithNullPartInId() throws IOException
    {
        DefaultRequest request = new DefaultRequest();
        request.setId("first", null, "second");

        JobStatus jobStatus = new DefaultJobStatus<>("type", request, null, null, null);

        this.store.saveJobStatus(jobStatus);

        assertTrue(new File(this.storeDirectory, "3/first/&null/second/status.xml.zip").exists());
    }

    @Test
    void storeJobStatusWithProblematicCharacters() throws IOException
    {
        DefaultRequest request = new DefaultRequest();
        request.setId("..", ".", " .*.");

        JobStatus jobStatus = new DefaultJobStatus<>("type", request, null, null, null);

        this.store.saveJobStatus(jobStatus);

        assertTrue(new File(this.storeDirectory, "3/%2E%2E/%2E/%20.%2A%2E/status.xml.zip").exists());
    }

    @Test
    void storeUnserializableJobStatus() throws IOException
    {
        List<String> id = Arrays.asList("test");
        DefaultRequest request = new DefaultRequest();
        request.setId(id);
        JobStatus jobStatus = new UnserializableJobStatus("type", request, null, null, null);

        this.store.saveJobStatus(jobStatus);

        // Verify that the status hasn't been serialized, indirectly verifying that isSerializable() has been called and
        // returned true.
        assertFalse(new File(this.storeDirectory, "3/test/status.xml").exists());
    }

    @Test
    void storeUnserializedJobStatus() throws IOException
    {
        List<String> id = Arrays.asList("test");
        DefaultRequest request = new DefaultRequest();
        request.setId(id);
        request.setStatusSerialized(false);
        JobStatus jobStatus = new DefaultJobStatus("type", request, null, null, null);

        this.store.saveJobStatus(jobStatus);

        // Verify that the status hasn't been serialized, indirectly verifying that isSerializable() has been called and
        // returned true.
        assertFalse(new File(this.storeDirectory, "3/test/status.xml").exists());
    }

    @Test
    void storeJobStatusWhenSerializable() throws IOException
    {
        List<String> id = Arrays.asList("newstatus");

        DefaultRequest request = new DefaultRequest();
        request.setId(id);
        JobStatus jobStatus = new DefaultJobStatus("type", request, null, null, null);

        this.store.saveJobStatus(jobStatus);

        JobStatus loadedJobStatus = this.store.loadJobStatus(id);
        assertEquals(jobStatus.getRequest(), loadedJobStatus.getRequest());
        assertEquals(jobStatus.getState(), loadedJobStatus.getState());
        assertEquals(jobStatus.getJobType(), loadedJobStatus.getJobType());

        // Verify that the status has been serialized, indirectly verifying that isSerializable() has been called and
        // returned true.
        assertTrue(new File(this.storeDirectory, "3/%s/status.xml.zip".formatted(id.get(0))).exists());
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

        status.getLoggerTail().error("error message", new DefaultPersistentJobStatusStore());

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

        status.getLoggerTail().error("error message", new SerializableObjectTest(new DefaultPersistentJobStatusStore()));

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
