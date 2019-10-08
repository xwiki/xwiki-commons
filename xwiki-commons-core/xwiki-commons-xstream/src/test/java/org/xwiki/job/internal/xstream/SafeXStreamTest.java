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
package org.xwiki.job.internal.xstream;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.xwiki.job.test.TestSerializable;
import org.xwiki.job.test.TestSerializableXStreamChecker;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.xstream.internal.SafeXStream;
import org.xwiki.xstream.internal.XStreamUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Validate {@link SafeXStream}.
 * 
 * @version $Id$
 */
@ComponentTest
@ComponentList({ TestSerializableXStreamChecker.class, XStreamUtils.class })
public class SafeXStreamTest
{
    static class RecursiveObject
    {
        RecursiveObject recurse;

        public RecursiveObject()
        {
            this.recurse = this;
        }

        @Override
        public String toString()
        {
            return "recursive object";
        }
    }

    static class RecursiveObjectThroughArray
    {
        RecursiveObjectThroughArray[] recurse;

        public RecursiveObjectThroughArray()
        {
            this.recurse = new RecursiveObjectThroughArray[] { this };
        }

        @Override
        public String toString()
        {
            return "recursive object through array";
        }
    }

    static class FailToSerializeObject implements Serializable
    {
        private void writeObject(java.io.ObjectOutputStream out) throws IOException
        {
            throw new IOException();
        }

        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException
        {
        }

        private void readObjectNoData() throws ObjectStreamException
        {
        }
    }

    static class FailToSerializeField implements Serializable
    {
        FailToSerializeObject failingObject;
    }

    @TestSerializable(false)
    private static class NotSerializableObject
    {
        public String field;

        public NotSerializableObject(String field)
        {
            this.field = field;
        }

        @Override
        public boolean equals(Object obj)
        {
            return Objects.equals(((NotSerializableObject) obj).field, this.field);
        }

        @Override
        public String toString()
        {
            return this.field;
        }
    }

    private static class NotSerializableField
    {
        public NotSerializableObject field;

        public NotSerializableField(String field)
        {
            this.field = new NotSerializableObject(field);
        }

        @Override
        public boolean equals(Object obj)
        {
            return Objects.equals(((NotSerializableObject) obj).field, this.field);
        }
    }

    @TestSerializable(false)
    static class NotSerializableObjectWithFailingToString
    {
        public NotSerializableObjectWithFailingToString()
        {

        }

        @Override
        public String toString()
        {
            throw new RuntimeException();
        }
    }

    @InjectMockComponents
    private SafeXStream xstream;

    private Object writeread(Object obj, String resource) throws IOException
    {
        String str = this.xstream.toXML(obj);

        if (resource != null) {
            String content = IOUtils.toString(getClass().getResourceAsStream(resource));

            assertEquals(content, str);
        }

        return this.xstream.fromXML(str);
    }

    private Object assertReadwrite(String resource) throws IOException
    {
        String content = IOUtils.toString(getClass().getResourceAsStream(resource));

        Object obj = this.xstream.fromXML(content);

        String str = this.xstream.toXML(obj);

        assertEquals(content, str);

        return obj;
    }

    // Tests

    @Test
    public void testRecursiveObject() throws IOException
    {
        RecursiveObject obj = (RecursiveObject) writeread(new RecursiveObject(), "/xstream/RecursiveObject.xml");

        assertNotNull(obj);
        assertNotNull(obj.recurse);
        assertSame(obj, obj.recurse);
    }

    @Test
    public void testRecursiveObjectThroughArray() throws IOException
    {
        RecursiveObjectThroughArray obj = (RecursiveObjectThroughArray) writeread(new RecursiveObjectThroughArray(),
            "/xstream/RecursiveObjectThroughArray.xml");

        assertNotNull(obj);
        assertNotNull(obj.recurse);
        assertEquals(1, obj.recurse.length);
        assertSame(obj, obj.recurse[0]);
    }

    @Test
    public void testArrayWithReference() throws IOException
    {
        assertReadwrite("/xstream/ArrayWithReference.xml");
    }

    @Test
    public void testFailToSerializeObject() throws IOException
    {
        FailToSerializeObject obj =
            (FailToSerializeObject) writeread(new FailToSerializeObject(), "/xstream/FailToSerializeObject.xml");

        assertNotNull(obj);
    }

    @Test
    public void testFailToSerializeField() throws IOException
    {
        FailToSerializeField obj =
            (FailToSerializeField) writeread(new FailToSerializeField(), "/xstream/FailToSerializeField.xml");

        assertNotNull(obj);
        assertNull(obj.failingObject);
    }

    @Test
    public void testNotSerializableObject() throws IOException
    {
        NotSerializableObject obj =
            (NotSerializableObject) writeread(new NotSerializableObject("value"), "/xstream/NotSerializableObject.xml");

        assertNotNull(obj);
        assertNull(obj.field);
    }

    @Test
    public void testNotSerializableObjectInArray() throws IOException
    {
        Object[] obj = (Object[]) writeread(new Object[] { new NotSerializableObject("value") },
            "/xstream/NotSerializableObjectInArray.xml");

        assertNotNull(obj);
        assertEquals("value", obj[0]);
    }

    @Test
    public void testNotSerializableField() throws IOException
    {
        NotSerializableField obj =
            (NotSerializableField) writeread(new NotSerializableField("value"), "/xstream/NotSerializableField.xml");

        assertNotNull(obj);
        assertNull(obj.field);
    }

    @Test
    public void testNotSerializableObjectWithFailingToStringInArray() throws IOException
    {
        Object[] obj = (Object[]) writeread(new Object[] { new NotSerializableObjectWithFailingToString() },
            "/xstream/NotSerializableObjectWithFailingToStringInArray.xml");

        assertNotNull(obj);
        assertNull(obj[0]);
    }
}
