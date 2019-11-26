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
package org.xwiki.filter.internal.job;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.filter.input.InputFilterStream;
import org.xwiki.filter.input.InputFilterStreamFactory;
import org.xwiki.filter.job.FilterConversionFinished;
import org.xwiki.filter.job.FilterConversionStarted;
import org.xwiki.filter.job.FilterStreamConverterJobRequest;
import org.xwiki.filter.job.FilterStreamJobRequest;
import org.xwiki.filter.output.OutputFilterStream;
import org.xwiki.filter.output.OutputFilterStreamFactory;
import org.xwiki.job.AbstractJob;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.GroupedJob;
import org.xwiki.job.JobGroupPath;

/**
 * Perform a Filter conversion.
 *
 * @version $Id$
 * @since 6.2M1
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
@Named(FilterStreamConverterJob.JOBTYPE)
public class FilterStreamConverterJob
    extends AbstractJob<FilterStreamConverterJobRequest, DefaultJobStatus<FilterStreamConverterJobRequest>>
    implements GroupedJob
{
    /**
     * The id of the job.
     */
    public static final String JOBTYPE = "filter.converter";

    /**
     * The root group of all filter conversion jobs.
     */
    public static final JobGroupPath ROOT_GROUP =
        new JobGroupPath(Arrays.asList(FilterStreamJobRequest.JOBID_PREFIX, "converter"));

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Override
    public String getType()
    {
        return JOBTYPE;
    }

    @Override
    public JobGroupPath getGroupPath()
    {
        return ROOT_GROUP;
    }

    @Override
    protected void runInternal() throws Exception
    {
        InputFilterStreamFactory inputFactory = this.componentManagerProvider.get()
            .getInstance(InputFilterStreamFactory.class, getRequest().getInputType().serialize());

        try (InputFilterStream inputFilter = inputFactory.createInputFilterStream(getRequest().getInputProperties())) {
            OutputFilterStreamFactory outputFactory = this.componentManagerProvider.get()
                .getInstance(OutputFilterStreamFactory.class, getRequest().getOutputType().serialize());

            try (OutputFilterStream outputFilter =
                outputFactory.createOutputFilterStream(getRequest().getOutputProperties())) {
                inputFilter.read(outputFilter.getFilter());
            }
        }
    }

    @Override
    protected void jobStarting()
    {
        this.observationManager.notify(new FilterConversionStarted(getRequest().getId(), getType(), this.request),
            this);

        super.jobStarting();
    }

    @Override
    protected void jobFinished(Throwable error)
    {
        try {
            super.jobFinished(error);
        } finally {
            this.observationManager.notify(new FilterConversionFinished(getRequest().getId(), getType(), this.request),
                this);
        }
    }
}
