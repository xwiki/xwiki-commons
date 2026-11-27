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
package org.xwiki.properties.internal;

import java.beans.Introspector;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.internal.ContextComponentManagerProvider;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.properties.BeanDescriptor;
import org.xwiki.properties.BeanManager;
import org.xwiki.properties.PropertyException;
import org.xwiki.properties.PropertyMandatoryException;
import org.xwiki.properties.RawProperties;
import org.xwiki.properties.internal.converter.ConvertUtilsConverter;
import org.xwiki.properties.internal.converter.EnumConverter;
import org.xwiki.properties.test.GenericTestConverter;
import org.xwiki.properties.test.TestBean;
import org.xwiki.properties.test.TestBeanFeatures;
import org.xwiki.properties.test.TestBeanValidation;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Validate {@link DefaultBeanManager}.
 *
 * @version $Id$
 */
@ComponentTest
// @formatter:off
@ComponentList({
    DefaultBeanManager.class,
    DefaultConverterManager.class,
    EnumConverter.class,
    ConvertUtilsConverter.class,
    ContextComponentManagerProvider.class,
    GenericTestConverter.class
})
// @formatter:on
class DefaultBeanManagerTest
{
    public static class RawPropertiesTest extends HashMap<String, Object> implements RawProperties
    {
        @Override
        public void set(String propertyName, Object value)
        {
            put(propertyName, value);
        }
    }

