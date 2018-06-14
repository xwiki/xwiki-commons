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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.context.Context;
import org.apache.velocity.util.introspection.DeprecatedCheckUberspector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.test.LogLevel;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.velocity.VelocityConfiguration;
import org.xwiki.velocity.XWikiVelocityContext;
import org.xwiki.velocity.XWikiVelocityException;
import org.xwiki.velocity.introspection.SecureUberspector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultVelocityEngine}.
 */
@ComponentTest
public class DefaultVelocityEngineTest
{
    @RegisterExtension
    LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @MockComponent
    private ComponentManager componentManager;

    @MockComponent
    private VelocityConfiguration configuration;

    @InjectMockComponents
    private DefaultVelocityEngine engine;

    @MockComponent
    private Execution execution;

    @BeforeEach
    void setUp() throws Exception
    {
        Properties properties = new Properties();
        properties.put("runtime.introspector.uberspect",
            SecureUberspector.class.getName() + "," + DeprecatedCheckUberspector.class.getName());
        properties.put("directive.set.null.allowed", Boolean.TRUE.toString());
        properties.put("velocimacro.permissions.allow.inline.local.scope", Boolean.TRUE.toString());

        when(configuration.getProperties()).thenReturn(properties);

        when(execution.getContext()).thenReturn(new ExecutionContext());
    }

    private void assertEvaluate(String expected, String content, String template) throws XWikiVelocityException
    {
        assertEvaluate(expected, content, template, new XWikiVelocityContext());
    }

    private void assertEvaluate(String expected, String content, String template, Context context)
        throws XWikiVelocityException
    {
        StringWriter writer = new StringWriter();
        this.engine.evaluate(context, writer, template, content);
        assertEquals(expected, writer.toString());
    }

    @Test
    public void testEvaluateReader() throws Exception
    {
        this.engine.initialize(new Properties());
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new XWikiVelocityContext(), writer, "mytemplate",
            new StringReader("#set($foo='hello')$foo World"));
        assertEquals("hello World", writer.toString());
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

        String content = "#set($foo = 'test')#set($object = $foo.class.forName('java.util.ArrayList')"
            + ".newInstance())$object.size()";
        assertEvaluate("$object.size()", content, "mytemplate");

