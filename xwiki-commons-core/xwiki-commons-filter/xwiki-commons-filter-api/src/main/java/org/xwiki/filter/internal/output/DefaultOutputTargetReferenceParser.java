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

import java.io.File;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.filter.FilterException;
import org.xwiki.filter.output.DefaultFileOutputTarget;
import org.xwiki.filter.output.OutputTarget;
import org.xwiki.filter.output.OutputTargetReferenceParser;
import org.xwiki.filter.output.StringWriterOutputTarget;

/**
 * The default implementation of {@link OutputTargetReferenceParser} which find the right parser based on the prefix.
 * 
 * @version $Id$
 * @since 13.4RC1
 */
@Component
@Singleton
public class DefaultOutputTargetReferenceParser implements OutputTargetReferenceParser
{
    @Inject
    @Named("context")
    private Provider<ComponentManager> contextComponentManagerProvider;

    @Override
    public OutputTarget parse(String reference) throws FilterException
    {
        if (reference == null) {
            return null;
        }

        OutputTarget outputTarget;

        int index = reference.indexOf(':');
        if (index > 0) {
            String prefix = reference.substring(0, index);
            String value = reference.substring(index + 1);

            // TODO: use some OutputTargetParser instead to make it extensible
            if (prefix.equals("file")) {
                outputTarget = new DefaultFileOutputTarget(new File(value));
            } else {
                ComponentManager componentManager = this.contextComponentManagerProvider.get();

                if (componentManager.hasComponent(OutputTargetReferenceParser.class, prefix)) {
                    OutputTargetReferenceParser parser;
                    try {
                        parser = componentManager.getInstance(OutputTargetReferenceParser.class, prefix);
                    } catch (ComponentLookupException e) {
                        throw new FilterException(
                            "Failed to get the input source reference parser component for prefix [" + prefix + "]", e);
                    }

                    outputTarget = parser.parse(value);
                } else {
                    outputTarget = new StringWriterOutputTarget();
                }
            }
        } else {
            outputTarget = new StringWriterOutputTarget();
        }

        return outputTarget;
    }

}
