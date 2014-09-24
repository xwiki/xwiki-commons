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
package org.xwiki.component.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>
 * Helps controlling the order in which Components are disposed upon XWiki's shutdown. This annotation is optional
 * and should be used only for Components needing to be disposed before or after other components (for example
 * the Database shutdown needs to be executed after all other components using DB storage have been disposed of).
 * </p>
 * <p>
 * When used, this annotation takes precedence over the natural order defined by explicit Component dependencies.
 * </p>
 * <p>
 * The default priority is 1000.
 * </p>
 *
 * @version $Id$
 * @since 6.2.1
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface DisposePriority
{
    /**
     * The priority to use, knowing that the smaller the value the higher the priority.
     */
    int value() default 1000;
}
