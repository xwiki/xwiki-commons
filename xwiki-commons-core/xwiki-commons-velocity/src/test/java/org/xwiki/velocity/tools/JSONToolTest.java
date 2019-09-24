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
package org.xwiki.velocity.tools;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import net.sf.json.JSON;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link JSONTool}.
 *
 * @version $Id$
 * @since 4.0M2
 */
public class JSONToolTest
{
    public static class MockBean
    {
        public boolean isEnabled()
        {
            return true;
        }

        public int getAge()
        {
            return 28;
        }

        public double getGrade()
        {
            return 9.48;
        }

        public String getName()
        {
            return "XWiki";
        }

        public List<String> getItems()
        {
            return Arrays.asList("one");
        }

        public Map<String, String> getParameters()
        {
            return Collections.singletonMap("foo", "bar");
        }

        @Transient
        public String getTransientProperty()
        {
            return "transient";
        }
    }

    /**
     * The object being tested.
     */
    private JSONTool tool = new JSONTool();

    @Test
    public void serializeNull()
    {
        assertEquals("null", this.tool.serialize(null));
    }

    @Test
    public void serializeMap()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("bool", false);
        map.put("int", 13);
        map.put("double", 0.78);
        map.put("string", "foo");
        map.put("array", new int[] { 9, 8 });
        map.put("list", Arrays.asList("one", "two"));
        map.put("map", Collections.singletonMap("level2", true));
        map.put("null", "null key value");

