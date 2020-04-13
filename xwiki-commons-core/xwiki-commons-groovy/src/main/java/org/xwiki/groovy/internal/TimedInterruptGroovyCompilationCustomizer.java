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

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.xwiki.component.annotation.Component;
import org.xwiki.groovy.GroovyCompilationCustomizer;
import org.xwiki.groovy.TimedInterruptCustomizerConfiguration;

import groovy.transform.TimedInterrupt;

/**
 * Allow interrupting Groovy scripts after a given timeout. The timeout is configurable using
 * {@link TimedInterruptCustomizerConfiguration}.
 *
 * @version $Id$
 * @since 4.1M1
 */
@Component
@Named("timedInterrupt")
@Singleton
public class TimedInterruptGroovyCompilationCustomizer implements GroovyCompilationCustomizer
{
    /**
     * Used to get the script timeout configuration parameter value.
     */
    @Inject
    private TimedInterruptCustomizerConfiguration configuration;

    @Override
    public CompilationCustomizer createCustomizer()
    {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("value", this.configuration.getTimeout());
        return new ASTTransformationCustomizer(parameters, TimedInterrupt.class);
    }
}
