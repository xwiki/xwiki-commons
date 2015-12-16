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
package org.xwiki.job.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Force the Job serializer to serialize the objects of that class (for example when a @Component should be serialized
 * despite the fact that they are skipped by default). It can also be used to force NOT serialize a class that would
 * otherwise be serialized (like a huge instance that don't make any sense to spend time serialize).
 * 
 * @version $Id$
 * @since 6.4M1
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface Serializable
{
    /**
     * @return true if the objects of that class should be serialized during Job status serialization.
     */
    boolean value() default true;
}
