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
package org.xwiki.diff.script.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.diff.xml.StringSplitter;
import org.xwiki.diff.xml.XMLDiffFilter;
import org.xwiki.diff.xml.internal.HTMLDiffConfiguration;

/**
 * @version $Id$
 * @since 11.10.1
 * @since 12.0RC1
 */
@Component
@Named("html/script")
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class ScriptHTMLDiffConfiguration extends HTMLDiffConfiguration
{
    @Inject
    private Logger logger;

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    /**
     * Sets the splitter to use for a specific XML DOM node type.
     * 
     * @param nodeType the node type to set the splitter for
     * @param splitterHint indicates the splitter implementation to use for the specified node type
     * @return this configuration, in order to chain calls
     */
    public ScriptHTMLDiffConfiguration setSplitterForNodeType(short nodeType, String splitterHint)
    {
        try {
            setSplitterForNodeType(nodeType,
                this.componentManagerProvider.get().<StringSplitter>getInstance(StringSplitter.class, splitterHint));
        } catch (ComponentLookupException e) {
            this.logger.error("Failed to lookup StringSplitter with hint [{}].", splitterHint, e);
        }

        return this;
    }

    /**
     * Adds a new filter to the list of filters that are applied before and after computing the changes.
     * 
     * @param filterHint indicates the filter implementation to add
     * @return this configuration, in order to chain calls
     */
    public ScriptHTMLDiffConfiguration addFilter(String filterHint)
    {
        try {
            getFilters().add(this.componentManagerProvider.get().getInstance(XMLDiffFilter.class, filterHint));
        } catch (ComponentLookupException e) {
            this.logger.error("Failed to lookup XMLDiffFilter with hint [{}].", filterHint, e);
        }

        return this;
    }
}
