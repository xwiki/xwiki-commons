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

import javax.inject.Inject;
import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.runtime.RuntimeConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.logging.LoggerConfiguration;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.velocity.VelocityContextFactory;
import org.xwiki.velocity.VelocityEngine;
import org.xwiki.velocity.XWikiVelocityContext;
import org.xwiki.velocity.XWikiVelocityException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultVelocityManager}.
 */
@ComponentTest
@ComponentList({DefaultVelocityConfiguration.class, DefaultVelocityContextFactory.class, InternalVelocityEngine.class})
class DefaultVelocityManagerTest
{
    public class TestClass
    {
        private String name = "name";

        public TestClass()
        {
        }

        public TestClass(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return this.name;
        }

        public String evaluate(String input) throws XWikiVelocityException
        {
            return evaluate(input, DEFAULT_TEMPLATE_NAME);
        }

        public String evaluate(String input, String namespace) throws XWikiVelocityException
        {
            return DefaultVelocityManagerTest.this.evaluate(input, namespace);
        }
    }

    private static final String DEFAULT_TEMPLATE_NAME = "mytemplate";

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @MockComponent
    private ComponentManager componentManager;

    @MockComponent
    private LoggerConfiguration loggerConfiguration;

    @MockComponent
    private Execution execution;

    @MockComponent
    private ConfigurationSource configurationSource;

    @MockComponent
    private ScriptContextManager scriptContextManager;

    @InjectMockComponents
    private DefaultVelocityManager velocityManager;

    @Inject
    private VelocityContextFactory contextFactory;

    private ExecutionContext executionContext;

    @BeforeEach
    void beforeEach()
    {
        this.executionContext = new ExecutionContext();
        when(this.execution.getContext()).thenReturn(executionContext);
        when(this.loggerConfiguration.isDeprecatedLogEnabled()).thenReturn(true);
    }

    private void assertEvaluate(String expected, String content) throws XWikiVelocityException
    {
        assertEvaluate(expected, content, DEFAULT_TEMPLATE_NAME);
    }

    private void assertEvaluate(String expected, String content, String template) throws XWikiVelocityException
    {
        assertEvaluate(expected, content, template, this.contextFactory.createContext());
    }

    private void assertEvaluate(String expected, String content, Context context) throws XWikiVelocityException
    {
        assertEvaluate(expected, content, DEFAULT_TEMPLATE_NAME, context);
    }

    private void assertEvaluate(String expected, String content, String namespace, Context context)
        throws XWikiVelocityException
    {
        String result = evaluate(content, namespace, context);

        assertEquals(expected, result);
    }

    private String evaluate(String content) throws XWikiVelocityException
    {
        return evaluate(content, DEFAULT_TEMPLATE_NAME);
    }

    private String evaluate(String content, String namespace) throws XWikiVelocityException
    {
        Context context =
            (Context) this.executionContext.getProperty(VelocityExecutionContextInitializer.VELOCITY_CONTEXT_ID);
        if (context == null) {
            context = this.contextFactory.createContext();
            this.executionContext.setProperty(VelocityExecutionContextInitializer.VELOCITY_CONTEXT_ID, context);
        }

        StringWriter writer = new StringWriter();
        this.velocityManager.evaluate(writer, namespace, new StringReader(content));

        return writer.toString();
    }

    private String evaluate(String content, String namespace, Context context) throws XWikiVelocityException
    {
        this.executionContext.setProperty(VelocityExecutionContextInitializer.VELOCITY_CONTEXT_ID, context);

        return evaluate(content, namespace);
    }

    private VelocityEngine getEngine() throws XWikiVelocityException
    {
        return this.velocityManager.getVelocityEngine();
    }

    // Tests

    @Test
    void evaluateWithReader() throws Exception
    {
        StringWriter writer = new StringWriter();
        this.velocityManager.evaluate(writer, DEFAULT_TEMPLATE_NAME, new StringReader("#set($foo='hello')$foo World"));
        assertEquals("hello World", writer.toString());
    }

    @Test
    void evaluateWithString() throws Exception
    {
        assertEvaluate("hello World", "#set($foo='hello')$foo World", DEFAULT_TEMPLATE_NAME);
    }

    /**
     * Verify that the default configuration doesn't allow calling Class.forName.
     */
    @Test
    void evaluateWithSecureUberspectorActiveByDefault() throws Exception
    {
        String content = "#set($foo = 'test')#set($object = $foo.class.forName('java.util.ArrayList')"
            + ".newInstance())$object.size()";
        assertEvaluate("$object.size()", content, DEFAULT_TEMPLATE_NAME);

        // Verify that we log a warning and verify the message.
        assertEquals(
            "Cannot retrieve method forName from object of class java.lang.Class due to security restrictions.",
            logCapture.getMessage(0));
    }

