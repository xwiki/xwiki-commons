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
package org.xwiki.xml;

import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.Enumeration;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.html.dom.HTMLDocumentImpl;
import org.apache.wml.dom.WMLDocumentImpl;
import org.apache.wml.dom.WMLPElementImpl;
import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.dom.DeferredDocumentImpl;
import org.apache.xerces.dom.ElementImpl;
import org.apache.xerces.dom.TextImpl;
import org.evosuite.runtime.ViolatedAssumptionAnswer;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import static org.evosuite.shaded.org.mockito.Mockito.doReturn;
import static org.evosuite.shaded.org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class XMLUtils_ESTest
{
    @Test(timeout = 4000)
    public void test00() throws Throwable
    {
        Document document0 = XMLUtils.createDOMDocument();
        // Undeclared exception!
        try {
            XMLUtils.extractXML(document0, (-2295), (-2295));
            fail("Expecting exception: RuntimeException");
        } catch (RuntimeException e) {
            //
            // Failed to extract XML
            //
        }
    }

    @Test(timeout = 4000)
    public void test01() throws Throwable
    {
        try {
            XMLUtils.formatXMLContent("K$#OB[dvAT=+4,fD");
            fail("Expecting exception: TransformerException");
        } catch (TransformerException e) {
        }
    }

    @Test(timeout = 4000)
    public void test02() throws Throwable
    {
        Object object0 = new Object();
        String string0 = XMLUtils.escape(object0);
        assertNotNull(string0);
    }

    @Test(timeout = 4000)
    public void test03() throws Throwable
    {
        DeferredDocumentImpl deferredDocumentImpl0 = new DeferredDocumentImpl(false, false);
        // Undeclared exception!
        try {
            XMLUtils.extractXML(deferredDocumentImpl0, 64, 64);
            fail("Expecting exception: RuntimeException");
        } catch (RuntimeException e) {
            //
            // Failed to extract XML
            //
        }
    }

    @Test(timeout = 4000)
    public void test04() throws Throwable
    {
        String string0 = XMLUtils.escapeAttributeValue((Object) null);
        assertNull(string0);
    }

    @Test(timeout = 4000)
    public void test05() throws Throwable
    {
        String string0 = XMLUtils.escapeAttributeValue("J\"pb_6n&@aF-d`V");
        assertEquals("J&#34;pb_6n&#38;@aF-d`V", string0);
        assertNotNull(string0);
    }

    @Test(timeout = 4000)
    public void test06() throws Throwable
    {
        String string0 = XMLUtils.escapeAttributeValue("^)'Ct< TT9S<z?@U");
        assertNotNull(string0);
        assertEquals("^)&#39;Ct&#60; TT9S&#60;z?@U", string0);
    }

    @Test(timeout = 4000)
    public void test07() throws Throwable
    {
        String string0 =
            XMLUtils.escapeAttributeValue("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<K$#OB[dvAT=+4,fD/>");
        assertNotNull(string0);
        assertEquals("&#60;?xml version=&#34;1.0&#34; encoding=&#34;UTF-8&#34;?&#62;\n&#60;K$#OB[dvAT=+4,fD/&#62;",
            string0);
    }

    @Test(timeout = 4000)
    public void test08() throws Throwable
    {
        String string0 = XMLUtils.escapeAttributeValue("{http://xml.apache.org/xslt}indent-amount");
        assertNotNull(string0);
        assertEquals("&#123;http://xml.apache.org/xslt}indent-amount", string0);
    }

    @Test(timeout = 4000)
    public void test09() throws Throwable
    {
        String string0 = XMLUtils.escapeElementContent("J\"pb_6n&@aF-d`V");
        assertNotNull(string0);
        assertEquals("J\"pb_6n&#38;@aF-d`V", string0);
    }

    @Test(timeout = 4000)
    public void test10() throws Throwable
    {
        String string0 = XMLUtils.escapeElementContent((Object) null);
        assertNull(string0);
    }

    @Test(timeout = 4000)
    public void test11() throws Throwable
    {
        String string0 = XMLUtils.escapeElementContent("a<vRY:gk6Dw>$86f/");
        assertEquals("a&#60;vRY:gk6Dw&#62;$86f/", string0);
        assertNotNull(string0);
    }

    @Test(timeout = 4000)
    public void test12() throws Throwable
    {
        Object object0 = new Object();
        String string0 = XMLUtils.unescape(object0);
        assertNotNull(string0);
    }

    @Test(timeout = 4000)
    public void test13() throws Throwable
    {
        String string0 = XMLUtils.unescape((Object) null);
        assertNull(string0);
    }

    @Test(timeout = 4000)
    public void test14() throws Throwable
    {
        DOMInputImpl dOMInputImpl0 = new DOMInputImpl();
        Document document0 = XMLUtils.parse(dOMInputImpl0);
        assertNull(document0);
    }

    @Test(timeout = 4000)
    public void test15() throws Throwable
    {
        TextImpl textImpl0 = new TextImpl();
        String string0 = XMLUtils.serialize((Node) textImpl0, false);
        assertEquals("", string0);
    }

    @Test(timeout = 4000)
    public void test16() throws Throwable
    {
        String string0 = XMLUtils.transform((Source) null, (Source) null);
        assertNull(string0);
    }

    @Test(timeout = 4000)
    public void test17() throws Throwable
    {
        SAXSource sAXSource0 = new SAXSource();
        String string0 = XMLUtils.transform(sAXSource0, (Source) null);
        assertNull(string0);
    }

    @Test(timeout = 4000)
    public void test18() throws Throwable
    {
        String string0 = XMLUtils.escapeXMLComment(")VsE\"K <dTErL{N#c-");
        assertEquals(")VsE\"K <dTErL{N#c-\\", string0);
    }

    @Test(timeout = 4000)
    public void test19() throws Throwable
    {
        String string0 = XMLUtils.unescapeXMLComment(")VsE\"K <\\dTErL{N#c-");
        assertEquals(")VsE\"K <dTErL{N#c-", string0);
    }

    @Test(timeout = 4000)
    public void test20() throws Throwable
    {
        String string0 = XMLUtils.serialize((Node) null, false);
        assertEquals("", string0);
    }

    @Test(timeout = 4000)
    public void test21() throws Throwable
    {
        DeferredDocumentImpl deferredDocumentImpl0 = new DeferredDocumentImpl(false, false);
        String string0 = XMLUtils.serialize((Node) deferredDocumentImpl0, false);
        assertEquals("", string0);
    }

    @Test(timeout = 4000)
    public void test22() throws Throwable
    {
        WMLDocumentImpl wMLDocumentImpl0 = mock(WMLDocumentImpl.class, new ViolatedAssumptionAnswer());
        doReturn((DocumentType) null).when(wMLDocumentImpl0).getDoctype();
        doReturn((String) null).when(wMLDocumentImpl0).getInputEncoding();
        doReturn((String) null, (String) null).when(wMLDocumentImpl0).getXmlEncoding();
        doReturn((String) null).when(wMLDocumentImpl0).getXmlVersion();
        WMLPElementImpl wMLPElementImpl0 = new WMLPElementImpl(wMLDocumentImpl0, "");
        String string0 = XMLUtils.serialize((Node) wMLPElementImpl0, true);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n</>", string0);
    }

    @Test(timeout = 4000)
    public void test23() throws Throwable
    {
        // Undeclared exception!
        try {
            XMLUtils.escapeXMLComment((String) null);
            fail("Expecting exception: NullPointerException");
        } catch (NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
        }
    }

    @Test(timeout = 4000)
    public void test24() throws Throwable
    {
        // Undeclared exception!
        try {
            XMLUtils.formatXMLContent((String) null);
            fail("Expecting exception: NullPointerException");
        } catch (NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
        }
    }

    @Test(timeout = 4000)
    public void test25() throws Throwable
    {
        // Undeclared exception!
        try {
            XMLUtils.unescapeXMLComment((String) null);
            fail("Expecting exception: NullPointerException");
        } catch (NullPointerException e) {
            //
            // no message in exception (getMessage() returned null)
            //
        }
    }

    @Test(timeout = 4000)
    public void test26() throws Throwable
    {
        String string0 = XMLUtils.escape("");
        assertEquals("", string0);
    }

    @Test(timeout = 4000)
    public void test27() throws Throwable
    {
        String string0 = XMLUtils.escape((Object) null);
        assertNull(string0);
    }

    @Test(timeout = 4000)
    public void test28() throws Throwable
    {
        String string0 = XMLUtils.escapeAttributeValue("");
        assertEquals("", string0);
    }

    @Test(timeout = 4000)
    public void test29() throws Throwable
    {
        String string0 = XMLUtils.escapeElementContent("");
        assertEquals("", string0);
    }

    @Test(timeout = 4000)
    public void test30() throws Throwable
    {
        String string0 = XMLUtils.escapeXMLComment("");
        assertEquals("", string0);
    }

    @Test(timeout = 4000)
    public void test31() throws Throwable
    {
        HTMLDocumentImpl hTMLDocumentImpl0 = new HTMLDocumentImpl();
        Text text0 = hTMLDocumentImpl0.createTextNode("");
        String string0 = XMLUtils.extractXML(text0, 9, 2116);
        assertEquals("", string0);
    }

    @Test(timeout = 4000)
    public void test32() throws Throwable
    {
        DeferredDocumentImpl deferredDocumentImpl0 = new DeferredDocumentImpl(true, true);
        ElementImpl elementImpl0 = new ElementImpl(deferredDocumentImpl0, "K$#OB[dvAT=+4,fD");
        XMLUtils.serialize((Node) elementImpl0);
        String string0 = XMLUtils.serialize((Node) elementImpl0);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<K$#OB[dvAT=+4,fD/>", string0);
    }

    @Test(timeout = 4000)
    public void test33() throws Throwable
    {
        String string0 = XMLUtils.unescape("");
        assertEquals("", string0);
    }

    @Test(timeout = 4000)
    public void test34() throws Throwable
    {
        String string0 = XMLUtils.unescapeXMLComment("");
        assertEquals("", string0);
    }

    @Test(timeout = 4000)
    public void test35() throws Throwable
    {
        String string0 = XMLUtils.escapeXMLComment("-5z)oJ_iNE");
        assertEquals("-5z)oJ_iNE", string0);
    }

    @Test(timeout = 4000)
    public void test36() throws Throwable
    {
        Enumeration<InputStream> enumeration0 =
            (Enumeration<InputStream>) mock(Enumeration.class, new ViolatedAssumptionAnswer());
        doReturn(false).when(enumeration0).hasMoreElements();
        SequenceInputStream sequenceInputStream0 = new SequenceInputStream(enumeration0);
        DOMSource dOMSource0 = new DOMSource();
        StreamSource streamSource0 = new StreamSource(sequenceInputStream0);
        String string0 = XMLUtils.transform(streamSource0, dOMSource0);
        assertNull(string0);
    }
}
