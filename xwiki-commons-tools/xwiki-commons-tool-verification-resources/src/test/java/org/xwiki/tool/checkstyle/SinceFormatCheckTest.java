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
package org.xwiki.tool.checkstyle;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

/**
 * Unit tests for {@link SinceFormatCheck}.
 *
 * @version $Id$
 * @since 8.3
 */
class SinceFormatCheckTest extends AbstractModuleTestSupport
{
    private DefaultConfiguration checkConfig;

    @BeforeEach
    void setUp()
    {
        this.checkConfig = createModuleConfig(SinceFormatCheck.class);
    }

    @Test
    void checkWithMultipleSinceSeparatedByComma() throws Exception
    {
        final String[] expected = {
            "29:1: There must be only a single version per @since tag for "
            + "[org.xwiki.tool.checkstyle.test.TestClassWithNoSinceJavadocTagAtClassLevel]. Got [8.0, 6.0]."
        };

        verify(this.checkConfig, getPath("TestClassWithMultipleSinceSeparatedByComma.java"), expected);
    }

    @Test
    void checkWithMultipleSinceSeparatedBySlash() throws Exception
    {
        final String[] expected = {
            "29:5: There must be only a single version per @since tag for "
            + "[org.xwiki.tool.checkstyle.test.TestClassWithNoSinceJavadocTagAtClassLevel.something()]. Got [6.0/8.0]."
        };

        verify(this.checkConfig, getPath("TestClassWithMultipleSinceSeparatedBySlash.java"), expected);
    }

    @Test
    void checkVersionFormat() throws Exception
    {
        String formatError = "%s: The format of the version is <major>.<minor>.<bugfix>[RC<rc index>]"
            + " for [org.xwiki.tool.checkstyle.test.TestClassWithVariousSinceVersions.%s]. Got [%s].";

        // @formatter:off
        final String[] expected = {
            String.format(formatError, "59:5", "invalid1()", "16.0"),
            String.format(formatError, "67:5", "invalid2()", "17.0"),
            String.format(formatError, "75:5", "invalid3()", "17.0.0-rc-1")};
        // @formatter:on

        verify(this.checkConfig, getPath("TestClassWithVariousSinceVersions.java"), expected);
    }

    @Override
    protected String getPackageLocation()
    {
        return "org/xwiki/tool/checkstyle/test/since";
    }
}
