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
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.function.FailableFunction;
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
    private static final String ID_WRAPPER_CLASS_NAME = "org.xwiki.job.internal.JobStatusSerializer_-IDWrapper";

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
    public Optional<List<String>> readID(File file) throws IOException
    {
        return processFile(file, this::readID);
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

    private Optional<List<String>> readID(InputStream inputStream) throws IOException
    {
        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);

        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(inputStream);

            // Navigate to the root element
            while (xmlEventReader.hasNext()) {
                XMLEvent event = xmlEventReader.nextEvent();
                if (event.isStartElement()) {
                    StartElement rootElement = event.asStartElement();
                    String rootClassName = rootElement.getName().getLocalPart();

                    // Verify the root class is a JobStatus
                    if (!isSimpleJobStatusClass(rootClassName)) {
                        this.logger.warn("Job status with class {} is not valid", rootClassName);
                        return Optional.empty();
                    }

                    // Process the elements within the root
                    return processRootElement(xmlEventReader, rootClassName);
                }
            }

            throw new IOException("No root element found in the XML");
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

    private Optional<List<String>> processRootElement(XMLEventReader xmlEventReader, String rootClassName)
        throws XMLStreamException
    {
        return findDirectChild(xmlEventReader, rootClassName, REQUEST_ELEMENT, requestElement -> {
            // Found the request element, check its class.
            Attribute classAttr = requestElement.getAttributeByName(new QName("class"));
            String requestClassName = classAttr != null ? classAttr.getValue() : null;

            if (isSimpleJobRequestClass(requestClassName)) {
                // Process the request element looking for the id element
                return processRequestElement(xmlEventReader);
            } else {
                this.logger.warn("Invalid request class {}", requestClassName);
                return Optional.empty();
            }
        });
    }

    private Optional<List<String>> processRequestElement(XMLEventReader xmlEventReader) throws XMLStreamException
    {
        return findDirectChild(xmlEventReader, REQUEST_ELEMENT, ID_ELEMENT, idElement ->
            // Found id element, process its string children
            processIdElement(idElement, xmlEventReader)
        );
    }

    /**
     * Iterate through XML elements searching for a direct child with the specified name.
     *
     * @param xmlEventReader the XML event reader
     * @param parentElementName the name of the parent element to search within
     * @param childElementName the name of the direct child element to find
     * @param childHandler handler to process the child when found
     * @return the result from the child handler or an empty optional if no matching child was found
     * @throws XMLStreamException if there's an error parsing the XML
     */
    private <T> Optional<T> findDirectChild(XMLEventReader xmlEventReader, String parentElementName,
        String childElementName, FailableFunction<StartElement, Optional<T>, XMLStreamException> childHandler)
        throws XMLStreamException
    {
        int depth = 0;
        while (xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();

            if (event.isStartElement()) {
                depth++;
                StartElement startElement = event.asStartElement();
                String elementName = startElement.getName().getLocalPart();

                // If we're at depth 1 (direct child) and found the element we're looking for
                if (depth == 1 && childElementName.equals(elementName)) {
                    return childHandler.apply(startElement);
                }
            } else if (event.isEndElement()) {
                EndElement endElement = event.asEndElement();
                String elementName = endElement.getName().getLocalPart();

                if (parentElementName.equals(elementName) && depth == 0) {
                    // Reached the end of the parent element
                    break;
                }
                depth--;
            }
        }

        return Optional.empty();
    }

    /**
     * Helper class to unserialize just the {@code id} property of a job request.
     */
    private static final class IDWrapper
    {
        private List<String> id;
    }

    private Optional<List<String>> processIdElement(StartElement idElement, XMLEventReader xmlEventReader)
        throws XMLStreamException
    {
        // Extract the raw XML content so we can use XStream to parse it as the exact content depends on the used
        // List implementation, and we need to be sure that the parsing is identical to XStream's parsing.
        StringWriter stringWriter = new StringWriter();
        idElement.writeAsEncodedUnicode(stringWriter);

        int depth = 0;

        while (xmlEventReader.hasNext()) {
            XMLEvent event = xmlEventReader.nextEvent();
            event.writeAsEncodedUnicode(stringWriter);

            if (event.isStartElement()) {
                depth++;
            } else if (event.isEndElement()) {
                // Reached the end of the id element
                if (depth == 0) {
                    break;
                }

                --depth;
            }
        }

        String idToParse =
            "<%s>%s</%s>".formatted(ID_WRAPPER_CLASS_NAME, stringWriter.toString(), ID_WRAPPER_CLASS_NAME);

        IDWrapper idWrapper = (IDWrapper) this.xstream.fromXML(idToParse);

        return Optional.of(idWrapper.id);
    }

    private boolean isZip(File file)
    {
        return FilenameUtils.getExtension(file.getName()).equals(ZIP_EXTENSION);
    }
}
