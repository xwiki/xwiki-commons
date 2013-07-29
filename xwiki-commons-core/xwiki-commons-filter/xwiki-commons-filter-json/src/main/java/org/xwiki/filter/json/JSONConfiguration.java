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
package org.xwiki.filter.json;

import java.util.regex.Pattern;

/**
 * Allow to customize the syntax.
 * 
 * @version $Id$
 * @since 5.2M1
 */
public class JSONConfiguration
{
    /**
     * @see #getBlockChildrenField()
     */
    public static final String DEFAULT_BLOCK_CHILDREN_NAME = "c";

    /**
     * @see #getBlockTypeField()
     */
    public static final String DEFAULT_BLOCK_TYPE_NAME = "t";

    /**
     * @see #getParameterField()
     */
    public static final String DEFAULT_BLOCK_PARAMETER_NAME = "p";

    /**
     * @see #getBlockChildrenField()
     */
    private String blockChildrenField;

    /**
     * @see #getBlockTypeField()
     */
    private String blockTypeField;

    /**
     * @see #getBlockParameterField()
     */
    private String blockParameterField;

    /**
     * @see #getElementParameterPattern()
     */
    private Pattern blockParameterFieldPattern;

    /**
     * Default constructor.
     */
    public JSONConfiguration()
    {
        setChildrenField(DEFAULT_BLOCK_CHILDREN_NAME);
        setBlockTypeField(DEFAULT_BLOCK_TYPE_NAME);
    }

    /**
     * @return the default name of children field.
     */
    public String getBlockChildrenField()
    {
        return this.blockChildrenField;
    }

    /**
     * @param childrenField the default name of children field.
     */
    public void setChildrenField(String childrenField)
    {
        this.blockChildrenField = childrenField;
    }

    /**
     * @return the default name of block type field.
     */
    public String getBlockTypeField()
    {
        return this.blockTypeField;
    }

    /**
     * @param blockTypeField the default name of block type field.
     */
    public void setBlockTypeField(String blockTypeField)
    {
        this.blockTypeField = blockTypeField;
    }

    /**
     * @return the generic name prefix of a field containing a parameter
     */
    public String getBlockParameterField()
    {
        return this.blockParameterField;
    }

    /**
     * @param blockParameterField the generic name prefix of a field containing a parameter
     */
    public void setBlockParameterField(String blockParameterField)
    {
        this.blockParameterField = blockParameterField;
        this.blockParameterFieldPattern = Pattern.compile(Pattern.quote(blockParameterField) + "(\\d*)");
    }

    /**
     * @return the pattern marching the name of a field containing a parameter
     */
    public Pattern getBlockParameterFieldPattern()
    {
        return this.blockParameterFieldPattern;
    }
}
