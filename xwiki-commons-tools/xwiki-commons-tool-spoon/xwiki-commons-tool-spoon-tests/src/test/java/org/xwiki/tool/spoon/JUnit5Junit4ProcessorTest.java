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
package org.xwiki.tool.spoon;

import org.junit.jupiter.api.Test;

import spoon.Launcher;
import spoon.SpoonException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link JUnit5JUnit4Processor}.
 *
 * @version $Id$
 */
public class JUnit5Junit4ProcessorTest
{
    @Test
    void process()
    {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.setArgs(new String[] {"--output-type", "nooutput" });
        launcher.addInputResource("./src/test/java/org/xwiki/tool/spoon/junit5junit4/");

        JUnit5JUnit4Processor processor = new JUnit5JUnit4Processor();
        launcher.addProcessor(processor);

        Throwable exception = assertThrows(SpoonException.class, () -> {
            launcher.run();
        });
        assertThat(exception.getMessage(), matchesPattern("\\QThe following errors were found:\\E\n"
            + "\\Q- There's a mix of JUnit4 and JUnit5 APIs at \\E(.*)\n"
        ));
    }
}
