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
package org.xwiki.extension.job.history.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.xwiki.extension.job.ExtensionRequest;
import org.xwiki.extension.job.InstallRequest;
import org.xwiki.extension.job.UninstallRequest;
import org.xwiki.extension.job.history.ExtensionJobHistoryRecord;
import org.xwiki.extension.job.history.ReplayJobStatus;
import org.xwiki.extension.job.history.ReplayRequest;
import org.xwiki.extension.job.internal.AbstractExtensionJob;
import org.xwiki.job.GroupedJob;
import org.xwiki.job.Job;
import org.xwiki.job.JobGroupPath;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReplayJob}.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
public class ReplayJobTest
{
    @Rule
    public MockitoComponentMockingRule<Job> mocker = new MockitoComponentMockingRule<Job>(ReplayJob.class);

    @Test
    public void replay() throws Exception
    {
        InstallRequest installRequest = new InstallRequest();
        ExtensionJobHistoryRecord installRecord =
            new ExtensionJobHistoryRecord("install", installRequest, null, null, null);
        Job installJob = this.mocker.registerMockComponent(Job.class, "install");

        UninstallRequest uninstallRequest = new UninstallRequest();
        ExtensionJobHistoryRecord uninstallRecord =
            new ExtensionJobHistoryRecord("uninstall", uninstallRequest, null, null, null);
        Job uninstallJob = this.mocker.registerMockComponent(Job.class, "uninstall");

        ReplayRequest request = new ReplayRequest();
        request.setRecords(Arrays.asList(installRecord, uninstallRecord));

        Job replayJob = this.mocker.getComponentUnderTest();
        replayJob.initialize(request);
        replayJob.run();

        verify(installJob).initialize(installRequest);
        verify(installJob).run();

        verify(uninstallJob).initialize(uninstallRequest);
        verify(uninstallJob).run();

        assertEquals(1, ((ReplayJobStatus) replayJob.getStatus()).getCurrentRecordNumber());
    }

    @Test
    public void getGroupPath() throws Exception
    {
        InstallRequest installOnTech = new InstallRequest();
        installOnTech.addNamespace("wiki:tech");

        InstallRequest installOnDesign = new InstallRequest();
        installOnDesign.addNamespace("wiki:design");

        InstallRequest installGlobally = new InstallRequest();

        InstallRequest installOnTechAndDesign = new InstallRequest();
        installOnTechAndDesign.addNamespace("wiki:tech");
        installOnTechAndDesign.addNamespace("wiki:design");

        assertGroupPath(new JobGroupPath("wiki:tech", AbstractExtensionJob.ROOT_GROUP), installOnTech, installOnTech);
        assertGroupPath(AbstractExtensionJob.ROOT_GROUP, installOnTech, installOnDesign);
        assertGroupPath(AbstractExtensionJob.ROOT_GROUP, installOnTech, installGlobally);
    }

    private void assertGroupPath(JobGroupPath expected, ExtensionRequest... requests) throws Exception
    {
        List<ExtensionJobHistoryRecord> records = new ArrayList<>();
        for (ExtensionRequest request : requests) {
            records.add(new ExtensionJobHistoryRecord(null, request, null, null, null));
        }

        ReplayRequest request = new ReplayRequest();
        request.setRecords(records);

        Job replayJob = this.mocker.getComponentUnderTest();
        replayJob.initialize(request);
        assertEquals(expected, ((GroupedJob) replayJob).getGroupPath());
    }
}
