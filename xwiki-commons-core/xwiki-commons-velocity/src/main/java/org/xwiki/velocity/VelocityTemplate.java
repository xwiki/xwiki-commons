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
package org.xwiki.velocity;

import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.xwiki.stability.Unstable;

/**
 * Expose a Velocity {@link Template} and its macros (which have been extracted and removed from the {@link Template}).
 * 
 * @version $Id$
 * @since 15.8RC1
 */
@Unstable
public class VelocityTemplate
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

    private Template template = new Template();

    private Map<String, Object> templateMacros = new ConcurrentHashMap<>();

    /**
     * @param name the name of the template
     * @param rs the Velocity {@link RuntimeServices} instance
     */
    public VelocityTemplate(String name, RuntimeServices rs)
    {
        this.template.setName(name);
        this.template.setRuntimeServices(rs);
    }

    /**
     * @param source the content to compile
     */
    public void compile(Reader source)
    {
        // Inject a custom resource loaded in charge of providing the template content
        this.template.setResourceLoader(new SingletonResourceReader(source));

        // Compile the template
        this.template.process();

        // Get the macro found in the template
        Map<String, Object> macros = this.template.getMacros();

        // Store the macro found in the template to reuse them later
        this.templateMacros.putAll(macros);

        // Velocity gives priority to the macros located in the same source as the executed directive but we want to be
        // able to override sub macros. The trick used is to make the source of all the directives think there is not
        // macro. They will be provided as libraries.
        macros.clear();

        // Reset the temporary resource loader since we don't need it anymore (and especially the content it retains in
        // memory)
        this.template.setResourceLoader(null);
    }

    /**
     * @return the actual Velocity template, without the macros
     */
    public Template getTemplate()
    {
        return this.template;
    }

    /**
     * @return the macros found in the template
     */
    public Map<String, Object> getMacros()
    {
        return this.templateMacros;
    }
}
