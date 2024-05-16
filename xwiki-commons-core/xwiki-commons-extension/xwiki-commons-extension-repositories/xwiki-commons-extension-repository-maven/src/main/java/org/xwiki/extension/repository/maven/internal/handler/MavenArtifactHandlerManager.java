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
package org.xwiki.extension.repository.maven.internal.handler;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;

/**
 * Helper to manipulate {@link ArtifactHandler}s.
 * 
 * @version $Id$
 * @since 16.4.0RC1
 */
@Component(roles = MavenArtifactHandlerManager.class)
@Singleton
public class MavenArtifactHandlerManager extends MavenArtifactHandlers implements Initializable
{
    @Inject
    private Provider<PlexusContainer> plexusProvider;

    @Override
    public void initialize() throws InitializationException
    {
        Map<String, ArtifactHandler> handlers;
        try {
            handlers = this.plexusProvider.get().lookupMap(ArtifactHandler.class);
        } catch (ComponentLookupException e) {
            throw new InitializationException("Failed to load standard Maven artifact handlers", e);
        }

        add(handlers);
    }
}
