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
package org.xwiki.velocity.internal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.Context;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.test.mockito.MockitoComponentMockingRule;
import org.xwiki.velocity.VelocityConfiguration;
import org.xwiki.velocity.XWikiVelocityException;
import org.xwiki.velocity.introspection.DeprecatedCheckUberspector;
import org.xwiki.velocity.introspection.SecureUberspector;

/**
 * Unit tests for {@link DefaultVelocityEngine}.
 */
public class DefaultVelocityEngineTest
{
    @Rule
    public MockitoComponentMockingRule<DefaultVelocityEngine> mocker =
        new MockitoComponentMockingRule<DefaultVelocityEngine>(DefaultVelocityEngine.class);

    private DefaultVelocityEngine engine;

    @Before
    public void setUp() throws Exception
    {
        Properties properties = new Properties();
        properties.put("runtime.introspector.uberspect", StringUtils.join(
            new String[] { SecureUberspector.class.getName(), DeprecatedCheckUberspector.class.getName() }, ','));
        properties.put("directive.set.null.allowed", Boolean.TRUE.toString());
        properties.put("velocimacro.permissions.allow.inline.local.scope", Boolean.TRUE.toString());

        VelocityConfiguration configuration = this.mocker.getInstance(VelocityConfiguration.class);
        when(configuration.getProperties()).thenReturn(properties);

        this.engine = this.mocker.getComponentUnderTest();
    }

    private void assertEvaluate(String expected, String content, String template) throws XWikiVelocityException
    {
        assertEvaluate(expected, content, template, new org.apache.velocity.VelocityContext());
    }

    private void assertEvaluate(String expected, String content, String template, Context context)
        throws XWikiVelocityException
    {
        StringWriter writer = new StringWriter();
        this.engine.evaluate(context, writer, template, content);
        Assert.assertEquals(expected, writer.toString());
    }

