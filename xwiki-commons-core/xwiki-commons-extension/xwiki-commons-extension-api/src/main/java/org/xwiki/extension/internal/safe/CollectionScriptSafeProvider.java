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
package org.xwiki.extension.internal.safe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

/**
 * Provide safe Collection.
 *
 * @version $Id$
 * @since 4.0M2
 */
@Component
@Singleton
@SuppressWarnings("rawtypes")
public class CollectionScriptSafeProvider implements ScriptSafeProvider<Collection>
{
    /**
     * Used to provide collection elements safe versions.
     */
    @Inject
    private ScriptSafeProvider safeProvider;

    @Override
    public <S> S get(Collection unsafe)
    {
        Collection safe;

        if (unsafe instanceof Set) {
            if (unsafe instanceof LinkedHashSet) {
                safe = new LinkedHashSet(unsafe.size());
            } else {
                safe = new HashSet(unsafe.size());
            }
        } else {
            safe = new ArrayList(unsafe.size());
        }

        for (Object unsafeElement : unsafe) {
            safe.add(this.safeProvider.get(unsafeElement));
        }

        return (S) safe;
    }
}
