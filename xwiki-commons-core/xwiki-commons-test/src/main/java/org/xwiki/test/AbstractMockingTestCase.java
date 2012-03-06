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
package org.xwiki.test;

import java.lang.reflect.Type;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.runner.RunWith;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.util.ReflectionUtils;

/**
 * Offers a JMock 2.x Mockery object.
 * 
 * @version $Id$
 * @since 2.4RC1
 */
@RunWith(JMock.class)
public abstract class AbstractMockingTestCase
{
    private Mockery mockery = new JUnit4Mockery();

    public Mockery getMockery()
    {
        return this.mockery;
    }

    /**
     * @return a configured Component Manager (which uses the plexus.xml file in the test resources directory) which can
     *         then be put in the XWiki Context for testing.
     */
    public abstract EmbeddableComponentManager getComponentManager() throws Exception;

    /**
     * @since 3.0M3
     */
    public <T> T registerMockComponent(Class<T> role, String hint, String mockId) throws Exception
    {
        DefaultComponentDescriptor<T> descriptor = createComponentDescriptor(role);
        descriptor.setRoleHint(hint);
        return registerMockComponent(descriptor, mockId);
    }

    /**
     * @since 4.0M1
     */
    public <T> T registerMockComponent(Type role, String hint, String mockId) throws Exception
    {
        DefaultComponentDescriptor<T> descriptor = createComponentDescriptor(role);
        descriptor.setRoleHint(hint);
        return registerMockComponent(descriptor, mockId);
    }

    /**
     * @since 2.4RC1
     */
    public <T> T registerMockComponent(Class<T> role, String hint) throws Exception
    {
        return registerMockComponent(role, hint, null);
    }

    /**
     * @since 4.0M1
     */
    public <T> T registerMockComponent(Type role, String hint) throws Exception
    {
        return registerMockComponent(role, hint, null);
    }

    /**
     * @since 2.4RC1
     */
    public <T> T registerMockComponent(Class<T> role) throws Exception
    {
        return registerMockComponent(this.<T> createComponentDescriptor(role));
    }

    /**
     * @since 4.0M1
     */
    public <T> T registerMockComponent(Type role) throws Exception
    {
        return registerMockComponent(this.<T> createComponentDescriptor(role));
    }

    /**
     * @since 2.4RC1
     */
    private <T> T registerMockComponent(ComponentDescriptor<T> descriptor) throws Exception
    {
        return registerMockComponent(descriptor, null);
    }

    /**
     * @since 3.0M3
     */
    private <T> T registerMockComponent(ComponentDescriptor<T> descriptor, String mockId) throws Exception
    {
        T instance;
        if (mockId != null) {
            instance = getMockery().mock((Class<T>) ReflectionUtils.getTypeClass(descriptor.getRoleType()), mockId);
        } else {
            instance = getMockery().mock((Class<T>) ReflectionUtils.getTypeClass(descriptor.getRoleType()));
        }

        getComponentManager().registerComponent(descriptor, instance);

        return instance;
    }

    /**
     * @since 2.4RC1
     */
    private <T> DefaultComponentDescriptor<T> createComponentDescriptor(Type role)
    {
        DefaultComponentDescriptor<T> descriptor = new DefaultComponentDescriptor<T>();
        descriptor.setRoleType(role);

        return descriptor;
    }
}
