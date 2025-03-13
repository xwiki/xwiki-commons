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

public class TestBeanFeatures
{

    private String propertyFeatureMandatory1;

    private String propertyFeatureMandatory2;

    private String propertyFeatureMandatory3;

    @PropertyFeature("mandatoryFeature")
    public String getPropertyFeatureMandatory1()
    {
        return propertyFeatureMandatory1;
    }
    public void setPropertyFeatureMandatory1(String propertyFeatureMandatory1)
    {
        this.propertyFeatureMandatory1 = propertyFeatureMandatory1;
    }
    
    @PropertyFeature(value = "mandatoryFeature", mandatory = true)
    public String getPropertyFeatureMandatory2()
    {
        return propertyFeatureMandatory2;
    }

    public void setPropertyFeatureMandatory2(String propertyFeatureMandatory2)
    {
        this.propertyFeatureMandatory2 = propertyFeatureMandatory2;
    }

    @PropertyFeature("mandatoryFeature")
    public String getPropertyFeatureMandatory3()
    {
        return propertyFeatureMandatory3;
    }

    public void setPropertyFeatureMandatory3(String propertyFeatureMandatory3)
    {
        this.propertyFeatureMandatory3 = propertyFeatureMandatory3;
    }
}