    /**
     * Loads its own copy of {@link TestBean}, so that each instance provides a class that no descriptor was computed
     * for yet and that can be garbage collected together with the class loader.
     */
    private static final class IsolatingClassLoader extends ClassLoader
    {
        IsolatingClassLoader()
        {
            super(DefaultBeanManagerTest.class.getClassLoader());
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
        {
            if (!TestBean.class.getName().equals(name)) {
                return super.loadClass(name, resolve);
            }

            synchronized (getClassLoadingLock(name)) {
                Class<?> loadedClass = findLoadedClass(name);
                if (loadedClass == null) {
                    try (InputStream stream = TestBean.class.getResourceAsStream("TestBean.class")) {
                        byte[] bytes = stream.readAllBytes();
                        loadedClass = defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
                return loadedClass;
            }
        }
    }

    @InjectComponentManager
    private ComponentManager componentManager;

    private BeanManager defaultBeanManager;

    @BeforeEach
    void setUp() throws Exception
    {
        this.defaultBeanManager = this.componentManager.getInstance(BeanManager.class);
    }

    @Test
    void populate() throws PropertyException
    {
        TestBean beanTest = new TestBean();

        Map<String, String> values = new HashMap<>();

        values.put("lowerprop", "lowerpropvalue");
        values.put("upperprop", "upperPropvalue");
        values.put("prop2", "42");
        values.put("prop3", "true");
        values.put("hiddenProperty", "hiddenPropertyvalue");
        values.put("publicField", "publicFieldvalue");
        values.put("genericProp", "1,2");

        this.defaultBeanManager.populate(beanTest, values);

        assertEquals("lowerpropvalue", beanTest.getLowerprop());
        assertEquals("upperPropvalue", beanTest.getUpperProp());
        assertEquals(42, beanTest.getProp2());
        assertTrue(beanTest.getProp3());
        assertNull(beanTest.getHiddenProperty());
        assertEquals("publicFieldvalue", beanTest.publicField);
        assertEquals(Arrays.asList(1, 2), beanTest.getGenericProp());
    }

    @Test
    void populateWithValidation() throws PropertyException
    {
        TestBeanValidation beanTest = new TestBeanValidation();

        Map<String, String> values = new HashMap<>();

        values.put("working", "true");

        this.defaultBeanManager.populate(beanTest, values);

        assertTrue(beanTest.isWorking());

        values.put("working", "false");

        assertThrows(PropertyException.class, () -> this.defaultBeanManager.populate(beanTest, values),
            "No Bean Validation was executed");
    }

    @Test
    void populateWhenMissingMandatoryProperty()
    {
        Throwable exception = assertThrows(PropertyMandatoryException.class,
            () -> this.defaultBeanManager.populate(new TestBean(), new HashMap<String, String>()));
        assertEquals("Property [prop2] mandatory", exception.getMessage());
    }

    @Test
    void populateRawProperties() throws PropertyException
    {
        Map<String, Object> values = new HashMap<>();

        values.put("pro1", "value1");
        values.put("prop2", 2);

        RawPropertiesTest bean = new RawPropertiesTest();

        this.defaultBeanManager.populate(bean, values);

        assertEquals(values, bean);
    }
    
    @Test
    void populateMandatoryFeatures() throws PropertyException
    {
        // The following tries should not throw any exception
        // We test when a property defined before the annotated one is filled up
        Map<String, Object> values1 = new HashMap<>();
        values1.put("propertyFeatureMandatory1", "value");
        this.defaultBeanManager.populate(new TestBeanFeatures(), values1);

        // We test when the annotated property is the one filled up
        Map<String, Object> values2 = new HashMap<>();
        values2.put("propertyFeatureMandatory2", "value");
        this.defaultBeanManager.populate(new TestBeanFeatures(), values2);

        // We test when a property defined after the annotated one is filled up
        Map<String, Object> values3 = new HashMap<>();
        values3.put("propertyFeatureMandatory3", "value");
        this.defaultBeanManager.populate(new TestBeanFeatures(), values3);

        // We test when all properties are filled up for the feature.
        Map<String, Object> values4 = new HashMap<>();
        values4.put("propertyFeatureMandatory1", "value");
        values4.put("propertyFeatureMandatory2", "value");
        values4.put("propertyFeatureMandatory3", "value");
        this.defaultBeanManager.populate(new TestBeanFeatures(), values4);

        // We test when no property is filled up for the feature.
        Throwable exception = assertThrows(PropertyException.class,
            () -> this.defaultBeanManager.populate(new TestBeanFeatures(), new HashMap<>()));
        assertEquals("Property [propertyFeatureMandatory2:mandatoryFeature] mandatory", 
            exception.getMessage());
    }

    @Test
    void getBeanDescriptorIsComputedOncePerClass()
    {
        BeanDescriptor descriptor = this.defaultBeanManager.getBeanDescriptor(TestBean.class);

        assertSame(descriptor, this.defaultBeanManager.getBeanDescriptor(TestBean.class));
        assertNull(this.defaultBeanManager.getBeanDescriptor(null));
    }

    @Test
    void getBeanDescriptorConcurrently() throws Exception
    {
        int threadCount = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            // Repeat with fresh classes since the threads only race on the first lookup of a class.
            for (int i = 0; i < 20; i++) {
                Class<?> beanClass = new IsolatingClassLoader().loadClass(TestBean.class.getName());
                CyclicBarrier barrier = new CyclicBarrier(threadCount);
                List<Future<BeanDescriptor>> futures = new ArrayList<>();
                for (int j = 0; j < threadCount; j++) {
                    futures.add(executor.submit(() -> {
                        barrier.await();
                        return this.defaultBeanManager.getBeanDescriptor(beanClass);
                    }));
                }

                BeanDescriptor descriptor = this.defaultBeanManager.getBeanDescriptor(beanClass);
                for (Future<BeanDescriptor> future : futures) {
                    assertSame(descriptor, future.get(30, TimeUnit.SECONDS));
                }
            }
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void getBeanDescriptorDoesNotPreventClassUnloading() throws Exception
    {
        WeakReference<Class<?>> beanClassReference = getBeanDescriptorOfIsolatedClass();

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
        while (beanClassReference.get() != null && System.nanoTime() < deadline) {
            System.gc();
        }

        assertNull(beanClassReference.get());
    }

    private WeakReference<Class<?>> getBeanDescriptorOfIsolatedClass() throws ClassNotFoundException
    {
        Class<?> beanClass = new IsolatingClassLoader().loadClass(TestBean.class.getName());
        assertNotSame(TestBean.class, beanClass);

        BeanDescriptor descriptor = this.defaultBeanManager.getBeanDescriptor(beanClass);
        assertSame(beanClass, descriptor.getBeanClass());
        assertSame(descriptor, this.defaultBeanManager.getBeanDescriptor(beanClass));

        // The JDK's Introspector, used to compute the descriptor, keeps its own soft cache of the class.
        Introspector.flushFromCaches(beanClass);

        return new WeakReference<>(beanClass);
    }
}
