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

import java.io.FileReader;
import java.io.Reader;

import javax.inject.Inject;

import org.apache.maven.enforcer.rule.api.AbstractEnforcerRule;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;

/**
 * Allows to write Enforcer Rules that perform checks on the POM.
 *
 * @version $Id$
 * @since 7.4RC1
 */
public abstract class AbstractPomCheck extends AbstractEnforcerRule
{
    @Inject
    protected MavenProject project;

    /**
     * The Maven model as it's present in the project's {@code pom.xml} (non resolved).
     *
     * @return the Model instance for the current Maven project (this contains the raw data from the pom.xml file before
     *         any interpolation)
     * @throws EnforcerRuleException if an error occurred getting the Model instance
     */
    protected Model getModel() throws EnforcerRuleException
    {
        Model model;
        try (Reader reader = new FileReader(this.project.getFile())) {
            MavenXpp3Reader xpp3Reader = new MavenXpp3Reader();
            model = xpp3Reader.read(reader);
        } catch (Exception e) {
            throw new EnforcerRuleException("Failed to read pom file [" + this.project.getFile() + "]", e);
        }

        return model;
    }

    /**
     * The resolved Maven model (i.e. with parent poms taken into account).
     *
     * @return the resolved Model instance for the current Maven project (this contains the data from the pom.xml file
     *         after interpolation)
     * @throws EnforcerRuleException if an error occurred getting the Model instance
     */
    protected Model getResolvedModel() throws EnforcerRuleException
    {
        // Note: the model is resolved at this point, which means the Model contains
        return this.project.getModel();
    }
}
