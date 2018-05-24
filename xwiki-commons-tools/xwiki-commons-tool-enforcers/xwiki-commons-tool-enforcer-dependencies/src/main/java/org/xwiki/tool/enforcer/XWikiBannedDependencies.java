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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.plugins.enforcer.AbstractVersionEnforcer;
import org.apache.maven.plugins.enforcer.BannedDependencies;

/**
 * Same as {@link BannedDependencies} but compares groupId and artifactId using regexes.
 * 
 * @version $Id$
 * @since 3.2RC1
 */
public class XWikiBannedDependencies extends BannedDependencies
{
    private static final String STAR = "*";

    @Override
    protected boolean compareDependency(String pattern, Artifact artifact) throws EnforcerRuleException
    {
        String[] patternParts = pattern.split(":", 7);

        return compareDependency(patternParts, artifact);
    }

    private boolean compareDependency(String[] pattern, Artifact artifact) throws EnforcerRuleException
    {
        boolean result = false;
        if (pattern.length > 0) {
            result = artifact.getGroupId().matches(pattern[0]);
        }

        if (result && pattern.length > 1) {
            result = artifact.getArtifactId().matches(pattern[1]);
        }

        result = handleTwoPartsOrMore(result, pattern, artifact);

        result = handleThreePartsOrMore(result, pattern, artifact);

        result = handleFourPartsOrMore(result, pattern, artifact);

        return result;
    }

    private boolean handleTwoPartsOrMore(boolean result, String[] pattern, Artifact artifact)
        throws EnforcerRuleException
    {
        boolean newresult = result;
        if (result && pattern.length > 2) {
            // short circuit if the versions are exactly the same
            if (STAR.equals(pattern[2]) || artifact.getVersion().equals(pattern[2])) {
                newresult = true;
            } else {
                try {
                    newresult = AbstractVersionEnforcer.containsVersion(VersionRange.createFromVersionSpec(pattern[2]),
                        new DefaultArtifactVersion(artifact.getBaseVersion()));
                } catch (InvalidVersionSpecificationException e) {
                    throw new EnforcerRuleException("Invalid Version Range: ", e);
                }
            }
        }
        return newresult;
    }

    private boolean handleThreePartsOrMore(boolean result, String[] pattern, Artifact artifact)
    {
        boolean newresult = result;
        if (result && pattern.length > 3) {
            String type = artifact.getType();
            if (type == null || type.equals("")) {
                type = "jar";
            }
            newresult = STAR.equals(pattern[3]) || type.equals(pattern[3]);
        }
        return newresult;
    }

    private boolean handleFourPartsOrMore(boolean result, String[] pattern, Artifact artifact)
    {
        boolean newresult = result;
        if (result && pattern.length > 4) {
            String scope = artifact.getScope();
            if (scope == null || scope.equals("")) {
                scope = "compile";
            }
            newresult = STAR.equals(pattern[4]) || scope.equals(pattern[4]);
        }
        return newresult;
    }
}