    /**
     * Verify that the default configuration allows #setting existing variables to null.
     */
    @Test
    void evaluateWithSettingNullAllowedByDefault() throws Exception
    {
        Context context = new XWikiVelocityContext();
        context.put("null", null);
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add(null);
        list.add("3");
        context.put("list", list);
        assertEvaluate("true${foo}\n1=1 2= 3=3 ",
            "#set($foo = true)${foo}#set($foo = $null)${foo}\n" + "#foreach($i in $list)${foreach.count}=$!{i} #end",
            DEFAULT_TEMPLATE_NAME, context);

        String content =
            "#set($foo = true)${foo}#set($foo = $null)${foo}\n" + "#foreach($i in $list)${foreach.count}=$!{i} #end";

        assertEvaluate("true${foo}\n1=1 2= 3=3 ", content, DEFAULT_TEMPLATE_NAME, context);
    }

    @Test
    void evaluateWithOverrideConfiguration() throws Exception
    {
        // For example try setting a non secure Uberspector.
        Properties properties = new Properties();
        properties.setProperty(RuntimeConstants.UBERSPECT_CLASSNAME,
            "org.apache.velocity.util.introspection.UberspectImpl");

        when(this.configurationSource.getProperty("velocity.properties", Properties.class)).thenReturn(properties);

        assertEvaluate("0", "#set($foo = 'test')#set($object = $foo.class.forName('java.util.ArrayList')"
            + ".newInstance())$object.size()", DEFAULT_TEMPLATE_NAME);
    }

    @Test
    void evaluateMacroIsolation() throws Exception
    {
        evaluate("#macro(mymacro)test#end", "template1");
        assertEvaluate("#mymacro", "#mymacro", "template2");
    }

    @Test
    void evaluateConfigureMacrosToBeGlobal() throws Exception
    {
        Properties properties = new Properties();
        // Force macros to be global
        properties.put(RuntimeConstants.VM_PERM_INLINE_LOCAL, "false");
        when(this.configurationSource.getProperty("velocity.properties", Properties.class)).thenReturn(properties);
        evaluate("#macro(mymacro)test#end", "template1");
        assertEvaluate("test", "#mymacro", "template2");
    }

    @Test
    void evaluateMacroScope() throws XWikiVelocityException
    {
        assertEvaluate("{}", "#macro (testMacro)$macro#end#testMacro()");
    }

    @Test
    void evaluateMacroWithMissingParameter() throws XWikiVelocityException
    {
        assertEvaluate("$param", "#macro (testMacro $param)$param#end#testMacro()");
    }

    @Test
    void evaluateMacroWithLiteralBooleanParameter() throws XWikiVelocityException
    {
        assertEvaluate("true", "#macro (testMacro $param)$param#end#testMacro(true)");
    }

    @Test
    void evaluateMacroWithLiteralMapParameter() throws XWikiVelocityException
    {
        assertEvaluate("{}", "#macro (testMacro $param)$param#end#testMacro({})");
    }

    @Test
    void evaluateMacroWithLiteralArrayParameter() throws XWikiVelocityException
    {
        assertEvaluate("[]", "#macro (testMacro $param)$param#end#testMacro([])");
    }

    @Test
    void evaluateUseGlobalMacroUseLocalMacro() throws XWikiVelocityException
    {
        // Register a global macro
        evaluate("#macro (globalMacro)#localMacro()#end", "");

        // Can the global macro call local macros
        assertEvaluate("locallocal", "#macro (localMacro)local#end#localMacro()#globalMacro()");
    }

    @Test
    void evaluateGlobalMacroUseLocalMacroAfterInclude() throws XWikiVelocityException
    {
        // Register a global macro
        evaluate("#macro (globalMacro)#localMacro()#end", "");

        Context context = new XWikiVelocityContext();
        context.put("test", new TestClass());

        // Can the global macro still call the local macro after executing another script in the same namespace
        assertEvaluate("locallocallocal",
            "#globalMacro()#macro (localMacro)local#end$test.evaluate('')#globalMacro()#localMacro()", context);
    }

    @Test
    void evaluateWithncludeMacro() throws XWikiVelocityException
    {
        Context context = new XWikiVelocityContext();
        context.put("test", new TestClass());

        // Can the script use an included macro
        assertEvaluate("included", "$test.evaluate('#macro(includedmacro)included#end')#includedmacro()", context);
    }