        String json = this.tool.serialize(map);
        // We can't predict the order in the map.
        assertTrue(json.contains("\"bool\":false"));
        assertTrue(json.contains("\"int\":13"));
        assertTrue(json.contains("\"double\":0.78"));
        assertTrue(json.contains("\"string\":\"foo\""));
        assertTrue(json.contains("\"array\":[9,8]"));
        assertTrue(json.contains("\"list\":[\"one\",\"two\"]"));
        assertTrue(json.contains("\"map\":{\"level2\":true}"));
        assertTrue(json.contains("\"null\":\"null key value\""));
    }

    @Test
    public void serializeList()
    {
        assertEquals("[1,2]", this.tool.serialize(Arrays.asList(1, 2)));
        assertEquals("[1.3,2.4]", this.tool.serialize(new double[] { 1.3, 2.4 }));
    }

    @Test
    public void serializeNumber()
    {
        assertEquals("27", this.tool.serialize(27));
        assertEquals("2.7", this.tool.serialize(2.7));
    }

    @Test
    public void serializeBoolean()
    {
        assertEquals("false", this.tool.serialize(false));
        assertEquals("true", this.tool.serialize(true));
    }

    @Test
    public void serializeString()
    {
        assertEquals("\"\\\"te'st\\\"\"", this.tool.serialize("\"te'st\""));
    }

    @Test
    public void serializeBean()
    {
        String json = this.tool.serialize(new MockBean());
        // We can't predict the order in the map.
        assertTrue(json.contains("\"age\":28"));
        assertTrue(json.contains("\"enabled\":true"));
        assertTrue(json.contains("\"grade\":9.48"));
        assertTrue(json.contains("\"items\":[\"one\"]"));
        assertTrue(json.contains("\"name\":\"XWiki\""));
        assertTrue(json.contains("\"parameters\":{\"foo\":\"bar\"}"));
        assertFalse(json.contains("\"transientProperty\":\"transient\""));
    }

    @Test
    public void parseArray()
    {
        JSON json = this.tool.parse("[1,2,3]");
        assertTrue(json.isArray());
        assertEquals(3, json.size());
    }

    @Test
    public void parseEmptyArray()
    {
        JSON json = this.tool.parse("[]");
        assertTrue(json.isArray());
        assertTrue(json.isEmpty());
        assertEquals(0, json.size());
    }

    @Test
    public void parseMap()
    {
        JSONObject json = (JSONObject) this.tool.parse("{\"a\" : 1, \"b\": [1], \"c\": true}");
        assertFalse(json.isArray());
        assertFalse(json.isEmpty());
        assertEquals(3, json.size());
        assertTrue(json.getBoolean("c"));
    }

    @Test
    public void parseEmptyMap()
    {
        JSONObject json = (JSONObject) this.tool.parse("{}");
        assertFalse(json.isArray());
        assertTrue(json.isEmpty());
        assertEquals(0, json.size());
    }

    @Test
    public void parseNull()
    {
        assertTrue(this.tool.parse(null) instanceof JSONNull);
    }

    @Test
    public void parseEmptyString()
    {
        assertNull(this.tool.parse(""));
    }

    @Test
    public void parseInvalidJSON()
    {
        assertNull(this.tool.parse("This is not the JSON you are looking for..."));
    }

    @Test
    public void fromStringArray()
    {
        assertEquals(Arrays.asList(1, 2, 3), this.tool.fromString("[1,2,3]"));
    }

    @Test
    public void fromStringEmptyArray()
    {
        assertEquals(new ArrayList<>(), this.tool.fromString("[]"));
    }

    @Test
    public void fromStringMap()
    {
        Map<?, ?> map = (Map) this.tool.fromString("{\"a\" : 1, \"b\": [1], \"c\": true}");

        assertEquals(3, map.size());
        assertEquals(1, map.get("a"));
        assertEquals(Arrays.asList(1), map.get("b"));
        assertEquals(true, map.get("c"));
    }

    @Test
    public void fromStringEmptyMap()
    {
        assertEquals(new HashMap(), this.tool.fromString("{}"));
    }

    @Test
    public void fromStringNull()
    {
        assertNull(this.tool.fromString(null));
    }

    @Test
    public void fromStringEmptyString()
    {
        assertNull(this.tool.fromString(""));
    }

    @Test
    public void fromStringInvalidJSON()
    {
        assertNull(this.tool.fromString("This is not the JSON you are looking for..."));
    }

    @Test
    public void serializeOrgJsonObjectWorks()
    {
        List<String> variants = new ArrayList<>(2);
        variants.add("{\"a\":\"b\",\"c\":true}");
        variants.add("{\"c\":true,\"a\":\"b\"}");
        org.json.JSONObject object = new org.json.JSONObject();
        object.put("a", "b");
        object.put("c", true);
        // assertEquals(variants.get(0), this.tool.serialize(object));
        assertTrue(variants.contains(this.tool.serialize(object)));
    }

    @Test
    public void serializeNestedOrgJsonObjectWorks()
    {
        List<String> variants = new ArrayList<>(2);
        variants.add("{\"before\":[\"nothing\"],\"json\":{\"a\":\"b\",\"c\":true},\"after\":42}");
        variants.add("{\"before\":[\"nothing\"],\"json\":{\"c\":true,\"a\":\"b\"},\"after\":42}");
        org.json.JSONObject object = new org.json.JSONObject();
        object.put("a", "b");
        object.put("c", true);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("before", Collections.singletonList("nothing"));
        map.put("json", object);
        map.put("after", 42);
        // assertEquals(variants.get(0), this.tool.serialize(map));
        assertTrue(variants.contains(this.tool.serialize(map)));
    }

    @Test
    public void serializeOrgJsonArrayWorks()
    {
        org.json.JSONArray array = new org.json.JSONArray();
        array.put("a");
        array.put(42);
        array.put(true);
        assertEquals("[\"a\",42,true]", this.tool.serialize(array));
    }

    @Test
    public void serializeNestedOrgJsonArrayWorks()
    {
        org.json.JSONArray array = new org.json.JSONArray();
        array.put("a");
        array.put(42);
        array.put(true);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("before", Collections.singletonList("nothing"));
        map.put("json", array);
        map.put("after", 42);
        assertEquals("{\"before\":[\"nothing\"],\"json\":[\"a\",42,true],\"after\":42}",
            this.tool.serialize(map));
    }
}
