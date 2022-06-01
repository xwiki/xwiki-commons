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

import org.xwiki.filter.annotation.Default;
import org.xwiki.filter.annotation.Name;

public interface TestFilter
{
    void beginContainer();

    void endContainer();

    void onChild();

    @Name("containerwithname")
    void beginNamedContainer();

    @Name("containerwithname")
    void endNamedContainer();

    @Name("childwithname")
    void onNamedChild();

    void onChildWithParameters(String param0, int param1);

    void beginContainerWithParameters(String param0, int param1);

    void endContainerWithParameters(String param0, int param1);

    void onChildWithNamedParameter(@Name("namedParam") String namedParam, int param1);

    void beginContainerWithNamedParameters(@Name("namedParam") String namedParam, int param1);

    void endContainerWithNamedParameters(@Name("namedParam") String namedParam, int param1);

    void beginContainerWithMap(@Name("map") Map<String, Integer> map);

    void endContainerWithMap(@Name("map") Map<String, Integer> map);

    void beginCustomData(@Name("custom") TestData data);

    void endCustomData(@Name("custom") TestData data);

    void beginBlock(@Name("blockName") String blockName);

    void endBlock(@Name("blockName") String blockName);

    void onBlock(@Name("blockName") String blockName);

    void beginBlockParameters();

    void endBlockParameters();

    void onBlockParameters();

    void onChildWithDefaultValue(@Name("int") @Default("42") int integer,
        @Name("string") @Default("default value") String string, @Name("color") @Default("#ffffff") Color color,
        @Name("map") @Default("") Map<String, String> map);

    void beginContainerWithMultilineParameter(String multi);

    void endContainerWithMultilineParameter(String multi);
}