    @Test
    void evaluateWithOverwrittenIncludeMacro() throws XWikiVelocityException
    {
        Context context = new XWikiVelocityContext();
        context.put("test", new TestClass());

        // Included macros overwrite the script macros because they are loaded after
        assertEvaluate("included",
            "$test.evaluate('#macro(includedmacro)included#end')#macro(includedmacro)ovewritten#end#includedmacro()",
            context);
        assertEvaluate("included",
            "#macro(includedmacro)ovewritten#end$test.evaluate('#macro(includedmacro)included#end')#includedmacro()",
            context);
    }

    @Test
    void evaluateTemplateScope() throws XWikiVelocityException
    {
        assertEvaluate("{}", "$template");
    }

    /**
     * Verify namespace is properly cleared when not needed anymore.
     */
    @Test
    void evaluateMacroNamespaceCleanup() throws Exception
    {
        // Unprotected namespace

        assertEvaluate("#mymacro", "#mymacro", "namespace");

        evaluate("#macro(mymacro)test#end", "namespace");

        assertEvaluate("#mymacro", "#mymacro", "namespace");

        // Protected namespace

        // Start using namespace "namespace"
        getEngine().startedUsingMacroNamespace("namespace");

        // Register macro
        evaluate("#macro(mymacro)test#end", "namespace");

        assertEvaluate("test", "#mymacro", "namespace");

        // Mark namespace "namespace" as not used anymore
        getEngine().stoppedUsingMacroNamespace("namespace");

        assertEvaluate("#mymacro", "#mymacro", "namespace");
    }

    @Test
    void evaluateWithStopCommand() throws Exception
    {
        assertEvaluate("hello world", "hello world#stop", DEFAULT_TEMPLATE_NAME);
    }

