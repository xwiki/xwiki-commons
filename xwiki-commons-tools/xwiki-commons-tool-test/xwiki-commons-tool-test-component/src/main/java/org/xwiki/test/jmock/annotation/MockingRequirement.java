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
package org.xwiki.test.jmock.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Defines a component that needs to have its injected components mocked.
 *
 * @version $Id$
 * @since 2.4RC1
 * @deprecated use {@link org.xwiki.test.junit5.mockito.InjectMockComponents} instead
 */
@Deprecated(since = "4.3.1")
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface MockingRequirement
{
    /**
     * @return the Component implementation class
     * @since 4.2M3
     */
    Class<?> value();

    /**
     * @return the role if the component implementation implements several roles
     */
    Class<?> role() default Object.class;

    /**
     * @return the hint if the component implementation has several roles
     */
    String hint() default "";

    /**
     * @return a list of component roles for classes that shouldn't be mocked
     */
    Class<?>[] exceptions() default {};
}
