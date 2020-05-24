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
package org.xwiki.diff.display.internal;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.diff.DiffConfiguration;
import org.xwiki.diff.DiffManager;
import org.xwiki.diff.DiffResult;
import org.xwiki.diff.MergeConfiguration;
import org.xwiki.diff.MergeResult;
import org.xwiki.diff.display.UnifiedDiffBlock;
import org.xwiki.diff.internal.DefaultDiffManager;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Integration tests on {@link DefaultUnifiedDiffDisplayer} that performs check by reading on files,
 * performs merge and diff using {@link DefaultDiffManager} and checks the result of the unified diff.
 *
 * @since 12.3RC1
 * @since 11.10.5
 * @version $Id$
 */
@ComponentTest
@ComponentList(DefaultDiffManager.class)
class DefaultUnifiedDiffDisplayerIntegrationTest
{
    @InjectMockComponents
    private DefaultUnifiedDiffDisplayer defaultUnifiedDiffDisplayer;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @Test
    void display() throws Exception
    {
        List<String> previous = Files.readAllLines(new File("src/test/resources/integration1/previous.txt").toPath());
        List<String> current = Files.readAllLines(new File("src/test/resources/integration1/current.txt").toPath());
        List<String> next = Files.readAllLines(new File("src/test/resources/integration1/next.txt").toPath());

        DiffManager diffManager = this.componentManager.getInstance(DiffManager.class);
        MergeResult<String> mergeResult = diffManager.merge(previous, next, current, new MergeConfiguration<>());
        assertEquals(4, mergeResult.getConflicts().size());

        DiffResult<String> diffResult = diffManager.diff(current, mergeResult.getMerged(), new DiffConfiguration<>());
        List<UnifiedDiffBlock<String, Object>> display =
            this.defaultUnifiedDiffDisplayer.display(diffResult, mergeResult.getConflicts());
        assertEquals(9, display.size());
    }
}
