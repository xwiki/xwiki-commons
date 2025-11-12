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
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;

/**
 * Unit tests for {@link UnstableAnnotationCheck}.
 *
 * @version $Id$
 * @since 8.1M1
 */
class UnstableAnnotationCheckTest extends AbstractModuleTestSupport
{
    private DefaultConfiguration checkConfig;

    @BeforeEach
    void setUp()
    {
        this.checkConfig = createModuleConfig(UnstableAnnotationCheck.class);
        this.checkConfig.addProperty("currentVersion", "8.1-SNAPSHOT");
    }

    @Test
    void checkWithNoSinceJavadocTagAtClassLevel() throws Exception
    {
        final String[] expected = {
            "24:1: There is an @Unstable annotation for [org.xwiki.tool.checkstyle.test."
            + "TestClassWithNoSinceJavadocTagAtClassLevel] but the @since javadoc tag is missing, you must add it!"
        };

        verify(this.checkConfig, getPath("TestClassWithNoSinceJavadocTagAtClassLevel.java"), expected);
    }

    @Test
    void checkWithNoSinceJavadocTagAtMethodLevel() throws Exception
    {
        final String[] expected = {
            "26:5: There is an @Unstable annotation for [org.xwiki.tool.checkstyle.test."
            + "TestClassWithNoSinceJavadocTagAtMethodLevel.method()] but the @since javadoc tag is missing, you must "
            + "add it!"
        };

        verify(this.checkConfig, getPath("TestClassWithNoSinceJavadocTagAtMethodLevel.java"), expected);
    }

    @Test
    void checkWithUnstableOkAtClassLevel() throws Exception
    {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verify(this.checkConfig, getPath("TestClassWithUnstableOkAtClassLevel.java"), expected);
    }

    @Test
    public void checkWithUnstableOkAtMethodLevel() throws Exception
    {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verify(this.checkConfig, getPath("TestClassWithUnstableOkAtMethodLevel.java"), expected);
    }

    @Test
    void checkWithUnstableAnnotationShouldBeRemoved() throws Exception
    {
        final String[] expected = {
            "29:1: The @Unstable annotation for [org.xwiki.tool.checkstyle.test."
            + "TestClassWithUnstableAnnotationShouldBeRemoved] must be removed since it's been there for more than a "
            + "full development cycle (was introduced in [6.0] and current version is [8.1-SNAPSHOT])"
        };

        verify(this.checkConfig, getPath("TestClassWithUnstableAnnotationShouldBeRemoved.java"), expected);
    }

    @Test
    void checkWithUnstableAnnotationShouldBeRemovedMultipleSince() throws Exception
    {
        final String[] expected = {
            "30:1: The @Unstable annotation for [org.xwiki.tool.checkstyle.test."
            + "TestClassWithUnstableAnnotationShouldBeRemovedMultipleSince] must be removed since it's been there for "
            + "more than a full development cycle (was introduced in [6.0, 5.0] and current version is [8.1-SNAPSHOT])"
        };

        verify(this.checkConfig, getPath("TestClassWithUnstableAnnotationShouldBeRemovedMultipleSince.java"), expected);
    }

    @Test
    void checkWithUnstableAnnotationShouldNotBeRemovedMultipleSince() throws Exception
    {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verify(this.checkConfig, getPath("TestClassWithUnstableAnnotationShouldNotBeRemovedMultipleSince.java"),
            expected);
    }

    @Test
    void checkWithUnstableAnnotationShouldBeRemovedAtMethodLevel() throws Exception
    {
        final String[] expected = {
            "29:5: The @Unstable annotation for [org.xwiki.tool.checkstyle.test."
                + "TestClassWithUnstableAnnotationShouldBeRemovedAtMethodLevel.method()] must be removed since it's "
                + "been there for more than a full development cycle (was introduced in [6.0] and current version is "
                + "[8.1-SNAPSHOT])"
        };

        verify(this.checkConfig, getPath("TestClassWithUnstableAnnotationShouldBeRemovedAtMethodLevel.java"), expected);
    }

    @Test
    void checkWithUnstableAnnotationShouldBeRemovedAtConstructorLevel() throws Exception
    {
        final String[] expected = {
            "29:5: The @Unstable annotation for [org.xwiki.tool.checkstyle.test."
            + "TestClassWithUnstableAnnotationShouldBeRemovedAtMethodLevel] must be removed since it's been there for"
            + " more than a full development cycle (was introduced in [6.0] and current version is [8.1-SNAPSHOT])"
        };

        verify(this.checkConfig, getPath("TestClassWithUnstableAnnotationShouldBeRemovedAtConstructorLevel.java"),
            expected);
    }

    @Test
    void checkPackageWithUnstable() throws Exception
    {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verify(this.checkConfig, getPath("TestPackageWithUnstable.java"), expected);
    }

    @Test
    void checkPackageWithOtherAnnotation() throws Exception
    {
        final String[] expected = CommonUtil.EMPTY_STRING_ARRAY;

        verify(this.checkConfig, getPath("TestPackageWithOtherAnnotation.java"), expected);
    }

    @Test
    void checkWithUnstableAtStaticFieldLevel() throws Exception
    {
        final String[] expected = {
            "29:5: The @Unstable annotation for [org.xwiki.tool.checkstyle.test."
                + "TestClassWithUnstableAtStaticFieldLevel.SOMETHING()] must be removed since it's been there for more "
                + "than a full development cycle (was introduced in [6.0] and current version is [8.1-SNAPSHOT])"
        };

        verify(this.checkConfig, getPath("TestClassWithUnstableAtStaticFieldLevel.java"), expected);
    }

    @Test
    void checkWithUnstableAtEnumLevel() throws Exception
    {
        final String[] expected = {
            "27:1: The @Unstable annotation for [org.xwiki.tool.checkstyle.test.TestEnumWithUnstable] must be removed "
            + "since it's been there for more than a full development cycle (was introduced in [6.0] and current "
            + "version is [8.1-SNAPSHOT])",
            "37:5: The @Unstable annotation for [org.xwiki.tool.checkstyle.test.TestEnumWithUnstable.OTHER()] "
                + "must be removed since it's been there for more than a full development cycle "
                + "(was introduced in [6.1.0] and current version is [8.1-SNAPSHOT])"
        };

        verify(this.checkConfig, getPath("TestEnumWithUnstable.java"), expected);
    }

    @Test
    void checkWithUnstableAtRecordLevel() throws Exception
    {
        final String[] expected = {
            "27:1: The @Unstable annotation for [org.xwiki.tool.checkstyle.test.TestRecordWithUnstable] must be "
                + "removed since it's been there for more than a full development cycle (was introduced in [6.0.0] and "
                + "current version is [8.1-SNAPSHOT])"
        };

        verify(this.checkConfig, getPath("TestRecordWithUnstable.java"), expected);
    }

    @Override
    protected String getPackageLocation()
    {
        return "org/xwiki/tool/checkstyle/test/unstable";
    }
}