        // Verify that we log a warning and verify the message.
        assertEquals(
            "Cannot retrieve method forName from object of class java.lang.Class due to security restrictions.",
            logCapture.getMessage(0));
    }

    /**
     * Verify that the default configuration allows #setting existing variables to null.
     */
    @Test
    public void testSettingNullAllowedByDefault() throws Exception
    {
        this.engine.initialize(new Properties());
        StringWriter writer = new StringWriter();
        Context context = new XWikiVelocityContext();
        context.put("null", null);
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add(null);
        list.add("3");
        context.put("list", list);
        this.engine.evaluate(context, writer, "mytemplate",
            "#set($foo = true)${foo}#set($foo = $null)${foo}\n" + "#foreach($i in $list)${foreach.count}=$!{i} #end");
        assertEquals("true${foo}\n1=1 2= 3=3 ", writer.toString());

        String content =
            "#set($foo = true)${foo}#set($foo = $null)${foo}\n" + "#foreach($i in $list)${foreach.count}=$!{i} #end";

        assertEvaluate("true${foo}\n1=1 2= 3=3 ", content, "mytemplate", context);
    }

    @Test
    public void testOverrideConfiguration() throws Exception
    {
        // For example try setting a non secure Uberspector.
        Properties properties = new Properties();
        properties.setProperty("runtime.introspector.uberspect",
            "org.apache.velocity.util.introspection.UberspectImpl");
        this.engine.initialize(properties);
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new XWikiVelocityContext(), writer, "mytemplate",
            "#set($foo = 'test')#set($object = $foo.class.forName('java.util.ArrayList')"
                + ".newInstance())$object.size()");
        assertEquals("0", writer.toString());
    }

    @Test
    public void testMacroIsolation() throws Exception
    {
        this.engine.initialize(new Properties());
        Context context = new XWikiVelocityContext();
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
        Context context = new XWikiVelocityContext();
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

        this.engine.evaluate(new XWikiVelocityContext(), new StringWriter(), "namespace", "#macro(mymacro)test#end");

        assertEvaluate("#mymacro", "#mymacro", "namespace");

        // Protected namespace

        // Start using namespace "namespace"
        this.engine.startedUsingMacroNamespace("namespace");

        // Register macro
        this.engine.evaluate(new XWikiVelocityContext(), new StringWriter(), "namespace", "#macro(mymacro)test#end");

        assertEvaluate("test", "#mymacro", "namespace");

        // Mark namespace "namespace" as not used anymore
        this.engine.stoppedUsingMacroNamespace("namespace");

        assertEvaluate("#mymacro", "#mymacro", "namespace");
    }

    @Test
    public void testEvaluateWithStopCommand() throws Exception
    {
        this.engine.initialize(new Properties());

        assertEvaluate("hello world", "hello world#stop", "mytemplate");
    }

    @Test
    public void testVelocityCount() throws XWikiVelocityException
    {
        this.engine.initialize(new Properties());

        assertEvaluate("1true2true3true4true5false", "#foreach($nb in [1,2,3,4,5])$velocityCount$velocityHasNext#end",
            "mytemplate");

        assertEquals("Deprecated binding [$velocityCount] used in [mytemplate]", logCapture.getMessage(0));
        assertEquals("Deprecated binding [$velocityHasNext] used in [mytemplate]", logCapture.getMessage(1));
        assertEquals("Deprecated binding [$velocityCount] used in [mytemplate]", logCapture.getMessage(2));
        assertEquals("Deprecated binding [$velocityHasNext] used in [mytemplate]", logCapture.getMessage(3));
        assertEquals("Deprecated binding [$velocityCount] used in [mytemplate]", logCapture.getMessage(4));
        assertEquals("Deprecated binding [$velocityHasNext] used in [mytemplate]", logCapture.getMessage(5));
        assertEquals("Deprecated binding [$velocityCount] used in [mytemplate]", logCapture.getMessage(6));
        assertEquals("Deprecated binding [$velocityHasNext] used in [mytemplate]", logCapture.getMessage(7));
        assertEquals("Deprecated binding [$velocityCount] used in [mytemplate]", logCapture.getMessage(8));
        assertEquals("Deprecated binding [$velocityHasNext] used in [mytemplate]", logCapture.getMessage(9));
    }

    @Test
    public void testEmptyNamespaceInheritance() throws Exception
    {
        this.engine.initialize(new Properties());

        assertEvaluate("#mymacro", "#mymacro", "namespace");

        this.engine.evaluate(new XWikiVelocityContext(), new StringWriter(), "", "#macro(mymacro)test#end");

        assertEvaluate("test", "#mymacro", "namespace");
    }

    @Test
    public void testOverrideMacros() throws Exception
    {
        this.engine.initialize(new Properties());

        assertEvaluate("#mymacro", "#mymacro", "namespace");

        this.engine.evaluate(new XWikiVelocityContext(), new StringWriter(), "", "#macro(mymacro)global#end");

        assertEvaluate("global", "#mymacro", "namespace");

        // Start using namespace "namespace"
        this.engine.startedUsingMacroNamespace("namespace");

        // Register macro
        this.engine.evaluate(new XWikiVelocityContext(), new StringWriter(), "namespace", "#macro(mymacro)test1#end");

        assertEvaluate("test1", "#mymacro", "namespace");

        // Override macro
        this.engine.evaluate(new XWikiVelocityContext(), new StringWriter(), "namespace", "#macro(mymacro)test2#end");

        assertEvaluate("test2", "#mymacro", "namespace");

        // Mark namespace "namespace" as not used anymore
        this.engine.stoppedUsingMacroNamespace("namespace");
    }

    @Test
    public void testMacroReturn() throws Exception
    {
        this.engine.initialize(new Properties());

        /*assertEvaluate("42$return", "#macro (testMacro $toto $return $titi)#setVariable('$return' 42)#end"
            + "#testMacro($aa, $returned, 'jj')$returned$return", "mytemplate");

        assertEvaluate("42$return", "#macro (testMacro $return $titi)#setVariable('$return' 42)#end"
            + "#testMacro($returned, 'jj')$returned$return", "mytemplate");

        assertEvaluate("42$return",
            "#macro (testMacro $return)#setVariable('$return' 42)#end" + "#testMacro($returned)$returned$return",
            "mytemplate");

        assertEvaluate("42$return",
            "#macro (testMacro $return)#setVariable('return' 42)#end" + "#testMacro($returned)$returned$return",
            "mytemplate");*/

        assertEvaluate("42",
            "#macro (testTopMacro $topreturn)#testSubMacro($subreturned)#setVariable('$topreturn' $subreturned)#end"
                + "#macro (testSubMacro $subreturn)#setVariable('$subreturn' 42)#end"
                + "#testTopMacro($topreturned)$topreturned",
            "mytemplate");
    }

    @Test
    @Disabled
    public void testSetSharpString() throws Exception
    {
        this.engine.initialize(new Properties());

        assertEvaluate("#", "#set($var = \"#\")", "mytemplate");
        assertEvaluate("test#", "#set($var = \"test#\")", "mytemplate");
    }

    @Test
    @Disabled
    public void testSetDollarString() throws Exception
    {
        this.engine.initialize(new Properties());

        assertEvaluate("$", "#set($var = \"$\")", "mytemplate");
        assertEvaluate("test$", "#set($var = \"test$\")", "mytemplate");
    }
}
