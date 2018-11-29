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
package org.xwiki.properties.test;

import org.xwiki.properties.annotation.PropertyFeature;
import org.xwiki.properties.annotation.PropertyGroup;

public class TestBeanError
{
    private String prop1;

    private int prop2;

    @PropertyGroup({"group"})
    @PropertyFeature("feature1")
    public void setProp1(String prop1)
    {
        this.prop1 = prop1;
    }

    public String getProp1()
    {
        return this.prop1;
    }

    @PropertyGroup({"group"})
    @PropertyFeature("feature2")
    public void setProp2(int prop2)
    {
        this.prop2 = prop2;
    }

    public int getProp2()
    {
        return this.prop2;
    }
}
