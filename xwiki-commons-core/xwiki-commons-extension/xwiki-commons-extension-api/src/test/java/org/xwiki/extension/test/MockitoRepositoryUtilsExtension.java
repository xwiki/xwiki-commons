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
package org.xwiki.extension.test;

import java.lang.reflect.Field;

import javax.inject.Inject;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.xwiki.component.util.ReflectionUtils;

import static org.xwiki.test.junit5.mockito.MockitoComponentManagerExtension.loadComponentManager;

/**
 * Must be used after the {@link org.xwiki.test.junit5.mockito.MockitoComponentManagerExtension} since it relies on
 * the ComponentManager being set up and saved in the Extension Context Store.
 *
 * @version $Id$
 */
public class MockitoRepositoryUtilsExtension implements BeforeEachCallback
{
    @Override
    public void beforeEach(ExtensionContext context) throws Exception
    {
        Object testInstance = context.getTestInstance().get();
        MockitoRepositoryUtils utils = new MockitoRepositoryUtils(loadComponentManager(context));

        // Initialize the MockitoRepositoryUtils instance
        utils.setup();

        // Inject the MockitoRepositoryUtils instance
        for (Field field : ReflectionUtils.getAllFields(testInstance.getClass())) {
            if (field.getType() == MockitoRepositoryUtils.class && field.isAnnotationPresent(Inject.class)) {
                ReflectionUtils.setFieldValue(testInstance, field.getName(), utils);
            }
        }
    }
}
