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

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.xwiki.extension.ExtensionId;
import org.xwiki.extension.repository.internal.local.DefaultLocalExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Validate {@link WebjarExtensionFeaturesInjector}.
 * 
 * @version $Id$
 */
public class WebjarExtensionFeaturesInjectorTest
{
    @Test
    public void getExtensionFeatures()
    {
        WebjarExtensionFeaturesInjector injector = new WebjarExtensionFeaturesInjector();

        DefaultLocalExtension extension =
            new DefaultLocalExtension(null, new ExtensionId("org.webjars:test", "version"), JarExtensionHandler.JAR);

        assertEquals(Arrays.asList(new ExtensionId("org.webjars.npm:test", "version"),
            new ExtensionId("org.webjars.bower:test", "version")), injector.getFeatures(extension));
    }
}