    @Test
    void evaluateVelocityCount() throws XWikiVelocityException
    {
        assertEvaluate("1true2true3true4true5false", "#foreach($nb in [1,2,3,4,5])$velocityCount$velocityHasNext#end",
            DEFAULT_TEMPLATE_NAME);

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
    void evaluateEmptyNamespaceInheritance() throws Exception
    {
        assertEvaluate("#mymacro", "#mymacro", "namespace");

        evaluate("#macro(mymacro)test#end", "");

        assertEvaluate("test", "#mymacro", "namespace");
    }

    @Test
    void evaluateOverrideMacros() throws Exception
    {
        assertEvaluate("#mymacro", "#mymacro", "namespace");

        evaluate("#macro(mymacro)global#end", "");

        assertEvaluate("global", "#mymacro", "namespace");

        // Start using namespace "namespace"
        getEngine().startedUsingMacroNamespace("namespace");

        // Register macro
        evaluate("#macro(mymacro)test1#end", "namespace");

        assertEvaluate("test1", "#mymacro", "namespace");

        // Override macro
        evaluate("#macro(mymacro)test2#end", "namespace");

        assertEvaluate("test2", "#mymacro", "namespace");

        // Override in the same script
        assertEvaluate("test4", "#macro(mymacro)test3#end#macro(mymacro)test4#end#mymacro", "namespace");

        // Mark namespace "namespace" as not used anymore
        getEngine().stoppedUsingMacroNamespace("namespace");
    }

    @Test
    void evaluateOverrideSubMacros() throws Exception
    {
        assertEvaluate("#mymacro", "#mymacro", "namespace");

        getEngine().startedUsingMacroNamespace("namespace");

        evaluate("#macro(mysubmacro)initial#end#macro(mymacro)#mysubmacro()#end", "namespace");

        assertEvaluate("initial", "#mymacro", "namespace");

        assertEvaluate("overwrite2", "#macro(mysubmacro)overwrite2#end#mymacro", "namespace");

        evaluate("#macro(mysubmacro)overwrite1#end", "namespace");

        assertEvaluate("overwrite1", "#mymacro", "namespace");

        // Mark namespace "namespace" as not used anymore
        getEngine().stoppedUsingMacroNamespace("namespace");
    }

    @Test
    void evaluateMacroParameters() throws Exception
    {
        assertEvaluate("$caller", "#macro (testMacro $called)$called#end#testMacro($caller)");
        assertEvaluate("$caller",
            "#macro (testMacro $called)#set($called = $NULL)$called#set($called = 'value')#end#testMacro($caller)");
        assertEvaluate("$caller",
            "#macro (testMacro $called)#set($called = $NULL)$called#set($called = 'value')#end#set($caller = 'value')#testMacro($caller)");
        assertEvaluate("$caller$caller2$caller",
            "#macro(macro1 $called)$called#macro2($caller2)$called#end#macro(macro2 $called)$called#end#macro1($caller)");

        Context context = new XWikiVelocityContext();
        context.put("test", new TestClass());
        context.put("other", new TestClass("othername"));

        assertEvaluate("namename", "#macro (testMacro $test $name)$name#end$test.name#testMacro($other, $test.name)",
            context);
    }

    @Test
    void evaluateMissingMacroParameter() throws XWikiVelocityException
    {
        assertEvaluate("global", "#macro(mymacro $var)$var#end#set($var = 'global')#mymacro()");
    }

    @Test
    void evaluateSetSharpString() throws Exception
    {
        assertEvaluate("#", "#set($var = \"#\")$var", DEFAULT_TEMPLATE_NAME);
        assertEvaluate("test#", "#set($var = \"test#\")$var", DEFAULT_TEMPLATE_NAME);
    }

    @Test
    void evaluateSetDollarString() throws Exception
    {
        assertEvaluate("$", "#set($var = \"$\")$var", DEFAULT_TEMPLATE_NAME);
        assertEvaluate("test$", "#set($var = \"test$\")$var", DEFAULT_TEMPLATE_NAME);
    }

    @Test
    void evaluateExpressionFollowedByTwoPipes() throws XWikiVelocityException
    {
        assertEvaluate("$var||", "$var||");
    }

    @Test
    void evaluateClassMethods() throws XWikiVelocityException
    {
        Context context = new XWikiVelocityContext();
        context.put("var", new TestClass());

        assertEvaluate("org.xwiki.velocity.internal.DefaultVelocityManagerTest$TestClass name",
            "$var.class.getName() $var.getName()", context);
    }

    @Test
    void evaluateWithSubEvaluate() throws XWikiVelocityException
    {
        Context context = new XWikiVelocityContext();
        context.put("test", new TestClass());

        assertEvaluate("top", "#set($var = 'top')$test.evaluate('$var')", context);
        assertEvaluate("sub", "$test.evaluate('#set($var = \"sub\")')$var", context);
        assertEvaluate("sub", "#set($var = 'top')$test.evaluate('#set($var = \"sub\")')$var", context);

        // TODO: update this test when a decision is taken in Velocity side regarding macro context behavior
        // See
        // https://mail-archives.apache.org/mod_mbox/velocity-dev/202001.mbox/%3cCAPnKnLHmL2oeYBNHvq3FHO1BmFW0DFjChk6sxXb9s+mwKhxirQ@mail.gmail.com%3e
        // assertEvaluate("local",
        // "#macro(mymacro)#set($var = 'local')$test.evaluate('#set($var = \"global\")')$var#end#mymacro()", context);
    }

    @Test
    void evaluateSpaceGobling() throws Exception
    {
        assertEvaluate("value\nvalueline", "#macro (testMacro)value#end#testMacro\n#testMacro()\nline");
    }

    // Note that this test is useless in Java 11 since it's not impacted by the same access restriction than more recent
    // versions of Java but keeping it anyway for when we move to Java 17.
    @Test
    void evaluateMethodCallFromUnaccessibleImplemetation() throws Exception
    {
        assertDoesNotThrow(() -> evaluate("$datetool.timeZone.getOffset($datetool.date.time)"));

        VelocityContext context = this.contextFactory.createContext();

        context.put("array", new String[] {"value0", "value1"});

        assertEvaluate("value1", "$array.get(1)", context);

        assertEvaluate("str", "$stringtool.trim('str')", context);
    }

    @Test
    void velocityContextToScriptContext() throws XWikiVelocityException
    {
        ScriptContext scriptContext = new SimpleScriptContext();

        when(this.scriptContextManager.getCurrentScriptContext()).thenReturn(scriptContext);
        when(this.scriptContextManager.getScriptContext()).thenReturn(scriptContext);

        assertTrue(scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).isEmpty());

        evaluate("content without any variable");

        assertTrue(scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).isEmpty());

        evaluate("#set($myvar = 'velocityvalue')");

        assertEquals(1, scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).size());
        assertEquals("velocityvalue", scriptContext.getAttribute("myvar", ScriptContext.ENGINE_SCOPE));

        scriptContext.setAttribute("scriptvar", "scriptvalue", ScriptContext.ENGINE_SCOPE);

        assertEvaluate("scriptvalue", "$scriptvar");
    }
}
