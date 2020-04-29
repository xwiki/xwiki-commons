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
package org.xwiki.configuration.internal;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.xwiki.configuration.ConfigurationSaveException;
import org.xwiki.configuration.ConfigurationSource;

/**
 * Decorator pattern for {@link ConfigurationSource}.
 *
 * @version $Id$
 * @since 12.4RC1
 */
public class ConfigurationSourceDecorator implements ConfigurationSource
{
    private ConfigurationSource wrappedConfigurationSource;

    /**
     * Functional interface allowing to execute some code before and after setter methods that throw an exception.
     *
     * @param <E> the exception that can be thrown
     */
    @FunctionalInterface
    public interface ThrowingRunnable<E extends Exception>
    {
        /**
         * Execute the code.
         *
         * @throws E the exception that can be thrown in case of error
         */
        void run() throws E;
    }

    /**
     * @param wrappedConfigurationSource the wrapped configuration source
     */
    public ConfigurationSourceDecorator(ConfigurationSource wrappedConfigurationSource)
    {
        this.wrappedConfigurationSource = wrappedConfigurationSource;
    }

    @Override
    public <T> T getProperty(String key, T defaultValue)
    {
        return executeRead(() -> getWrappedConfigurationSource().getProperty(key, defaultValue));
    }

    @Override
    public <T> T getProperty(String key, Class<T> valueClass)
    {
        return executeRead(() -> getWrappedConfigurationSource().getProperty(key, valueClass));
    }

    @Override
    public <T> T getProperty(String key, Class<T> valueClass, T defaultValue)
    {
        return executeRead(() -> getWrappedConfigurationSource().getProperty(key, valueClass, defaultValue));
    }

    @Override
    public <T> T getProperty(String key)
    {
        return executeRead(() -> getWrappedConfigurationSource().getProperty(key));
    }

    @Override
    public List<String> getKeys()
    {
        return executeRead(() -> getWrappedConfigurationSource().getKeys());
    }

    @Override
    public boolean containsKey(String key)
    {
        return executeRead(() -> getWrappedConfigurationSource().containsKey(key));
    }

    @Override
    public boolean isEmpty()
    {
        return executeRead(() -> getWrappedConfigurationSource().isEmpty());
    }

    @Override
    public void setProperties(Map<String, Object> properties) throws ConfigurationSaveException
    {
        executeWrite(() -> getWrappedConfigurationSource().setProperties(properties));
    }

    /**
     * To be overridden to execute some code before and after the getter calls.
     *
     * @param supplier the code to execute
     * @param <T> the getter's return type
     * @return the result of calling the getter
     */
    protected <T> T executeRead(Supplier<T> supplier)
    {
        return supplier.get();
    }

    /**
     * To be overridden to execute some code before and after the setter calls.
     *
     * @param runnable the code to execute
     * @throws E in case of error
     */
    protected <E extends Exception> void executeWrite(ThrowingRunnable<E> runnable) throws E
    {
        runnable.run();
    }

    /**
     * @return the wrapped configuration source
     */
    protected ConfigurationSource getWrappedConfigurationSource()
    {
        return this.wrappedConfigurationSource;
    }
}
