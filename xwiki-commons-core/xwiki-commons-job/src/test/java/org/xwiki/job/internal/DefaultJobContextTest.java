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
package org.xwiki.job.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.job.Job;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Validate {@link DefaultJobContext}.
 * 
 * @version $Id$
 */
@ComponentTest
public class DefaultJobContextTest
{
    @InjectMockComponents
    private DefaultJobContext context;

    @MockComponent
    private Execution execution;

    @BeforeEach
    public void beforeEach()
    {
        when(this.execution.getContext()).thenReturn(new ExecutionContext());
    }

    @Test
    public void pushpop()
    {
        Job job1 = mock(Job.class);
        Job job2 = mock(Job.class);

        assertNull(this.context.getCurrentJob());

        this.context.pushCurrentJob(job1);

        assertSame(job1, this.context.getCurrentJob());

        this.context.pushCurrentJob(job2);

        assertSame(job2, this.context.getCurrentJob());

        this.context.popCurrentJob();

        assertSame(job1, this.context.getCurrentJob());

        this.context.popCurrentJob();

        assertNull(this.context.getCurrentJob());
    }
}
