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
package org.xwiki.filter.test.integration.junit5;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.opentest4j.IncompleteExecutionException;
import org.xwiki.filter.test.integration.TestConfiguration;
import org.xwiki.filter.test.integration.TestDataGenerator;
import org.xwiki.test.mockito.MockitoComponentManager;

/**
 * Run all tests found in {@code *.test} files located in the classpath.
 * <p>
 * Usage Example
 * </p>
 * 
 * <pre>
 * <code>
 * &#064;AllComponents
 * class MyIntegrationTests extends FilterTest
 * {
 * }
 * </code>
 * </pre>
 * <p>
 * It's also possible to get access to the underlying Component Manager used, for example in order to register Mock
 * implementations of components. For example:
 * </p>
 * 
 * <pre>
 * <code>
 * &#064;AllComponents
 * class MyIntegrationTests extends FilterTest
 * {
 *     &#064;Initialized
 *     public void initialize(MockitoComponentManager componentManager)
 *     {
 *         // Init mocks here for example
 *     }
 * }
 * </code>
 * </pre>
 *
 * @version $Id$
 * @since 18.0.0RC1
 */
public class FilterTest
{
    private static final TestDataGenerator GENERATOR = new TestDataGenerator();

    private static final String DEFAULT_PATTERN = ".*\\.test";

    /**
     * Used to perform specific initializations before each test in the suite is executed.
     * 
     * @version $Id$
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Initialized
    {
    }

    /**
     * Annotation to use to indicate the resources directory containing the tests to execute.
     * 
     * @version $Id$
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Scope
    {
        /**
         * @return the classpath prefix to search in
         */
        String value() default "";

        /**
         * @return the regex pattern to filter *.test files to execute
         */
        String pattern() default DEFAULT_PATTERN;
    }

    private MockitoComponentManager componentManager;

    @BeforeEach
    void initializeComponentManager()
    {
        // Initialize a component manager used to locate filter components to decide what tests to execute in
        // TestDataGenerator.
        if (this.componentManager == null) {
            MockitoComponentManager mockitoComponentManager = new MockitoComponentManager();
            try {
                mockitoComponentManager.initializeTest(this);
                mockitoComponentManager.registerMemoryConfigurationSource();
            } catch (Exception e) {
                throw new IncompleteExecutionException("Failed to initialize Component Manager", e);
            }
            this.componentManager = mockitoComponentManager;
        }
    }

    @BeforeEach
    void callInitializers()
    {
        callAnnotatedMethods(Initialized.class);
    }

    @AfterEach
    void shutdownComponentManager()
    {
        if (this.componentManager != null) {
            try {
                this.componentManager.shutdownTest();
            } catch (Exception e) {
                throw new IncompleteExecutionException("Failed to shutdown Component Manager", e);
            }
        }
    }

    protected MockitoComponentManager getComponentManager()
    {
        return this.componentManager;
    }

    private void callAnnotatedMethods(Class<? extends Annotation> annotationClass)
    {
        try {
            for (Method klassMethod : getClass().getDeclaredMethods()) {
                Annotation componentManagerAnnotation = klassMethod.getAnnotation(annotationClass);
                if (componentManagerAnnotation != null) {
                    // Call it!
                    klassMethod.invoke(this, this.componentManager);
                }
            }
        } catch (Exception e) {
            throw new IncompleteExecutionException(
                String.format("Failed to call test methods annotated with [%s]", annotationClass.getCanonicalName()),
                e);
        }
    }

    /**
     * @return the dynamic list of tests to execute
     * @throws Exception when failing to generate the tests
     */
    @TestFactory
    Stream<DynamicTest> filterTests() throws Exception
    {
        // Step 0: Allow subclasses to perform specific initializations
        beforeTests();

        // Step 1: Generate inputs

        // If a Scope Annotation is present then use it to define the scope
        Scope scopeAnnotation = getClass().getAnnotation(Scope.class);
        String packagePrefix = "";
        String pattern = DEFAULT_PATTERN;
        if (scopeAnnotation != null) {
            packagePrefix = scopeAnnotation.value();
            pattern = scopeAnnotation.pattern();
        }
        Collection<TestConfiguration> configurations = GENERATOR.generateData(packagePrefix, pattern);

        // Step 2: Generate test names
        Function<TestConfiguration, String> displayNameGenerator = configuration -> configuration.name;

        // Step 3: Generate tests to execute
        ThrowingConsumer<TestConfiguration> testExecutor =
            configuration -> new InternalFilterTest(configuration, getComponentManager()).execute();

        // Return the dynamically created tests
        return DynamicTest.stream(configurations.iterator(), displayNameGenerator, testExecutor);
    }

    protected void beforeTests() throws Exception
    {
        // Default implementation does nothing
    }
}
