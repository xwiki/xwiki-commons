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
package org.xwiki.filter.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xwiki.filter.type.FilterStreamType;
import org.xwiki.job.AbstractRequest;
import org.xwiki.job.Request;

/**
 * The request used to configure "filter.converter" job.
 *
 * @version $Id$
 * @since 6.2M1
 */
public class FilterStreamConverterJobRequest extends AbstractRequest implements FilterStreamJobRequest
{
    /**
     * Serialization identifier.
     */
    private static final long serialVersionUID = 1L;

    /**
     * @see #getInputType()
     */
    private FilterStreamType inputType;

    /**
     * @see #getInputProperties()
     */
    private Map<String, Object> inputProperties;

    /**
     * @see #getOutputType()
     */
    private FilterStreamType outputType;

    /**
     * @see #isEventsFolded()
     */
    private boolean eventsFolded;

    /**
     * @see #getOutputProperties()
     */
    private Map<String, Object> outputProperties;

    /**
     * @param inputType the type of the input module
     * @param inputProperties the configuration of the input module
     * @param outputType the type of the output module
     * @param outputProperties the configuration of the output module
     */
    public FilterStreamConverterJobRequest(FilterStreamType inputType, Map<String, Object> inputProperties,
        FilterStreamType outputType, Map<String, Object> outputProperties)
    {
        this(inputType, inputProperties, outputType, true, outputProperties);
    }

    /**
     * @param inputType the type of the input module
     * @param inputProperties the configuration of the input module
     * @param outputType the type of the output module
     * @param eventsFolded true if events produced during the conversion should be folded
     * @param outputProperties the configuration of the output module
     */
    public FilterStreamConverterJobRequest(FilterStreamType inputType, Map<String, Object> inputProperties,
        FilterStreamType outputType, boolean eventsFolded, Map<String, Object> outputProperties)
    {
        this.inputType = inputType;
        this.inputProperties = inputProperties;
        this.outputType = outputType;
        this.outputProperties = outputProperties;
        this.eventsFolded = eventsFolded;

        List<String> jobId = new ArrayList<>();
        jobId.add(JOBID_PREFIX);
        jobId.add("converter");
        jobId.add(inputType.serialize());
        jobId.add(outputType.serialize());
        setId(jobId);
    }

    /**
     * @param request the request to copy
     */
    public FilterStreamConverterJobRequest(Request request)
    {
        super(request);
    }

    /**
     * @return the type of the input module
     */
    public FilterStreamType getInputType()
    {
        return this.inputType;
    }

    /**
     * @return the configuration of the input module
     */
    public Map<String, Object> getInputProperties()
    {
        return this.inputProperties;
    }

    /**
     * @return the type of the output module
     */
    public FilterStreamType getOutputType()
    {
        return this.outputType;
    }

    /**
     * @return true if events produced during the conversion should be folded
     * @since 8.2RC1
     */
    public boolean isEventsFolded()
    {
        return this.eventsFolded;
    }

    /**
     * @return the configuration of the output module
     */
    public Map<String, Object> getOutputProperties()
    {
        return this.outputProperties;
    }
}
