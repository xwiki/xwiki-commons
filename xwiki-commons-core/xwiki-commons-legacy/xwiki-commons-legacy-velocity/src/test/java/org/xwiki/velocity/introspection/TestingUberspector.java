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

import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.VelMethod;
import org.apache.velocity.util.introspection.VelPropertyGet;

public class TestingUberspector extends AbstractChainableUberspector
{
    public static int methodCalls = 0;

    public static int getterCalls = 0;

    @Override
    public VelMethod getMethod(Object obj, String methodName, Object[] args, Info i)
    {
        ++methodCalls;
        return super.getMethod(obj, methodName, args, i);
    }

    @Override
    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i)
    {
        ++getterCalls;
        return super.getPropertyGet(obj, identifier, i);
    }
}
