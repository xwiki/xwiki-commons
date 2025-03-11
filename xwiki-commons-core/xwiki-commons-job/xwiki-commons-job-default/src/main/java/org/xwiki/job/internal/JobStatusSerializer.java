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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.function.FailableSupplier;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.environment.Environment;
import org.xwiki.job.event.status.JobStatus;
import org.xwiki.xstream.internal.SafeXStream;

/**
 * Serialize/unserialize tool for job statuses.
 *
 * @version $Id$
 * @since 5.2M2
 */
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
@Component(roles = JobStatusSerializer.class)
@Singleton
public class JobStatusSerializer
{
    /**
     * Encoding used for file content and names.
     */
    private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

    private static final String ZIP_EXTENSION = "zip";

    private static final String REQUEST_ELEMENT = "request";

    private static final String ID_ELEMENT = "id";

    /**
     * Used to serialize and unserialize status.
     */
    @Inject
    private SafeXStream xstream;

    @Inject
    private Environment environment;

    @Inject
    private Logger logger;

    /**
     * @param status the status to serialize
     * @param file the file to serialize the status to
     * @throws IOException when failing to serialize the status
     */
    public void write(JobStatus status, File file) throws IOException
    {
        File parent = new File(this.environment.getTemporaryDirectory(), "job/");
        parent.mkdirs();
        File tempFile = File.createTempFile(file.getName(), ".tmp", parent);

        try (OutputStream stream = getOutputStream(tempFile, isZip(file))) {
            if (stream instanceof ArchiveOutputStream) {
                ((ArchiveOutputStream) stream).putArchiveEntry(new ZipArchiveEntry("status.xml"));
            }

            write(status, stream);

            if (stream instanceof ArchiveOutputStream) {
                ((ArchiveOutputStream) stream).closeArchiveEntry();
            }
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
                // Wait a bit before retrying
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ei) {
                    throw e;
                }
            }
        }
    }

    private OutputStream getOutputStream(File tempFile, boolean zip) throws IOException
    {
        tempFile.mkdirs();

        if (zip) {
            return new ZipArchiveOutputStream(tempFile);
        } else {
            return FileUtils.openOutputStream(tempFile);
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
        writer.write("<?xml version=\"1.0\" encoding=\"" + DEFAULT_ENCODING.name() + "\"?>\n");
        this.xstream.toXML(status, writer);
        writer.flush();
    }

    /**
     * @param file the file to read
     * @return the status
     * @throws IOException when failing to read the status file
     */
    public JobStatus read(File file) throws IOException
    {
        return processFile(file, stream -> (JobStatus) this.xstream.fromXML(stream));
    }

    /**
     * Load the job ID from the given file. This doesn't unserialize the job status but tries parsing it directly. If
     * the format of the job status isn't as expected, an empty result is returned.
     *
     * @param file the file to read
     * @return the job ID if the file could be parsed
     * @throws IOException when failing to read the status file
     */
    public Optional<List<String>> loadID(File file) throws IOException
    {
        return processFile(file, this::loadID);
    }

    /**
     * Process a file with the given handler function.
     *
     * @param <T> the return type of the function
     * @param file the file to read
     * @param streamHandler the function to process the input stream
     * @return the result of the handler function
     * @throws IOException when failing to read or process the file
     */
    private <T> T processFile(File file, FailableFunction<InputStream, T, IOException> streamHandler) throws IOException
    {
        try (InputStream is = new FileInputStream(file)) {
            if (isZip(file)) {
                try (ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(is)) {
                    ZipArchiveEntry entry = zipArchiveInputStream.getNextEntry();
                    if (entry == null) {
                        throw new IOException("Missing entry in the status zip file");
                    }

                    return streamHandler.apply(zipArchiveInputStream);
                }
            } else {
                return streamHandler.apply(is);
            }
        }
    }

    private Optional<List<String>> loadID(InputStream inputStream) throws IOException
    {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        try {
            XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(inputStream);

            // Navigate to the root element
            xmlStreamReader.nextTag();
            if (!xmlStreamReader.isStartElement()) {
                throw new IOException("Expected START_ELEMENT at the root, but got " + xmlStreamReader.getEventType());
            }

            // Verify the root class is a JobStatus
            String rootClassName = xmlStreamReader.getLocalName();
            if (!isSimpleJobStatusClass(rootClassName)) {
                this.logger.warn("Job status with class {} is not valid", rootClassName);
                return Optional.empty();
            }

            // Process the elements within the root
            return processRootElement(xmlStreamReader, rootClassName);
        } catch (XMLStreamException e) {
            throw new IOException("Error parsing XML", e);
        }
    }

    /**
     * @param className the class to check
     * @return if the given class is a subclass of {@link org.xwiki.job.AbstractJobStatus} and doesn't override
     * {@code getRequest}.
     */
    private boolean isSimpleJobStatusClass(String className)
    {
        try {
            Class<?> statusClass = Class.forName(className);
            return org.xwiki.job.AbstractJobStatus.class.isAssignableFrom(statusClass)
                && isMethodNotOverridden(statusClass, "getRequest", org.xwiki.job.AbstractJobStatus.class);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * @param className the class to check
     * @return if the given class is a subclass of {@link org.xwiki.job.AbstractRequest} and doesn't override
     * {@code getId}.
     */
    private boolean isSimpleJobRequestClass(String className)
    {
        if (className == null) {
            return false;
        }

        try {
            Class<?> requestClass = Class.forName(className);
            return org.xwiki.job.AbstractRequest.class.isAssignableFrom(requestClass)
                && isMethodNotOverridden(requestClass, "getId", org.xwiki.job.AbstractRequest.class);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    boolean isMethodNotOverridden(Class<?> subClass, String methodName, Class<?> ancestor)
    {
        try {
            return subClass.getMethod(methodName).getDeclaringClass() == ancestor;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Method " + methodName + " not found in class " + subClass, e);
        }
    }

    private Optional<List<String>> processRootElement(XMLStreamReader xmlStreamReader, String rootClassName)
        throws XMLStreamException
    {
        return findDirectChild(xmlStreamReader, rootClassName, REQUEST_ELEMENT, () -> {
            // Found request element, check its class
            String requestClassName = getClassAttribute(xmlStreamReader);

            if (isSimpleJobRequestClass(requestClassName)) {
                // Process the request element looking for the id element
                return processRequestElement(xmlStreamReader);
            } else {
                this.logger.warn("Invalid request class {}", requestClassName);
                return Optional.empty();
            }
        });
    }

    private String getClassAttribute(XMLStreamReader xmlStreamReader)
    {
        for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
            if ("class".equals(xmlStreamReader.getAttributeLocalName(i))) {
                return xmlStreamReader.getAttributeValue(i);
            }
        }
        return null;
    }

    private Optional<List<String>> processRequestElement(XMLStreamReader xmlStreamReader) throws XMLStreamException
    {
        return findDirectChild(xmlStreamReader, REQUEST_ELEMENT, ID_ELEMENT, () ->
            // Found id element, process its string children
            processIdElement(xmlStreamReader)
        );
    }

    /**
     * Iterate through XML elements searching for a direct child with the specified name.
     *
     * @param xmlStreamReader the XML stream reader
     * @param parentElementName the name of the parent element to search within
     * @param childElementName the name of the direct child element to find
     * @param childHandler handler to process the child when found
     * @return the result from the child handler or an empty optional if no matching child was found
     * @throws XMLStreamException if there's an error parsing the XML
     */
    private <T> Optional<T> findDirectChild(XMLStreamReader xmlStreamReader, String parentElementName,
        String childElementName, FailableSupplier<Optional<T>, XMLStreamException> childHandler)
        throws XMLStreamException
    {
        int depth = 0;
        while (xmlStreamReader.hasNext()) {
            int eventType = xmlStreamReader.next();

            if (eventType == XMLStreamConstants.START_ELEMENT) {
                depth++;
                // If we're at depth 1 (direct child) and found the element we're looking for
                if (depth == 1 && childElementName.equals(xmlStreamReader.getLocalName())) {
                    return childHandler.get();
                }
            } else if (eventType == XMLStreamConstants.END_ELEMENT) {
                if (parentElementName.equals(xmlStreamReader.getLocalName()) && depth == 0) {
                    // Reached the end of the parent element
                    break;
                }
                depth--;
            }
        }

        return Optional.empty();
    }

    private Optional<List<String>> processIdElement(XMLStreamReader xmlStreamReader) throws XMLStreamException
    {
        List<String> jobIds = new ArrayList<>();

        // Extract the string elements from the id element
        while (xmlStreamReader.hasNext()) {
            int eventType = xmlStreamReader.next();

            if (eventType == XMLStreamConstants.START_ELEMENT && "string".equals(xmlStreamReader.getLocalName())) {
                eventType = xmlStreamReader.next();
                if (eventType == XMLStreamConstants.CHARACTERS) {
                    jobIds.add(xmlStreamReader.getText());
                }
            } else if (eventType == XMLStreamConstants.START_ELEMENT && "null".equals(xmlStreamReader.getLocalName())) {
                jobIds.add(null);
            } else if (eventType == XMLStreamConstants.END_ELEMENT && ID_ELEMENT.equals(xmlStreamReader.getLocalName()))
            {
                // Reached the end of the id element
                break;
            }
        }

        return Optional.of(jobIds);
    }

    private boolean isZip(File file)
    {
        return FilenameUtils.getExtension(file.getName()).equals(ZIP_EXTENSION);
    }
}
