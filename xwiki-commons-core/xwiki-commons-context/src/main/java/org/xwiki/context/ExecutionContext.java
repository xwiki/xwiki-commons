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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.context.internal.ExecutionContextProperty;

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
    private Map<String, ExecutionContextProperty> properties = new HashMap<>();

    /**
     * @param key the key under which is stored the property to retrieve
     * @return the property matching the passed key
     */
    public Object getProperty(String key)
    {
        ExecutionContextProperty property = this.properties.get(key);

        if (property == null) {
            LOGGER.debug("Property [{}] doesn't exist in the Execution Context", key);
            return null;
        }

        return property.getValue();
    }

    /**
     * @param key the key of the property.
     * @return a builder object for performing the declaration. The property will not be declared until the declare
     *         method is called on the builder object.
     */
    public DeclarationBuilder newProperty(String key)
    {
        return new DeclarationBuilder(key);
    }

    /**
     * @param key the key under which is stored the property to retrieve
     * @return {@code true} if there is a property declared for the given key.
     */
    public boolean hasProperty(String key)
    {
        return this.properties.containsKey(key);
    }

    /**
     * @return all the context properties
     */
    public Map<String, Object> getProperties()
    {
        Map<String, Object> map = new HashMap<>();

        for (Map.Entry<String, ExecutionContextProperty> entry : this.properties.entrySet()) {
            map.put(entry.getKey(), entry.getValue().getValue());
        }

        return map;
    }

    /**
     * @param key remove the property whose key matches the passed key
     */
    public void removeProperty(String key)
    {
        ExecutionContextProperty property = this.properties.get(key);

        if (property != null && property.isFinal()) {
            throw new PropertyIsFinalException(key);
        }

        this.properties.remove(key);
    }

    /**
     * @param key the key under which to save the passed property value
     * @param value the value to set
     */
    public void setProperty(String key, Object value)
    {
        ExecutionContextProperty property = this.properties.get(key);

        if (property == null) {
            LOGGER.debug("Implicit declaration of property [{}] in the Execution Context", key);
            newProperty(key).declare();
            property = this.properties.get(key);
        } else if (property.isFinal()) {
            throw new PropertyIsFinalException(key);
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
     * @throws PropertyAlreadyExistsException if the property alread exists in this execution context.
     * @since 4.3M1
     */
    private void declareProperty(ExecutionContextProperty property)
    {
        if (this.properties.containsKey(property.getKey())) {
            throw new PropertyAlreadyExistsException(property.getKey());
        }

        this.properties.put(property.getKey(), property);
    }

    /**
     * Inherit properties marked for inheritance from the given execution context.
     * <p>
     * Inheritance is performed both in {@link Execution#setContext(ExecutionContext)} and in
     * {@link Execution#pushContext(ExecutionContext)} if there is a current execution context.
     * <p>
     * All properties marked as 'inherited' will be copied into this context, unless the property already is declared in
     * this context.
     * <p>
     * It is an error if this context contain a value that was declared as 'inherited' and 'final' in the inherited
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
        if (property.isFinal()) {
            ExecutionContextProperty shadowingProperty = this.properties.get(property.getKey());
            if (!(shadowingProperty == property || shadowingProperty.isClonedFrom(property))) {
                throw new IllegalStateException(
                    String.format("Execution context cannot be inherited because it already contains"
                        + " property [%s] which must be inherited because it is an inherited"
                        + " final property.", property.getKey()));
            }
        }
    }

    /**
     * Builder class for declaring a new proprety.
     *
     * @since 4.3M2
     */
    public final class DeclarationBuilder
    {

        /** @see ExecutionContextProperty#key */
        private final String key;

        /** @see ExecutionContextProperty#value */
        private Object value;

        /** @see ExecutionContextProperty#cloneValue */
        private boolean cloneValue;

        /** @see ExecutionContextProperty#isFinal */
        private boolean isFinal;

        /** @see ExecutionContextProperty#inherited */
        private boolean inherited;

        /** @see ExecutionContextProperty#nonNull */
        private boolean nonNull;

        /** @see ExecutionContextProperty#type */
        private Class<?> type;

        /**
         * Start building a property for the given key.
         *
         * @param key The property key.
         */
        private DeclarationBuilder(String key)
        {
            this.key = key;
        }

        /**
         * Finish the building by declaring the property in this execution context.
         */
        public void declare()
        {
            ExecutionContext.this.declareProperty(
                new ExecutionContextProperty(this.key, this.value, this.cloneValue, this.isFinal, this.inherited,
                    this.nonNull, this.type));
        }

        /**
         * @param value The initial value.
         * @return this declaration builder.
         */
        public DeclarationBuilder initial(Object value)
        {
            this.value = value;
            return this;
        }

        /**
         * Make the initial value the final value.
         *
         * @return this declaration builder.
         */
        public DeclarationBuilder makeFinal()
        {
            this.isFinal = true;
            return this;
        }

        /**
         * Indicate that the value should be cloned when the property is cloned.
         *
         * @return this declaration builder.
         */
        public DeclarationBuilder cloneValue()
        {
            this.cloneValue = true;
            return this;
        }

        /**
         * Set the type of the value.
         *
         * @param type The type to declare for the property.
         * @return this declaration builder.
         */
        public DeclarationBuilder type(Class<?> type)
        {
            this.type = type;
            return this;
        }

        /**
         * Indicate that the property should be inherited.
         *
         * @return this declaration builder.
         */
        public DeclarationBuilder inherited()
        {
            this.inherited = true;
            return this;
        }

        /**
         * Indicate that the property value may not be {@literal null}.
         *
         * @return this declaration builder.
         */
        public DeclarationBuilder nonNull()
        {
            this.nonNull = true;
            return this;
        }
    }
}
