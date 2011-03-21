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
package org.xwiki.test.integration;

import java.util.ArrayList;
import java.util.List;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * Starts/Stop XWiki before/after all tests and run all tests found in the current classloader using
 * <a href="http://www.johanneslink.net/projects/cpsuite.jsp">cpsuite</a> (we extend it).
 *
 * Tests can be filtered by passing the "pattern" System Property.
 *
 * @version $Id$
 * @since 3.0RC1
 */
public class XWikiExecutorSuite extends ClasspathSuite
{
    public static final String PATTERN = ".*" + System.getProperty("pattern", "");

    public XWikiExecutorSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError
    {
        super(klass, builder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Runner> getChildren()
    {
        List<Runner> runners = new ArrayList<Runner>();

        // Filter classes to run
        for (Runner runner : super.getChildren()) {
            if (runner.getDescription().getClassName().matches(PATTERN)) {
                runners.add(runner);
            }
        }

        return runners;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(RunNotifier notifier)
    {
        XWikiExecutor executor = new XWikiExecutor(0);

        try {
            executor.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start XWiki", e);
        }

        try {
            super.run(notifier);
        } finally {
            try {
                executor.stop();
            } catch (Exception e) {
                throw new RuntimeException("Failed to stop XWiki", e);
            }
        }
    }
}
