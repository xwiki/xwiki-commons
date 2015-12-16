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
package org.xwiki.script.internal.safe;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

/**
 * Provide safe Map.
 *
 * @version $Id$
 * @since 4.0M2
 */
@Component
@Singleton
@SuppressWarnings("rawtypes")
public class MapScriptSafeProvider implements ScriptSafeProvider<Map>
{
    /**
     * Used to provide collection elements safe versions.
     */
    @Inject
    private ScriptSafeProvider safeProvider;

    @Override
    public <S> S get(Map unsafe)
    {
        Map safe;

        if (unsafe instanceof LinkedHashMap) {
            safe = new LinkedHashMap(unsafe.size());
        } else {
            safe = new HashMap(unsafe.size());
        }

        for (Map.Entry entry : (Set<Map.Entry>) unsafe.entrySet()) {
            safe.put(this.safeProvider.get(entry.getKey()), this.safeProvider.get(entry.getValue()));
        }

        return (S) safe;
    }
}
