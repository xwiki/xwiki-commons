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
package org.xwiki.component.descriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.xwiki.text.XWikiToStringBuilder;

/**
 * Default implementation of {@link ComponentDescriptor}.
 *
 * @version $Id$
 * @param <T> the type of the component role
 * @since 1.7M1
 */
public class DefaultComponentDescriptor<T> extends DefaultComponentRole<T> implements ComponentDescriptor<T>
{
    /**
     * @see #getImplementation()
     */
    private Class<? extends T> implementation;

    /**
     * @see #getInstantiationStrategy()
     */
    private ComponentInstantiationStrategy instantiationStrategy = ComponentInstantiationStrategy.SINGLETON;

    /**
     * @see #getComponentDependencies()
     */
    private List<ComponentDependency<?>> componentDependencies = new ArrayList<>();

    /**
     * Default constructor.
     */
    public DefaultComponentDescriptor()
    {
    }

    /**
     * Creating a new {@link DefaultComponentDescriptor} by cloning the provided {@link ComponentDescriptor}.
     *
     * @param descriptor the component descriptor to clone
     * @since 3.4M1
     */
    public DefaultComponentDescriptor(ComponentDescriptor<T> descriptor)
    {
        super(descriptor);

        setImplementation(descriptor.getImplementation());
        setInstantiationStrategy(descriptor.getInstantiationStrategy());
        for (ComponentDependency<?> dependency : descriptor.getComponentDependencies()) {
            addComponentDependency(new DefaultComponentDependency(dependency));
        }
    }

    /**
     * @param implementation the class of the component implementation
     */
    public void setImplementation(Class<? extends T> implementation)
    {
        this.implementation = implementation;
    }

    @Override
    public Class<? extends T> getImplementation()
    {
        return this.implementation;
    }

    /**
     * @param instantiationStrategy the way the component should be instantiated
     * @see ComponentInstantiationStrategy
     */
    public void setInstantiationStrategy(ComponentInstantiationStrategy instantiationStrategy)
    {
        this.instantiationStrategy = instantiationStrategy;
    }

    @Override
    public ComponentInstantiationStrategy getInstantiationStrategy()
    {
        return this.instantiationStrategy;
    }

    @Override
    public Collection<ComponentDependency<?>> getComponentDependencies()
    {
        return this.componentDependencies;
    }

    /**
     * @param componentDependency the dependency to add
     */
    public void addComponentDependency(ComponentDependency<?> componentDependency)
    {
        this.componentDependencies.add(componentDependency);
    }

    /**
     * @param role the class of the component role
     * @param roleHint the hint of the component role
     * @param <D> the type of the dependency role
     */
    public <D> void addComponentDependency(Class<D> role, String roleHint)
    {
        DefaultComponentDependency<D> componentDependency = new DefaultComponentDependency<>();
        componentDependency.setRole(role);
        componentDependency.setRoleHint(roleHint);

        this.componentDependencies.add(componentDependency);
    }

    @Override
    public String toString()
    {
        ToStringBuilder builder = new XWikiToStringBuilder(this);
        builder.appendSuper(super.toString());
        builder.append("implementation", getImplementation() == null ? null : getImplementation().getName());
        builder.append("instantiation", getInstantiationStrategy());
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.3M1
     */
    @Override
    public boolean equals(Object object)
    {
        boolean result;

        // See http://www.technofundo.com/tech/java/equalhash.html for the detail of this algorithm.
        if (this == object) {
            result = true;
        } else {
            if ((object == null) || (object.getClass() != this.getClass())) {
                result = false;
            } else {
                // object must be Syntax at this point
                ComponentDescriptor cd = (ComponentDescriptor) object;

                result =
                    super.equals(cd) && Objects.equals(getImplementation(), cd.getImplementation())
                        && Objects.equals(getInstantiationStrategy(), cd.getInstantiationStrategy())
                        && Objects.equals(getComponentDependencies(), cd.getComponentDependencies());
            }
        }

        return result;
    }

    /**
     * {@inheritDoc}
     *
     * @since 3.3M1
     */
    @Override
    public int hashCode()
    {
        HashCodeBuilder builder = new HashCodeBuilder();

        builder.appendSuper(super.hashCode());

        builder.append(getImplementation());
        builder.append(getInstantiationStrategy());
        builder.append(getComponentDependencies());

        return builder.toHashCode();
    }
}
