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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xwiki.extension.DefaultExtensionAuthor;
import org.xwiki.extension.DefaultExtensionComponent;
import org.xwiki.extension.DefaultExtensionDependency;
import org.xwiki.extension.DefaultExtensionIssueManagement;
import org.xwiki.extension.DefaultExtensionScm;
import org.xwiki.extension.DefaultExtensionScmConnection;
import org.xwiki.extension.DefaultExtensionSupportPlan;
import org.xwiki.extension.DefaultExtensionSupportPlans;
import org.xwiki.extension.DefaultExtensionSupporter;
import org.xwiki.extension.ExtensionLicense;
import org.xwiki.extension.ExtensionLicenseManager;
import org.xwiki.extension.internal.ExtensionFactory;
import org.xwiki.extension.internal.converter.ExtensionPatternConverter;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionAuthor;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionDependency;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionId;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionIssueManagement;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionRating;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionRepository;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionScm;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionScmConnection;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionSupportPlan;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionSupporter;
import org.xwiki.extension.repository.xwiki.model.jaxb.ExtensionVersion;
import org.xwiki.extension.repository.xwiki.model.jaxb.License;
import org.xwiki.extension.repository.xwiki.model.jaxb.Namespaces;
import org.xwiki.extension.repository.xwiki.model.jaxb.Property;
import org.xwiki.extension.version.internal.DefaultVersionConstraint;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Validate {@link XWikiExtension}.
 * 
 * @version $Id$
 */
class XWikiExtensionTest
{
    private org.xwiki.extension.ExtensionDependency extensionDependency(String id, String constraint, boolean optional,
        String... exclusions)
    {
        DefaultExtensionDependency dependency =
            new DefaultExtensionDependency(id, new DefaultVersionConstraint(constraint), optional);
        for (String exclusion : exclusions) {
            dependency.addExclusion(ExtensionPatternConverter.toExtensionPattern(exclusion));
        }

        return dependency;
    }

