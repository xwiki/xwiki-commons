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

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.PredicateUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xwiki.extension.job.InstallRequest;
import org.xwiki.extension.job.UninstallRequest;
import org.xwiki.extension.job.history.ExtensionJobHistory;
import org.xwiki.extension.job.history.ExtensionJobHistoryConfiguration;
import org.xwiki.extension.job.history.ExtensionJobHistoryRecord;
import org.xwiki.test.mockito.MockitoComponentMockingRule;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DefaultExtensionJobHistory}.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
public class DefaultExtensionJobHistoryTest
{
    @Rule
    public MockitoComponentMockingRule<ExtensionJobHistory> mocker =
        new MockitoComponentMockingRule<ExtensionJobHistory>(DefaultExtensionJobHistory.class);

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void addGetRecords() throws Exception
    {
        ExtensionJobHistoryConfiguration config = this.mocker.getInstance(ExtensionJobHistoryConfiguration.class);
        when(config.getStorage()).thenReturn(testFolder.getRoot());

        long now = new Date().getTime();
        ExtensionJobHistoryRecord firstRecord =
            new ExtensionJobHistoryRecord("install", new InstallRequest(), null, null, new Date(now - 7000));
        ExtensionJobHistoryRecord secondRecord =
            new ExtensionJobHistoryRecord("uninstall", new UninstallRequest(), null, null, new Date(now + 4000));

        ExtensionJobHistory history = this.mocker.getComponentUnderTest();
        history.addRecord(firstRecord);
        history.addRecord(secondRecord);

        // Get all records.
        Predicate<ExtensionJobHistoryRecord> all =
            PredicateUtils.allPredicate(Collections.<Predicate<ExtensionJobHistoryRecord>>emptyList());
        assertEquals(Arrays.asList(secondRecord, firstRecord), history.getRecords(all, null, -1));

        // Limit the number of returned records.
        assertEquals(Arrays.asList(secondRecord), history.getRecords(all, null, 1));

        // Get records from offset.
        assertEquals(Arrays.asList(firstRecord), history.getRecords(all, secondRecord.getId(), -1));

        // Filter the records by job type.
        assertEquals(Arrays.asList(firstRecord), history.getRecords(new Predicate<ExtensionJobHistoryRecord>()
        {
            @Override
            public boolean evaluate(ExtensionJobHistoryRecord record)
            {
                return "install".equals(record.getJobType());
            }
        }, null, -1));
    }
}
