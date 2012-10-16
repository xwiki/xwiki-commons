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
package org.xwiki.context;

import java.util.Map;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains all state data related to the current user action. Note that the execution context is independent of the
 * environment and all environment-dependent data are stored in the Container component instead.
 *
 * @version $Id$ 
 * @since 1.5M2
 */
public class ExecutionContext
{
    /** Logger object. */
    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionContext.class);

    /**
     * @see #getProperty(String)
     */
    private Map<String, ExecutionContextProperty> properties = new HashMap<String, ExecutionContextProperty>();

    /**
     * @param key the key under which is stored the property to retrieve
     * @return the property matching the passed key
     */
    public Object getProperty(String key)
    {
        ExecutionContextProperty property = properties.get(key);

        if (property == null) {
            LOGGER.debug("Getting undefined property {} from execution context.", key);
            return null;
        }

        return property.getValue();
    }

    /**
     * @param key the key under which is stored the property to retrieve
     * @return {@code true} if there is a property declared for the given key.
     */
    public boolean hasProperty(String key)
    {
        return properties.containsKey(key);
    }

    /**
     * @return all the context properties
     */
    public Map<String, Object> getProperties()
    {
        Map<String, Object> map = new HashMap<String, Object>();

        for (Map.Entry<String, ExecutionContextProperty> entry : properties.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getValue());
        }

        return map;
    }
    
    /**
     * @param key remove the property whose key matches the passed key
     */
    public void removeProperty(String key)
    {
        ExecutionContextProperty property = properties.get(key);

        if (property == null) {
            LOGGER.warn("Tried to remove non-existing property [{}] from execution context.", key);
            return;
        }

        if (property.isReadonly()) {
            throw new PropertyIsReadonlyException(key);
        }

        this.properties.remove(key);
    }

    /**
     * @param key the key under which to save the passed property value
     * @param value the value to set
     */
    public void setProperty(String key, Object value)
    {
        ExecutionContextProperty property = properties.get(key);

        if (property == null) {
            LOGGER.debug("Implicit declaration of property {}.", key);
            property = new ExecutionContextProperty(key);
            properties.put(key, property);
        } else if (property.isReadonly()) {
            throw new PropertyIsReadonlyException(key);
        }

        property.setValue(value);
    }

    /**
     * @param properties the properties to add to the context
     */
    public void setProperties(Map<String, Object> properties)
    {
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            setProperty(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Declare a property.
     *
     * @param property The property with configured metadata attributes.
     *
     * @throws PropertyAlreadyExistsException if the property alread exists in this execution context.
     * 
     * @since 4.3M1
     */
    public void declareProperty(ExecutionContextProperty property)
    {
        if (properties.containsKey(property.getKey())) {
            throw new PropertyAlreadyExistsException(property.getKey());
        }

        properties.put(property.getKey(), property);
    }

    /**
     * Inherit properties marked for inheritance from the given execution context.
     *
     * Inheritance is performed both in {@link Execution#setContext()} and in {@link Execution.pushContext()}, if there
     * is a current execution context.
     *
     * All properties marked as 'inherited' will be copied into this context, unless the property already is declared in
     * this context.
     *
     * It is an error if this context contain a value that was declared as 'inherited' and 'read-only' in the inherited
     * execution context and an exception will be thrown.
     * 
     * @param executionContext The execution to inherit.
     * @throws IllegalStateException if the execution context cannot be inherited.
     * @since 4.3M1
     */
    public void inheritFrom(ExecutionContext executionContext)
    {
        for (ExecutionContextProperty property : executionContext.properties.values()) {
            if (property.isInherited()) {
                if (this.properties.containsKey(property.getKey())) {
                    checkIfInheritedPropertyMayBeIgnored(property);
                } else {
                    declareProperty(property.clone());
                }
            }
        }
    }

    /**
     * @param property Property to check.
     * @throws IllegalStateException if the property may not be ignored.
     */
    private void checkIfInheritedPropertyMayBeIgnored(ExecutionContextProperty property)
    {
        if (property.isReadonly()) {
            ExecutionContextProperty shadowingProperty = this.properties.get(property.getKey());
            if (!shadowingProperty.isClonedFrom(property)) {
                throw new IllegalStateException(
                     String.format("Execution context cannot be inherited because it already contains"
                                 + "  property [%s] which must be inherited because it is an inherited"
                                 + " read-only property.", property.getKey()));
            }
        }
    }
}
