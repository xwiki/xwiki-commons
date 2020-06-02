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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.cache.CacheManager;
import org.xwiki.cache.internal.MapCache;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.job.GroupedJobInitializer;
import org.xwiki.job.JobGroupPath;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DefaultGroupedJobInitializerManager}.
 *
 * @since 12.5RC1
 * @version $Id$
 */
@ComponentTest
public class DefaultGroupedJobInitializerManagerTest
{
    @InjectMockComponents
    private DefaultGroupedJobInitializerManager groupedJobInitializerManager;

    @MockComponent
    private GroupedJobInitializer defaultGroupedJobInitializer;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @MockComponent
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @MockComponent
    private CacheManager cacheManager;

    private List<GroupedJobInitializer> initializerList;

    @BeforeComponent
    public void componentSetup() throws Exception
    {
        when(this.cacheManager.createNewCache(any())).thenReturn(new MapCache<>());
    }

    @BeforeEach
    void setup() throws Exception
    {
        when(this.componentManagerProvider.get()).thenReturn(componentManager);

        this.initializerList = new ArrayList<>();
        this.initializerList.add(this.componentManager.registerMockComponent(GroupedJobInitializer.class, "foo"));
        this.initializerList.add(this.componentManager.registerMockComponent(GroupedJobInitializer.class, "foo/bar"));
        this.initializerList.add(this.componentManager.registerMockComponent(GroupedJobInitializer.class, "a/b/c"));
        this.initializerList.add(
            this.componentManager.registerMockComponent(GroupedJobInitializer.class, "foo/baz/buz"));

        when(this.initializerList.get(0).getId()).thenReturn(new JobGroupPath(Collections.singletonList("foo")));
        when(this.initializerList.get(1).getId()).thenReturn(new JobGroupPath(Arrays.asList("foo", "bar")));
        when(this.initializerList.get(2).getId()).thenReturn(new JobGroupPath(Arrays.asList("a", "b", "c")));
        when(this.initializerList.get(3).getId()).thenReturn(new JobGroupPath(Arrays.asList("foo", "bar", "buz")));
    }

    @Test
    public void getInitializerExactMatch()
    {
        assertSame(this.initializerList.get(0),
            this.groupedJobInitializerManager.getGroupedJobInitializer(
                new JobGroupPath(Collections.singletonList("foo"))));
        assertSame(this.initializerList.get(3),
            this.groupedJobInitializerManager.getGroupedJobInitializer(
                new JobGroupPath(Arrays.asList("foo", "bar", "buz"))));
    }

    @Test
    public void getInitializerFallbackOnParent()
    {
        assertSame(this.initializerList.get(0),
            this.groupedJobInitializerManager.getGroupedJobInitializer(
                new JobGroupPath(Arrays.asList("foo", "something"))));
        assertSame(this.initializerList.get(1),
            this.groupedJobInitializerManager.getGroupedJobInitializer(
                new JobGroupPath(Arrays.asList("foo", "bar", "something"))));
        assertSame(this.initializerList.get(3),
            this.groupedJobInitializerManager.getGroupedJobInitializer(
                new JobGroupPath(Arrays.asList("foo", "bar", "buz", "something"))));
    }

    @Test
    public void getInitializerFallbackOnDefault()
    {
        assertSame(this.defaultGroupedJobInitializer,
            this.groupedJobInitializerManager.getGroupedJobInitializer(
                new JobGroupPath(Collections.singletonList("something"))));
        assertSame(this.defaultGroupedJobInitializer,
            this.groupedJobInitializerManager.getGroupedJobInitializer(null));
        assertSame(this.defaultGroupedJobInitializer,
            this.groupedJobInitializerManager.getGroupedJobInitializer(
                new JobGroupPath(Arrays.asList("a", "b"))));
    }
}
