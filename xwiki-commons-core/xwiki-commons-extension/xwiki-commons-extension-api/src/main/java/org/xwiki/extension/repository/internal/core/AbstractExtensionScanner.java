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
package org.xwiki.extension.repository.internal.core;

import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * Various base utilities for {@link ExtensionScanner} implementations.
 * 
 * @version $Id$
 * @since 9.0RC1
 */
public abstract class AbstractExtensionScanner implements ExtensionScanner
{
    @Inject
    protected Logger logger;

    protected void addCoreExtension(Map<String, DefaultCoreExtension> extensions, DefaultCoreExtension coreExtension)
    {
        DefaultCoreExtensionScanner.addCoreExtension(extensions, coreExtension, this.logger);
    }
}
