package org.xwiki.extension.repository.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.extension.ResolveException;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryManager;
import org.xwiki.extension.repository.result.CollectionIterableResult;
import org.xwiki.extension.repository.result.IterableResult;
import org.xwiki.extension.version.Version;
import org.xwiki.extension.version.internal.DefaultVersion;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import com.google.common.collect.Lists;

/**
 * Test {@link DefaultExtensionRepositoryManager}.
 * 
 * @version $Id$
 */
public class DefaultExtensionRepositoryManagerTest
{
    @Rule
    public MockitoComponentMockingRule<ExtensionRepositoryManager> mock =
        new MockitoComponentMockingRule<ExtensionRepositoryManager>(DefaultExtensionRepositoryManager.class);

    private ExtensionRepository mockRepository1;

    private ExtensionRepository mockRepository2;

    @Before
    public void configure() throws Exception
    {
        this.mockRepository1 = mock(ExtensionRepository.class, "repository1");
        when(this.mockRepository1.getDescriptor()).thenReturn(
            new DefaultExtensionRepositoryDescriptor("repository1", "type", new URI("uri:uri")));
        this.mock.getComponentUnderTest().addRepository(this.mockRepository1);

        this.mockRepository2 = mock(ExtensionRepository.class, "repository2");
        when(this.mockRepository2.getDescriptor()).thenReturn(
            new DefaultExtensionRepositoryDescriptor("repository2", "type", new URI("uri:uri")));
        this.mock.getComponentUnderTest().addRepository(this.mockRepository2);
    }

    private List<Version> toVersionList(String... versions)
    {
        List<Version> versionList = new ArrayList<Version>(versions.length);

        for (String version : versions) {
            versionList.add(new DefaultVersion(version));
        }

        return versionList;
    }

    private IterableResult<Version> toIterableVersions(String... versions)
    {
        List<Version> versionList = toVersionList(versions);

        return new CollectionIterableResult<Version>(versionList.size(), 0, versionList);
    }

    private void assertResolveVersions(String id, int offset, int nb, String... versions) throws ResolveException,
        ComponentLookupException
    {
        Assert.assertEquals(toVersionList(versions),
            Lists.newArrayList(this.mock.getComponentUnderTest().resolveVersions("id", offset, nb)));
    }

    // Tests

    @Test
    public void resolveVersions() throws ResolveException, ComponentLookupException
    {
        when(this.mockRepository1.resolveVersions("id", 0, -1)).thenReturn(toIterableVersions("1.0", "2.0"));
        when(this.mockRepository2.resolveVersions("id", 0, -1)).thenReturn(toIterableVersions("3.0", "4.0"));

        assertResolveVersions("id", 0, -1, "1.0", "2.0", "3.0", "4.0");
        assertResolveVersions("id", 0, 1, "1.0");
        assertResolveVersions("id", 1, -1, "2.0", "3.0", "4.0");
    }
}
