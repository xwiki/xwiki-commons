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
package org.apache.maven.repository.internal;

import java.util.List;

import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.RequestTrace;
import org.eclipse.aether.impl.ArtifactResolver;
import org.eclipse.aether.impl.RemoteRepositoryManager;
import org.eclipse.aether.impl.VersionRangeResolver;
import org.eclipse.aether.repository.RemoteRepository;

/**
 * Hack to be able to reuse {@link DefaultModelResolver}.
 *
 * @version $Id$
 * @since 6.2RC1
 */
public class PublicDefaultModelResolver extends DefaultModelResolver
{
    /**
     * @param session the session
     * @param trace the trace
     * @param context the context
     * @param resolver the resolver
     * @param versionRangeResolver the version range resolver
     * @param remoteRepositoryManager the remote repository manager
     * @param repositories the repositories
     * @see DefaultModelResolver#DefaultModelResolver(RepositorySystemSession, RequestTrace, String, ArtifactResolver,
     *      VersionRangeResolver, RemoteRepositoryManager, List)
     */
    public PublicDefaultModelResolver(RepositorySystemSession session, RequestTrace trace, String context,
        ArtifactResolver resolver, VersionRangeResolver versionRangeResolver,
        RemoteRepositoryManager remoteRepositoryManager, List<RemoteRepository> repositories)
    {
        super(session, trace, context, resolver, versionRangeResolver, remoteRepositoryManager, repositories);
    }
}
