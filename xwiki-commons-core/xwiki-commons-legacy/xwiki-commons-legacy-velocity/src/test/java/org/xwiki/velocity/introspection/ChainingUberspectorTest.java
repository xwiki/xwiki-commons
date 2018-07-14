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
import org.jmock.Expectations;
import org.jmock.States;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.test.ComponentManagerRule;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.jmock.JMockRule;
import org.xwiki.velocity.VelocityEngine;
import org.xwiki.velocity.internal.DefaultVelocityConfiguration;
import org.xwiki.velocity.internal.DefaultVelocityContextFactory;
import org.xwiki.velocity.internal.DefaultVelocityEngine;

/**
 * Unit tests for {@link ChainingUberspector}.
 */
@ComponentList({
    DefaultVelocityEngine.class,
    DefaultVelocityConfiguration.class,
    DefaultVelocityContextFactory.class
})
public class ChainingUberspectorTest
{
    @Rule
    public final ComponentManagerRule componentManager = new ComponentManagerRule();

    @Rule
    public final JMockRule mockery = new JMockRule();

    private VelocityEngine engine;

    private Logger mockLogger;

    private States loggingVerification = this.mockery.states("loggingVerification");

    @Before
    public void setUp() throws Exception
    {
        // Register in-memory configuration sources for the test.
        this.componentManager.registerMemoryConfigurationSource();

        this.engine = this.componentManager.getInstance(VelocityEngine.class);

        this.mockLogger = this.mockery.mock(Logger.class);
        this.mockery.checking(new Expectations()
        {
            {
                ignoring(ChainingUberspectorTest.this.mockLogger);
                when(ChainingUberspectorTest.this.loggingVerification.isNot("on"));
            }
        });

        ReflectionUtils.setFieldValue(this.engine, "logger", this.mockLogger);
    }

