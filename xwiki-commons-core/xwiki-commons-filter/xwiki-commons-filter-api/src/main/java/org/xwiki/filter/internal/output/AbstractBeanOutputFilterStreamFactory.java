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
package org.xwiki.filter.internal.output;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.internal.AbstractBeanFilterStreamFactory;
import org.xwiki.filter.internal.input.BeanInputFilterStream;
import org.xwiki.filter.output.BeanOutputFilterStreamFactory;
import org.xwiki.filter.output.OutputFilterStream;
import org.xwiki.filter.type.FilterStreamType;

/**
 * @param <P>
 * @version $Id$
 * @since 6.2M1
 */
public abstract class AbstractBeanOutputFilterStreamFactory<P, F> extends AbstractBeanFilterStreamFactory<P> implements
    BeanOutputFilterStreamFactory<P>
{
    @Inject
    private ComponentManager componentManager;

    private List<Class< ? >> filerInterfaces;

    public AbstractBeanOutputFilterStreamFactory(FilterStreamType type)
    {
        super(type);
    }

    @Override
    public void initialize() throws InitializationException
    {
        super.initialize();

        // Get bean properties type
        ParameterizedType genericType =
            (ParameterizedType) ReflectionUtils.resolveType(AbstractBeanOutputFilterStreamFactory.class, getClass());
        this.filerInterfaces =
            Arrays.<Class< ? >> asList(ReflectionUtils.getTypeClass(genericType.getActualTypeArguments()[1]));
    }

    @Override
    public Collection<Class< ? >> getFilterInterfaces() throws FilterException
    {
        return this.filerInterfaces;
    }

    @Override
    public OutputFilterStream createOutputFilterStream(Map<String, Object> properties) throws FilterException
    {
        return createOutputFilterStream(createPropertiesBean(properties));
    }

    @Override
    public BeanOutputFilterStream<P> createOutputFilterStream(P properties) throws FilterException
    {
        BeanOutputFilterStream<P> inputFilter;
        try {
            inputFilter =
                this.componentManager.getInstance(new DefaultParameterizedType(null, BeanOutputFilterStream.class,
                    getPropertiesBeanClass()), getType().serialize());
        } catch (ComponentLookupException e) {
            throw new FilterException(String.format("Failed to get instance of [%s] for type [%s]",
                BeanInputFilterStream.class, getType()), e);
        }

        inputFilter.setProperties(properties);

        return inputFilter;
    }
}
