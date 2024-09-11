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
package org.xwiki.extension.internal.converter;

import java.net.MalformedURLException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.xwiki.extension.ExtensionSupportPlan;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.xwiki.extension.DefaultExtensionSupportPlansTest.plan;

/**
 * Validate {@link ExtensionSupportPlanConverter} component.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class ExtensionSupportPlanConverterTest
{
    @InjectMockComponents
    private DefaultConverterManager manager;

    private void assertFromString(String supporter, String supporterURL, String plan, String planURL, boolean paying,
        String input)
    {
        assertEquals(plan(supporter, supporterURL, plan, planURL, paying),
            this.manager.convert(ExtensionSupportPlan.class, input));
    }

    private void assertToString(String expect, String supporter, String supporterURL, String plan, String planURL,
        boolean paying)
    {
        assertEquals(expect, this.manager.getConverter(ExtensionSupportPlan.class).convert(String.class,
            plan(supporter, supporterURL, plan, planURL, paying)));
    }

    @Test
    void convertFromString()
    {
        assertNull(this.manager.convert(ExtensionSupportPlan.class, null));
        assertFromString("", null, null, null, false, "");

        assertFromString("supporter", null, null, null, false, "supporter");
        assertFromString("supporter", "http://supporter", "name", "http://host", true,
            "supporter/http:\\/\\/supporter/name/http:\\/\\/host/true");
        assertFromString("supporter", null, "id\\", "http://host", false, "supporter//id\\\\/http:\\/\\/host");

        assertFromString("supporter", null, "name/url", null, false, "supporter//name\\/url");
        assertFromString("supporter", null, "name//url", null, false, "supporter//name\\/\\/url");
        assertFromString("supporter", null, "/url", null, false, "supporter//\\/url");
        assertFromString("supporter", null, "name\\/url", null, false, "supporter//name\\\\\\/url");
    }

    @Test
    void convertToString()
    {
        assertToString("////false", "", null, null, null, false);
        assertToString("/http:\\/\\/supporter//http:\\/\\/host/false", null, "http://supporter", null, "http://host",
            false);

        assertToString("supporter//name//false", "supporter", null, "name", null, false);
        assertToString("supporter/http:\\/\\/supporter/name/http:\\/\\/host/false", "supporter", "http://supporter",
            "name", "http://host", false);
        assertToString("supporter\\/http:\\/\\/supporter//name\\/http:\\/\\/host//false", "supporter/http://supporter",
            null, "name/http://host", null, false);
    }

    @Test
    void toExtensionSupportPlan() throws MalformedURLException
    {
        assertEquals(plan("supporter1", "http://supporter1", "name1", "http://host1", true),
            ExtensionSupportPlanConverter
                .toExtensionSupportPlan("supporter1/http:\\/\\/supporter1/name1/http:\\/\\/host1/true"));
    }

    @Test
    void toExtensionSupportPlanList() throws MalformedURLException
    {
        assertEquals(
            List.of(plan("supporter1", "http://supporter1", "name1", "http://host1", true),
                plan("supporter2", "http://supporter2", "name2", "http://host2", false)),
            ExtensionSupportPlanConverter
                .toExtensionSupportPlanList(List.of("supporter1/http:\\/\\/supporter1/name1/http:\\/\\/host1/true",
                    "supporter2/http:\\/\\/supporter2/name2/http:\\/\\/host2/false")));
    }
}
