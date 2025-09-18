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
package org.xwiki.component;

import java.util.List;
import java.util.Map;

import org.xwiki.component.annotation.Component;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

/**
 * Class used in several tests.
 *
 * @version $Id$
 */
@Component
@Singleton
public class TestComponentWithProviders implements TestComponentRole
{
    @Inject
    public Provider<String> provider1;

    @Inject
    @Named("another")
    public Provider<String> provider12;

    @Inject
    public Provider<Integer> provider2;

    @Inject
    public Provider<List<TestRole>> providerList;

    @Inject
    public Provider<Map<String, TestRole>> providerMap;
}