    /*
     * Tests that the uberspectors in the chain are called, and without a real uberspector no methods are found.
     */
    @Test
    public void testEmptyChain() throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ChainingUberspector.class
                .getCanonicalName());
        prop.setProperty(ChainingUberspector.UBERSPECT_CHAIN_CLASSNAMES, TestingUberspector.class
                .getCanonicalName());
        TestingUberspector.methodCalls = 0;
        this.engine.initialize(prop);
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new org.apache.velocity.VelocityContext(), writer, "mytemplate",
            new StringReader("#set($foo = 'hello')#set($bar = $foo.toString())$bar"));
        Assert.assertEquals("$bar", writer.toString());
        Assert.assertEquals(1, TestingUberspector.methodCalls);
    }

    /*
     * Tests that using several uberspectors in the chain works, and methods are correctly found by the last uberspector
     * in the chain.
     */
    @Test
    public void testBasicChaining() throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ChainingUberspector.class
            .getCanonicalName());
        prop.setProperty(ChainingUberspector.UBERSPECT_CHAIN_CLASSNAMES, UberspectImpl.class
            .getCanonicalName()
            + "," + TestingUberspector.class.getCanonicalName());
        TestingUberspector.methodCalls = 0;
        TestingUberspector.getterCalls = 0;
        this.engine.initialize(prop);
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new org.apache.velocity.VelocityContext(), writer, "mytemplate",
            new StringReader("#set($foo = 'hello')#set($bar = $foo.toString())$bar"));
        Assert.assertEquals("hello", writer.toString());
        Assert.assertEquals(1, TestingUberspector.methodCalls);
        Assert.assertEquals(0, TestingUberspector.getterCalls);
    }

    /*
     * Tests that invalid uberspectors classnames are ignored.
     */
    @Test
    public void testInvalidUberspectorsAreIgnored() throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ChainingUberspector.class
            .getCanonicalName());
        prop.setProperty(ChainingUberspector.UBERSPECT_CHAIN_CLASSNAMES, UberspectImpl.class
            .getCanonicalName()
            + ","
            + AbstractChainableUberspector.class.getCanonicalName()
            + ","
            + InvalidUberspector.class.getCanonicalName()
            + ","
            + TestingUberspector.class.getCanonicalName() + "," + Date.class.getCanonicalName());
        TestingUberspector.methodCalls = 0;
        InvalidUberspector.methodCalls = 0;
        this.engine.initialize(prop);
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new org.apache.velocity.VelocityContext(), writer, "mytemplate",
            new StringReader("#set($foo = 'hello')#set($bar = $foo.toString())$bar"));
        Assert.assertEquals("hello", writer.toString());
        Assert.assertEquals(1, TestingUberspector.methodCalls);
        Assert.assertEquals(0, InvalidUberspector.methodCalls);
    }

    /*
     * Tests that a non-chainable entry in the chain does not forward calls.
     */
    @Test
    public void testChainBreakingOnNonChainableEntry() throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ChainingUberspector.class
            .getCanonicalName());
        prop.setProperty(ChainingUberspector.UBERSPECT_CHAIN_CLASSNAMES, TestingUberspector.class
            .getCanonicalName()
            + "," + UberspectImpl.class.getCanonicalName());
        TestingUberspector.methodCalls = 0;
        this.engine.initialize(prop);
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new org.apache.velocity.VelocityContext(), writer, "mytemplate",
            new StringReader("#set($foo = 'hello')#set($bar = $foo.toString())$bar"));
        Assert.assertEquals("hello", writer.toString());
        Assert.assertEquals(0, TestingUberspector.methodCalls);
    }

    /*
     * Checks that the default (non-secure) uberspector works and allows calling restricted methods.
     */
    @Test
    public void testDefaultUberspectorWorks() throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ChainingUberspector.class
            .getCanonicalName());
        prop.setProperty(ChainingUberspector.UBERSPECT_CHAIN_CLASSNAMES, UberspectImpl.class
            .getCanonicalName());
        this.engine.initialize(prop);
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new org.apache.velocity.VelocityContext(), writer, "mytemplate",
            new StringReader("#set($foo = 'hello')"
                + "#set($bar = $foo.getClass().getConstructors())$bar"));
        Assert.assertTrue(writer.toString().startsWith("[Ljava.lang.reflect.Constructor"));
    }

    /*
     * Checks that the secure uberspector works and does not allow calling restricted methods.
     */
    @Test
    public void testSecureUberspectorWorks() throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ChainingUberspector.class
            .getCanonicalName());
        prop.setProperty(ChainingUberspector.UBERSPECT_CHAIN_CLASSNAMES, SecureUberspector.class
            .getCanonicalName());
        this.engine.initialize(prop);
        StringWriter writer = new StringWriter();

        this.loggingVerification.become("on");
        this.mockery.checking(new Expectations()
        {{
            // Get rid of debug log
            allowing(mockLogger).isDebugEnabled();
            returnValue(false);

            // Allow one warning for getConstructors since it's forbidden
            oneOf(mockLogger).warn("Cannot retrieve method getConstructors from object of class java.lang.Class due to security restrictions.");
        }});

        this.engine.evaluate(new org.apache.velocity.VelocityContext(), writer, "mytemplate",
            new StringReader("#set($foo = 'hello')"
                + "#set($bar = $foo.getClass().getConstructors())$foo $foo.class.name $bar"));
        Assert.assertEquals("hello java.lang.String $bar", writer.toString());
    }

    /*
     * Checks that when the chain property is not configured, by default the secure ubespector is used.
     */
    @Test
    public void testSecureUberspectorEnabledByDefault() throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ChainingUberspector.class
            .getCanonicalName());
        prop.setProperty(ChainingUberspector.UBERSPECT_CHAIN_CLASSNAMES, "");
        this.engine.initialize(prop);
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new org.apache.velocity.VelocityContext(), writer, "mytemplate",
            new StringReader("#set($foo = 'hello')"
                + "#set($bar = $foo.getClass().getConstructors())$foo $foo.class.name $bar"));
        Assert.assertEquals("hello java.lang.String $bar", writer.toString());
    }

    /*
     * Checks that the deprecated check uberspector works.
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testDeprecatedUberspector() throws Exception
    {
        Properties prop = new Properties();
        prop.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME, ChainingUberspector.class
            .getCanonicalName());
        prop.setProperty(ChainingUberspector.UBERSPECT_CHAIN_CLASSNAMES, UberspectImpl.class
            .getCanonicalName()
            + ","
            + TestingUberspector.class.getCanonicalName()
            + ","
            + DeprecatedCheckUberspector.class.getCanonicalName());
        TestingUberspector.methodCalls = 0;
        TestingUberspector.getterCalls = 0;
        this.engine.initialize(prop);
        StringWriter writer = new StringWriter();
        VelocityContext context = new org.apache.velocity.VelocityContext();
        Date d = new Date();
        context.put("date", d);
        context.put("dobject", new DeprecatedObject());

        final String threadIdPrefix = Thread.currentThread().getId() + ":";

        // Define expectations on the Logger
        this.loggingVerification.become("on");
        this.mockery.checking(new Expectations()
        {{
            oneOf(mockLogger).warn("Deprecated usage of method [java.util.Date.getYear] in " + threadIdPrefix + "mytemplate@1,19");
            oneOf(mockLogger).warn("Deprecated usage of getter [java.util.Date.getMonth] in " + threadIdPrefix + "mytemplate@1,40");
            oneOf(mockLogger).warn("Deprecated usage of method [org.xwiki.velocity.introspection.DeprecatedObject.foo] in " + threadIdPrefix + "mytemplate@1,55");
            oneOf(mockLogger).warn("Deprecated usage of method [org.xwiki.velocity.introspection.DeprecatedObject.size] in " + threadIdPrefix + "mytemplate@1,70");
        }});

        this.engine.evaluate(context, writer, "mytemplate",
            new StringReader("#set($foo = $date.getYear())$foo $date.month $dobject.foo() $dobject.size()"));

        Assert.assertEquals(d.getYear() + " " + d.getMonth() + " foo 0", writer.toString());
        Assert.assertEquals(3, TestingUberspector.methodCalls);
        Assert.assertEquals(1, TestingUberspector.getterCalls);
    }
}
