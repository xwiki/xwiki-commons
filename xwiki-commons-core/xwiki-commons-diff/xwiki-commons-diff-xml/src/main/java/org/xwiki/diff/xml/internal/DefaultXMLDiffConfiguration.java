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
package org.xwiki.diff.xml.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.w3c.dom.Node;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.diff.xml.StringSplitter;
import org.xwiki.diff.xml.XMLDiffConfiguration;
import org.xwiki.diff.xml.XMLDiffFilter;

/**
 * Used to configure the way we compute changes between two XML documents.
 * 
 * @version $Id$
 * @since 11.10.1
 * @since 12.0RC1
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
public class DefaultXMLDiffConfiguration implements XMLDiffConfiguration, Initializable
{
    @Inject
    private StringSplitter defaultSplitter;

    @Inject
    @Named("character")
    private StringSplitter characterSplitter;

    private final Map<Short, StringSplitter> splitterByNodeType = new HashMap<>();

    private double similarityThreshold = .6;

    private final List<XMLDiffFilter> filters = new ArrayList<>();

    @Override
    public void initialize() throws InitializationException
    {
        // Defaults.
        setSplitterForNodeType(Node.TEXT_NODE, this.characterSplitter);
    }

    @Override
    public StringSplitter getSplitterForNodeType(short nodeType)
    {
        return this.splitterByNodeType.getOrDefault(nodeType, this.defaultSplitter);
    }

    /**
     * Sets the splitter to use for a specific XML DOM node type.
     * 
     * @param nodeType the node type to set the splitter for
     * @param splitter the splitter to use for the specified node type
     */
    public void setSplitterForNodeType(short nodeType, StringSplitter splitter)
    {
        this.splitterByNodeType.put(nodeType, splitter);
    }

    @Override
    public double getSimilarityThreshold()
    {
        return this.similarityThreshold;
    }

    /**
     * Sets the threshold below which two nodes are considered to be very similar. The value must be between 0 and 1,
     * where 0 means identical nodes and 1 means completely different nodes.
     * 
     * @param similarityThreshold the new threshold value
     */
    public void setSimilarityThreshold(double similarityThreshold)
    {
        this.similarityThreshold = similarityThreshold;
    }

    @Override
    public List<XMLDiffFilter> getFilters()
    {
        return this.filters;
    }
}
