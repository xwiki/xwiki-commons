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
package org.xwiki.tool.xar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.codehaus.plexus.archiver.ArchiveEntry;
import org.codehaus.plexus.archiver.zip.ZipArchiver;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.dom.DOMDocument;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xwiki.tool.xar.internal.XWikiDocument;

/**
 * Gather all resources in a XAR file (which is actually a ZIP file). Also generates a XAR descriptor if none is
 * provided.
 *
 * @version $Id$
 */
@Mojo(name = "xar", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class XARMojo extends AbstractXARMojo
{
    /**
     * Indicate if XAR dependencies should be included in the produced XAR package.
     */
    @Parameter(property = "xar.includeDependencies", defaultValue = "false")
    private boolean includeDependencies;

    /**
     * Allows to change the default entry type that will be applied for each entry of the project.
     *
     * @since 12.7
     */
    @Parameter(property = "defaultEntryType", readonly = true, required = false)
    private String defaultEntryType;

    /**
     * List of XML transformations to execute on the XML files.
     */
    @Parameter
    private List<Transformation> transformations;

    @Override
    public void execute() throws MojoExecutionException
    {
        if (this.project.getResources().isEmpty()) {
            getLog().warn("No XAR created as no resources were found");
            return;
        }

        try {
            performArchive();
        } catch (Exception e) {
            throw new MojoExecutionException("Error while creating XAR file", e);
        }
    }

    /**
     * Create the XAR by zipping the resource files.
     *
     * @throws Exception if the zipping failed for some reason
     */
    private void performArchive() throws Exception
    {
        // The source dir points to the target/classes directory where the Maven resources plugin
        // has copied the XAR files during the process-resources phase.
        // For package.xml, however, we look in the resources directory (i.e. src/main/resources).
        File sourceDir = new File(this.project.getBuild().getOutputDirectory());

        // Check that there are files in the source dir
        if (sourceDir.listFiles() == null) {
            throw new Exception(
                String.format("No XAR XML files found in [%s]. Has the Maven Resource plugin be called?", sourceDir));
        }

        File xarFile = new File(this.project.getBuild().getDirectory(),
            this.project.getArtifactId() + "-" + this.project.getVersion() + ".xar");

        ZipArchiver archiver = new ZipArchiver();
        archiver.setEncoding(this.encoding);
        archiver.setDestFile(xarFile);
        archiver.setIncludeEmptyDirs(false);
        archiver.setCompress(true);

        if (this.includeDependencies) {
            // Unzip dependent XARs on top of this project's XML documents but without overwriting
            // existing files since we want this project's files to be used if they override a file
            // present in a XAR dependency.
            unpackDependentXARs();
        }

        // Perform XML transformations
        performTransformations();

        // If no package.xml can be found at the top level of the current project, generate one
        // otherwise, try to use the existing one
        File resourcesDir = getResourcesDirectory();
        if (!hasPackageXmlFile(resourcesDir)) {
            // Add files and generate the package.xml file
            addFilesToArchive(archiver, sourceDir);
        } else {
            File packageXml = new File(resourcesDir, PACKAGE_XML);
            addFilesToArchive(archiver, sourceDir, packageXml);
        }
        archiver.createArchive();

        this.project.getArtifact().setFile(xarFile);
    }

    private boolean hasPackageXmlFile(File resourcesDir) throws MojoExecutionException
    {
        // If the src/main/resources directory doesn't exist then we consider there isn't any package.xml file...
        if (!resourcesDir.exists()) {
            return false;
        }

        FilenameFilter packageXmlFiler = new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String name)
            {
                return (name.equals(PACKAGE_XML));
            }
        };
        String[] fileNames = resourcesDir.list(packageXmlFiler);
        if (fileNames == null) {
            throw new MojoExecutionException(
                String.format("Couldn't get list of files in resources dir [%s]", resourcesDir));
        }
        return fileNames.length != 0;
    }

    private void performTransformations() throws Exception
    {
        if (this.transformations == null) {
            return;
        }

        // Copy XML pages from dependent XAR if we modify them.
        unpackTransformedXARs();

        SAXReader reader = new SAXReader();

        // For each defined file, perform the transformation asked
        for (Transformation transformation : this.transformations) {
            File file = new File(this.project.getBuild().getOutputDirectory(), transformation.getFile());
            Document document = reader.read(file);
            Node node = document.selectSingleNode(transformation.getXpath());

            if (node != null) {
                String value = transformation.getValue();
                if (value != null) {
                    // Get the current value at node and replace $1 with it (if any)
                    String currentValue = node.getText();
                    node.setText(value.replace("$1", currentValue));
                } else if (transformation.getAction() != null) {
                    // Parse xml value
                    Document xmlDocument = null;
                    Element xmlElement = null;
                    File content = null;
                    if (transformation.getXml() != null) {
                        xmlDocument = reader.read(transformation.getXml());
                        xmlElement = xmlDocument.getRootElement();
                    } else if (transformation.getContent() != null) {
                        content = transformation.getContent();
                    }

                    // Apply action
                    switch (transformation.getAction()) {
                        case INSERT_CHILD:
                            // Insert xml as a child of the node
                            ((Element) node).add(xmlElement);
                            break;

                        case REPLACE:
                            // Replace the node with the xml
                            Element parent = node.getParent();
                            if (parent == null) {
                                document = xmlDocument;
                            } else {
                                // this is the method we use to replace the child and it seems to be
                                // the canonical way in dom4j (for which a manipulation on a collection
                                // is a manipulation of the XML tree: We manipulate the elements' list
                                // by setting the child at the position where it was found.
                                List<Element> siblings = parent.elements();
                                siblings.set(siblings.indexOf(node), xmlElement);
                            }
                            break;

                        case INSERT_ATTACHMENT_CONTENT:
                            if(content != null) {
                                node.selectSingleNode("filesize").setText(Long.toString(content.length(),10));
                                node.selectSingleNode("content").setText( "\r\n" +
                                    new String(
                                        Base64.getMimeEncoder().encode(FileUtils.readFileToByteArray(content))) + "\r\n");
                            }
                            break;

                        case INSERT_TEXT:
                            if( content != null) {
                                node.setText(FileUtils.readFileToString(content, Charset.forName(transformation.getCharset())));
                            }
                            break;

                        case REMOVE:
                            // Replace the node
                            node.getParent().remove(node);
                            break;

                        default:
                            break;
                    }
                }

                // Write the modified file to disk
                XMLWriter writer = new XMLWriter(new FileOutputStream(file));
                try {
                    writer.write(document);
                    writer.flush();
                } finally {
                    writer.close();
                }
            } else {
                getLog().warn("Can't find any node matching the xpath [" + transformation.getXpath() + "]");
            }
        }
    }

    private void unpackTransformedXARs() throws MojoExecutionException
    {
        for (Transformation transformation : this.transformations) {
            Set<Artifact> artifacts = this.project.getArtifacts();
            if (artifacts != null) {
                for (Artifact artifact : artifacts) {
                    if (!artifact.isOptional() && "xar".equals(artifact.getType())) {
                        String id = String.format("%s:%s", artifact.getGroupId(), artifact.getArtifactId());
                        if (id.equals(transformation.getArtifact())) {
                            unpackXARToOutputDirectory(artifact, new String[] { transformation.getFile() },
                                new String[] {});
                        }
                    }
                }
            }
        }
    }

    /**
     * Unpack XAR dependencies before pack then into it.
     *
     * @throws MojoExecutionException error when unpack dependencies.
     */
    private void unpackDependentXARs() throws MojoExecutionException
    {
        Set<Artifact> artifacts = this.project.getArtifacts();
        if (artifacts != null) {
            for (Artifact artifact : artifacts) {
                if (!artifact.isOptional() && "xar".equals(artifact.getType())) {
                    unpackXARToOutputDirectory(artifact, getIncludes(), getExcludes());
                }
            }
        }
    }

    /**
     * Create and add package configuration file to the package.
     *
     * @param packageFile the package when to add configuration file.
     * @param files the files in the package.
     * @throws Exception error when writing the configuration file.
     */
    private void generatePackageXml(File packageFile, Collection<ArchiveEntry> files) throws Exception
    {
        getLog().info(String.format("Generating package.xml descriptor at [%s]", packageFile.getPath()));

        OutputFormat outputFormat = new OutputFormat("", true);
        outputFormat.setEncoding(this.encoding);
        OutputStream out = new FileOutputStream(packageFile);
        XMLWriter writer = new XMLWriter(out, outputFormat);
        writer.write(toXML(files));
        writer.close();
        out.close();
    }

    /**
     * Generate a DOM4J Document containing the generated XML.
     *
     * @param files the list of files that we want to include in the generated package XML file.
     * @return the DOM4J Document containing the generated XML
     */
    private Document toXML(Collection<ArchiveEntry> files) throws Exception
    {
        Document doc = new DOMDocument();

        Element packageElement = new DOMElement("package");
        doc.setRootElement(packageElement);

        Element infoElement = new DOMElement("infos");
        packageElement.add(infoElement);
        addInfoElements(infoElement);

        Element filesElement = new DOMElement(FILES_TAG);
        packageElement.add(filesElement);
        addFileElements(files, filesElement);

        return doc;
    }

    /**
     * Add all the XML elements under the &lt;info&gt; element (name, description, license, author, version and whether
     * it's a backup pack or not).
     *
     * @param infoElement the info element to which to add to
     */
    private void addInfoElements(Element infoElement)
    {
        Element el = new DOMElement("name");
        el.addText(this.project.getName());
        infoElement.add(el);

        el = new DOMElement("description");
        String description = this.project.getDescription();
        if (description == null) {
            el.addText("");
        } else {
            el.addText(description);
        }
        infoElement.add(el);

        el = new DOMElement("licence");
        el.addText("");
        infoElement.add(el);

        el = new DOMElement("author");
        el.addText("XWiki.Admin");
        infoElement.add(el);

        el = new DOMElement("extensionId");
        el.addText(this.project.getGroupId() + ':' + this.project.getArtifactId());
        infoElement.add(el);

        el = new DOMElement("version");
        el.addText(this.project.getVersion());
        infoElement.add(el);

        el = new DOMElement("backupPack");
        el.addText("false");
        infoElement.add(el);
    }

    /**
     * Add all the XML elements under the &lt;files&gt; element (the list of files present in the XAR).
     *
     * @param files the list of files that we want to include in the generated package XML file.
     * @param filesElement the files element to which to add to
     */
    private void addFileElements(Collection<ArchiveEntry> files, Element filesElement) throws Exception
    {
        for (ArchiveEntry entry : files) {
            // Don't add files in META-INF to the package.xml file
            if (entry.getName().indexOf("META-INF") == -1) {
                XWikiDocument xdoc = getDocFromXML(entry.getFile());
                String reference = xdoc.getReference();
                Element element = new DOMElement(FILE_TAG);
                element.setText(reference);
                element.addAttribute("language", xdoc.getLocale());
                element.addAttribute("defaultAction", "0");

                // Add configured properties
                XAREntry cfgEntry = getEntryMap().get(reference);
                if (cfgEntry != null && cfgEntry.getType() != null) {
                    element.addAttribute("type", cfgEntry.getType());
                } else if (defaultEntryType != null) {
                    element.addAttribute("type", defaultEntryType);
                }

                filesElement.add(element);
            }
        }
    }

    /**
     * Gets the list of entry from a 'package.xml'-like document.
     *
     * @param file the XML document to parse
     * @return the Map of entries contained in the XML document (ordered in the same order as the file)
     * @throws Exception if the XML document is invalid or it contains no document list or it doesn't exist
     * @since 10.3
     */
    protected static Map<String, XAREntry> getXarEntriesMapFromXML(File file) throws Exception
    {
        try (FileInputStream stream = new FileInputStream(file)) {
            return getXarEntriesMapFromXML(stream);
        }
    }

    /**
     * Gets the list of entry from a 'package.xml'-like document.
     *
     * @param stream the XML document to parse
     * @return the Map of entries contained in the XML document (ordered in the same order as the file)
     * @throws Exception if the XML document is invalid or it contains no document list or it doesn't exist
     * @since 10.3
     */
    public static Map<String, XAREntry> getXarEntriesMapFromXML(InputStream stream) throws Exception
    {
        SAXReader reader = new SAXReader();
        Document domdoc;
        domdoc = reader.read(stream);

        Element filesElement = domdoc.getRootElement().element(FILES_TAG);

        if (filesElement == null) {
            throw new Exception("The supplied document contains no document list ");
        }

        Map<String, XAREntry> result = new LinkedHashMap<>();
        Collection elements = filesElement.elements(FILE_TAG);
        for (Object item : elements) {
            if (item instanceof Element) {
                Element currentElement = (Element) item;

                XAREntry entry = new XAREntry();
                entry.setDocument(currentElement.getText());
                entry.setType(currentElement.attributeValue("type"));

                result.put(entry.getDocument(), entry);
            }
        }

        return result;
    }

    /**
     * Gets the list of document names from a 'package.xml'-like document.
     *
     * @param file the XML document to parse
     * @return the list of document names contained in the XML document
     * @throws Exception if the XML document is invalid or it contains no document list or it doesn't exist
     */
    protected static Collection<String> getDocumentNamesFromXML(File file) throws Exception
    {
        return getXarEntriesMapFromXML(file).keySet();
    }

    public static Collection<String> getDocumentNamesFromXML(InputStream stream) throws Exception
    {
        return getXarEntriesMapFromXML(stream).keySet();
    }

    /**
     * Adds the files from a specific directory to an archive. It also builds a package.xml file based on that content
     * which is also added to the archive.
     *
     * @param archiver the archive in which the files will be added
     * @param sourceDir the directory whose contents will be added to the archive
     * @throws Exception if the files cannot be added to the archive
     */
    private void addFilesToArchive(ZipArchiver archiver, File sourceDir) throws Exception
    {
        File generatedPackageFile = new File(sourceDir, PACKAGE_XML);
        if (generatedPackageFile.exists()) {
            generatedPackageFile.delete();
        }

        archiver.addDirectory(sourceDir, getIncludes(), getExcludes());
        generatePackageXml(generatedPackageFile, archiver.getFiles().values());
        archiver.addFile(generatedPackageFile, PACKAGE_XML);
    }

    /**
     * Adds files from a specific directory to an archive. It uses an existing package.xml to filter the files to be
     * added.
     *
     * @param archiver the archive in which the files will be added
     * @param sourceDir the directory whose contents will be added to the archive
     * @param packageXml the corresponding package.xml file
     * @throws Exception if the files cannot be added to the archive
     */
    private void addFilesToArchive(ZipArchiver archiver, File sourceDir, File packageXml) throws Exception
    {
        Collection<String> documentNames;
        getLog().info(String.format("Using the existing package.xml descriptor at [%s]", packageXml.getPath()));
        try {
            documentNames = getDocumentNamesFromXML(packageXml);
        } catch (Exception e) {
            getLog().error(String.format("The existing [%s] is invalid.", PACKAGE_XML));
            throw e;
        }

        // Next, we scan the hole directory and subdirectories for documents.

        Queue<File> fileQueue = new LinkedList<>();
        addContentsToQueue(fileQueue, sourceDir);
        while (!fileQueue.isEmpty() && !documentNames.isEmpty()) {
            File currentFile = fileQueue.poll();
            if (currentFile.isDirectory()) {
                addContentsToQueue(fileQueue, currentFile);
            } else {
                String documentReference = XWikiDocument.getReference(currentFile);
                if (documentNames.contains(documentReference)) {
                    // building the path the current file will have within the archive
                    //
                    // Note: DO NOT USE String.split since it requires a regexp. Under Windows XP, the FileSeparator is
                    // '\' when not escaped is a special character of the regexp
                    //   String archivedFilePath =
                    //   currentFile.getAbsolutePath().split(sourceDir.getAbsolutePath() + File.separator)[1];
                    String archivedFilePath = currentFile.getAbsolutePath()
                        .substring((sourceDir.getAbsolutePath() + File.separator).length());
                    archivedFilePath = archivedFilePath.replace(File.separatorChar, '/');

                    archiver.addFile(currentFile, archivedFilePath);
                    documentNames.remove(documentReference);
                }
            }
        }

        if (!documentNames.isEmpty()) {
            StringBuilder errorMessage = new StringBuilder("The following documents could not be found: ");
            for (String name : documentNames) {
                errorMessage.append(name);
                errorMessage.append(" ");
            }
            throw new Exception(errorMessage.toString());
        }

        archiver.addFile(packageXml, PACKAGE_XML);
    }

    /**
     * Adds the contents of a specific directory to a queue of files.
     *
     * @param fileQueue the queue of files
     * @param sourceDir the directory to be scanned
     */
    private static void addContentsToQueue(Queue<File> fileQueue, File sourceDir) throws MojoExecutionException
    {
        File[] files = sourceDir.listFiles();
        if (files != null) {
            for (File currentFile : files) {
                fileQueue.add(currentFile);
            }
        } else {
            throw new MojoExecutionException(String.format("Couldn't get list of files in source dir [%s]", sourceDir));
        }
    }
}
