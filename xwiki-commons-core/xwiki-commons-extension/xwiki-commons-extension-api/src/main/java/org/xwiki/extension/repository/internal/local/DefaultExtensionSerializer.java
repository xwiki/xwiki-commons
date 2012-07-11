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
package org.xwiki.extension.repository.internal.local;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xwiki.component.annotation.Component;
import org.xwiki.extension.DefaultExtensionAuthor;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionAuthor;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionLicense;
import org.xwiki.extension.ExtensionLicenseManager;
import org.xwiki.extension.InvalidExtensionException;
import org.xwiki.extension.LocalExtension;
import org.xwiki.extension.repository.internal.installed.DefaultInstalledExtension;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;

/**
 * Local repository storage serialization tool.
 * 
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
public class DefaultExtensionSerializer implements ExtensionSerializer
{
    private static final String ELEMENT_ID = "id";

    private static final String ELEMENT_VERSION = "version";

    private static final String ELEMENT_TYPE = "type";

    private static final String ELEMENT_LICENSES = "licenses";

    private static final String ELEMENT_LLICENSE = "license";

    private static final String ELEMENT_LLNAME = "name";

    private static final String ELEMENT_LLCONTENT = "content";

    private static final String ELEMENT_NAME = "name";

    private static final String ELEMENT_SUMMARY = "summary";

    private static final String ELEMENT_DESCRIPTION = "description";

    private static final String ELEMENT_WEBSITE = "website";

    private static final String ELEMENT_AUTHORS = "authors";

    private static final String ELEMENT_AAUTHOR = "author";

    private static final String ELEMENT_AANAME = "name";

    private static final String ELEMENT_AAURL = "url";

    private static final String ELEMENT_DEPENDENCIES = "dependencies";

    private static final String ELEMENT_DDEPENDENCY = "dependency";

    private static final String ELEMENT_FEATURES = "features";

    private static final String ELEMENT_FFEATURE = "feature";

    private static final String ELEMENT_PROPERTIES = "properties";

    @Deprecated
    private static final String ELEMENT_INSTALLED = "installed";

    @Deprecated
    private static final String ELEMENT_NAMESPACES = "namespaces";

    @Deprecated
    private static final String ELEMENT_NNAMESPACE = "namespace";

    @Inject
    private ExtensionLicenseManager licenseManager;

    /**
     * Used to parse XML descriptor file.
     */
    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    protected Map<String, ExtensionPropertySerializer> serializerById;

    protected Map<Class< ? >, ExtensionPropertySerializer> serializerByClass;

    {
        {
            this.serializerById = new HashMap<String, ExtensionPropertySerializer>();
            this.serializerByClass = new LinkedHashMap<Class< ? >, ExtensionPropertySerializer>();

            StringExtensionPropertySerializer stringSerializer = new StringExtensionPropertySerializer();
            IntegerExtensionPropertySerializer integerSerializer = new IntegerExtensionPropertySerializer();
            BooleanExtensionPropertySerializer booleanSerializer = new BooleanExtensionPropertySerializer();
            CollectionExtensionPropertySerializer collectionSerializer =
                new CollectionExtensionPropertySerializer(this.serializerById, this.serializerByClass);
            SetExtensionPropertySerializer setSerializer =
                new SetExtensionPropertySerializer(this.serializerById, this.serializerByClass);

            this.serializerById.put(null, stringSerializer);
            this.serializerById.put("", stringSerializer);
            this.serializerById.put(integerSerializer.getType(), integerSerializer);
            this.serializerById.put(booleanSerializer.getType(), booleanSerializer);
            this.serializerById.put(collectionSerializer.getType(), collectionSerializer);
            this.serializerById.put(setSerializer.getType(), setSerializer);

            this.serializerByClass.put(String.class, stringSerializer);
            this.serializerByClass.put(Integer.class, integerSerializer);
            this.serializerByClass.put(Boolean.class, booleanSerializer);
            this.serializerByClass.put(Set.class, setSerializer);
            this.serializerByClass.put(Collection.class, collectionSerializer);
        }
    }

    @Override
    public DefaultLocalExtension loadDescriptor(DefaultLocalExtensionRepository repository, InputStream descriptor)
        throws InvalidExtensionException
    {
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new InvalidExtensionException("Failed to create new DocumentBuilder", e);
        }

        Document document;
        try {
            document = documentBuilder.parse(descriptor);
        } catch (Exception e) {
            throw new InvalidExtensionException("Failed to parse descriptor", e);
        }

        Element extensionElement = document.getDocumentElement();

        // Mandatory fields

        Node idNode = extensionElement.getElementsByTagName(ELEMENT_ID).item(0);
        Node versionNode = extensionElement.getElementsByTagName(ELEMENT_VERSION).item(0);
        Node typeNode = extensionElement.getElementsByTagName(ELEMENT_TYPE).item(0);

        DefaultLocalExtension localExtension =
            new DefaultLocalExtension(repository,
                new ExtensionId(idNode.getTextContent(), versionNode.getTextContent()), typeNode.getTextContent());

        // Optional fields

        Node nameNode = getNode(extensionElement, ELEMENT_NAME);
        if (nameNode != null) {
            localExtension.setName(nameNode.getTextContent());
        }
        Node summaryNode = getNode(extensionElement, ELEMENT_SUMMARY);
        if (summaryNode != null) {
            localExtension.setSummary(summaryNode.getTextContent());
        }
        Node descriptionNode = getNode(extensionElement, ELEMENT_DESCRIPTION);
        if (descriptionNode != null) {
            localExtension.setDescription(descriptionNode.getTextContent());
        }
        Node websiteNode = getNode(extensionElement, ELEMENT_WEBSITE);
        if (websiteNode != null) {
            localExtension.setWebsite(websiteNode.getTextContent());
        }

        // Licenses
        Node licensesNode = getNode(extensionElement, ELEMENT_LICENSES);
        if (licensesNode != null) {
            NodeList licenseNodeList = licensesNode.getChildNodes();
            for (int i = 0; i < licenseNodeList.getLength(); ++i) {
                Node licenseNode = licenseNodeList.item(i);

                if (licenseNode.getNodeName().equals(ELEMENT_LLICENSE)) {
                    Node licenseNameNode = getNode(licenseNode, ELEMENT_LLNAME);
                    Node licenceContentNode = getNode(licenseNode, ELEMENT_LLCONTENT);

                    String licenseName = licenseNameNode.getTextContent();
                    ExtensionLicense license = this.licenseManager.getLicense(licenseName);
                    if (license == null) {
                        try {
                            license =
                                new ExtensionLicense(licenseName, licenceContentNode != null
                                    ? IOUtils.readLines(new StringReader(licenceContentNode.getTextContent())) : null);
                        } catch (IOException e) {
                            // That should never happen
                            throw new InvalidExtensionException("Failed to write license content", e);
                        }
                    }

                    localExtension.addLicense(license);
                }
            }
        }

        // Authors
        Node authorsNode = getNode(extensionElement, ELEMENT_AUTHORS);
        if (authorsNode != null) {
            NodeList authors = authorsNode.getChildNodes();
            for (int i = 0; i < authors.getLength(); ++i) {
                Node authorNode = authors.item(i);

                if (authorNode.getNodeName() == ELEMENT_AAUTHOR) {
                    Node authorNameNode = getNode(authorNode, ELEMENT_AANAME);
                    Node authorURLNode = getNode(authorNode, ELEMENT_AAURL);

                    String authorName = authorNameNode != null ? authorNameNode.getTextContent() : null;
                    URL authorURL;
                    try {
                        authorURL = authorURLNode != null ? new URL(authorURLNode.getTextContent()) : null;
                    } catch (MalformedURLException e) {
                        // That should never happen
                        throw new InvalidExtensionException("Malformed URL [" + authorURLNode.getTextContent() + "]", e);
                    }

                    localExtension.addAuthor(new DefaultExtensionAuthor(authorName, authorURL));
                }
            }
        }

        // Features
        List<String> features = parseList(extensionElement, ELEMENT_FEATURES, ELEMENT_FFEATURE);
        if (features != null) {
            localExtension.setFeatures(features);
        }

        // Dependencies
        Node dependenciesNode = getNode(extensionElement, ELEMENT_DEPENDENCIES);
        if (dependenciesNode != null) {
            NodeList dependenciesNodeList = dependenciesNode.getChildNodes();
            for (int i = 0; i < dependenciesNodeList.getLength(); ++i) {
                Node dependency = dependenciesNodeList.item(i);

                if (dependency.getNodeName().equals(ELEMENT_DDEPENDENCY)) {
                    Node dependencyIdNode = getNode(dependency, ELEMENT_ID);
                    Node dependencyVersionNode = getNode(dependency, ELEMENT_VERSION);

                    localExtension.addDependency(new DefaultExtensionDependency(dependencyIdNode.getTextContent(),
                        new DefaultVersionConstraint(dependencyVersionNode.getTextContent()),
                        parseProperties((Element) dependency)));
                }
            }
        }

        // Properties
        Map<String, Object> properties = parseProperties(extensionElement);
        if (properties != null) {
            localExtension.setProperties(properties);
        }

        // Deprecated Install fields

        Node enabledNode = getNode(extensionElement, ELEMENT_INSTALLED);
        if (enabledNode != null) {
            localExtension.putProperty(DefaultInstalledExtension.PKEY_INSTALLED,
                Boolean.valueOf(enabledNode.getTextContent()));
        }

        // Deprecated Namespaces
        List<String> namespaces = parseList(extensionElement, ELEMENT_NAMESPACES, ELEMENT_NNAMESPACE);
        if (namespaces != null) {
            localExtension.putProperty(DefaultInstalledExtension.PKEY_NAMESPACES, namespaces);
        }

        return localExtension;
    }

    private List<String> parseList(Element extensionElement, String rootElement, String childElement)
    {
        List<String> list;

        Node featuresNode = getNode(extensionElement, rootElement);
        if (featuresNode != null) {
            list = new LinkedList<String>();

            NodeList features = featuresNode.getChildNodes();
            for (int i = 0; i < features.getLength(); ++i) {
                Node featureNode = features.item(i);

                if (featureNode.getNodeName() == childElement) {
                    list.add(featureNode.getTextContent());
                }
            }
        } else {
            list = null;
        }

        return list;
    }

    private Map<String, Object> parseProperties(Element parentElement)
    {
        Map<String, Object> properties = null;

        Node propertiesNode = getNode(parentElement, ELEMENT_PROPERTIES);
        if (propertiesNode != null) {
            properties = new HashMap<String, Object>();
            NodeList propertyNodeList = propertiesNode.getChildNodes();
            for (int i = 0; i < propertyNodeList.getLength(); ++i) {
                Node propertyNode = propertyNodeList.item(i);

                if (propertyNode.getNodeType() == Node.ELEMENT_NODE) {
                    Object value =
                        CollectionExtensionPropertySerializer.toValue((Element) propertyNode, serializerById);

                    if (value != null) {
                        properties.put(propertyNode.getNodeName(), value);
                    }
                }
            }
        }

        return properties;
    }

    private Node getNode(Node parentNode, String elementName)
    {
        NodeList children = parentNode.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node node = children.item(i);

            if (node.getNodeName().equals(elementName)) {
                return node;
            }
        }

        return null;
    }

    @Override
    public void saveDescriptor(LocalExtension extension, OutputStream fos) throws ParserConfigurationException,
        TransformerException
    {
        DocumentBuilder documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        Element extensionElement = document.createElement("extension");
        document.appendChild(extensionElement);

        addElement(document, extensionElement, ELEMENT_ID, extension.getId().getId());
        addElement(document, extensionElement, ELEMENT_VERSION, extension.getId().getVersion().getValue());
        addElement(document, extensionElement, ELEMENT_TYPE, extension.getType());
        addElement(document, extensionElement, ELEMENT_NAME, extension.getName());
        addElement(document, extensionElement, ELEMENT_SUMMARY, extension.getSummary());
        addElement(document, extensionElement, ELEMENT_DESCRIPTION, extension.getDescription());
        addElement(document, extensionElement, ELEMENT_WEBSITE, extension.getWebSite());

        addFeatures(document, extensionElement, extension);

        addAuthors(document, extensionElement, extension);

        addLicenses(document, extensionElement, extension);

        addDependencies(document, extensionElement, extension);

        addProperties(document, extensionElement, extension.getProperties());

        // save

        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = transfac.newTransformer();
        trans.setOutputProperty(OutputKeys.INDENT, "yes");

        DOMSource source = new DOMSource(document);
        Result result = new StreamResult(fos);
        trans.transform(source, result);
    }

    private void addLicenses(Document document, Element parentElement, Extension extension)
    {
        if (extension.getLicenses() != null && !extension.getLicenses().isEmpty()) {
            Element licensesElement = document.createElement(ELEMENT_LICENSES);
            parentElement.appendChild(licensesElement);

            for (ExtensionLicense license : extension.getLicenses()) {
                Element licenseElement = document.createElement(ELEMENT_LLICENSE);
                licensesElement.appendChild(licenseElement);

                addElement(document, licenseElement, ELEMENT_LLNAME, license.getName());
                if (this.licenseManager.getLicense(license.getName()) == null && license.getContent() != null) {
                    // Only store content if it's a custom license (license content is pretty big generally)
                    StringWriter content = new StringWriter();
                    try {
                        IOUtils.writeLines(license.getContent(), IOUtils.LINE_SEPARATOR_UNIX, content);
                    } catch (IOException e) {
                        // That should never happen
                    }
                    addElement(document, licenseElement, ELEMENT_LLCONTENT, content.toString());
                }
            }
        }
    }

    private void addFeatures(Document document, Element parentElement, Extension extension)
    {
        Collection<String> features = extension.getFeatures();
        if (!features.isEmpty()) {
            Element featuresElement = document.createElement(ELEMENT_FEATURES);
            parentElement.appendChild(featuresElement);

            for (String feature : features) {
                addElement(document, featuresElement, ELEMENT_FFEATURE, feature);
            }
        }
    }

    private void addAuthors(Document document, Element parentElement, Extension extension)
    {
        Collection<ExtensionAuthor> authors = extension.getAuthors();
        if (!authors.isEmpty()) {
            Element authorsElement = document.createElement(ELEMENT_AUTHORS);
            parentElement.appendChild(authorsElement);

            for (ExtensionAuthor author : authors) {
                Element authorElement = document.createElement(ELEMENT_AAUTHOR);
                authorsElement.appendChild(authorElement);

                addElement(document, authorElement, ELEMENT_AANAME, author.getName());

                URL authorURL = author.getURL();
                if (authorURL != null) {
                    addElement(document, authorElement, ELEMENT_AAURL, authorURL.toString());
                }
            }
        }
    }

    private void addDependencies(Document document, Element parentElement, Extension extension)
    {
        if (extension.getDependencies() != null && !extension.getDependencies().isEmpty()) {
            Element dependenciesElement = document.createElement(ELEMENT_DEPENDENCIES);
            parentElement.appendChild(dependenciesElement);

            for (ExtensionDependency dependency : extension.getDependencies()) {
                Element dependencyElement = document.createElement(ELEMENT_DDEPENDENCY);
                dependenciesElement.appendChild(dependencyElement);

                addElement(document, dependencyElement, ELEMENT_ID, dependency.getId());
                addElement(document, dependencyElement, ELEMENT_VERSION, dependency.getVersionConstraint().getValue());
                addProperties(document, dependencyElement, dependency.getProperties());
            }
        }
    }

    private void addProperties(Document document, Element parentElement, Map<String, Object> properties)
    {
        if (!properties.isEmpty()) {
            Element propertiesElement = document.createElement(ELEMENT_PROPERTIES);
            parentElement.appendChild(propertiesElement);

            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                addElement(document, propertiesElement, entry.getKey(), entry.getValue());
            }
        }
    }

    // Tools

    private void addElement(Document document, Element parentElement, String elementName, Object elementValue)
    {
        Element element =
            CollectionExtensionPropertySerializer
                .toElement(elementValue, document, elementName, this.serializerByClass);

        if (element != null) {
            parentElement.appendChild(element);
        }
    }
}
