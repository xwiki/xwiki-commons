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
package org.xwiki.groovy.internal;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.xwiki.component.annotation.Component;
import org.xwiki.groovy.GroovyConfiguration;

import groovy.lang.GroovyClassLoader;

/**
 * This class is required since the JSR223 doesn't allow configuring the classloader used by the Script Engine
 * implementation and this is how Groovy supports customizing script compilation.
 * 
 * @version $Id$
 * @since 4.1M1
 */
@Component(roles = {ScriptEngineFactory.class })
@Named("groovy")
@Singleton
public class GroovyScriptEngineFactory extends org.codehaus.groovy.jsr223.GroovyScriptEngineFactory
{
    /**
     * The Groovy configuration, used to get the list of Groovy Compilation Customizers.
     */
    @Inject
    private GroovyConfiguration configuration;

    @Override
    public ScriptEngine getScriptEngine()
    {
        // We configure the Groovy Script Engine with a custom GroovyClassLoader that we specifically configure with
        // Compilation Configurations to protect for example against scripts taking too long to execute.
        GroovyScriptEngineImpl engine = (GroovyScriptEngineImpl) super.getScriptEngine();

        // Add all the defined Customizers
        CompilerConfiguration config = new CompilerConfiguration();

        List<CompilationCustomizer> customizers = this.configuration.getCompilationCustomizers();
        if (!customizers.isEmpty()) {
            config.addCompilationCustomizers(customizers.toArray(new CompilationCustomizer[customizers.size()]));
        }

        ClassLoader parentClassLoader = Thread.currentThread().getContextClassLoader();
        GroovyClassLoader loader = new GroovyClassLoader(parentClassLoader, config);
        engine.setClassLoader(loader);

        return engine;
    }
}
