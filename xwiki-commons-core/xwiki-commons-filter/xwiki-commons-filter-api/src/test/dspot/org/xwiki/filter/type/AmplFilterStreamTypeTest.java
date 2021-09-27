/**
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
package org.xwiki.filter.type;


import org.junit.Assert;


/**
 * Validate {@link FilterStreamType}.
 *
 * @version $Id$
 */
public class AmplFilterStreamTypeTest {
    // Tests
    @org.junit.Test(timeout = 10000)
    public void testSerializeWithDataAndVersion() throws Exception {
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        String o_testSerializeWithDataAndVersion__4 = type.serialize();
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_testSerializeWithDataAndVersion__4);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    // Tests
    @org.junit.Test(timeout = 10000)
    public void testSerializeWithDataAndVersion_mg63416() throws Exception {
        FilterStreamType __DSPOT_o_2281 = new FilterStreamType(new SystemType("t&2>SvhnMj00&y_`Gdk%"), "7#h72xJU<i>jn-S]m>`@");
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        String o_testSerializeWithDataAndVersion_mg63416__7 = type.serialize();
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_testSerializeWithDataAndVersion_mg63416__7);
        // AssertGenerator create local variable with return value of invocation
        int o_testSerializeWithDataAndVersion_mg63416__8 = type.compareTo(__DSPOT_o_2281);
        // AssertGenerator add assertion
        Assert.assertEquals(83, ((int) (o_testSerializeWithDataAndVersion_mg63416__8)));
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_testSerializeWithDataAndVersion_mg63416__7);
    }

    // Tests
    @org.junit.Test(timeout = 10000)
    public void testSerializeWithDataAndVersion_mg63417() throws Exception {
        FilterStreamType __DSPOT_object_2282 = new FilterStreamType(new SystemType("`<<B-^!cgVMrowd_v!QM"), "igZ@|)Yo|wL,/:Gke]$7");
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        String o_testSerializeWithDataAndVersion_mg63417__7 = type.serialize();
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_testSerializeWithDataAndVersion_mg63417__7);
        // AssertGenerator create local variable with return value of invocation
        boolean o_testSerializeWithDataAndVersion_mg63417__8 = type.equals(__DSPOT_object_2282);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testSerializeWithDataAndVersion_mg63417__8);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_testSerializeWithDataAndVersion_mg63417__7);
    }

    // Tests
    @org.junit.Test(timeout = 10000)
    public void testSerializeWithDataAndVersionlitString63393() throws Exception {
        FilterStreamType type = new FilterStreamType(new SystemType("t]ype"), "data", "version");
        // AssertGenerator add assertion
        Assert.assertEquals("t]ype+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(758235892, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("t]ype", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(110018853, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("t]ype", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        String o_testSerializeWithDataAndVersionlitString63393__4 = type.serialize();
        // AssertGenerator add assertion
        Assert.assertEquals("t]ype+data/version", o_testSerializeWithDataAndVersionlitString63393__4);
        // AssertGenerator add assertion
        Assert.assertEquals("t]ype+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(758235892, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("t]ype", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(110018853, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("t]ype", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    // Tests
    @org.junit.Test(timeout = 10000)
    public void testSerializeWithDataAndVersion_mg63416litString63548() throws Exception {
        FilterStreamType __DSPOT_o_2281 = new FilterStreamType(new SystemType("\n"), "7#h72xJU<i>jn-S]m>`@");
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        String o_testSerializeWithDataAndVersion_mg63416__7 = type.serialize();
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_testSerializeWithDataAndVersion_mg63416__7);
        // AssertGenerator create local variable with return value of invocation
        int o_testSerializeWithDataAndVersion_mg63416__8 = type.compareTo(__DSPOT_o_2281);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_testSerializeWithDataAndVersion_mg63416__7);
    }

    // Tests
    @org.junit.Test(timeout = 10000)
    public void testSerializeWithDataAndVersion_mg63416_mg63940() throws Exception {
        Object __DSPOT_object_2301 = new Object();
        FilterStreamType __DSPOT_o_2281 = new FilterStreamType(new SystemType("t&2>SvhnMj00&y_`Gdk%"), "7#h72xJU<i>jn-S]m>`@");
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        String o_testSerializeWithDataAndVersion_mg63416__7 = type.serialize();
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_testSerializeWithDataAndVersion_mg63416__7);
        // AssertGenerator create local variable with return value of invocation
        int o_testSerializeWithDataAndVersion_mg63416__8 = type.compareTo(__DSPOT_o_2281);
        // AssertGenerator create local variable with return value of invocation
        boolean o_testSerializeWithDataAndVersion_mg63416_mg63940__15 = __DSPOT_o_2281.equals(__DSPOT_object_2301);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testSerializeWithDataAndVersion_mg63416_mg63940__15);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_testSerializeWithDataAndVersion_mg63416__7);
    }

    // Tests
    @org.junit.Test(timeout = 10000)
    public void testSerializeWithDataAndVersion_mg63418_mg64066() throws Exception {
        FilterStreamType __DSPOT_o_2313 = new FilterStreamType(new SystemType(",`-N^?;sh#m]Al>DG-<@"), "MT!dNMPjO A1+l+H)I2C");
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        String o_testSerializeWithDataAndVersion_mg63418__4 = type.serialize();
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_testSerializeWithDataAndVersion_mg63418__4);
        // AssertGenerator create local variable with return value of invocation
        String o_testSerializeWithDataAndVersion_mg63418__5 = type.serialize();
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_testSerializeWithDataAndVersion_mg63418__5);
        // AssertGenerator create local variable with return value of invocation
        int o_testSerializeWithDataAndVersion_mg63418_mg64066__13 = type.compareTo(__DSPOT_o_2313);
        // AssertGenerator add assertion
        Assert.assertEquals(72, ((int) (o_testSerializeWithDataAndVersion_mg63418_mg64066__13)));
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_testSerializeWithDataAndVersion_mg63418__4);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_testSerializeWithDataAndVersion_mg63418__5);
    }

    // Tests
    @org.junit.Test(timeout = 10000)
    public void testSerializeWithDataAndVersion_mg63416null64035_failAssert144litString69843_failAssert149() throws Exception {
        // AssertGenerator generate try/catch block with fail statement
        try {
            {
                FilterStreamType __DSPOT_o_2281 = new FilterStreamType(new SystemType("\n"), "7#h72xJU<i>jn-S]m>`@");
                FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");
                // AssertGenerator create local variable with return value of invocation
                String o_testSerializeWithDataAndVersion_mg63416__7 = type.serialize();
                // AssertGenerator create local variable with return value of invocation
                int o_testSerializeWithDataAndVersion_mg63416__8 = type.compareTo(null);
                org.junit.Assert.fail("testSerializeWithDataAndVersion_mg63416null64035 should have thrown NullPointerException");
            }
            org.junit.Assert.fail("testSerializeWithDataAndVersion_mg63416null64035_failAssert144litString69843 should have thrown NullPointerException");
        } catch (NullPointerException expected) {
            Assert.assertEquals(null, expected.getMessage());
        }
    }

    // Tests
    @org.junit.Test(timeout = 10000)
    public void testSerializeWithDataAndVersionlitString63400litString63765_add70585() throws Exception {
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "/ata", "");
        // AssertGenerator add assertion
        Assert.assertEquals("type+/ata/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(656296114, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
        ((SystemType) (((FilterStreamType) (type)).getType())).toString();
        // AssertGenerator create local variable with return value of invocation
        String o_testSerializeWithDataAndVersionlitString63400__4 = type.serialize();
        // AssertGenerator add assertion
        Assert.assertEquals("type+/ata/", o_testSerializeWithDataAndVersionlitString63400__4);
        // AssertGenerator add assertion
        Assert.assertEquals("type+/ata/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(656296114, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithDataAndVersion_mg82660() throws Exception {
        FilterStreamType __DSPOT_o_3438 = new FilterStreamType(new SystemType(">y&L{hqX*z_^Bl^W%Ml|"), ":RmN&x+Y$ aO`|pq[[zW");
        FilterStreamType type = FilterStreamType.unserialize("type+data/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        int o_testUnserializeWithDataAndVersion_mg82660__10 = type.compareTo(__DSPOT_o_3438);
        // AssertGenerator add assertion
        Assert.assertEquals(54, ((int) (o_testUnserializeWithDataAndVersion_mg82660__10)));
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithDataAndVersion_mg82661() throws Exception {
        FilterStreamType __DSPOT_object_3439 = new FilterStreamType(new SystemType("^P8Vl= |(cmN])AG-KYo"), "S5UBn`bQ)+e{g;1oANal");
        FilterStreamType type = FilterStreamType.unserialize("type+data/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        boolean o_testUnserializeWithDataAndVersion_mg82661__10 = type.equals(__DSPOT_object_3439);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testUnserializeWithDataAndVersion_mg82661__10);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithDataAndVersion_mg82662() throws Exception {
        FilterStreamType type = FilterStreamType.unserialize("type+data/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        String o_testUnserializeWithDataAndVersion_mg82662__7 = type.serialize();
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_testUnserializeWithDataAndVersion_mg82662__7);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithDataAndVersion_add82655_mg83363() throws Exception {
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithDataAndVersion_add82655__1 = FilterStreamType.unserialize("type+data/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type+data/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        String o_testUnserializeWithDataAndVersion_add82655_mg83363__10 = o_testUnserializeWithDataAndVersion_add82655__1.serialize();
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_testUnserializeWithDataAndVersion_add82655_mg83363__10);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithDataAndVersion_add82655_mg83368() throws Exception {
        FilterStreamType __DSPOT_o_3477 = new FilterStreamType(new SystemType("P0hIB{(aG4+J*;G:}z{m"), "<Tvvj@(HQ!=E<<3P-@<O");
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithDataAndVersion_add82655__1 = FilterStreamType.unserialize("type+data/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type+data/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        int o_testUnserializeWithDataAndVersion_add82655_mg83368__13 = type.compareTo(__DSPOT_o_3477);
        // AssertGenerator add assertion
        Assert.assertEquals(4, ((int) (o_testUnserializeWithDataAndVersion_add82655_mg83368__13)));
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithDataAndVersionlitString82654_mg83154() throws Exception {
        Object __DSPOT_object_3460 = new Object();
        FilterStreamType type = FilterStreamType.unserialize(":");
        // AssertGenerator add assertion
        Assert.assertEquals(":", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(940503, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals(":", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(58, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals(":", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        boolean o_testUnserializeWithDataAndVersionlitString82654_mg83154__9 = type.equals(__DSPOT_object_3460);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testUnserializeWithDataAndVersionlitString82654_mg83154__9);
        // AssertGenerator add assertion
        Assert.assertEquals(":", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(940503, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals(":", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(58, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals(":", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithDataAndVersion_add82655_add83069litString88709() throws Exception {
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithDataAndVersion_add82655__1 = FilterStreamType.unserialize("type+data/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithDataAndVersion_add82655_add83069__4 = FilterStreamType.unserialize("type+data/versXion");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/versXion", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(-1270848951, ((int) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("versXion", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type+data/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/versXion", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(-1270848951, ((int) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("versXion", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithDataAndVersion_add82655_add82977_mg88376() throws Exception {
        FilterStreamType __DSPOT_object_3766 = new FilterStreamType(new SystemType("x)4&hV3[*<aOg$i>WJE("), "Q:l,V@`8SGL^JO],{MNn", "D|Qr829;[>>MRtVCM(Bd");
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithDataAndVersion_add82655_add82977__1 = FilterStreamType.unserialize("type+data/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add82977__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add82977__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add82977__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add82977__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add82977__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add82977__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add82977__1)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithDataAndVersion_add82655__1 = FilterStreamType.unserialize("type+data/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type+data/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        boolean o_testUnserializeWithDataAndVersion_add82655_add82977_mg88376__16 = o_testUnserializeWithDataAndVersion_add82655_add82977__1.equals(__DSPOT_object_3766);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testUnserializeWithDataAndVersion_add82655_add82977_mg88376__16);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add82977__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add82977__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add82977__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add82977__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add82977__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add82977__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add82977__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithDataAndVersion_add82655_add83069_mg89568() throws Exception {
        FilterStreamType __DSPOT_o_3842 = new FilterStreamType(new SystemType("A7b>^Vt,G@N?xp>4xrUk"), "z.yaqy@+s kkO19>][7;");
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithDataAndVersion_add82655__1 = FilterStreamType.unserialize("type+data/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithDataAndVersion_add82655_add83069__4 = FilterStreamType.unserialize("type+data/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type+data/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        int o_testUnserializeWithDataAndVersion_add82655_add83069_mg89568__16 = o_testUnserializeWithDataAndVersion_add82655__1.compareTo(__DSPOT_o_3842);
        // AssertGenerator add assertion
        Assert.assertEquals(19, ((int) (o_testUnserializeWithDataAndVersion_add82655_add83069_mg89568__16)));
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithDataAndVersion_add82655_add83069__4)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithData() throws Exception {
        FilterStreamType type = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithData_mg73208() throws Exception {
        FilterStreamType __DSPOT_o_2899 = new FilterStreamType(new SystemType("k>zZx]r6GC(*Y3CklvK8"), "Kx$4go#(Deg*V.a1O^|!");
        FilterStreamType type = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        int o_testUnserializeWithData_mg73208__10 = type.compareTo(__DSPOT_o_2899);
        // AssertGenerator add assertion
        Assert.assertEquals(9, ((int) (o_testUnserializeWithData_mg73208__10)));
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithData_mg73209() throws Exception {
        FilterStreamType __DSPOT_object_2900 = new FilterStreamType(new SystemType("P{lzSH1|AD61!Viixq;J"), "FpePK/vM^8RbsPht&sx_", "N#2UdI,x!u8iddM ke)=");
        FilterStreamType type = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        boolean o_testUnserializeWithData_mg73209__10 = type.equals(__DSPOT_object_2900);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testUnserializeWithData_mg73209__10);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithData_add73205() throws Exception {
        FilterStreamType type = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType();
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithData_add73203_mg73932() throws Exception {
        FilterStreamType __DSPOT_o_2939 = new FilterStreamType(new SystemType("yKj a#YR-Nn07DNOV]^#"), "a$ewq#+@tT?Ve%@OwzEj", "R;1]:`<7>.iL+iaiS/Ru");
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithData_add73203__1 = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        int o_testUnserializeWithData_add73203_mg73932__13 = type.compareTo(__DSPOT_o_2939);
        // AssertGenerator add assertion
        Assert.assertEquals(-5, ((int) (o_testUnserializeWithData_add73203_mg73932__13)));
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithData_add73203_mg73933() throws Exception {
        FilterStreamType __DSPOT_object_2940 = new FilterStreamType(new SystemType("6u1ew+[+4U(^!s<e=*!<"), "iVYu{:Qpc!jVY {#D|<A", "Y]v`(1QW$J|mF=n0>ud!");
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithData_add73203__1 = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        boolean o_testUnserializeWithData_add73203_mg73933__13 = type.equals(__DSPOT_object_2940);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testUnserializeWithData_add73203_mg73933__13);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithData_add73203_add73898() throws Exception {
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithData_add73203__1 = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        ((FilterStreamType) (type)).toString();
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithData_add73203_add73773_add80334() throws Exception {
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithData_add73203__1 = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithData_add73203_add73773__4 = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getDataFormat();
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithData_add73203_add73773litString79553_failAssert208() throws Exception {
        // AssertGenerator generate try/catch block with fail statement
        try {
            // AssertGenerator create local variable with return value of invocation
            FilterStreamType o_testUnserializeWithData_add73203__1 = FilterStreamType.unserialize("+ype+data");
            // AssertGenerator create local variable with return value of invocation
            FilterStreamType o_testUnserializeWithData_add73203_add73773__4 = FilterStreamType.unserialize("type+data");
            FilterStreamType type = FilterStreamType.unserialize("type+data");
            type.getType().getId();
            type.getDataFormat();
            type.getVersion();
            org.junit.Assert.fail("testUnserializeWithData_add73203_add73773litString79553 should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            Assert.assertEquals("\'+\' is invalid as first character: +ype+data", expected.getMessage());
        }
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithData_add73203_add73773_mg80591() throws Exception {
        Object __DSPOT_object_3309 = new Object();
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithData_add73203__1 = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithData_add73203_add73773__4 = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        boolean o_testUnserializeWithData_add73203_add73773_mg80591__15 = o_testUnserializeWithData_add73203__1.equals(__DSPOT_object_3309);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testUnserializeWithData_add73203_add73773_mg80591__15);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203_add73773__4)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithData_add73203_add73623_mg81866() throws Exception {
        FilterStreamType __DSPOT_o_3414 = new FilterStreamType(new SystemType("|`y^P$LA*`EmQ[&[!NBI"), "dfd&5}Z,s1z6vgYl8?/V");
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithData_add73203_add73623__1 = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203_add73623__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73623__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73623__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73623__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73623__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203_add73623__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203_add73623__1)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithData_add73203__1 = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type+data");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        int o_testUnserializeWithData_add73203_add73623_mg81866__16 = o_testUnserializeWithData_add73203__1.compareTo(__DSPOT_o_3414);
        // AssertGenerator add assertion
        Assert.assertEquals(-8, ((int) (o_testUnserializeWithData_add73203_add73623_mg81866__16)));
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203_add73623__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73623__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73623__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73623__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203_add73623__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203_add73623__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203_add73623__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithData_add73203__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714716265, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyData_mg91025() throws Exception {
        FilterStreamType __DSPOT_o_3910 = new FilterStreamType(new SystemType("}y[V0&.pcHUQG|&D=UtU"), "i:y*XDvMsA#n0z8kQbrZ");
        FilterStreamType type = FilterStreamType.unserialize("type+");
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        int o_testUnserializeWithEmptyData_mg91025__10 = type.compareTo(__DSPOT_o_3910);
        // AssertGenerator add assertion
        Assert.assertEquals(-9, ((int) (o_testUnserializeWithEmptyData_mg91025__10)));
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyData_add91024() throws Exception {
        FilterStreamType type = FilterStreamType.unserialize("type+");
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        type.getVersion();
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyData_mg91026() throws Exception {
        FilterStreamType __DSPOT_object_3911 = new FilterStreamType(new SystemType("8WuE&DdAUZ;4MOA{DS-F"), "7:am1xeOE:Z:l8>`F1A<");
        FilterStreamType type = FilterStreamType.unserialize("type+");
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        boolean o_testUnserializeWithEmptyData_mg91026__10 = type.equals(__DSPOT_object_3911);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testUnserializeWithEmptyData_mg91026__10);
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyData_add91020_mg91737() throws Exception {
        Object __DSPOT_object_3950 = new Object();
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithEmptyData_add91020__1 = FilterStreamType.unserialize("type+");
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type+");
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        boolean o_testUnserializeWithEmptyData_add91020_mg91737__12 = o_testUnserializeWithEmptyData_add91020__1.equals(__DSPOT_object_3950);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testUnserializeWithEmptyData_add91020_mg91737__12);
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyData_add91020_mg91739() throws Exception {
        FilterStreamType __DSPOT_o_3951 = new FilterStreamType(new SystemType("07@]`$[1=B-?&Zlc1&WH"), "c}oHRjF)];F:1Da1rD-!");
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithEmptyData_add91020__1 = FilterStreamType.unserialize("type+");
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type+");
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        int o_testUnserializeWithEmptyData_add91020_mg91739__13 = type.compareTo(__DSPOT_o_3951);
        // AssertGenerator add assertion
        Assert.assertEquals(68, ((int) (o_testUnserializeWithEmptyData_add91020_mg91739__13)));
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyDatalitString91013_remove91614() throws Exception {
        FilterStreamType type = FilterStreamType.unserialize("tTpe+");
        // AssertGenerator add assertion
        Assert.assertEquals("ttpe+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(594325850, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("ttpe", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3570805, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("ttpe", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator add assertion
        Assert.assertEquals("ttpe+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(594325850, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("ttpe", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3570805, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("ttpe", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyData_add91020_add91693_mg97055() throws Exception {
        FilterStreamType __DSPOT_object_4262 = new FilterStreamType(new SystemType("M)|9z-3v#q})+yl!V^g@"), "{C@8x&qa|H<d`Hj?JE3(", "/$xzNalF?[PzSF=&Km-m");
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithEmptyData_add91020__1 = FilterStreamType.unserialize("type+");
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithEmptyData_add91020_add91693__4 = FilterStreamType.unserialize("type+");
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91693__4)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91693__4)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91693__4)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91693__4)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91693__4)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91693__4)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91693__4)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type+");
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        boolean o_testUnserializeWithEmptyData_add91020_add91693_mg97055__16 = o_testUnserializeWithEmptyData_add91020__1.equals(__DSPOT_object_4262);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testUnserializeWithEmptyData_add91020_add91693_mg97055__16);
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91693__4)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91693__4)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91693__4)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91693__4)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91693__4)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91693__4)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91693__4)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyData_add91020_add91632_add98263() throws Exception {
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithEmptyData_add91020_add91632__1 = FilterStreamType.unserialize("type+");
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithEmptyData_add91020__1 = FilterStreamType.unserialize("type+");
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type+");
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        ((FilterStreamType) (type)).getVersion();
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyData_add91020_add91632_mg98416() throws Exception {
        FilterStreamType __DSPOT_o_4348 = new FilterStreamType(new SystemType("Z]C rm$!ze&po@?84`W5"), "+/Hs|3!rSvu6P=Gx6JBC");
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithEmptyData_add91020_add91632__1 = FilterStreamType.unserialize("type+");
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithEmptyData_add91020__1 = FilterStreamType.unserialize("type+");
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type+");
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        int o_testUnserializeWithEmptyData_add91020_add91632_mg98416__16 = type.compareTo(__DSPOT_o_4348);
        // AssertGenerator add assertion
        Assert.assertEquals(-6, ((int) (o_testUnserializeWithEmptyData_add91020_add91632_mg98416__16)));
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyData_add91020_add91632__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyData_add91020__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithVersion_mg109890() throws Exception {
        FilterStreamType __DSPOT_object_4992 = new FilterStreamType(new SystemType("?q(l#Z:dTWOE7V#,(-z{"), "*M<!xHauOVu:n+$jsghP", "@>-!&JRzzKomc,8n2cl!");
        FilterStreamType type = FilterStreamType.unserialize("type/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        boolean o_testUnserializeWithVersion_mg109890__10 = type.equals(__DSPOT_object_4992);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testUnserializeWithVersion_mg109890__10);
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithVersion_add109886() throws Exception {
        FilterStreamType type = FilterStreamType.unserialize("type/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        type.getType();
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithVersion_mg109889() throws Exception {
        FilterStreamType __DSPOT_o_4991 = new FilterStreamType(new SystemType("uY@[+aV$5eZw_Qb^?9w4"), ")l^`weHPu43Pu.M?;D0w");
        FilterStreamType type = FilterStreamType.unserialize("type/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        int o_testUnserializeWithVersion_mg109889__10 = type.compareTo(__DSPOT_o_4991);
        // AssertGenerator add assertion
        Assert.assertEquals(-1, ((int) (o_testUnserializeWithVersion_mg109889__10)));
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithVersion_add109884_mg110611() throws Exception {
        Object __DSPOT_object_5031 = new Object();
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithVersion_add109884__1 = FilterStreamType.unserialize("type/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        boolean o_testUnserializeWithVersion_add109884_mg110611__12 = o_testUnserializeWithVersion_add109884__1.equals(__DSPOT_object_5031);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testUnserializeWithVersion_add109884_mg110611__12);
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithVersion_add109884_mg110613() throws Exception {
        FilterStreamType __DSPOT_o_5032 = new FilterStreamType(new SystemType("0##Iqf40$J8Pb&O<fKKd"), "C@GuY%8W##0%F#$qlgf_");
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithVersion_add109884__1 = FilterStreamType.unserialize("type/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        int o_testUnserializeWithVersion_add109884_mg110613__13 = type.compareTo(__DSPOT_o_5032);
        // AssertGenerator add assertion
        Assert.assertEquals(68, ((int) (o_testUnserializeWithVersion_add109884_mg110613__13)));
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithVersionlitString109877_remove110549() throws Exception {
        FilterStreamType type = FilterStreamType.unserialize("type/vmrsion");
        // AssertGenerator add assertion
        Assert.assertEquals("type/vmrsion", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1181545127, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("vmrsion", ((FilterStreamType) (type)).getVersion());
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator add assertion
        Assert.assertEquals("type/vmrsion", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1181545127, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("vmrsion", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithVersion_add109884_add110497_mg118097() throws Exception {
        FilterStreamType __DSPOT_object_5476 = new FilterStreamType(new SystemType("wt68(vSgA$Vpsh#E3{tT"), "@y@=4R<GtOLdnRMY49ti");
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithVersion_add109884_add110497__1 = FilterStreamType.unserialize("type/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithVersion_add109884__1 = FilterStreamType.unserialize("type/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        boolean o_testUnserializeWithVersion_add109884_add110497_mg118097__16 = o_testUnserializeWithVersion_add109884_add110497__1.equals(__DSPOT_object_5476);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testUnserializeWithVersion_add109884_add110497_mg118097__16);
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithVersion_add109884_add110563_mg118346() throws Exception {
        FilterStreamType __DSPOT_o_5493 = new FilterStreamType(new SystemType("o;S[ZU,vmJaamk{)ZK]P"), "]|h0mnN>}jkN8GvdY*<O", "zz*!YK1aI,uK.uDny{29");
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithVersion_add109884__1 = FilterStreamType.unserialize("type/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithVersion_add109884_add110563__4 = FilterStreamType.unserialize("type/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110563__4)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110563__4)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110563__4)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110563__4)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110563__4)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110563__4)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110563__4)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        int o_testUnserializeWithVersion_add109884_add110563_mg118346__16 = o_testUnserializeWithVersion_add109884__1.compareTo(__DSPOT_o_5493);
        // AssertGenerator add assertion
        Assert.assertEquals(5, ((int) (o_testUnserializeWithVersion_add109884_add110563_mg118346__16)));
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110563__4)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110563__4)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110563__4)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110563__4)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110563__4)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110563__4)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110563__4)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithVersion_add109884_add110497_add118011() throws Exception {
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithVersion_add109884_add110497__1 = FilterStreamType.unserialize("type/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithVersion_add109884__1 = FilterStreamType.unserialize("type/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type/version");
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        ((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getVersion();
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884_add110497__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (o_testUnserializeWithVersion_add109884__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(952511919, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyVersion_add100471() throws Exception {
        FilterStreamType type = FilterStreamType.unserialize("type/");
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
        type.getType();
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyVersion_mg100475() throws Exception {
        Object __DSPOT_object_4453 = new Object();
        FilterStreamType type = FilterStreamType.unserialize("type/");
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        boolean o_testUnserializeWithEmptyVersion_mg100475__9 = type.equals(__DSPOT_object_4453);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testUnserializeWithEmptyVersion_mg100475__9);
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyVersion_mg100474() throws Exception {
        FilterStreamType __DSPOT_o_4452 = new FilterStreamType(new SystemType(" F0]40WL:Qy]^#N]E[Y]"), "R<ncA5&L$mj]JT}L!xL_");
        FilterStreamType type = FilterStreamType.unserialize("type/");
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        int o_testUnserializeWithEmptyVersion_mg100474__10 = type.compareTo(__DSPOT_o_4452);
        // AssertGenerator add assertion
        Assert.assertEquals(84, ((int) (o_testUnserializeWithEmptyVersion_mg100474__10)));
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyVersion_add100469_mg101168() throws Exception {
        FilterStreamType __DSPOT_o_4489 = new FilterStreamType(new SystemType("|sH8%f)U<#pY^LH} t<5"), "nX{AL;mw/[(>sFNrbd}I");
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithEmptyVersion_add100469__1 = FilterStreamType.unserialize("type/");
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type/");
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        int o_testUnserializeWithEmptyVersion_add100469_mg101168__13 = o_testUnserializeWithEmptyVersion_add100469__1.compareTo(__DSPOT_o_4489);
        // AssertGenerator add assertion
        Assert.assertEquals(-8, ((int) (o_testUnserializeWithEmptyVersion_add100469_mg101168__13)));
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyVersion_add100469_mg101169() throws Exception {
        FilterStreamType __DSPOT_object_4490 = new FilterStreamType(new SystemType("]^q{_{awt`3w%?rx0aC9"), "8#8z+8`R#@b&%vxsJO2A");
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithEmptyVersion_add100469__1 = FilterStreamType.unserialize("type/");
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type/");
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        boolean o_testUnserializeWithEmptyVersion_add100469_mg101169__13 = o_testUnserializeWithEmptyVersion_add100469__1.equals(__DSPOT_object_4490);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testUnserializeWithEmptyVersion_add100469_mg101169__13);
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyVersion_add100473litString100996() throws Exception {
        FilterStreamType type = FilterStreamType.unserialize("JRJ9N");
        // AssertGenerator add assertion
        Assert.assertEquals("jrj9n", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1369120396, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("jrj9n", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(101393143, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("jrj9n", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        type.getVersion();
        // AssertGenerator add assertion
        Assert.assertEquals("jrj9n", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1369120396, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("jrj9n", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(101393143, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("jrj9n", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyVersion_add100469_add101049_mg107546() throws Exception {
        Object __DSPOT_object_4871 = new Object();
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithEmptyVersion_add100469_add101049__1 = FilterStreamType.unserialize("type/");
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithEmptyVersion_add100469__1 = FilterStreamType.unserialize("type/");
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type/");
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        boolean o_testUnserializeWithEmptyVersion_add100469_add101049_mg107546__15 = type.equals(__DSPOT_object_4871);
        // AssertGenerator add assertion
        Assert.assertFalse(o_testUnserializeWithEmptyVersion_add100469_add101049_mg107546__15);
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyVersion_add100469_add101049_add107174() throws Exception {
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithEmptyVersion_add100469_add101049__1 = FilterStreamType.unserialize("type/");
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithEmptyVersion_add100469__1 = FilterStreamType.unserialize("type/");
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type/");
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getVersion();
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void testUnserializeWithEmptyVersion_add100469_add101049_mg107534() throws Exception {
        FilterStreamType __DSPOT_o_4870 = new FilterStreamType(new SystemType("f#}Lw@|]10d6RaDz{pCB"), "C}QHday]oCmT^$kNl^z$", "Scz_Fs(n:l}%(K=0<wLh");
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithEmptyVersion_add100469_add101049__1 = FilterStreamType.unserialize("type/");
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        FilterStreamType o_testUnserializeWithEmptyVersion_add100469__1 = FilterStreamType.unserialize("type/");
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getVersion());
        FilterStreamType type = FilterStreamType.unserialize("type/");
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
        type.getType().getId();
        type.getDataFormat();
        type.getVersion();
        // AssertGenerator create local variable with return value of invocation
        int o_testUnserializeWithEmptyVersion_add100469_add101049_mg107534__16 = type.compareTo(__DSPOT_o_4870);
        // AssertGenerator add assertion
        Assert.assertEquals(14, ((int) (o_testUnserializeWithEmptyVersion_add100469_add101049_mg107534__16)));
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469_add101049__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (o_testUnserializeWithEmptyVersion_add100469__1)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type/", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(600903895, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertNull(((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void equalslitString32695() throws Exception {
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        new FilterStreamType(new SystemType("type"), "data", "ve@sion");
        new FilterStreamType(new SystemType("type2"), "data", "version");
        new FilterStreamType(new SystemType("type"), "data2", "version");
        new FilterStreamType(new SystemType("type"), "data", "version2");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void equals_mg32774() throws Exception {
        FilterStreamType __DSPOT_o_1363 = new FilterStreamType(new SystemType("rf7OG!f9wm%rWk#b{#w/"), "?+[j$+/XxI/z}SL_$h@(");
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        new FilterStreamType(new SystemType("type"), "data", "version");
        new FilterStreamType(new SystemType("type2"), "data", "version");
        new FilterStreamType(new SystemType("type"), "data2", "version");
        new FilterStreamType(new SystemType("type"), "data", "version2");
        // AssertGenerator create local variable with return value of invocation
        int o_equals_mg32774__15 = type.compareTo(__DSPOT_o_1363);
        // AssertGenerator add assertion
        Assert.assertEquals(2, ((int) (o_equals_mg32774__15)));
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void equals_mg32775() throws Exception {
        Object __DSPOT_object_1364 = new Object();
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        new FilterStreamType(new SystemType("type"), "data", "version");
        new FilterStreamType(new SystemType("type2"), "data", "version");
        new FilterStreamType(new SystemType("type"), "data2", "version");
        new FilterStreamType(new SystemType("type"), "data", "version2");
        // AssertGenerator create local variable with return value of invocation
        boolean o_equals_mg32775__14 = type.equals(__DSPOT_object_1364);
        // AssertGenerator add assertion
        Assert.assertFalse(o_equals_mg32775__14);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void equals_mg32775_mg38800() throws Exception {
        FilterStreamType __DSPOT_object_1510 = new FilterStreamType(new SystemType("F*%ul$A0ek dWc&@ (Db"), "pVJ8X_W&gz_LY|Qc#eq&");
        Object __DSPOT_object_1364 = new Object();
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        new FilterStreamType(new SystemType("type"), "data", "version");
        new FilterStreamType(new SystemType("type2"), "data", "version");
        new FilterStreamType(new SystemType("type"), "data2", "version");
        new FilterStreamType(new SystemType("type"), "data", "version2");
        // AssertGenerator create local variable with return value of invocation
        boolean o_equals_mg32775__14 = type.equals(__DSPOT_object_1364);
        // AssertGenerator create local variable with return value of invocation
        boolean o_equals_mg32775_mg38800__20 = type.equals(__DSPOT_object_1510);
        // AssertGenerator add assertion
        Assert.assertFalse(o_equals_mg32775_mg38800__20);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void equals_mg32774null37663_failAssert0() throws Exception {
        // AssertGenerator generate try/catch block with fail statement
        try {
            FilterStreamType __DSPOT_o_1363 = new FilterStreamType(new SystemType("rf7OG!f9wm%rWk#b{#w/"), "?+[j$+/XxI/z}SL_$h@(");
            FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");
            new FilterStreamType(new SystemType("type"), "data", "version");
            new FilterStreamType(new SystemType("type2"), "data", "version");
            new FilterStreamType(new SystemType("type"), "data2", "version");
            new FilterStreamType(new SystemType("type"), "data", "version2");
            // AssertGenerator create local variable with return value of invocation
            int o_equals_mg32774__15 = type.compareTo(null);
            org.junit.Assert.fail("equals_mg32774null37663 should have thrown NullPointerException");
        } catch (NullPointerException expected) {
            Assert.assertEquals(null, expected.getMessage());
        }
    }

    @org.junit.Test(timeout = 10000)
    public void equals_mg32774litString37277() throws Exception {
        FilterStreamType __DSPOT_o_1363 = new FilterStreamType(new SystemType("rf7OG!f9wm%rWk#b{#w/"), "?+[j$+/XxI/z}SL_$h@(");
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        new FilterStreamType(new SystemType("type"), "data", "version");
        new FilterStreamType(new SystemType("type2"), "data", "version");
        new FilterStreamType(new SystemType("type"), "data2", "version");
        new FilterStreamType(new SystemType("type"), "data", "versiton2");
        // AssertGenerator create local variable with return value of invocation
        int o_equals_mg32774__15 = type.compareTo(__DSPOT_o_1363);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void equals_mg32774null37663_failAssert0_mg59943_failAssert139() throws Exception {
        // AssertGenerator generate try/catch block with fail statement
        try {
            {
                FilterStreamType __DSPOT_o_1363 = new FilterStreamType(new SystemType("rf7OG!f9wm%rWk#b{#w/"), "?+[j$+/XxI/z}SL_$h@(");
                FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");
                new FilterStreamType(new SystemType("type"), "data", "version");
                new FilterStreamType(new SystemType("type2"), "data", "version");
                new FilterStreamType(new SystemType("type"), "data2", "version");
                new FilterStreamType(new SystemType("type"), "data", "version2");
                // AssertGenerator create local variable with return value of invocation
                int o_equals_mg32774__15 = type.compareTo(null);
                org.junit.Assert.fail("equals_mg32774null37663 should have thrown NullPointerException");
                __DSPOT_o_1363.serialize();
            }
            org.junit.Assert.fail("equals_mg32774null37663_failAssert0_mg59943 should have thrown NullPointerException");
        } catch (NullPointerException expected) {
            Assert.assertEquals(null, expected.getMessage());
        }
    }

    @org.junit.Test(timeout = 10000)
    public void equals_mg32776_mg39858_add61432() throws Exception {
        FilterStreamType __DSPOT_o_1531 = new FilterStreamType(new SystemType("C7QE]Sb.g5{8UXfw{BHn"), "[Evsr&zBY2fHBr/[P!ne");
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        ((SystemType) (((FilterStreamType) (type)).getType())).toString();
        new FilterStreamType(new SystemType("type"), "data", "version");
        new FilterStreamType(new SystemType("type2"), "data", "version");
        new FilterStreamType(new SystemType("type"), "data2", "version");
        new FilterStreamType(new SystemType("type"), "data", "version2");
        // AssertGenerator create local variable with return value of invocation
        String o_equals_mg32776__12 = type.serialize();
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_equals_mg32776__12);
        // AssertGenerator create local variable with return value of invocation
        int o_equals_mg32776_mg39858__18 = type.compareTo(__DSPOT_o_1531);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", o_equals_mg32776__12);
    }

    @org.junit.Test(timeout = 10000)
    public void equals_mg32775_mg38800_mg62062() throws Exception {
        FilterStreamType __DSPOT_object_2245 = new FilterStreamType(new SystemType("2aZ-0hxE}K3e.G7a>zY6"), "[0)R8n2&w|[*B9[tLmX|", "6TdV23psp8Y3Pn&*IZlw");
        FilterStreamType __DSPOT_object_1510 = new FilterStreamType(new SystemType("F*%ul$A0ek dWc&@ (Db"), "pVJ8X_W&gz_LY|Qc#eq&");
        Object __DSPOT_object_1364 = new Object();
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "version");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
        new FilterStreamType(new SystemType("type"), "data", "version");
        new FilterStreamType(new SystemType("type2"), "data", "version");
        new FilterStreamType(new SystemType("type"), "data2", "version");
        new FilterStreamType(new SystemType("type"), "data", "version2");
        // AssertGenerator create local variable with return value of invocation
        boolean o_equals_mg32775__14 = type.equals(__DSPOT_object_1364);
        // AssertGenerator create local variable with return value of invocation
        boolean o_equals_mg32775_mg38800__20 = type.equals(__DSPOT_object_1510);
        // AssertGenerator create local variable with return value of invocation
        boolean o_equals_mg32775_mg38800_mg62062__26 = __DSPOT_object_1510.equals(__DSPOT_object_2245);
        // AssertGenerator add assertion
        Assert.assertFalse(o_equals_mg32775_mg38800_mg62062__26);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/version", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(1066324289, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("version", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void compareTo() throws Exception {
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "2.0");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo__4 = type.compareTo(new FilterStreamType(new SystemType("type"), "data", "2.0"));
        // AssertGenerator add assertion
        Assert.assertEquals(0, ((int) (o_compareTo__4)));
        boolean boolean_0 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "1.0"))) > 0;
        boolean boolean_1 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "3.0"))) < 0;
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals(0, ((int) (o_compareTo__4)));
    }

    @org.junit.Test(timeout = 10000)
    public void compareTolitString34() throws Exception {
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "2.0");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        int o_compareTolitString34__4 = type.compareTo(new FilterStreamType(new SystemType("type"), "d|ata", "2.0"));
        // AssertGenerator add assertion
        Assert.assertEquals(-27, ((int) (o_compareTolitString34__4)));
        boolean boolean_68 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "1.0"))) > 0;
        boolean boolean_69 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "3.0"))) < 0;
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals(-27, ((int) (o_compareTolitString34__4)));
    }

    @org.junit.Test(timeout = 10000)
    public void compareTo_mg100() throws Exception {
        FilterStreamType __DSPOT_object_658 = new FilterStreamType(new SystemType("Zo^f3TBJA+KQleH:!l74"), "E&<>g32*bUYE=Vv`DP$=");
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "2.0");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_mg100__7 = type.compareTo(new FilterStreamType(new SystemType("type"), "data", "2.0"));
        // AssertGenerator add assertion
        Assert.assertEquals(0, ((int) (o_compareTo_mg100__7)));
        boolean boolean_200 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "1.0"))) > 0;
        boolean boolean_201 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "3.0"))) < 0;
        // AssertGenerator create local variable with return value of invocation
        boolean o_compareTo_mg100__18 = type.equals(__DSPOT_object_658);
        // AssertGenerator add assertion
        Assert.assertFalse(o_compareTo_mg100__18);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals(0, ((int) (o_compareTo_mg100__7)));
    }

    @org.junit.Test(timeout = 10000)
    public void compareTo_add97_add6466() throws Exception {
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "2.0");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_add97__4 = type.compareTo(new FilterStreamType(new SystemType("type"), "data", "2.0"));
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_add97_add6466__9 = type.compareTo(new FilterStreamType(new SystemType("type"), "data", "1.0"));
        // AssertGenerator add assertion
        Assert.assertEquals(1, ((int) (o_compareTo_add97_add6466__9)));
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_add97__7 = type.compareTo(new FilterStreamType(new SystemType("type"), "data", "1.0"));
        boolean boolean_194 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "1.0"))) > 0;
        boolean boolean_195 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "3.0"))) < 0;
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals(1, ((int) (o_compareTo_add97_add6466__9)));
    }

    @org.junit.Test(timeout = 10000)
    public void compareTo_add97_add6410() throws Exception {
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "2.0");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_add97_add6410__4 = type.compareTo(new FilterStreamType(new SystemType("type"), "data", "2.0"));
        // AssertGenerator add assertion
        Assert.assertEquals(0, ((int) (o_compareTo_add97_add6410__4)));
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_add97__4 = type.compareTo(new FilterStreamType(new SystemType("type"), "data", "2.0"));
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_add97__7 = type.compareTo(new FilterStreamType(new SystemType("type"), "data", "1.0"));
        boolean boolean_194 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "1.0"))) > 0;
        boolean boolean_195 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "3.0"))) < 0;
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator add assertion
        Assert.assertEquals(0, ((int) (o_compareTo_add97_add6410__4)));
    }

    @org.junit.Test(timeout = 10000)
    public void compareTo_mg99_mg5231() throws Exception {
        Object __DSPOT_object_784 = new Object();
        FilterStreamType __DSPOT_o_657 = new FilterStreamType(new SystemType("M{vpovoB/#[(3](cc@8d"), "s^|ph8l:x7i9fRUovI)b", "lU,S{O)NJ1xd5yDr<}0+");
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "2.0");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_mg99__7 = type.compareTo(new FilterStreamType(new SystemType("type"), "data", "2.0"));
        boolean boolean_198 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "1.0"))) > 0;
        boolean boolean_199 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "3.0"))) < 0;
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_mg99__18 = type.compareTo(__DSPOT_o_657);
        // AssertGenerator create local variable with return value of invocation
        boolean o_compareTo_mg99_mg5231__25 = type.equals(__DSPOT_object_784);
        // AssertGenerator add assertion
        Assert.assertFalse(o_compareTo_mg99_mg5231__25);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void compareTo_add97_add6405() throws Exception {
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "2.0");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
        ((FilterStreamType) (type)).getVersion();
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_add97__4 = type.compareTo(new FilterStreamType(new SystemType("type"), "data", "2.0"));
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_add97__7 = type.compareTo(new FilterStreamType(new SystemType("type"), "data", "1.0"));
        boolean boolean_194 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "1.0"))) > 0;
        boolean boolean_195 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "3.0"))) < 0;
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void compareTo_add98_add3737_mg27376() throws Exception {
        FilterStreamType __DSPOT_object_1270 = new FilterStreamType(new SystemType("R<I*u1##@4p?o5A&b-92"), "}ScoxJRk_QM!a4[v.3 @");
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "2.0");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_add98__4 = type.compareTo(new FilterStreamType(new SystemType("type"), "data", "2.0"));
        boolean boolean_196 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "1.0"))) > 0;
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_add98_add3737__13 = type.compareTo(new FilterStreamType(new SystemType("type"), "data", "3.0"));
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_add98__11 = type.compareTo(new FilterStreamType(new SystemType("type"), "data", "3.0"));
        boolean boolean_197 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "3.0"))) < 0;
        // AssertGenerator create local variable with return value of invocation
        boolean o_compareTo_add98_add3737_mg27376__30 = type.equals(__DSPOT_object_1270);
        // AssertGenerator add assertion
        Assert.assertFalse(o_compareTo_add98_add3737_mg27376__30);
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
    }

    @org.junit.Test(timeout = 10000)
    public void compareTo_mg99_add5087_mg22689() throws Exception {
        FilterStreamType __DSPOT_o_1181 = new FilterStreamType(new SystemType("OFdf]Ee<4n},]6Ex*lQV"), "u<Ym)V@[c[Nn)=rZ8)-0", "; IOE-88[tF7yE.eNilS");
        FilterStreamType __DSPOT_o_657 = new FilterStreamType(new SystemType("M{vpovoB/#[(3](cc@8d"), "s^|ph8l:x7i9fRUovI)b", "lU,S{O)NJ1xd5yDr<}0+");
        FilterStreamType type = new FilterStreamType(new SystemType("type"), "data", "2.0");
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_mg99__7 = type.compareTo(new FilterStreamType(new SystemType("type"), "data", "2.0"));
        boolean boolean_198 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "1.0"))) > 0;
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_mg99_add5087__16 = type.compareTo(new FilterStreamType(new SystemType("type"), "data", "3.0"));
        boolean boolean_199 = (type.compareTo(new FilterStreamType(new SystemType("type"), "data", "3.0"))) < 0;
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_mg99__18 = type.compareTo(__DSPOT_o_657);
        // AssertGenerator create local variable with return value of invocation
        int o_compareTo_mg99_add5087_mg22689__31 = __DSPOT_o_657.compareTo(__DSPOT_o_1181);
        // AssertGenerator add assertion
        Assert.assertEquals(-2, ((int) (o_compareTo_mg99_add5087_mg22689__31)));
        // AssertGenerator add assertion
        Assert.assertEquals("type+data/2.0", ((FilterStreamType) (type)).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(714765789, ((int) (((FilterStreamType) (type)).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).toString());
        // AssertGenerator add assertion
        Assert.assertEquals(3575610, ((int) (((SystemType) (((FilterStreamType) (type)).getType())).hashCode())));
        // AssertGenerator add assertion
        Assert.assertEquals("type", ((SystemType) (((FilterStreamType) (type)).getType())).getId());
        // AssertGenerator add assertion
        Assert.assertEquals("data", ((FilterStreamType) (type)).getDataFormat());
        // AssertGenerator add assertion
        Assert.assertEquals("2.0", ((FilterStreamType) (type)).getVersion());
    }
}

