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

import java.util.List;

import org.xwiki.properties.annotation.PropertyAdvanced;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.properties.annotation.PropertyDisplayType;
import org.xwiki.properties.annotation.PropertyFeature;
import org.xwiki.properties.annotation.PropertyGroup;
import org.xwiki.properties.annotation.PropertyHidden;
import org.xwiki.properties.annotation.PropertyId;
import org.xwiki.properties.annotation.PropertyMandatory;
import org.xwiki.properties.annotation.PropertyName;

public class TestBean
{
    public static final String STATICFIELD = "staticfield";

    private String lowerprop;

    private String upperProp;

    private String prop1 = "defaultprop1";

    private int prop2;

    private boolean prop3;

    private String hiddenProperty;

    private List<Integer> genericProp;

    public String propertyWithDifferentId;

    private String deprecatedParameter;

    private String advancedParameter;

    private String displayTypeParameter;

    @PropertyName("Public Field")
    @PropertyDescription("a public field")
    public String publicField;

    public List<Integer> genericField;

    @PropertyId("impossible.field.name")
    public String publicFieldWithDifferentId;

    public void setLowerprop(String lowerprop)
    {
        this.lowerprop = lowerprop;
    }

    public String getLowerprop()
    {
        return this.lowerprop;
    }

    public void setUpperProp(String upperProp)
    {
        this.upperProp = upperProp;
    }

    public String getUpperProp()
    {
        return this.upperProp;
    }

    @PropertyDescription("prop1 description")
    @PropertyFeature("feature1")
    public void setProp1(String prop1)
    {
        this.prop1 = prop1;
    }

    public String getProp1()
    {
        return this.prop1;
    }

    @PropertyMandatory
    @PropertyDescription("prop2 description")
    public void setProp2(int prop2)
    {
        this.prop2 = prop2;
    }

    public int getProp2()
    {
        return this.prop2;
    }

    public void setProp3(boolean prop3)
    {
        this.prop3 = prop3;
    }

    @PropertyMandatory
    @PropertyDescription("prop3 description")
    public boolean getProp3()
    {
        return this.prop3;
    }

    @PropertyHidden
    public void setHiddenProperty(String hiddenProperty)
    {
        this.hiddenProperty = hiddenProperty;
    }

    public String getHiddenProperty()
    {
        return this.hiddenProperty;
    }

    public List<Integer> getGenericProp()
    {
        return this.genericProp;
    }

    public void setGenericProp(List<Integer> genericProp)
    {
        this.genericProp = genericProp;
    }

    @PropertyId("impossible.method.name")
    public String getPropertyWithDifferentId()
    {
        return this.propertyWithDifferentId;
    }

    public void setPropertyWithDifferentId(String propertyWithDifferentId)
    {
        this.propertyWithDifferentId = propertyWithDifferentId;
    }

    @Deprecated
    public String getDeprecatedParameter()
    {
        return deprecatedParameter;
    }

    @Deprecated
    @PropertyGroup({"test1", "test2"})
    public void setDeprecatedParameter(String deprecatedParameter)
    {
        this.deprecatedParameter = deprecatedParameter;
    }

    @PropertyAdvanced
    @PropertyGroup({"test1", "test2"})
    @PropertyFeature("feature2")
    public String getAdvancedParameter()
    {
        return advancedParameter;
    }

    public void setAdvancedParameter(String advancedParameter)
    {
        this.advancedParameter = advancedParameter;
    }

    @PropertyDisplayType({List.class, Boolean.class})
    public String getDisplayTypeParameter()
    {
        return displayTypeParameter;
    }

    public void setDisplayTypeParameter(String displayTypeParameter)
    {
        this.displayTypeParameter = displayTypeParameter;
    }
}
