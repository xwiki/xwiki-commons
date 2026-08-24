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
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void endContainer()
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void onChild()
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void onChildWithParameters(String differentParam0, int differentParam1)
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void beginContainerWithParameters(String differentParam0, int differentParam1)
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void endContainerWithParameters(String differentParam0, int differentParam1)
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void onChildWithNamedParameter(String namedParam, int param1)
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void beginContainerWithNamedParameters(String namedParam, int param1)
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void endContainerWithNamedParameters(String namedParam, int param1)
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void beginContainerWithMap(Map<String, Integer> map)
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void endContainerWithMap(Map<String, Integer> map)
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void beginCustomData(TestData data)
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void endCustomData(TestData data)
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void onChildWithDefaultValue(int integer, String string, Color color, Map<String, String> map)
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void beginContainerWithMultilineParameter(String multiline)
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void endContainerWithMultilineParameter(String multiline)
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    // Reserved names

    @Override
    public void beginBlock(String blockName)
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void endBlock(String blockName)
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void onBlock(String blockName)
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void beginBlockParameters()
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void endBlockParameters()
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void onBlockParameters()
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void beginNamedContainer()
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void endNamedContainer()
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }

    @Override
    public void onNamedChild()
    {
        // Nothing to do, this test implementation only needs to implement the filter interface.
    }
}
