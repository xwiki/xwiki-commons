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
package org.xwiki.groovy;

import org.codehaus.groovy.control.customizers.CompilationCustomizer;
import org.xwiki.component.annotation.Role;

/**
 * Allow providing Groovy's {@link CompilationCustomizer}s to perform Groovy customization such as: automatically
 * add imports, stop script execution after a certain timeout, prevents using some imports/statements/operators/etc,
 * and more.
 * <p>
 * Note that we would have liked to not have this interface and instead have our component implementations directly
 * implement {@link CompilationCustomizer} but unfortunately the Groovy guys made the choice(wrong, IMO) to not use
 * an interface for CompilationCustomizer; it's an Abstract class! (yuck, even the name is wrong since a good practice
 * is to have Abstract in the name of Abstract classes...).
 * </p>
 * @version $Id$
 * @since 4.1M1
 */
@Role
public interface GroovyCompilationCustomizer
{
    /**
     * @return the Groovy Customizer implementation class to use.
     */
    CompilationCustomizer createCustomizer();
}
