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
package org.xwiki.velocity.internal;

import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.velocity.Template;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;

/**
 * Extends {@link Template} to change how macros are resolved.
 * 
 * @version $Id$
 * @since 15.8RC1
 */
public class VelocityTemplate extends Template
{
    private static class SingletonResourceReader extends StringResourceLoader
    {
        private final Reader reader;

        SingletonResourceReader(Reader r)
        {
            this.reader = r;
        }

        @Override
        public Reader getResourceReader(String source, String encoding)
        {
            return this.reader;
        }
    }

    private Map<String, Object> templateMacros = new ConcurrentHashMap<>();

    /**
     * @param name the name of the template
     * @param rs the Velocity {@link RuntimeServices} instance
     */
    public VelocityTemplate(String name, RuntimeServices rs)
    {
        setName(name);
        setRuntimeServices(rs);
    }

    /**
     * @param source the content to compile
     */
    public void compile(Reader source)
    {
        // Inject a custom resource loaded in charge of providing the template content
        setResourceLoader(new SingletonResourceReader(source));

        // Compile the template
        process();
    }

    @Override
    public boolean process() throws ResourceNotFoundException, ParseErrorException
    {
        boolean successful = super.process();

        // Get the macro found in the template
        Map<String, Object> macros = getMacros();

        // Store the macro found in the template to reuse them later
        this.templateMacros.putAll(macros);

        // Velocity gives priority to the macros located in the same source as the executed directive but we want to be
        // able to override sub macros. The trick used is to make the source of all the directives think there is not
        // macro. They will be provided as libraries.
        macros.clear();

        return successful;
    }

    /**
     * @return the macros
     */
    public Map<String, Object> getTemplateMacros()
    {
        return this.templateMacros;
    }
}
