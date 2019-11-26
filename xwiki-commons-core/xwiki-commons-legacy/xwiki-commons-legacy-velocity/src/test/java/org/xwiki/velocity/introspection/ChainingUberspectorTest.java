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
package org.xwiki.velocity.introspection;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;
import java.util.Properties;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.util.introspection.UberspectImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.context.Execution;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.velocity.VelocityEngine;
import org.xwiki.velocity.internal.DefaultVelocityConfiguration;
import org.xwiki.velocity.internal.DefaultVelocityContextFactory;
import org.xwiki.velocity.internal.DefaultVelocityEngine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ChainingUberspector}.
 */
@ComponentList({ DefaultVelocityEngine.class, DefaultVelocityConfiguration.class, DefaultVelocityContextFactory.class })
@ComponentTest
public class ChainingUberspectorTest
{
    @InjectComponentManager
    MockitoComponentManager componentManager;

    @MockComponent
    Execution execution;

    VelocityEngine engine;

    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension();

    @BeforeEach
    public void beforeEach() throws Exception
    {
        // Register in-memory configuration sources for the test.
        this.componentManager.registerMemoryConfigurationSource();

        this.engine = this.componentManager.getInstance(VelocityEngine.class);
    }

    /*
     * Tests that the uberspectors in the chain are called, and without a real uberspector no methods are found.
     */
    @Test
    public void testEmptyChain() throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ChainingUberspector.class.getCanonicalName());
        prop.setProperty(ChainingUberspector.UBERSPECT_CHAIN_CLASSNAMES, TestingUberspector.class.getCanonicalName());
        TestingUberspector.methodCalls = 0;
        this.engine.initialize(prop);
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new org.apache.velocity.VelocityContext(), writer, "mytemplate",
            new StringReader("#set($foo = 'hello')#set($bar = $foo.toString())$bar"));
        assertEquals("$bar", writer.toString());
        assertEquals(1, TestingUberspector.methodCalls);
    }

    /*
     * Tests that using several uberspectors in the chain works, and methods are correctly found by the last uberspector
     * in the chain.
     */
    @Test
    public void testBasicChaining() throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ChainingUberspector.class.getCanonicalName());
        prop.setProperty(ChainingUberspector.UBERSPECT_CHAIN_CLASSNAMES,
            UberspectImpl.class.getCanonicalName() + "," + TestingUberspector.class.getCanonicalName());
        TestingUberspector.methodCalls = 0;
        TestingUberspector.getterCalls = 0;
        this.engine.initialize(prop);
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new org.apache.velocity.VelocityContext(), writer, "mytemplate",
            new StringReader("#set($foo = 'hello')#set($bar = $foo.toString())$bar"));
        assertEquals("hello", writer.toString());
        assertEquals(1, TestingUberspector.methodCalls);
        assertEquals(0, TestingUberspector.getterCalls);
    }

    /*
     * Tests that invalid uberspectors classnames are ignored.
     */
    @Test
    public void testInvalidUberspectorsAreIgnored() throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ChainingUberspector.class.getCanonicalName());
        prop.setProperty(ChainingUberspector.UBERSPECT_CHAIN_CLASSNAMES,
            UberspectImpl.class.getCanonicalName() + "," + AbstractChainableUberspector.class.getCanonicalName() + ","
                + InvalidUberspector.class.getCanonicalName() + "," + TestingUberspector.class.getCanonicalName() + ","
                + Date.class.getCanonicalName());
        TestingUberspector.methodCalls = 0;
        InvalidUberspector.methodCalls = 0;
        this.engine.initialize(prop);
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new org.apache.velocity.VelocityContext(), writer, "mytemplate",
            new StringReader("#set($foo = 'hello')#set($bar = $foo.toString())$bar"));
        assertEquals("hello", writer.toString());
        assertEquals(1, TestingUberspector.methodCalls);
        assertEquals(0, InvalidUberspector.methodCalls);

        this.logCapture.ignoreAllMessages();
    }

    /*
     * Tests that a non-chainable entry in the chain does not forward calls.
     */
    @Test
    public void testChainBreakingOnNonChainableEntry() throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ChainingUberspector.class.getCanonicalName());
        prop.setProperty(ChainingUberspector.UBERSPECT_CHAIN_CLASSNAMES,
            TestingUberspector.class.getCanonicalName() + "," + UberspectImpl.class.getCanonicalName());
        TestingUberspector.methodCalls = 0;
        this.engine.initialize(prop);
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new org.apache.velocity.VelocityContext(), writer, "mytemplate",
            new StringReader("#set($foo = 'hello')#set($bar = $foo.toString())$bar"));
        assertEquals("hello", writer.toString());
        assertEquals(0, TestingUberspector.methodCalls);
    }

    /*
     * Checks that the default (non-secure) uberspector works and allows calling restricted methods.
     */
    @Test
    public void testDefaultUberspectorWorks() throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ChainingUberspector.class.getCanonicalName());
        prop.setProperty(ChainingUberspector.UBERSPECT_CHAIN_CLASSNAMES, UberspectImpl.class.getCanonicalName());
        this.engine.initialize(prop);
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new org.apache.velocity.VelocityContext(), writer, "mytemplate",
            new StringReader("#set($foo = 'hello')" + "#set($bar = $foo.getClass().getConstructors())$bar"));
        assertTrue(writer.toString().contains("public java.lang.String(byte[],int,int)"), writer.toString());

        this.logCapture.ignoreAllMessages();
    }

    /*
     * Checks that the secure uberspector works and does not allow calling restricted methods.
     */
    @Test
    public void testSecureUberspectorWorks() throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ChainingUberspector.class.getCanonicalName());
        prop.setProperty(ChainingUberspector.UBERSPECT_CHAIN_CLASSNAMES, SecureUberspector.class.getCanonicalName());
        this.engine.initialize(prop);
        StringWriter writer = new StringWriter();

        this.engine.evaluate(new org.apache.velocity.VelocityContext(), writer, "mytemplate", new StringReader(
            "#set($foo = 'hello')" + "#set($bar = $foo.getClass().getConstructors())$foo $foo.class.name $bar"));
        assertEquals("hello java.lang.String $bar", writer.toString());

        assertEquals(
            "Cannot retrieve method getConstructors from object of class java.lang.Class due to security restrictions.",
            this.logCapture.getMessage(0));
    }

    /*
     * Checks that when the chain property is not configured, by default the secure ubespector is used.
     */
    @Test
    public void testSecureUberspectorEnabledByDefault() throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ChainingUberspector.class.getCanonicalName());
        prop.setProperty(ChainingUberspector.UBERSPECT_CHAIN_CLASSNAMES, "");
        this.engine.initialize(prop);
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new org.apache.velocity.VelocityContext(), writer, "mytemplate", new StringReader(
            "#set($foo = 'hello')" + "#set($bar = $foo.getClass().getConstructors())$foo $foo.class.name $bar"));
        assertEquals("hello java.lang.String $bar", writer.toString());

        this.logCapture.ignoreAllMessages();
    }

    /*
     * Checks that the deprecated check uberspector works.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedUberspector() throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ChainingUberspector.class.getCanonicalName());
        prop.setProperty(ChainingUberspector.UBERSPECT_CHAIN_CLASSNAMES,
            UberspectImpl.class.getCanonicalName() + "," + TestingUberspector.class.getCanonicalName() + ","
                + org.apache.velocity.util.introspection.DeprecatedCheckUberspector.class.getCanonicalName());
        TestingUberspector.methodCalls = 0;
        TestingUberspector.getterCalls = 0;
        this.engine.initialize(prop);
        StringWriter writer = new StringWriter();
        VelocityContext context = new org.apache.velocity.VelocityContext();
        Date d = new Date();
        context.put("date", d);
        context.put("dobject", new DeprecatedObject());

        this.engine.evaluate(context, writer, "mytemplate",
            new StringReader("#set($foo = $date.getYear())$foo $date.month $dobject.foo() $dobject.size()"));

        assertEquals(d.getYear() + " " + d.getMonth() + " foo 0", writer.toString());
        assertEquals(3, TestingUberspector.methodCalls);
        assertEquals(1, TestingUberspector.getterCalls);

        assertEquals("Deprecated usage of method [java.util.Date.getYear] in mytemplate@1,19",
            this.logCapture.getMessage(0));
        assertEquals("Deprecated usage of getter [java.util.Date.getMonth] in mytemplate@1,40",
            this.logCapture.getMessage(1));
        assertEquals(
            "Deprecated usage of method [org.xwiki.velocity.introspection.DeprecatedObject.foo] in mytemplate@1,55",
            this.logCapture.getMessage(2));
        assertEquals(
            "Deprecated usage of method [org.xwiki.velocity.introspection.DeprecatedObject.size] in mytemplate@1,70",
            this.logCapture.getMessage(3));
    }
}
