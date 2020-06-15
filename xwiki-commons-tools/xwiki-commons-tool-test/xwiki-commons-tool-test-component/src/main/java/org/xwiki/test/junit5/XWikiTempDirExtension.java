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
package org.xwiki.test.junit5;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.test.XWikiTempDirUtil;

/**
 * Allows injecting a temporary directory created inside the Maven target directory, in a similar way as it's done by
 * the default {@code @XWikiTemp} JUnit5 extension.
 *
 * Example usage:
 * <pre><code>
 *     &#064;XWikiTempDir
 *     private static File TEST_DIR;
 *     ...
 *     &#064;XWikiTempDir
 *     private File tmpDir;
 *     ...
 *     &#064;Test
 *     public void testXXX(@XWikiTempDir File tmpDir)...
 * </code></pre>
 *
 * @version $Id$
 * @since 11.7RC1
 */
public class XWikiTempDirExtension implements BeforeAllCallback, BeforeEachCallback, ParameterResolver
{
    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception
    {
        // Find all static fields annotated with @XWikiTempDir and inject a temporary directory in them.
        Class<?> testClass = extensionContext.getRequiredTestClass();
        for (Field field : ReflectionUtils.getAllFields(testClass)) {
            if (field.isAnnotationPresent(XWikiTempDir.class) && Modifier.isStatic(field.getModifiers())
                && File.class.isAssignableFrom(field.getType()))
            {
                boolean isAccessible = field.isAccessible();
                try {
                    field.setAccessible(true);
                    field.set(null, XWikiTempDirUtil.createTemporaryDirectory());
                } finally {
                    field.setAccessible(isAccessible);
                }
            }
        }
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext)
    {
        // Find all fields annotated with @XWikiTempDir and inject a temporary directory in them.
        if (extensionContext.getTestInstance().isPresent()) {
            Object testInstance = extensionContext.getTestInstance().get();
            for (Field field : ReflectionUtils.getAllFields(testInstance.getClass())) {
                if (field.isAnnotationPresent(XWikiTempDir.class) && !Modifier.isStatic(field.getModifiers())
                    && File.class.isAssignableFrom(field.getType()))
                {
                    ReflectionUtils.setFieldValue(testInstance, field.getName(),
                        XWikiTempDirUtil.createTemporaryDirectory());
                }
            }
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        return parameterContext.isAnnotated(XWikiTempDir.class)
            && File.class.isAssignableFrom(parameterContext.getParameter().getType());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
        throws ParameterResolutionException
    {
        return XWikiTempDirUtil.createTemporaryDirectory();
    }
}
