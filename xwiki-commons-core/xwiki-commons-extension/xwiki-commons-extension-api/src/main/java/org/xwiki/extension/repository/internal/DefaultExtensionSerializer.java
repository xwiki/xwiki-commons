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
package org.xwiki.extension.repository.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
import org.xwiki.extension.DefaultExtensionScm;
import org.xwiki.extension.DefaultExtensionScmConnection;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionAuthor;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionIssueManagement;
import org.xwiki.extension.ExtensionLicense;
import org.xwiki.extension.ExtensionLicenseManager;
import org.xwiki.extension.ExtensionPattern;
import org.xwiki.extension.ExtensionScm;
import org.xwiki.extension.ExtensionScmConnection;
import org.xwiki.extension.InstalledExtension;
import org.xwiki.extension.InvalidExtensionException;
import org.xwiki.extension.MutableExtension;
import org.xwiki.extension.internal.ExtensionFactory;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtension;
import org.xwiki.extension.repository.internal.core.DefaultCoreExtensionRepository;
import org.xwiki.extension.repository.internal.local.DefaultLocalExtension;
import org.xwiki.extension.repository.internal.local.DefaultLocalExtensionRepository;

/**
 * Local repository storage serialization tool.
 *
 * @version $Id$
 * @since 6.4M1
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

    private static final String ELEMENT_CATEGORY = "category";

    private static final String ELEMENT_DESCRIPTION = "description";

    private static final String ELEMENT_WEBSITE = "website";

    private static final String ELEMENT_AUTHORS = "authors";

    private static final String ELEMENT_AAUTHOR = "author";

    private static final String ELEMENT_AANAME = "name";

    private static final String ELEMENT_AAURL = "url";

    private static final String ELEMENT_DEPENDENCIES = "dependencies";

    private static final String ELEMENT_MANAGEDDEPENDENCIES = "manageddependencies";

    private static final String ELEMENT_REPOSITORIES = "repositories";

    private static final String ELEMENT_REPOSITORY = "repository";

    private static final String ELEMENT_URI = "uri";

    private static final String ELEMENT_RTYPE = "type";

    private static final String ELEMENT_DDEPENDENCY = "dependency";

    private static final String ELEMENT_DEXCLUSIONS = "exclusions";

    private static final String ELEMENT_EXTENIONPATTERN = "extensionpattern";

    private static final String ELEMENT_DDOPTIONAL = "optional";

    private static final String ELEMENT_EXTENSIONFEATURES = "extensionfeatures";

    private static final String ELEMENT_EFFEATURE = "feature";

    private static final String ELEMENT_EFFID = "feature";

    private static final String ELEMENT_EFFVERSION = "version";

    private static final String ELEMENT_ALLOWEDNAMESPACES = "allowednamespaces";

    private static final String ELEMENT_ANNAMESPACE = "namespace";

    private static final String ELEMENT_SCM = "scm";

    private static final String ELEMENT_SCONNECTION = "connection";

    private static final String ELEMENT_SDEVELOPERCONNECTION = "developerconnection";

    private static final String ELEMENT_SCSYSTEM = "system";

    private static final String ELEMENT_SCPATH = "path";

    private static final String ELEMENT_SURL = "url";

    private static final String ELEMENT_ISSUEMANAGEMENT = "issuemanagement";

    private static final String ELEMENT_ISYSTEM = "system";

    private static final String ELEMENT_IURL = "url";

    private static final String ELEMENT_PROPERTIES = "properties";

    @Deprecated
    private static final String ELEMENT_FEATURES = "features";

    @Deprecated
    private static final String ELEMENT_FFEATURE = "feature";

    @Deprecated
    private static final String ELEMENT_INSTALLED = "installed";

    @Deprecated
    private static final String ELEMENT_NAMESPACES = "namespaces";

    @Deprecated
    private static final String ELEMENT_NNAMESPACE = "namespace";

    @Inject
    private ExtensionLicenseManager licenseManager;

    @Inject
    private ExtensionFactory factory;

    /**
     * Used to parse XML descriptor file.
     */
    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    protected Map<String, ExtensionPropertySerializer> serializerById;

    protected Map<Class<?>, ExtensionPropertySerializer> serializerByClass;

    {
        {
            this.serializerById = new HashMap<>();
            this.serializerByClass = new LinkedHashMap<>();

            StringExtensionPropertySerializer stringSerializer = new StringExtensionPropertySerializer();
            IntegerExtensionPropertySerializer integerSerializer = new IntegerExtensionPropertySerializer();
            BooleanExtensionPropertySerializer booleanSerializer = new BooleanExtensionPropertySerializer();
            DateExtensionPropertySerializer dateSerializer = new DateExtensionPropertySerializer();
            URLExtensionPropertySerializer urlSerializer = new URLExtensionPropertySerializer();
            CollectionExtensionPropertySerializer collectionSerializer =
                new CollectionExtensionPropertySerializer(this.serializerById, this.serializerByClass);
            SetExtensionPropertySerializer setSerializer =
                new SetExtensionPropertySerializer(this.serializerById, this.serializerByClass);
            StringKeyMapExtensionPropertySerializer mapSerializer =
                new StringKeyMapExtensionPropertySerializer(this.serializerById, this.serializerByClass);

            this.serializerById.put(null, stringSerializer);
            this.serializerById.put("", stringSerializer);
            this.serializerById.put(integerSerializer.getType(), integerSerializer);
            this.serializerById.put(booleanSerializer.getType(), booleanSerializer);
            this.serializerById.put(dateSerializer.getType(), dateSerializer);
            this.serializerById.put(urlSerializer.getType(), urlSerializer);
            this.serializerById.put(collectionSerializer.getType(), collectionSerializer);
            this.serializerById.put(setSerializer.getType(), setSerializer);
            this.serializerById.put(mapSerializer.getType(), mapSerializer);

            this.serializerByClass.put(String.class, stringSerializer);
            this.serializerByClass.put(Integer.class, integerSerializer);
            this.serializerByClass.put(Boolean.class, booleanSerializer);
            this.serializerByClass.put(Date.class, dateSerializer);
            this.serializerByClass.put(URL.class, urlSerializer);
            this.serializerByClass.put(Set.class, setSerializer);
            this.serializerByClass.put(Collection.class, collectionSerializer);
            this.serializerByClass.put(Map.class, mapSerializer);
        }
    }

    @Override
    public DefaultCoreExtension loadCoreExtensionDescriptor(DefaultCoreExtensionRepository repository, URL url,
        InputStream descriptor) throws InvalidExtensionException
    {
        Element extensionElement = getExtensionElement(descriptor);

        DefaultCoreExtension coreExtension = new DefaultCoreExtension(repository, url, getExtensionId(extensionElement),
            getExtensionType(extensionElement));

        loadExtensionDescriptor(coreExtension, extensionElement);

        coreExtension.setComplete(true);

        return coreExtension;
    }

    @Override
    public DefaultLocalExtension loadLocalExtensionDescriptor(DefaultLocalExtensionRepository repository,
        InputStream descriptor) throws InvalidExtensionException
    {
        Element extensionElement = getExtensionElement(descriptor);

        DefaultLocalExtension localExtension =
            new DefaultLocalExtension(repository, getExtensionId(extensionElement), getExtensionType(extensionElement));

        loadExtensionDescriptor(localExtension, extensionElement);

        return localExtension;
    }

    private Element getExtensionElement(InputStream descriptor) throws InvalidExtensionException
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

        return document.getDocumentElement();
    }

    private ExtensionId getExtensionId(Element extensionElement)
    {
        Node idNode = extensionElement.getElementsByTagName(ELEMENT_ID).item(0);
        Node versionNode = extensionElement.getElementsByTagName(ELEMENT_VERSION).item(0);

        return new ExtensionId(idNode.getTextContent(), this.factory.getVersion(versionNode.getTextContent()));
    }

    private String getExtensionType(Element extensionElement)
    {
        NodeList nodeList = extensionElement.getElementsByTagName(ELEMENT_TYPE);

        return nodeList.getLength() > 0 ? nodeList.item(0).getTextContent() : null;
    }

    private void loadExtensionDescriptor(MutableExtension extension, Element extensionElement)
        throws InvalidExtensionException
    {
        Node nameNode = getNode(extensionElement, ELEMENT_NAME);
        if (nameNode != null) {
            extension.setName(nameNode.getTextContent());
        }
        Node categoryNode = getNode(extensionElement, ELEMENT_CATEGORY);
        if (categoryNode != null) {
            extension.setCategory(categoryNode.getTextContent());
        }
        Node summaryNode = getNode(extensionElement, ELEMENT_SUMMARY);
        if (summaryNode != null) {
            extension.setSummary(summaryNode.getTextContent());
        }
        Node descriptionNode = getNode(extensionElement, ELEMENT_DESCRIPTION);
        if (descriptionNode != null) {
            extension.setDescription(descriptionNode.getTextContent());
        }
        Node websiteNode = getNode(extensionElement, ELEMENT_WEBSITE);
        if (websiteNode != null) {
            extension.setWebsite(websiteNode.getTextContent());
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
                            license = new ExtensionLicense(licenseName, licenceContentNode != null
                                ? IOUtils.readLines(new StringReader(licenceContentNode.getTextContent())) : null);
                        } catch (IOException e) {
                            // That should never happen
                            throw new InvalidExtensionException("Failed to write license content", e);
                        }
                    }

                    extension.addLicense(license);
                }
            }
        }

        // Authors
        Node authorsNode = getNode(extensionElement, ELEMENT_AUTHORS);
        if (authorsNode != null) {
            NodeList authors = authorsNode.getChildNodes();
            for (int i = 0; i < authors.getLength(); ++i) {
                Node authorNode = authors.item(i);

                if (authorNode.getNodeName().equals(ELEMENT_AAUTHOR)) {
                    Node authorNameNode = getNode(authorNode, ELEMENT_AANAME);
                    Node authorURLNode = getNode(authorNode, ELEMENT_AAURL);

                    String authorName = authorNameNode != null ? authorNameNode.getTextContent() : null;
                    String authorURL;
                    if (authorURLNode != null) {
                        authorURL = authorURLNode.getTextContent();
                    } else {
                        authorURL = null;
                    }

                    extension.addAuthor(this.factory.getExtensionAuthor(authorName, authorURL));
                }
            }
        }

        // Extension features
        Node featuresNode = getNode(extensionElement, ELEMENT_EXTENSIONFEATURES);
        if (featuresNode != null) {
            NodeList extensionFeatures = featuresNode.getChildNodes();
            for (int i = 0; i < extensionFeatures.getLength(); ++i) {
                Node featureNode = extensionFeatures.item(i);

                if (featureNode.getNodeName().equals(ELEMENT_EFFEATURE)) {
                    Node idNode = getNode(featureNode, ELEMENT_EFFID);
                    Node versionNode = getNode(featureNode, ELEMENT_EFFVERSION);

                    String id = idNode != null ? idNode.getTextContent() : null;
                    String version = versionNode != null ? versionNode.getTextContent() : null;

                    if (version != null) {
                        extension.addExtensionFeature(new ExtensionId(id, this.factory.getVersion(version)));
                    } else {
                        extension.addExtensionFeature(new ExtensionId(id, extension.getId().getVersion()));
                    }
                }
            }
        }
        if (featuresNode == null) {
            // @Deprecated Features
            List<String> legacyFeatures = parseList(extensionElement, ELEMENT_FEATURES, ELEMENT_FFEATURE);
            if (legacyFeatures != null) {
                extension.setFeatures(legacyFeatures);
            }
        }

        // Allowed namespaces
        List<String> allowedNamespaces = parseList(extensionElement, ELEMENT_ALLOWEDNAMESPACES, ELEMENT_ANNAMESPACE);
        if (allowedNamespaces != null) {
            extension.setAllowedNamespaces(allowedNamespaces);
        }

        // Scm
        extension.setScm(loadlScm(extensionElement));

        // Issue Management
        extension.setIssueManagement(loadIssueManagement(extensionElement));

        // Dependencies
        extension.setDependencies(loadDependencies(extensionElement, ELEMENT_DEPENDENCIES));

        // Managed dependencies
        extension.setManagedDependencies(loadDependencies(extensionElement, ELEMENT_MANAGEDDEPENDENCIES));

        // Properties
        Map<String, Object> properties = parseProperties(extensionElement);
        if (properties != null) {
            extension.setProperties(properties);
        }

        // @Deprecated Install fields

        Node enabledNode = getNode(extensionElement, ELEMENT_INSTALLED);
        if (enabledNode != null) {
            extension.putProperty(InstalledExtension.PKEY_INSTALLED, Boolean.valueOf(enabledNode.getTextContent()));
        }

        // @Deprecated Namespaces
        List<String> namespaces = parseList(extensionElement, ELEMENT_NAMESPACES, ELEMENT_NNAMESPACE);
        if (namespaces != null) {
            extension.putProperty(InstalledExtension.PKEY_NAMESPACES, namespaces);
        }
    }

    private Collection<ExtensionDependency> loadDependencies(Element extensionElement, String dependenciesFiel)
        throws InvalidExtensionException
    {
        Node dependenciesNode = getNode(extensionElement, dependenciesFiel);
        if (dependenciesNode != null) {
            NodeList dependenciesNodeList = dependenciesNode.getChildNodes();

            List<ExtensionDependency> dependencies = new ArrayList<>(dependenciesNodeList.getLength());

            for (int i = 0; i < dependenciesNodeList.getLength(); ++i) {
                Node dependencyNode = dependenciesNodeList.item(i);

                if (dependencyNode.getNodeName().equals(ELEMENT_DDEPENDENCY)) {
                    ExtensionDependency dependency = loadDependency(dependencyNode);

                    if (dependency != null) {
                        dependencies.add(dependency);
                    }
                }
            }

            return dependencies;
        }

        return Collections.emptyList();
    }

    private ExtensionDependency loadDependency(Node dependencyNode) throws InvalidExtensionException
    {
        Node dependencyIdNode = getNode(dependencyNode, ELEMENT_ID);

        if (dependencyIdNode != null) {
            Node dependencyVersionNode = getNode(dependencyNode, ELEMENT_VERSION);
            Node dependencyOptionalNode = getNode(dependencyNode, ELEMENT_DDOPTIONAL);
            Map<String, Object> properties = parseProperties((Element) dependencyNode);

            Collection<ExtensionRepositoryDescriptor> repositories = loadRepositories(dependencyNode);
            Collection<ExtensionPattern> exclusions = loadExclusions(dependencyNode);

            return this.factory.getExtensionDependency(dependencyIdNode.getTextContent(),
                dependencyVersionNode != null
                    ? this.factory.getVersionConstraint(dependencyVersionNode.getTextContent()) : null,
                dependencyOptionalNode != null && Boolean.parseBoolean(dependencyOptionalNode.getTextContent()),
                exclusions, repositories, properties);
        }

        return null;
    }

    private Collection<ExtensionRepositoryDescriptor> loadRepositories(Node parentElement)
        throws InvalidExtensionException
    {
        Node repositoriesNode = getNode(parentElement, ELEMENT_REPOSITORIES);
        if (repositoriesNode != null) {
            NodeList repositoriesNodeList = repositoriesNode.getChildNodes();

            List<ExtensionRepositoryDescriptor> repositories = new ArrayList<>(repositoriesNodeList.getLength());

            for (int i = 0; i < repositoriesNodeList.getLength(); ++i) {
                Node repository = repositoriesNodeList.item(i);

                if (repository.getNodeName().equals(ELEMENT_REPOSITORY)) {
                    Node repositoryIdNode = getNode(repository, ELEMENT_ID);
                    Node repositoryTypeNode = getNode(repository, ELEMENT_RTYPE);
                    Node repositoryURINode = getNode(repository, ELEMENT_URI);
                    Map<String, String> properties = parseProperties((Element) repository);

                    try {
                        repositories.add(this.factory.getExtensionRepositoryDescriptor(
                            repositoryIdNode.getTextContent(), repositoryTypeNode.getTextContent(),
                            new URI(repositoryURINode.getTextContent()), properties));
                    } catch (Exception e) {
                        throw new InvalidExtensionException("Failed to read repository descriptor", e);
                    }
                }
            }

            return repositories;
        }

        return Collections.emptyList();
    }

    private Collection<ExtensionPattern> loadExclusions(Node dependencyNode)
    {
        Node exclusionsNode = getNode(dependencyNode, ELEMENT_DEXCLUSIONS);
        if (exclusionsNode != null) {
            NodeList exclusionsNodeList = exclusionsNode.getChildNodes();

            List<ExtensionPattern> exclusions = new ArrayList<>(exclusionsNodeList.getLength());

            for (int i = 0; i < exclusionsNodeList.getLength(); ++i) {
                Node exclusion = exclusionsNodeList.item(i);

                if (exclusion.getNodeName().equals(ELEMENT_EXTENIONPATTERN)) {
                    Node dependencyIdNode = getNode(exclusion, ELEMENT_ID);

                    Pattern patternString =
                        dependencyIdNode != null ? Pattern.compile(dependencyIdNode.getTextContent()) : null;

                    exclusions.add(this.factory.getExtensionPattern(patternString));
                }
            }

            return exclusions;
        }

        return Collections.emptyList();
    }

    private ExtensionScm loadlScm(Element extensionElement)
    {
        Node node = getNode(extensionElement, ELEMENT_SCM);

        if (node != null) {
            Node connectionNode = getNode(node, ELEMENT_SCONNECTION);
            Node developerConnectionNode = getNode(node, ELEMENT_SDEVELOPERCONNECTION);
            Node urlNode = getNode(node, ELEMENT_SURL);

            return new DefaultExtensionScm(urlNode != null ? urlNode.getTextContent() : null,
                loadlScmConnection(connectionNode), loadlScmConnection(developerConnectionNode));
        }

        return null;
    }

    private ExtensionScmConnection loadlScmConnection(Node scmConnectionElement)
    {
        if (scmConnectionElement != null) {
            Node system = getNode(scmConnectionElement, ELEMENT_SCSYSTEM);
            Node path = getNode(scmConnectionElement, ELEMENT_SCPATH);

            if (system != null) {
                return new DefaultExtensionScmConnection(system.getTextContent(),
                    path != null ? path.getTextContent() : null);
            }
        }

        return null;
    }

    private ExtensionIssueManagement loadIssueManagement(Element extensionElement)
    {
        Node node = getNode(extensionElement, ELEMENT_ISSUEMANAGEMENT);

        if (node != null) {
            Node systemNode = getNode(node, ELEMENT_ISYSTEM);
            Node urlNode = getNode(node, ELEMENT_IURL);

            if (systemNode != null) {
                return this.factory.getExtensionIssueManagement(systemNode.getTextContent(),
                    urlNode != null ? urlNode.getTextContent() : null);
            }
        }

        return null;
    }

    private List<String> parseList(Element extensionElement, String rootElement, String childElement)
    {
        List<String> list;

        Node featuresNode = getNode(extensionElement, rootElement);
        if (featuresNode != null) {
            list = new LinkedList<>();

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

    private <T> Map<String, T> parseProperties(Node parentElement)
    {
        Map<String, T> properties = null;

        Node propertiesNode = getNode(parentElement, ELEMENT_PROPERTIES);
        if (propertiesNode != null) {
            properties = new HashMap<>();
            NodeList propertyNodeList = propertiesNode.getChildNodes();
            for (int i = 0; i < propertyNodeList.getLength(); ++i) {
                Node propertyNode = propertyNodeList.item(i);

                if (propertyNode.getNodeType() == Node.ELEMENT_NODE) {
                    T value =
                        CollectionExtensionPropertySerializer.toValue((Element) propertyNode, this.serializerById);

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
    public void saveExtensionDescriptor(Extension extension, OutputStream fos)
        throws ParserConfigurationException, TransformerException
    {
        DocumentBuilder documentBuilder = this.documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        Element extensionElement = document.createElement("extension");
        document.appendChild(extensionElement);

        addElement(document, extensionElement, ELEMENT_ID, extension.getId().getId());
        addElement(document, extensionElement, ELEMENT_VERSION, extension.getId().getVersion().getValue());
        addElement(document, extensionElement, ELEMENT_TYPE, extension.getType());
        addElement(document, extensionElement, ELEMENT_NAME, extension.getName());
        addElement(document, extensionElement, ELEMENT_CATEGORY, extension.getCategory());
        addElement(document, extensionElement, ELEMENT_SUMMARY, extension.getSummary());
        addElement(document, extensionElement, ELEMENT_DESCRIPTION, extension.getDescription());
        addElement(document, extensionElement, ELEMENT_WEBSITE, extension.getWebSite());

        addExtensionFeatures(document, extensionElement, extension);
        addLegacyFeatures(document, extensionElement, extension);

        addAuthors(document, extensionElement, extension);

        addAllowedNamespaces(document, extensionElement, extension);

        addLicenses(document, extensionElement, extension);

        addScm(document, extensionElement, extension);

        addIssueManagement(document, extensionElement, extension);

        addDependencies(document, extensionElement, extension);
        addManagedDependencies(document, extensionElement, extension);

        addRepositories(document, extensionElement, extension.getRepositories());

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

    @Deprecated
    private void addLegacyFeatures(Document document, Element parentElement, Extension extension)
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

    private void addAllowedNamespaces(Document document, Element parentElement, Extension extension)
    {
        Collection<String> namespaces = extension.getAllowedNamespaces();
        if (namespaces != null) {
            Element namespacesElement = document.createElement(ELEMENT_ALLOWEDNAMESPACES);
            parentElement.appendChild(namespacesElement);

            for (String namespace : namespaces) {
                addElement(document, namespacesElement, ELEMENT_ANNAMESPACE, namespace);
            }
        }
    }

    private void addExtensionFeatures(Document document, Element parentElement, Extension extension)
    {
        Collection<ExtensionId> features = extension.getExtensionFeatures();
        if (!features.isEmpty()) {
            Element featuresElement = document.createElement(ELEMENT_EXTENSIONFEATURES);
            parentElement.appendChild(featuresElement);

            for (ExtensionId feature : features) {
                Element authorElement = document.createElement(ELEMENT_EFFEATURE);
                featuresElement.appendChild(authorElement);

                addElement(document, authorElement, ELEMENT_EFFID, feature.getId());
                addElement(document, authorElement, ELEMENT_EFFVERSION, feature.getVersion().getValue());
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

                String authorURL = author.getURLString();
                if (authorURL != null) {
                    addElement(document, authorElement, ELEMENT_AAURL, authorURL);
                }
            }
        }
    }

    private void addScm(Document document, Element extensionElement, Extension extension)
    {
        ExtensionScm scm = extension.getScm();

        if (scm != null) {
            Element scmElement = document.createElement(ELEMENT_SCM);
            extensionElement.appendChild(scmElement);

            addElement(document, scmElement, ELEMENT_SURL, scm.getUrl());
            addScmConnection(document, scmElement, scm.getConnection(), ELEMENT_SCONNECTION);
            addScmConnection(document, scmElement, scm.getDeveloperConnection(), ELEMENT_SDEVELOPERCONNECTION);
        }
    }

    private void addScmConnection(Document document, Element scmElement, ExtensionScmConnection connection,
        String elementName)
    {
        if (connection != null) {
            Element connectionElement = document.createElement(elementName);
            scmElement.appendChild(connectionElement);

            addElement(document, connectionElement, ELEMENT_SCSYSTEM, connection.getSystem());
            addElement(document, connectionElement, ELEMENT_SCPATH, connection.getPath());
        }
    }

    private void addIssueManagement(Document document, Element extensionElement, Extension extension)
    {
        ExtensionIssueManagement issueManagement = extension.getIssueManagement();

        if (issueManagement != null) {
            Element issuemanagementElement = document.createElement(ELEMENT_ISSUEMANAGEMENT);
            extensionElement.appendChild(issuemanagementElement);

            addElement(document, issuemanagementElement, ELEMENT_ISYSTEM, issueManagement.getSystem());
            addElement(document, issuemanagementElement, ELEMENT_IURL, issueManagement.getURL());
        }
    }

    private void addDependencies(Document document, Element parentElement, Extension extension)
    {
        addDependencies(document, parentElement, ELEMENT_DEPENDENCIES, extension.getDependencies());
    }

    private void addManagedDependencies(Document document, Element parentElement, Extension extension)
    {
        addDependencies(document, parentElement, ELEMENT_MANAGEDDEPENDENCIES, extension.getManagedDependencies());
    }

    private void addDependencies(Document document, Element parentElement, String fieldName,
        Collection<ExtensionDependency> exclusions)
    {
        if (exclusions != null && !exclusions.isEmpty()) {
            Element dependenciesElement = document.createElement(fieldName);
            parentElement.appendChild(dependenciesElement);

            for (ExtensionDependency dependency : exclusions) {
                Element dependencyElement = document.createElement(ELEMENT_DDEPENDENCY);
                dependenciesElement.appendChild(dependencyElement);

                addElement(document, dependencyElement, ELEMENT_ID, dependency.getId());
                addElement(document, dependencyElement, ELEMENT_VERSION, dependency.getVersionConstraint().getValue());
                addElement(document, dependencyElement, ELEMENT_DDOPTIONAL, dependency.isOptional());
                addProperties(document, dependencyElement, dependency.getProperties());

                addDependencyExclusions(document, dependencyElement, dependency.getExclusions());
            }
        }
    }

    private void addRepositories(Document document, Element parentElement,
        Collection<ExtensionRepositoryDescriptor> repositories)
    {
        if (repositories != null && !repositories.isEmpty()) {
            Element dependenciesElement = document.createElement(ELEMENT_REPOSITORIES);
            parentElement.appendChild(dependenciesElement);

            for (ExtensionRepositoryDescriptor repository : repositories) {
                Element dependencyElement = document.createElement(ELEMENT_REPOSITORY);
                dependenciesElement.appendChild(dependencyElement);

                addElement(document, dependencyElement, ELEMENT_ID, repository.getId());
                addElement(document, dependencyElement, ELEMENT_RTYPE, repository.getType());
                addElement(document, dependencyElement, ELEMENT_URI, repository.getURI());
                addProperties(document, dependencyElement, repository.getProperties());
            }
        }
    }

    private void addDependencyExclusions(Document document, Element parentElement,
        Collection<ExtensionPattern> exclusions)
    {
        if (exclusions != null && !exclusions.isEmpty()) {
            Element dependenciesElement = document.createElement(ELEMENT_DEXCLUSIONS);
            parentElement.appendChild(dependenciesElement);

            for (ExtensionPattern exclusion : exclusions) {
                Element dependencyElement = document.createElement(ELEMENT_EXTENIONPATTERN);
                dependenciesElement.appendChild(dependencyElement);

                if (exclusion.getIdPattern() != null) {
                    addElement(document, dependencyElement, ELEMENT_ID, exclusion.getIdPattern().pattern());
                }
            }
        }
    }

    private void addProperties(Document document, Element parentElement, Map<String, ?> properties)
    {
        if (!properties.isEmpty()) {
            Element propertiesElement = document.createElement(ELEMENT_PROPERTIES);
            parentElement.appendChild(propertiesElement);

            for (Map.Entry<String, ?> entry : properties.entrySet()) {
                addElement(document, propertiesElement, entry.getKey(), entry.getValue());
            }
        }
    }

    // Tools

    private void addElement(Document document, Element parentElement, String elementName, Object elementValue)
    {
        Element element = CollectionExtensionPropertySerializer.toElement(elementValue, document, elementName,
            this.serializerByClass);

        if (element != null) {
            parentElement.appendChild(element);
        }
    }
}
