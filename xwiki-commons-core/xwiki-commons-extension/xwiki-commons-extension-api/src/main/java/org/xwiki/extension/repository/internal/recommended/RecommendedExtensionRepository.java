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
package org.xwiki.extension.repository.internal.recommended;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.RemoteExtension;
import org.xwiki.extension.repository.AbstractFilteredExtensionRepository;
import org.xwiki.extension.repository.DefaultExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.search.ExtensionQuery.COMPARISON;

/**
 * A proxy repository which only take into account recommended extensions.
 * 
 * @version $Id$
 * @since 8.3RC1
 */
@Component(roles = ExtensionRepository.class)
@Named(RecommendedExtensionRepository.ID)
@Singleton
public class RecommendedExtensionRepository extends AbstractFilteredExtensionRepository implements Initializable
{
    /**
     * The identifier of the sub extension {@link org.xwiki.script.service.ScriptService}.
     */
    public static final String ID = "recommended";

    @Override
    public void initialize() throws InitializationException
    {
        // Set descriptor
        setDescriptor(new DefaultExtensionRepositoryDescriptor(ID));

        // Filter recommended extensions
        addFilter(RemoteExtension.FIELD_RECOMMENDED, true, COMPARISON.EQUAL);
    }
}
