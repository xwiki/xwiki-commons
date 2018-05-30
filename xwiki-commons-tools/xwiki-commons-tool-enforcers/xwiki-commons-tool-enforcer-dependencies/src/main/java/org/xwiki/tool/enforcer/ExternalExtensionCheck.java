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
package org.xwiki.tool.enforcer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.enforcer.rule.api.EnforcerRuleHelper;
import org.apache.maven.model.Developer;
import org.apache.maven.model.Model;

/**
 * Enforce some POM values. There are 2 main goals:
 * <ul>
 *   <li>Ensure that non XWiki Core Committers extensions don't use reserved values</li>
 *   <li>Ensure that XWiki Core Committers extensions use best practices</li>
 * </ul>
 *
 * @version $Id$
 * @since 7.4RC1
 */
public class ExternalExtensionCheck extends AbstractPomCheck
{
    private static final String CORE_GROUP_ID = "org.xwiki";

    private static final String CORE_GROUP_ID_PREFIX = CORE_GROUP_ID + '.';

    private static final String COMMONS_GROUP_ID = CORE_GROUP_ID_PREFIX + "commons";

    private static final String RENDERING_GROUP_ID = CORE_GROUP_ID_PREFIX + "rendering";

    private static final String PLATFORM_GROUP_ID = CORE_GROUP_ID_PREFIX + "platform";

    private static final String ENTERPRISE_GROUP_ID = CORE_GROUP_ID_PREFIX + "enterprise";

    private static final String CONTRIB_GROUP_ID = CORE_GROUP_ID_PREFIX + "contrib";

    private static final String CONTRIB_GROUP_ID_PREFIX = CONTRIB_GROUP_ID + '.';

    private static final List<String> CORE_GROUP_IDS = createCoreGroupIdsList();

    private static final String COMMONS_ARTIFACT_ID_PREFIX = "xwiki-commons";

    private static final String RENDERING_ARTIFACT_ID_PREFIX = "xwiki-rendering";

    private static final String PLATFORM_ARTIFACT_ID_PREFIX = "xwiki-platform";

    private static final String ENTERPRISE_ARTIFACT_ID_PREFIX = "xwiki-enterprise";

    private static final List<String> CORE_ARTIFACT_ID_PREFIXES = Arrays.asList(COMMONS_ARTIFACT_ID_PREFIX,
        RENDERING_ARTIFACT_ID_PREFIX, PLATFORM_ARTIFACT_ID_PREFIX, ENTERPRISE_ARTIFACT_ID_PREFIX);

    private static final String CORE_DEVELOPERS = "XWiki Development Team";

    @Override
    public void execute(EnforcerRuleHelper helper) throws EnforcerRuleException
    {
        Model model = getResolvedModel(helper);

        if (isXWikiCoreCommitterExtension(model)) {
            checkCoreArtifactId(model);
        } else {
            checkNonCoreGroupId(model);
            checkNonCoreArtifactId(model);
            checkNonCoreDevelopers(model);
        }
    }

    private static List<String> createCoreGroupIdsList()
    {
        List<String> reservedGroupIds = new ArrayList<>();
        reservedGroupIds.add(COMMONS_GROUP_ID);
        reservedGroupIds.add(RENDERING_GROUP_ID);
        reservedGroupIds.add(PLATFORM_GROUP_ID);
        reservedGroupIds.add(ENTERPRISE_GROUP_ID);
        return reservedGroupIds;
    }

    private boolean isXWikiCoreCommitterExtension(Model model)
    {
        String groupId = model.getGroupId();
        if (CORE_GROUP_IDS.contains(groupId)) {
            return true;
        }
        return false;
    }

    /**
     * Make sure the group id does not start with "org.xwiki" or starts with "org.xwiki.contrib".
     */
    private void checkNonCoreGroupId(Model model) throws EnforcerRuleException
    {
        String groupId = model.getGroupId();
        if (groupId.equals(CORE_GROUP_ID) || (groupId.startsWith(CORE_GROUP_ID_PREFIX)
            && !groupId.equals(CONTRIB_GROUP_ID) && !groupId.startsWith(CONTRIB_GROUP_ID_PREFIX))) {
            throw new EnforcerRuleException(
                String.format("Contrib extension group id have to be prefixed with [%s]", CONTRIB_GROUP_ID));
        }
    }

    /**
     * Make sure that non core artifacts don't use reserved artifact prefixes.
     */
    private void checkNonCoreArtifactId(Model model) throws EnforcerRuleException
    {
        String artifactId = model.getArtifactId();
        for (String prefix : CORE_ARTIFACT_ID_PREFIXES) {
            if (artifactId.startsWith(prefix)) {
                throw new EnforcerRuleException("The [%s] artifact id prefix is reserved for XWiki Core Committers.");
            }
        }
    }

    private void checkCoreArtifactId(Model model) throws EnforcerRuleException
    {
        String groupId = model.getGroupId();
        String artifactId = model.getArtifactId();
        switch (groupId) {
            case COMMONS_GROUP_ID:
                checkArtifactId(groupId, artifactId, COMMONS_ARTIFACT_ID_PREFIX);
                break;
            case RENDERING_GROUP_ID:
                checkArtifactId(groupId, artifactId, RENDERING_ARTIFACT_ID_PREFIX);
                break;
            case PLATFORM_GROUP_ID:
                checkArtifactId(groupId, artifactId, PLATFORM_ARTIFACT_ID_PREFIX);
                break;
            case ENTERPRISE_GROUP_ID:
                checkArtifactId(groupId, artifactId, ENTERPRISE_ARTIFACT_ID_PREFIX);
                break;
            default:
                // Unhandled groupId, don't do anything
                break;
        }
    }

    private void checkArtifactId(String groupId, String artifactId, String prefix) throws EnforcerRuleException
    {
        if (!artifactId.startsWith(prefix)) {
            throw new EnforcerRuleException(String.format(
                "Artifact Id must start with [%s] for group Id [%s] but found [%s]", prefix, groupId, artifactId));
        }
    }

    private void checkNonCoreDevelopers(Model model) throws EnforcerRuleException
    {
        List developers = model.getDevelopers();
        if (developers.size() == 1) {
            String developerName = ((Developer) developers.get(0)).getName();

            if (developerName == null) {
                throw new EnforcerRuleException(
                    "You must define <developers> section as otherwise your Extension will have no author");
            } else if (developerName.equals(CORE_DEVELOPERS)) {
                throw new EnforcerRuleException(String.format("You must override the <developers> section as otherwise "
                    + "your Extension will be considered as developed by the [%s]", CORE_DEVELOPERS));
            }
        }
    }
}