    @Test
    void newXWikiExtension() throws URISyntaxException, MalformedURLException
    {
        XWikiExtensionRepository repository = null;
        ExtensionLicenseManager licenseManager = mock();
        ExtensionFactory factory = new ExtensionFactory();

        ExtensionVersion restExtension = new ExtensionVersion();
        restExtension.setId("id");
        restExtension.setVersion("version");
        restExtension.setType("type");
        restExtension.setName("name");
        restExtension.setSummary("summary");
        restExtension.setCategory("category");
        restExtension.setDescription("description");
        restExtension.setRecommended(false);
        restExtension.setWebsite("http://website");
        Namespaces allowedNamespace = new Namespaces();
        allowedNamespace.getNamespaces().add("namespace1");
        allowedNamespace.getNamespaces().add("namespace2");
        restExtension.setAllowedNamespaces(allowedNamespace);
        ExtensionIssueManagement issueManagement = new ExtensionIssueManagement();
        issueManagement.setSystem("issues");
        issueManagement.setUrl("http://issues");
        restExtension.setIssueManagement(issueManagement);
        ExtensionRating rating = new ExtensionRating();
        rating.setAverageVote(1.0f);
        rating.setTotalVotes(42);
        restExtension.setRating(rating);
        ExtensionScm scm = new ExtensionScm();
        ExtensionScmConnection devConnection = new ExtensionScmConnection();
        devConnection.setPath("dpath");
        devConnection.setSystem("dsystem");
        ExtensionScmConnection connection = new ExtensionScmConnection();
        connection.setPath("cpath");
        connection.setSystem("csystem");
        scm.setConnection(connection);
        scm.setDeveloperConnection(devConnection);
        scm.setUrl("http://scm");
        restExtension.setScm(scm);
        ExtensionAuthor author1 = new ExtensionAuthor();
        author1.setName("author1");
        author1.setUrl("http://author1");
        ExtensionAuthor author2 = new ExtensionAuthor();
        author2.setName("author2");
        author2.setUrl("http://author2");
        restExtension.withAuthors(author1, author2);
        ExtensionDependency dependency1 = new ExtensionDependency();
        dependency1.setConstraint("constraint1");
        dependency1.setId("dependency1");
        dependency1.setOptional(true);
        dependency1.withExclusions("exclusion1.0", "exclusion1.1");
        ExtensionDependency dependency2 = new ExtensionDependency();
        dependency2.setConstraint("constraint2");
        dependency2.setId("dependency2");
        dependency2.setOptional(false);
        restExtension.withDependencies(dependency1, dependency2);
        ExtensionId feature1 = new ExtensionId();
        feature1.setId("feature1");
        feature1.setVersion("fversion1");
        ExtensionId feature2 = new ExtensionId();
        feature2.setId("feature2");
        feature2.setVersion("fversion2");
        restExtension.withExtensionFeatures(feature1, feature2);
        License license1 = new License();
        license1.setName("license1");
        license1.setContent("licencecontent1");
        License license2 = new License();
        license2.setName("license2");
        license2.setContent("licencecontent2");
        restExtension.withLicenses(license1, license2);
        ExtensionDependency mdependency1 = new ExtensionDependency();
        mdependency1.setConstraint("mconstraint1");
        mdependency1.setId("mdependency1");
        mdependency1.setOptional(true);
        mdependency1.withExclusions("exclusion1.0", "exclusion1.1");
        ExtensionDependency mdependency2 = new ExtensionDependency();
        mdependency2.setConstraint("mconstraint2");
        mdependency2.setId("mdependency2");
        mdependency2.setOptional(false);
        restExtension.withManagedDependencies(mdependency1, mdependency2);
        Property property1 = new Property();
        property1.setKey("key1");
        property1.setStringValue("value1");
        Property property2 = new Property();
        property2.setKey("key2");
        property2.setStringValue("value2");
        Property propertyComponents = new Property();
        propertyComponents.setKey("xwiki.extension.components");
        propertyComponents.setStringValue("role1/hint1\nrole2/hint2");
        restExtension.withProperties(property1, property2, propertyComponents);
        ExtensionRepository repository1 = new ExtensionRepository();
        repository1.setId("repository1");
        repository1.setType("rtype1");
        repository1.setUri("http://repository1");
        ExtensionRepository repository2 = new ExtensionRepository();
        repository2.setId("repository2");
        repository2.setType("rtype2");
        repository2.setUri("http://repository2");
        restExtension.withRepositories(repository1, repository2);
        ExtensionSupporter supporter1 = new ExtensionSupporter();
        supporter1.setName("supporter1");
        supporter1.setUrl("http://supporter1");
        ExtensionSupporter supporter2 = new ExtensionSupporter();
        supporter2.setName("supporter2");
        supporter2.setUrl("http://supporter2");
        ExtensionSupportPlan supportPlan1 = new ExtensionSupportPlan();
        supportPlan1.setName("plan1");
        supportPlan1.setPaying(true);
        supportPlan1.setSupporter(supporter1);
        supportPlan1.setUrl("http://plan1");
        ExtensionSupportPlan supportPlan2 = new ExtensionSupportPlan();
        supportPlan2.setName("plan2");
        supportPlan2.setPaying(true);
        supportPlan2.setSupporter(supporter2);
        supportPlan2.setUrl("http://plan2");
        restExtension.withSupportPlans(supportPlan1, supportPlan2);

        XWikiExtension extension = new XWikiExtension(repository, restExtension, licenseManager, factory);

        assertCollectionEquals(List.of("namespace1", "namespace2"), extension.getAllowedNamespaces());
        assertCollectionEquals(List.of(new DefaultExtensionAuthor("author1", "http://author1"),
            new DefaultExtensionAuthor("author2", "http://author2")), extension.getAuthors());
        assertEquals("category", extension.getCategory());
        assertCollectionEquals(
            List.of(new DefaultExtensionComponent("role1", "hint1"), new DefaultExtensionComponent("role2", "hint2")),
            extension.getComponents());
        assertCollectionEquals(
            List.of(extensionDependency("dependency1", "constraint1", true, "exclusion1.0", "exclusion1.1"),
                extensionDependency("dependency2", "constraint2", false)),
            extension.getDependencies());
        assertEquals("description", extension.getDescription());
        assertCollectionEquals(List.of(new org.xwiki.extension.ExtensionId("feature1", "fversion1"),
            new org.xwiki.extension.ExtensionId("feature2", "fversion2")), extension.getExtensionFeatures());
        assertEquals(new org.xwiki.extension.ExtensionId("id", "version"), extension.getId());
        assertEquals(new DefaultExtensionIssueManagement("issues", "http://issues"), extension.getIssueManagement());
        assertCollectionEquals(List.of(new ExtensionLicense("license1", List.of("content1")),
            new ExtensionLicense("license2", List.of("content2"))), extension.getLicenses());
        assertCollectionEquals(
            List.of(extensionDependency("mdependency1", "mconstraint1", true, "exclusion1.0", "exclusion1.1"),
                extensionDependency("mdependency2", "mconstraint2", false)),
            extension.getManagedDependencies());
        assertEquals("name", extension.getName());
        assertEquals(Map.of("key1", "value1", "key2", "value2"), extension.getProperties());
        assertEquals(42, extension.getRating().getTotalVotes());
        assertCollectionEquals(
            List.of(new DefaultExtensionRepositoryDescriptor("repository1", "rtype1", new URI("http://repository1")),
                new DefaultExtensionRepositoryDescriptor("repository2", "rtype2", new URI("http://repository2"))),
            extension.getRepositories());
        assertEquals(new DefaultExtensionScm("http://scm", new DefaultExtensionScmConnection("csystem", "cpath"),
            new DefaultExtensionScmConnection("dsystem", "dpath")), extension.getScm());
        assertEquals("summary", extension.getSummary());
        assertEquals(new DefaultExtensionSupportPlans(List.of(
            new DefaultExtensionSupportPlan(new DefaultExtensionSupporter("supporter1", new URL("http://supporter1")),
                "plan1", new URL("http://plan1"), true),
            new DefaultExtensionSupportPlan(new DefaultExtensionSupporter("supporter2", new URL("http://supporter2")),
                "plan2", new URL("http://plan2"), true))),
            extension.getSupportPlans());
        assertEquals("type", extension.getType());
        assertEquals("http://website", extension.getWebSite());
    }

    private void assertCollectionEquals(Collection<?> collection1, Collection<?> collection2)
    {
        assertEquals(collection1, new ArrayList<>(collection2));
    }
}
