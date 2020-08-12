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
package org.xwiki.extension.repository.xwiki.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Consts;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.AuthCache;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionDependency;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.ExtensionLicenseManager;
import org.xwiki.extension.ExtensionNotFoundException;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.index.Indexable;
import org.xwiki.extension.internal.ExtensionFactory;
import org.xwiki.extension.rating.ExtensionRating;
import org.xwiki.extension.repository.AbstractExtensionRepository;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.http.internal.HttpClientFactory;
import org.xwiki.extension.repository.rating.RatableExtensionRepository;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.repository.search.AdvancedSearchable;
import org.xwiki.extension.repository.search.SearchException;
import org.xwiki.extension.repository.xwiki.model.jaxb.COMPARISON;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionQuery;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionVersion;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionVersionSummary;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionVersions;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionsSearchResult;
import org.xwiki.extension.repository.xwiki.model.jaxb.Filter;
import org.xwiki.extension.repository.xwiki.model.jaxb.ORDER;
import org.xwiki.extension.repository.xwiki.model.jaxb.ObjectFactory;
import org.xwiki.extension.repository.xwiki.model.jaxb.Repository;
import org.xwiki.extension.repository.xwiki.model.jaxb.SortClause;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.VersionConstraint;
import org.xwiki.extension.version.internal.DefaultVersion;
import org.xwiki.extension.version.internal.VersionUtils;
import org.xwiki.repository.Resources;
import org.xwiki.repository.UriBuilder;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class XWikiExtensionRepository extends AbstractExtensionRepository
    implements AdvancedSearchable, RatableExtensionRepository, Indexable
{
    public static final Version VERSION10 = new DefaultVersion(Resources.VERSION10);

    public static final Version VERSION11 = new DefaultVersion(Resources.VERSION11);

    private static final Logger LOGGER = LoggerFactory.getLogger(XWikiExtensionRepository.class);

    private static final ObjectFactory EXTENSION_OBJECT_FACTORY = new ObjectFactory();

    private final transient XWikiExtensionRepositoryFactory repositoryFactory;

    private final transient ExtensionLicenseManager licenseManager;

    private final transient HttpClientFactory httpClientFactory;

    private final transient ExtensionFactory factory;

    private final transient UriBuilder rootUriBuider;

    private final transient UriBuilder extensionVersionUriBuider;

    private final transient UriBuilder extensionVersionFileUriBuider;

    private final transient UriBuilder extensionVersionsUriBuider;

    private final transient UriBuilder searchUriBuider;

    private HttpClientContext localContext;

    private Version repositoryVersion;

    private boolean filterable;

    private boolean sortable;

    public XWikiExtensionRepository(ExtensionRepositoryDescriptor repositoryDescriptor,
        XWikiExtensionRepositoryFactory repositoryFactory, ExtensionLicenseManager licenseManager,
        HttpClientFactory httpClientFactory, ExtensionFactory factory) throws Exception
    {
        super(repositoryDescriptor.getURI().getPath().endsWith("/")
            ? new DefaultExtensionRepositoryDescriptor(repositoryDescriptor.getId(), repositoryDescriptor.getType(),
                new URI(StringUtils.chop(repositoryDescriptor.getURI().toString())))
            : repositoryDescriptor);

        this.repositoryFactory = repositoryFactory;
        this.licenseManager = licenseManager;
        this.httpClientFactory = httpClientFactory;
        this.factory = factory;

        // Uri builders
        this.rootUriBuider = createUriBuilder(Resources.ENTRYPOINT);
        this.extensionVersionUriBuider = createUriBuilder(Resources.EXTENSION_VERSION);
        this.extensionVersionFileUriBuider = createUriBuilder(Resources.EXTENSION_VERSION_FILE);
        this.extensionVersionsUriBuider = createUriBuilder(Resources.EXTENSION_VERSIONS);
        this.searchUriBuider = createUriBuilder(Resources.SEARCH);

        // Setup preemptive authentication
        if (getDescriptor().getProperty("auth.user") != null) {
            // Create AuthCache instance
            AuthCache authCache = new BasicAuthCache();
            // Generate BASIC scheme object and add it to the local
            // auth cache
            BasicScheme basicAuth = new BasicScheme();
            authCache.put(new HttpHost(getDescriptor().getURI().getHost(), getDescriptor().getURI().getPort(),
                getDescriptor().getURI().getScheme()), basicAuth);

            // Add AuthCache to the execution context
            this.localContext = HttpClientContext.create();
            this.localContext.setAuthCache(authCache);
        }
    }

    /**
     * Check what is supported by the remote repository.
     */
    private void initRepositoryFeatures()
    {
        if (this.repositoryVersion == null) {
            // Default features

            this.repositoryVersion = new DefaultVersion(Resources.VERSION10);
            this.filterable = false;
            this.sortable = false;

            // Get remote features

            CloseableHttpResponse response;
            try {
                response = getRESTResource(this.rootUriBuider);
            } catch (IOException e) {
                // Assume it's a 1.0 repository
                return;
            }

            try {
                Repository repository = getRESTObject(response);

                this.repositoryVersion = new DefaultVersion(repository.getVersion());
                this.filterable = repository.isFilterable() == Boolean.TRUE;
                this.sortable = repository.isSortable() == Boolean.TRUE;
            } catch (Exception e) {
                LOGGER.error("Failed to get repository features", e);
            }
        }
    }

    /**
     * @return the version of the protocol supported by the repository
     */
    public Version getRepositoryVersion()
    {
        initRepositoryFeatures();

        return this.repositoryVersion;
    }

    @Override
    public boolean isFilterable()
    {
        initRepositoryFeatures();

        return this.filterable;
    }

    @Override
    public boolean isSortable()
    {
        initRepositoryFeatures();

        return this.sortable;
    }

    protected UriBuilder getExtensionFileUriBuider()
    {
        return this.extensionVersionFileUriBuider;
    }

    protected CloseableHttpResponse getRESTResource(UriBuilder builder, Object... values) throws IOException
    {
        String url;
        try {
            url = builder.build(values).toString();
        } catch (Exception e) {
            throw new IOException("Failed to build REST URL", e);
        }

        CloseableHttpClient httpClient = this.httpClientFactory.createClient(getDescriptor().getProperty("auth.user"),
            getDescriptor().getProperty("auth.password"));

        HttpGet getMethod = new HttpGet(url);
        getMethod.addHeader("Accept", "application/xml");
        CloseableHttpResponse response;
        try {
            if (this.localContext != null) {
                response = httpClient.execute(getMethod, this.localContext);
            } else {
                response = httpClient.execute(getMethod);
            }
        } catch (Exception e) {
            throw new IOException(String.format("Failed to request [%s]", getMethod.getURI()), e);
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND) {
                throw new ResourceNotFoundException(
                    String.format("Resource with URI [%s] does not exist", getMethod.getURI()));
            } else {
                throw new IOException(String.format("Invalid answer [%s] from the server when requesting [%s]",
                    response.getStatusLine().getStatusCode(), getMethod.getURI()));
            }
        }

        return response;
    }

    protected CloseableHttpResponse postRESTResource(UriBuilder builder, String content, Object... values)
        throws IOException
    {
        String url;
        try {
            url = builder.build(values).toString();
        } catch (Exception e) {
            throw new IOException("Failed to build REST URL", e);
        }

        CloseableHttpClient httpClient = this.httpClientFactory.createClient(getDescriptor().getProperty("auth.user"),
            getDescriptor().getProperty("auth.password"));

        HttpPost postMethod = new HttpPost(url);
        postMethod.addHeader("Accept", "application/xml");

        StringEntity entity =
            new StringEntity(content, ContentType.create(ContentType.APPLICATION_XML.getMimeType(), Consts.UTF_8));
        postMethod.setEntity(entity);

        CloseableHttpResponse response;
        try {
            if (this.localContext != null) {
                response = httpClient.execute(postMethod, this.localContext);
            } else {
                response = httpClient.execute(postMethod);
            }
        } catch (Exception e) {
            throw new IOException(String.format("Failed to request [%s]", postMethod.getURI()), e);
        }

        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new IOException(String.format("Invalid answer [%s] from the server when requesting [%s]",
                response.getStatusLine().getStatusCode(), postMethod.getURI()));
        }

        return response;
    }

    protected Object getRESTObject(UriBuilder builder, Object... values)
        throws IllegalStateException, IOException, JAXBException
    {
        return getRESTObject(getRESTResource(builder, values));
    }

    protected Object postRESTObject(UriBuilder builder, Object restObject, Object... values)
        throws IllegalStateException, IOException, JAXBException
    {
        StringWriter writer = new StringWriter();
        this.repositoryFactory.createMarshaller().marshal(restObject, writer);

        return getRESTObject(postRESTResource(builder, writer.toString(), values));
    }

    protected <T> T getRESTObject(CloseableHttpResponse response)
        throws IllegalStateException, IOException, JAXBException
    {
        try {
            try (InputStream inputStream = response.getEntity().getContent()) {
                return (T) this.repositoryFactory.createUnmarshaller().unmarshal(inputStream);
            }
        } finally {
            response.close();
        }
    }

    private UriBuilder createUriBuilder(String path)
    {
        return new UriBuilder(getDescriptor().getURI(), path);
    }

    // ExtensionRepository

    @Override
    public Extension resolve(ExtensionId extensionId) throws ResolveException
    {
        try {
            return resolve(extensionId.getId(), extensionId.getVersion());
        } catch (ResourceNotFoundException e) {
            throw new ExtensionNotFoundException("Could not find extension [" + extensionId + "]", e);
        } catch (Exception e) {
            throw new ResolveException("Failed to create extension object for extension [" + extensionId + "]", e);
        }
    }

    private Extension resolve(String id, Version version) throws IllegalStateException, IOException, JAXBException
    {
        return new XWikiExtension(this, (ExtensionVersion) getRESTObject(this.extensionVersionUriBuider, id, version),
            this.licenseManager, this.factory);
    }

    @Override
    public Extension resolve(ExtensionDependency extensionDependency) throws ResolveException
    {
        try {
            Version version =
                resolveVersionConstraint(extensionDependency.getId(), extensionDependency.getVersionConstraint());

            return resolve(extensionDependency.getId(), version);
        } catch (ResourceNotFoundException e) {
            throw new ExtensionNotFoundException(
                "Could not find any extension to match dependency [" + extensionDependency + "]", e);
        } catch (Exception e) {
            throw new ResolveException(
                "Failed to create extension object for extension dependency [" + extensionDependency + "]", e);
        }
    }

    private Version resolveVersionConstraint(String id, VersionConstraint versionConstraint) throws ResolveException
    {
        // Single version
        if (versionConstraint.getVersion() != null) {
            return versionConstraint.getVersion();
        }

        // Strict version
        Version strictVersion = VersionUtils.getStrictVersion(versionConstraint.getRanges());
        if (strictVersion != null) {
            return strictVersion;
        }

        // Ranges
        ExtensionVersions versions = resolveExtensionVersions(id, versionConstraint, 0, -1, false);
        if (versions.getExtensionVersionSummaries().isEmpty()) {
            throw new ExtensionNotFoundException(
                "Can't find any version with id [" + id + "] matching version constraint [" + versionConstraint + "]");
        }

        return new DefaultVersion(versions.getExtensionVersionSummaries()
            .get(versions.getExtensionVersionSummaries().size() - 1).getVersion());
    }

    private ExtensionVersions resolveExtensionVersions(String id, VersionConstraint constraint, int offset, int nb,
        boolean requireTotalHits) throws ResolveException
    {
        UriBuilder builder = this.extensionVersionsUriBuider.clone();

        builder.queryParam(Resources.QPARAM_LIST_REQUIRETOTALHITS, requireTotalHits);
        builder.queryParam(Resources.QPARAM_LIST_START, offset);
        builder.queryParam(Resources.QPARAM_LIST_NUMBER, nb);
        if (constraint != null) {
            builder.queryParam(Resources.QPARAM_VERSIONS_RANGES, constraint.getValue());
        }

        try {
            return (ExtensionVersions) getRESTObject(builder, id);
        } catch (ResourceNotFoundException e) {
            throw new ExtensionNotFoundException("Could not find extension with id [" + id + "]", e);
        } catch (Exception e) {
            throw new ResolveException("Failed to find version for extension id [" + id + "]", e);
        }
    }

    @Override
    public IterableResult<Version> resolveVersions(String id, int offset, int nb) throws ResolveException
    {
        ExtensionVersions restExtensions = resolveExtensionVersions(id, null, offset, nb, true);

        List<Version> versions = new ArrayList<>(restExtensions.getExtensionVersionSummaries().size());
        for (ExtensionVersionSummary restExtension : restExtensions.getExtensionVersionSummaries()) {
            versions.add(new DefaultVersion(restExtension.getVersion()));
        }

        return new CollectionIterableResult<>(restExtensions.getTotalHits(), restExtensions.getOffset(),
            versions);
    }

    // Searchable

    @Override
    public IterableResult<Extension> search(String pattern, int offset, int nb) throws SearchException
    {
        UriBuilder builder = this.searchUriBuider.clone();

        builder.queryParam(Resources.QPARAM_LIST_START, offset);
        builder.queryParam(Resources.QPARAM_LIST_NUMBER, nb);
        if (pattern != null) {
            builder.queryParam(Resources.QPARAM_SEARCH_QUERY, pattern);
        }

        ExtensionsSearchResult restExtensions;
        try {
            restExtensions = (ExtensionsSearchResult) getRESTObject(builder);
        } catch (Exception e) {
            throw new SearchException("Failed to search extensions based on pattern [" + pattern + "]", e);
        }

        List<Extension> extensions = new ArrayList<>(restExtensions.getExtensions().size());
        for (ExtensionVersion restExtension : restExtensions.getExtensions()) {
            extensions.add(new XWikiExtension(this, restExtension, this.licenseManager, this.factory));
        }

        return new CollectionIterableResult<>(restExtensions.getTotalHits(), restExtensions.getOffset(),
            extensions);
    }

    @Override
    public IterableResult<Extension> search(org.xwiki.extension.repository.search.ExtensionQuery query)
        throws SearchException
    {
        if (getRepositoryVersion().equals(VERSION10)) {
            return search(query.getQuery(), query.getOffset(), query.getLimit());
        }

        UriBuilder builder = this.searchUriBuider.clone();

        ExtensionQuery restQuery = EXTENSION_OBJECT_FACTORY.createExtensionQuery();

        restQuery.setQuery(query.getQuery());
        restQuery.setOffset(query.getOffset());
        restQuery.setLimit(query.getLimit());
        for (org.xwiki.extension.repository.search.ExtensionQuery.Filter filter : query.getFilters()) {
            Filter restFilter = EXTENSION_OBJECT_FACTORY.createFilter();
            restFilter.setField(filter.getField());
            restFilter.setValueString(filter.getValue().toString());
            restFilter.setComparison(COMPARISON.fromValue(filter.getComparison().name()));
            restQuery.getFilters().add(restFilter);
        }
        for (org.xwiki.extension.repository.search.ExtensionQuery.SortClause sortClause : query.getSortClauses()) {
            SortClause restSortClause = EXTENSION_OBJECT_FACTORY.createSortClause();
            restSortClause.setField(sortClause.getField());
            restSortClause.setOrder(ORDER.fromValue(sortClause.getOrder().name()));
            restQuery.getSortClauses().add(restSortClause);
        }

        ExtensionsSearchResult restExtensions;
        try {
            restExtensions = (ExtensionsSearchResult) postRESTObject(builder, restQuery);
        } catch (Exception e) {
            throw new SearchException("Failed to search extensions based on pattern [" + query.getQuery() + "]", e);
        }

        List<Extension> extensions = new ArrayList<>(restExtensions.getExtensions().size());
        for (ExtensionVersion restExtension : restExtensions.getExtensions()) {
            extensions.add(new XWikiExtension(this, restExtension, this.licenseManager, this.factory));
        }

        return new CollectionIterableResult<>(restExtensions.getTotalHits(), restExtensions.getOffset(),
            extensions);
    }

    // Ratable

    @Override
    public ExtensionRating getRating(ExtensionId extensionId) throws ResolveException
    {
        return getRating(extensionId.getId(), extensionId.getVersion());
    }

    @Override
    public ExtensionRating getRating(String extensionId, Version extensionVersion) throws ResolveException
    {
        return getRating(extensionId, extensionVersion.getValue());
    }

    @Override
    public ExtensionRating getRating(String extensionId, String extensionVersion) throws ResolveException
    {
        try {
            return new XWikiExtension(this,
                (ExtensionVersion) getRESTObject(this.extensionVersionUriBuider, extensionId, extensionVersion),
                this.licenseManager, this.factory).getRating();
        } catch (ResourceNotFoundException e) {
            throw new ExtensionNotFoundException(
                "Could not find extension with id [" + extensionId + "] and version [" + extensionVersion + "]", e);
        } catch (Exception e) {
            throw new ResolveException(
                "Failed to create extension object for extension [" + extensionId + ":" + extensionVersion + "]", e);
        }
    }
}
