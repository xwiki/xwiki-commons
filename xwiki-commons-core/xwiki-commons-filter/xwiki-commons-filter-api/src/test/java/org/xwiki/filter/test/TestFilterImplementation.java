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
package org.xwiki.filter.test;

import java.awt.Color;
import java.util.Map;

public class TestFilterImplementation implements TestFilter
{
    @Override
    public void beginContainer()
    {

    }

    @Override
    public void endContainer()
    {

    }

    @Override
    public void onChild()
    {

    }

    @Override
    public void onChildWithParameters(String differentParam0, int differentParam1)
    {

    }

    @Override
    public void beginContainerWithParameters(String differentParam0, int differentParam1)
    {

    }

    @Override
    public void endContainerWithParameters(String differentParam0, int differentParam1)
    {

    }

    @Override
    public void onChildWithNamedParameter(String namedParam, int param1)
    {

    }

    @Override
    public void beginContainerWithNamedParameters(String namedParam, int param1)
    {

    }

    @Override
    public void endContainerWithNamedParameters(String namedParam, int param1)
    {

    }

    @Override
    public void beginContainerWithMap(Map<String, Integer> map)
    {

    }

    @Override
    public void endContainerWithMap(Map<String, Integer> map)
    {

    }

    @Override
    public void beginCustomData(TestData data)
    {

    }

    @Override
    public void endCustomData(TestData data)
    {

    }

    @Override
    public void onChildWithDefaultValue(int integer, String string, Color color, Map<String, String> map)
    {

    }

    @Override
    public void beginContainerWithMultilineParameter(String multiline)
    {

    }

    @Override
    public void endContainerWithMultilineParameter(String multiline)
    {

    }

    // Reserved names

    @Override
    public void beginBlock(String blockName)
    {

    }

    @Override
    public void endBlock(String blockName)
    {

    }

    @Override
    public void onBlock(String blockName)
    {

    }

    @Override
    public void beginBlockParameters()
    {

    }

    @Override
    public void endBlockParameters()
    {

    }

    @Override
    public void onBlockParameters()
    {

    }

    @Override
    public void beginNamedContainer()
    {

    }

    @Override
    public void endNamedContainer()
    {

    }

    @Override
    public void onNamedChild()
    {

    }
}