    @Test
    public void testEvaluateReader() throws Exception
    {
        this.engine.initialize(new Properties());
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new org.apache.velocity.VelocityContext(), writer, "mytemplate", new StringReader(
            "#set($foo='hello')$foo World"));
        Assert.assertEquals("hello World", writer.toString());
    }

    @Test
    public void testEvaluateString() throws Exception
    {
        this.engine.initialize(new Properties());

        assertEvaluate("hello World", "#set($foo='hello')$foo World", "mytemplate");
    }

    /**
     * Verify that the default configuration doesn't allow calling Class.forName.
     */
    @Test
    public void testSecureUberspectorActiveByDefault() throws Exception
    {
        this.engine.initialize(new Properties());

        String content =
            "#set($foo = 'test')#set($object = $foo.class.forName('java.util.ArrayList')"
                + ".newInstance())$object.size()";
        assertEvaluate("$object.size()", content, "mytemplate");

        // Verify that we log a warning and verify the message.
        verify(this.mocker.getMockedLogger()).warn(
            "Cannot retrieve method forName from object of class " + "java.lang.Class due to security restrictions.");
    }

    /**
     * Verify that the default configuration allows #setting existing variables to null.
     */
    @Test
    public void testSettingNullAllowedByDefault() throws Exception
    {
        this.engine.initialize(new Properties());
        StringWriter writer = new StringWriter();
        Context context = new org.apache.velocity.VelocityContext();
        context.put("null", null);
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add(null);
        list.add("3");
        context.put("list", list);
        this.engine.evaluate(context, writer, "mytemplate", "#set($foo = true)${foo}#set($foo = $null)${foo}\n"
            + "#foreach($i in $list)${velocityCount}=$!{i} #end");
        Assert.assertEquals("true${foo}\n1=1 2= 3=3 ", writer.toString());

        String content =
            "#set($foo = true)${foo}#set($foo = $null)${foo}\n" + "#foreach($i in $list)${velocityCount}=$!{i} #end";

        assertEvaluate("true${foo}\n1=1 2= 3=3 ", content, "mytemplate", context);
    }

    @Test
    public void testOverrideConfiguration() throws Exception
    {
        // For example try setting a non secure Uberspector.
        Properties properties = new Properties();
        properties
            .setProperty("runtime.introspector.uberspect", "org.apache.velocity.util.introspection.UberspectImpl");
        this.engine.initialize(properties);
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new org.apache.velocity.VelocityContext(), writer, "mytemplate",
            "#set($foo = 'test')#set($object = $foo.class.forName('java.util.ArrayList')"
                + ".newInstance())$object.size()");
        Assert.assertEquals("0", writer.toString());
    }

    @Test
    public void testMacroIsolation() throws Exception
    {
        this.engine.initialize(new Properties());
        Context context = new org.apache.velocity.VelocityContext();
        this.engine.evaluate(context, new StringWriter(), "template1", "#macro(mymacro)test#end");
        assertEvaluate("#mymacro", "#mymacro", "template2");
    }

    @Test
    public void testConfigureMacrosToBeGlobal() throws Exception
    {
        Properties properties = new Properties();
        // Force macros to be global
        properties.put("velocimacro.permissions.allow.inline.local.scope", "false");
        this.engine.initialize(properties);
        Context context = new org.apache.velocity.VelocityContext();
        this.engine.evaluate(context, new StringWriter(), "template1", "#macro(mymacro)test#end");
        assertEvaluate("test", "#mymacro", "template2");
    }

    /**
     * Verify namespace is properly cleared when not needed anymore.
     */
    @Test
    public void testMacroNamespaceCleanup() throws Exception
    {
        this.engine.initialize(new Properties());

        // Unprotected namespace

        assertEvaluate("#mymacro", "#mymacro", "namespace");

        this.engine.evaluate(new org.apache.velocity.VelocityContext(), new StringWriter(), "namespace",
            "#macro(mymacro)test#end");

        assertEvaluate("#mymacro", "#mymacro", "namespace");

        // Protected namespace

        // Start using namespace "namespace"
        this.engine.startedUsingMacroNamespace("namespace");

        // Register macro
        this.engine.evaluate(new org.apache.velocity.VelocityContext(), new StringWriter(), "namespace",
            "#macro(mymacro)test#end");

        assertEvaluate("test", "#mymacro", "namespace");

        // Mark namespace "namespace" as not used anymore
        this.engine.stoppedUsingMacroNamespace("namespace");

        assertEvaluate("#mymacro", "#mymacro", "namespace");
    }

    /**
     * Make sure several thread that supposedly use the same namespace string don't collide.
     * 
     * @throws XWikiVelocityException
     * @throws InterruptedException
     * @throws ExecutionException
     */
    @Test
    public void testThreadsafeNamespaces() throws XWikiVelocityException, InterruptedException, ExecutionException
    {
        this.engine.initialize(new Properties());

        // Start using namespace "namespace"
        this.engine.startedUsingMacroNamespace("namespace");

        // Register macro
        Context context = new org.apache.velocity.VelocityContext();
        this.engine.evaluate(context, new StringWriter(), "namespace", "#macro(mymacro)test#end");

        assertEvaluate("test", "#mymacro", "namespace");

        ExecutorService pool = Executors.newSingleThreadExecutor();
        Future<Void> future = pool.submit(new Callable<Void>()
        {
            @Override
            public Void call() throws Exception
            {
                // the macro should not be available in a different thread than the one which registered it
                assertEvaluate("#mymacro", "#mymacro", "namespace");

                return null;
            }
        });
        future.get();

        // Mark namespace "namespace" as not used anymore
        this.engine.stoppedUsingMacroNamespace("namespace");
    }

    @Test
    public void testEvaluateWithStopCommand() throws Exception
    {
        this.engine.initialize(new Properties());

        assertEvaluate("hello world", "hello world#stop", "mytemplate");
    }
}
