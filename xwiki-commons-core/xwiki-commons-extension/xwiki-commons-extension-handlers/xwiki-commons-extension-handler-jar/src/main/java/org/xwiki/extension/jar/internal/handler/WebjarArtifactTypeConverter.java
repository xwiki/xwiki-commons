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
package org.xwiki.extension.jar.internal.handler;

import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.extension.maven.ArtifactPackagingToExtensionType;
import org.xwiki.extension.maven.ArtifactTypeToExtensionType;

/**
 * Conversion to webjar extension type.
 * 
 * @version $Id$
 * @since 16.4.0RC1
 */
@Component
@Named("webjar")
@Singleton
public class WebjarArtifactTypeConverter
    implements ArtifactPackagingToExtensionType, ArtifactTypeToExtensionType
{
    @Override
    public String getExtensionType()
    {
        // The Maven file extension for packaging "webjar" is "jar" but in the context of XWiki extension we want webjar
        // to be recognized as being of a specific "webjar" type
        return "webjar";
    }
}
