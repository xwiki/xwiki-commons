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
package org.xwiki.extension;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validate {@link DefaultExtensionSupportPlans}.
 * 
 * @version $Id$
 */
public class DefaultExtensionSupportPlansTest
{
    public static ExtensionSupporter supporter(String name, String url)
    {
        try {
            return new DefaultExtensionSupporter(name, url != null ? new URL(url) : null);
        } catch (MalformedURLException e) {
            throw new AssertionFailedError(null, e);
        }
    }

    public static ExtensionSupportPlan plan(String supporter, String supporterURL, String plan, String planURL,
        boolean paying)
    {
        try {
            return new DefaultExtensionSupportPlan(supporter(supporter, supporterURL), plan,
                planURL != null ? new URL(planURL) : null, paying);
        } catch (MalformedURLException e) {
            throw new AssertionFailedError(null, e);
        }
    }

    public static final ExtensionSupporter SUPPORTER_0 = supporter("supporter0", "http://supporter0");

    public static final ExtensionSupporter SUPPORTER_1 = supporter("supporter1", "http://supporter1");

    public static final ExtensionSupporter SUPPORTER_2 = supporter("supporter2", "http://supporter2");

    public static final ExtensionSupportPlan PLAN_00 =
        plan("supporter0", "http://supporter0", "plan00", "http://plan00", false);

    public static final ExtensionSupportPlan PLAN_01 =
        plan("supporter0", "http://supporter0", "plan01", "http://plan01", true);

    public static final ExtensionSupportPlan PLAN_1 =
        plan("supporter1", "http://supporter1", "plan1", "http://plan1", false);

    public static final ExtensionSupportPlan PLAN_2 =
        plan("supporter2", "http://supporter2", "plan2", "http://plan2", true);

    public static final List<ExtensionSupportPlan> PLANS_ALL = List.of(PLAN_00, PLAN_01, PLAN_1, PLAN_2);

    public static final List<ExtensionSupportPlan> PLANS_0 = List.of(PLAN_00, PLAN_01);

    public static final Set<ExtensionSupporter> SUPPORTERS = Set.of(SUPPORTER_0, SUPPORTER_1, SUPPORTER_2);

    @Test
    void getSupportPlans()
    {
        DefaultExtensionSupportPlans plans = new DefaultExtensionSupportPlans(PLANS_ALL);

        assertEquals(PLANS_ALL, plans.getSupportPlans());
    }

    @Test
    void getSupportPlansForSupporter()
    {
        DefaultExtensionSupportPlans plans = new DefaultExtensionSupportPlans(List.of());

        assertEquals(Set.of(), plans.getSupporters());

        plans = new DefaultExtensionSupportPlans(PLANS_ALL);

        assertEquals(SUPPORTERS, plans.getSupporters());
    }

    @Test
    void getSupporters()
    {
        DefaultExtensionSupportPlans plans = new DefaultExtensionSupportPlans(List.of());

        assertEquals(List.of(), plans.getSupportPlans(supporter("supporter", null)));

        plans = new DefaultExtensionSupportPlans(PLANS_ALL);

        assertEquals(PLANS_0, plans.getSupportPlans(SUPPORTER_0));
        assertEquals(List.of(PLAN_1), plans.getSupportPlans(SUPPORTER_1));
        assertEquals(List.of(PLAN_2), plans.getSupportPlans(SUPPORTER_2));
    }
}
