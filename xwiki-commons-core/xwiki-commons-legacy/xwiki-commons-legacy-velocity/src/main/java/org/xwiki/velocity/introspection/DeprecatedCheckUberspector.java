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
package org.xwiki.velocity.introspection;

/**
 * Chainable Velocity Uberspector that checks for deprecated method calls. It does that by checking if the returned
 * method has a Deprecated annotation. Because this is a chainable uberspector, it has to re-get the method using a
 * default introspector, which is not safe; future uberspectors might not be able to return a precise method name, or a
 * method of the original target object.
 *
 * @since 1.5M1
 * @version $Id$
 * @see ChainableUberspector
 * @deprecated since 10.5RC1, use {@link org.apache.velocity.util.introspection.DeprecatedCheckUberspector} instead
 */
@Deprecated
public class DeprecatedCheckUberspector extends org.apache.velocity.util.introspection.DeprecatedCheckUberspector
{
}
