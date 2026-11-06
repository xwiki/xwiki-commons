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
package org.xwiki.tool.spoon.macro.sourcerole;

import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.macro.Macro;

/**
 * Test class for {@code MacroDeclarationProcessor}. It is not compiled, so that one of its roles, which is the class
 * itself, cannot be loaded from the classpath.
 *
 * @version $Id$
 */
@Component(roles = { Macro.class, SourceRoleMacro.class })
@Named("sourceRoleMacro")
public class SourceRoleMacro implements Macro<Object>
{
}
