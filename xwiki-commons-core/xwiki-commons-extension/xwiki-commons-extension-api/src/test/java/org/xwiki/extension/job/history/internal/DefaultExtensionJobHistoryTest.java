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

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.collections4.Predicate;
import org.apache.commons.collections4.PredicateUtils;
import org.junit.jupiter.api.Test;
import org.xwiki.extension.job.InstallRequest;
import org.xwiki.extension.job.UninstallRequest;
import org.xwiki.extension.job.history.ExtensionJobHistoryConfiguration;
import org.xwiki.extension.job.history.ExtensionJobHistoryRecord;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.XWikiTempDir;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DefaultExtensionJobHistory}.
 * 
 * @version $Id$
 * @since 7.1RC1
 */
@ComponentTest
public class DefaultExtensionJobHistoryTest
{
    @XWikiTempDir
    private static File TMP_DIRECTORY;

    @InjectMockComponents
    private DefaultExtensionJobHistory history;

    @MockComponent
    private ExtensionJobHistoryConfiguration configuration;

    @BeforeComponent
    public void setupConfiguration()
    {
        // Needed in @BeforeComponent because it's used in the DefaultExtensionJobHistory initialize() method.
        when(this.configuration.getStorage()).thenReturn(TMP_DIRECTORY);
    }

    @Test
    public void addGetRecords()
    {
        long now = new Date().getTime();
        ExtensionJobHistoryRecord firstRecord =
            new ExtensionJobHistoryRecord("install", new InstallRequest(), null, null, new Date(now - 7000));
        ExtensionJobHistoryRecord secondRecord =
            new ExtensionJobHistoryRecord("uninstall", new UninstallRequest(), null, null, new Date(now + 4000));

        this.history.addRecord(firstRecord);
        this.history.addRecord(secondRecord);

        // Get all records.
        Predicate<ExtensionJobHistoryRecord> all =
            PredicateUtils.allPredicate(Collections.<Predicate<ExtensionJobHistoryRecord>>emptyList());
        assertEquals(Arrays.asList(secondRecord, firstRecord), this.history.getRecords(all, null, -1));

        // Limit the number of returned records.
        assertEquals(Arrays.asList(secondRecord), this.history.getRecords(all, null, 1));

        // Get records from offset.
        assertEquals(Arrays.asList(firstRecord), this.history.getRecords(all, secondRecord.getId(), -1));

        // Filter the records by job type.
        assertEquals(Arrays.asList(firstRecord), this.history.getRecords(new Predicate<ExtensionJobHistoryRecord>()
        {
            @Override
            public boolean evaluate(ExtensionJobHistoryRecord record)
            {
                return "install".equals(record.getJobType());
            }
        }, null, -1));
    }
}
