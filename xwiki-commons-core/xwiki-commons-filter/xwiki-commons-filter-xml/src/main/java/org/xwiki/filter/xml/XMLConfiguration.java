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
package org.xwiki.filter.xml;

/**
 * Allow to customize the syntax.
 * 
 * @version $Id$
 * @since 5.2M1
 */
public class XMLConfiguration
{
    /**
     * The default name of block element.
     */
    public static final String DEFAULT_ELEM_BLOCK = "block";

    /**
     * The default name of the attribute containing the name of the block.
     */
    public static final String DEFAULT_ATT_BLOCK_NAME = "n";

    /**
     * The default name of the attribute containing the name of the parameter.
     */
    public static final String DEFAULT_ATT_PARAMETER_NAME = DEFAULT_ATT_BLOCK_NAME;

    /**
     * The default name of the attribute containing the type of the parameter.
     */
    public static final String DEFAULT_ATT_PARAMETER_TYPE = "t";

    /**
     * The default name of the parameter element.
     */
    public static final String DEFAULT_ELEM_PARAMETER = "p";

    /**
     * The default name of the parameter element.
     */
    public static final String DEFAULT_ELEM_PARAMETERS = DEFAULT_ELEM_PARAMETER;

    /**
     * @see #getElementBlock()
     */
    private String elementBlock;

    /**
     * @see #getElementParameters()
     */
    private String elementParameters;

    /**
     * @see #getAttributeParameterName()
     */
    private String attributeParameterName;

    /**
     * @see #getAttributeParameterType()
     */
    private String attributeParameterType;

    /**
     * @see #getAttributeBlockName()
     */
    private String attributeBlockName;

    /**
     * Default constructor.
     */
    public XMLConfiguration()
    {
        setElementBlock(DEFAULT_ELEM_BLOCK);
        setElementParameters(DEFAULT_ELEM_PARAMETERS);
        setAttributeBlockName(DEFAULT_ATT_BLOCK_NAME);
        setAttributeParameterName(DEFAULT_ATT_PARAMETER_NAME);
        setAttributeParameterType(DEFAULT_ATT_PARAMETER_TYPE);
    }

    /**
     * @return the name of the block element
     */
    public String getElementBlock()
    {
        return this.elementBlock;
    }

    /**
     * @param elementBlock the name of the block element
     */
    public void setElementBlock(String elementBlock)
    {
        this.elementBlock = elementBlock;
    }

    /**
     * @return the name of the parameters element
     */
    public String getElementParameters()
    {
        return this.elementParameters;
    }

    /**
     * @param elementParameters the name of the parameters element
     */
    public void setElementParameters(String elementParameters)
    {
        this.elementParameters = elementParameters;
    }

    /**
     * @return the name of the attribute containing the name of the block
     */
    public String getAttributeBlockName()
    {
        return this.attributeBlockName;
    }

    /**
     * @param attributeBlockName the name of the attribute containing the name of the block
     */
    public void setAttributeBlockName(String attributeBlockName)
    {
        this.attributeBlockName = attributeBlockName;
    }

    /**
     * @return the name of the attribute containing the name of the parameter
     */
    public String getAttributeParameterName()
    {
        return this.attributeParameterName;
    }

    /**
     * @param attributeParameterName the name of the attribute containing the name of the parameter
     */
    public void setAttributeParameterName(String attributeParameterName)
    {
        this.attributeParameterName = attributeParameterName;
    }

    /**
     * @return the name of the attribute containing the type of the parameter
     */
    public String getAttributeParameterType()
    {
        return this.attributeParameterType;
    }

    /**
     * @param attributeParameterType the name of the attribute containing the type of the parameter
     */
    public void setAttributeParameterType(String attributeParameterType)
    {
        this.attributeParameterType = attributeParameterType;
    }
}
