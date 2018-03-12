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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xwiki.extension.Extension;
import org.xwiki.extension.ExtensionFeaturesInjector;
import org.xwiki.extension.ExtensionId;

/**
 * Make sure all standard wejars expose all three possible groupid so that they don't end up duplicated.
 * 
 * @version $Id$
 * @since 10.2RC1
 */
public class WebjarExtensionFeaturesInjector implements ExtensionFeaturesInjector
{
    private static final String WEBJAR_CLASS = "org.webjars";

    private static final String WEBJAR_NPM = "org.webjars.npm";

    private static final String WEBJAR_BOWER = "org.webjars.bower";

    private static final Map<String, List<String>> MAPPING = new HashMap<>();

    static {
        MAPPING.put(WEBJAR_CLASS, Arrays.asList(WEBJAR_NPM, WEBJAR_BOWER));
        MAPPING.put(WEBJAR_NPM, Arrays.asList(WEBJAR_CLASS, WEBJAR_BOWER));
        MAPPING.put(WEBJAR_BOWER, Arrays.asList(WEBJAR_CLASS, WEBJAR_NPM));
    }

    @Override
    public Collection<ExtensionId> getFeatures(Extension extension)
    {
        for (Map.Entry<String, List<String>> entry : MAPPING.entrySet()) {
            if (extension.getId().getId().startsWith(entry.getKey() + ':')) {
                List<ExtensionId> features = new ArrayList<>(entry.getValue().size());
                for (String idPrefix : entry.getValue()) {
                    features
                        .add(new ExtensionId(idPrefix + extension.getId().getId().substring(entry.getKey().length()),
                            extension.getId().getVersion()));
                }

                return features;
            }
        }

        return null;
    }
}
