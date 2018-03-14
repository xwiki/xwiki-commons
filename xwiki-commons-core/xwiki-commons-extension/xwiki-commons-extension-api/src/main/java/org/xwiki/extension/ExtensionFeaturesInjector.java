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

/**
 * Allow injecting features in extensions whiles they are loaded from a repository. This is useful for extension you
 * don't control comming from various repositories. A good example is various group ids (npm, bower, classic) used for
 * webjar extensions which are the same thing from XWiki point of view.
 * 
 * @version $Id$
 * @since 10.2RC1
 */
public interface ExtensionFeaturesInjector
{
    /**
     * @param extension the extension for which to inject additional features
     * @return the additional features that should be added to this extension
     */
    Collection<ExtensionId> getFeatures(Extension extension);
}
