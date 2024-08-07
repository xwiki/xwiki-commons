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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.xwiki.stability.Unstable;

/**
 * The support plans associated with an extension.
 * 
 * @version $Id$
 * @since 16.7.0RC1
 */
@Unstable
public class DefaultExtensionSupportPlans implements ExtensionSupportPlans
{
    /**
     * @see #getSupportPlans()
     */
    protected final List<ExtensionSupportPlan> supportPlans;

    protected final Map<ExtensionSupporter, List<ExtensionSupportPlan>> supporters;

    /**
     * @param supportPlans the support plans
     */
    public DefaultExtensionSupportPlans(Collection<? extends ExtensionSupportPlan> supportPlans)
    {
        this.supportPlans = List.copyOf(supportPlans);
        this.supporters = this.supportPlans.stream().collect(
            Collectors.groupingBy(ExtensionSupportPlan::getSupporter, Collectors.mapping(p -> p, Collectors.toList())));
    }

    @Override
    public List<ExtensionSupportPlan> getSupportPlans()
    {
        return this.supportPlans;
    }

    @Override
    public Set<ExtensionSupporter> getSupporters()
    {
        return this.supporters.keySet();
    }

    @Override
    public List<ExtensionSupportPlan> getSupportPlans(ExtensionSupporter supporter)
    {
        return this.supporters.getOrDefault(supporter, List.of());
    }
}
