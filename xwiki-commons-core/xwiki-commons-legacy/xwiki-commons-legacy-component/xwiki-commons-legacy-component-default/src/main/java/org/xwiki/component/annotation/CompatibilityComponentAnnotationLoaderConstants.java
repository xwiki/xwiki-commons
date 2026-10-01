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

/**
 * Re-adds the deprecated constants of {@link ComponentAnnotationLoader}, as interface fields so that they remain
 * compile-time constants.
 *
 * @version $Id$
 * @since 18.9.0RC1
 */
// Only an interface field can be added to ComponentAnnotationLoader while staying a compile-time constant (AspectJ
// inter-type fields are never constants), so this interface cannot describe a type.
@SuppressWarnings("checkstyle:InterfaceIsType")
public interface CompatibilityComponentAnnotationLoaderConstants
{
    /**
     * Location in the classloader of the file specifying which component implementation to use when several components
     * with the same role/hint are found.
     *
     * @deprecated use the notion of priorities instead (see {@link ComponentDeclaration}).
     */
    @Deprecated(since = "3.3M1")
    String COMPONENT_OVERRIDE_LIST = "META-INF/component-overrides.txt";
}
