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
package org.xwiki.test.isolation;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Specialized JUnit4 runner to isolate some classes during the execution of a test class.
 * This runner should be used in combination with the {@link IsolatedClassPrefix} annotation.
 * <p>
 * Isolation is helpful when you do not want to pollute the application ClassLoader with some
 * classes under test, or some dynamically loaded classes during a test. It could be used to
 * reinitialize statics and drop out those dynamically loaded classes after the test.
 * <p>
 * To use this class, define a JUnit {@code @RunWith} annotation on your test class and also
 * add a {@link IsolatedClassPrefix} annotation to define the list of class prefixes that should
 * be isolated from the rest of your tests.
 * <p>
 * For example:
 * <pre>{@code
 * &#64;RunWith(IsolatedTestRunner)
 * &#64;IsolatedClassPrefix("org.xwiki.mypackage")
 * public class MyPackageTest
 * {
 *     &#64;Test
 *     public void someTest() throws Exception
 *     {
 *     ...
 *     }
 * ...
 * }
 * }</pre>
 *
 * The prefixes should at least include a prefix that match your test class, else the initialization will fail,
 * since your test would not be run in isolation properly.
 *
 * If you are mocking some of your isolated classes with Mockito in different tests (either isolated or not), you
 * will need to disable the class cache used by Mockito to avoid ClassCastException during mocking. You can disable
 * the cache by adding the following class to your test Jar:
 *
 * <pre>{@code
 * package org.mockito.configuration;
 * public class MockitoConfiguration extends DefaultMockitoConfiguration
 * {
 *     &#64;Override
 *     public boolean enableClassCache()
 *     {
 *         return false;
 *     }
 * }
 * }</pre>
 *
 * @version $Id$
 * @since 5.0M2
 */
public class IsolatedTestRunner extends BlockJUnit4ClassRunner
{
    /**
     * Creates a BlockJUnit4ClassRunner to run {@code clazz}.
     *
     * @param clazz the test class to be run in isolation
     * @throws InitializationError if the test class is malformed.
     */
    public IsolatedTestRunner(Class<?> clazz) throws InitializationError
    {
        super(getFromTestClassloader(clazz));
    }

    /**
     * @param clazz the test class to be run in isolation
     * @return an isolated version of the test class, using an separated ClassLoader.
     * @throws InitializationError if the test class is malformed.
     */
    private static Class<?> getFromTestClassloader(Class<?> clazz) throws InitializationError
    {
        String name = clazz.getName();
        IsolatedClassPrefix isolatedClassPrefix = clazz.getAnnotation(IsolatedClassPrefix.class);

        if (isolatedClassPrefix == null) {
            throw new InitializationError("To run test with some classes isolated, you need to define the prefix of "
                + "these classes using annotation @IsolatedClassPrefix "
                + "(ie: @IsolatedClassPrefix(\"org.xwiki.mymodule\").");
        }

        String[] prefixes = isolatedClassPrefix.value();
        StringBuilder prefixList = null;
        boolean classMatched = false;
        for (String prefix : prefixes) {
            if (name.startsWith(prefix)) {
                classMatched = true;
            }
            if (prefixList == null) {
                prefixList = new StringBuilder(prefix);
            } else {
                prefixList.append(", ").append(prefix);
            }
        }

        if (!classMatched) {
            throw new InitializationError(String.format("To run test with some classes isolated, your test class "
                + "should be itself isolated, and therefore, should part of the class prefix used for isolation. "
                + "Your class [%s] does not match any prefixes in [%s]", name, prefixList));
        }

        try {
            ClassLoader reloadRightClassLoader = new IsolatedTestClassLoader(prefixes);
            return Class.forName(name, true, reloadRightClassLoader);
        } catch (ClassNotFoundException e) {
            throw new InitializationError(e);
        }
    }

    /**
     * A ClassLoader implementation that loads itself classes based on given prefixes, and delegate the rest to
     * its parent ClassLoader.
     */
    private static class IsolatedTestClassLoader extends ClassLoader
    {
        /**
         * List of class prefixes isolated by this ClassLoader.
         */
        private final String[] prefixes;

        /**
         * Creates a IsolatedTestClassLoader for the provided prefixes.
         * @param prefixes List of class name prefix use to limit isolation to the given classes.
         */
        IsolatedTestClassLoader(String[] prefixes) {
            super(Thread.currentThread().getContextClassLoader());
            this.prefixes = prefixes;
        }

        @Override
        public Class<?> loadClass(String name) throws ClassNotFoundException {
            for (String prefix : prefixes) {
                if (name.startsWith(prefix)) {
                    return loadLocalClass(name);
                }
            }
            return super.loadClass(name);
        }

        private Class<?> loadLocalClass(String name) throws ClassNotFoundException
        {
            synchronized (getClassLoadingLock(name)) {
                // First, check if the class has already been loaded
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    c = findClass(name);
                }

                return c;
            }
        }
    }
}
