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

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

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
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.velocity.VelocityContextFactory;
import org.xwiki.velocity.XWikiVelocityContext;
import org.xwiki.velocity.XWikiVelocityException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DefaultVelocityEngine}.
 */
@ComponentTest
@ComponentList({DefaultVelocityConfiguration.class, DefaultVelocityContextFactory.class})
class DefaultVelocityEngineTest
{
    public class TestClass
    {
        private Context context;

        private String name = "name";

        public TestClass()
        {
        }

        public TestClass(Context context)
        {
            this.context = context;
        }

        public TestClass(Context context, String name)
        {
            this.context = context;
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
            StringWriter writer = new StringWriter();
            engine.evaluate(this.context, writer, namespace, input);
            return writer.toString();
        }
    }

    private static final String DEFAULT_TEMPLATE_NAME = "mytemplate";

    @RegisterExtension
    private LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.WARN);

    @MockComponent
    private ComponentManager componentManager;

    @MockComponent
    private LoggerConfiguration loggerConfiguration;

    @InjectMockComponents
    private DefaultVelocityEngine engine;

    @MockComponent
    private Execution execution;

    @MockComponent
    private ConfigurationSource configurationSource;

    @Inject
    private VelocityContextFactory contextFactory;

    @BeforeEach
    void beforeEach()
    {
        when(this.execution.getContext()).thenReturn(new ExecutionContext());
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

    private void assertEvaluate(String expected, String content, String template, Context context)
        throws XWikiVelocityException
    {
        String result = evaluate(content, template, context);

        assertEquals(expected, result);
    }

    private String evaluate(String content) throws XWikiVelocityException
    {
        return evaluate(content, DEFAULT_TEMPLATE_NAME);
    }

    private String evaluate(String content, String template) throws XWikiVelocityException
    {
        return evaluate(content, template, this.contextFactory.createContext());
    }

    private String evaluate(String content, String template, Context context) throws XWikiVelocityException
    {
        StringWriter writer = new StringWriter();
        this.engine.evaluate(context, writer, template, content);

        return writer.toString();
    }

    @Test
    void evaluateWithReader() throws Exception
    {
        this.engine.initialize(new Properties());
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new XWikiVelocityContext(), writer, DEFAULT_TEMPLATE_NAME,
            new StringReader("#set($foo='hello')$foo World"));
        assertEquals("hello World", writer.toString());
    }

    @Test
    void evaluateWithString() throws Exception
    {
        this.engine.initialize(new Properties());

        assertEvaluate("hello World", "#set($foo='hello')$foo World", DEFAULT_TEMPLATE_NAME);
    }

    /**
     * Verify that the default configuration doesn't allow calling Class.forName.
     */
    @Test
    void evaluateWithSecureUberspectorActiveByDefault() throws Exception
    {
        this.engine.initialize(new Properties());

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
        this.engine.initialize(new Properties());
        StringWriter writer = new StringWriter();
        Context context = new XWikiVelocityContext();
        context.put("null", null);
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add(null);
        list.add("3");
        context.put("list", list);
        this.engine.evaluate(context, writer, DEFAULT_TEMPLATE_NAME,
            "#set($foo = true)${foo}#set($foo = $null)${foo}\n" + "#foreach($i in $list)${foreach.count}=$!{i} #end");
        assertEquals("true${foo}\n1=1 2= 3=3 ", writer.toString());

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
        this.engine.initialize(properties);
        StringWriter writer = new StringWriter();
        this.engine.evaluate(new XWikiVelocityContext(), writer, DEFAULT_TEMPLATE_NAME,
            "#set($foo = 'test')#set($object = $foo.class.forName('java.util.ArrayList')"
                + ".newInstance())$object.size()");
        assertEquals("0", writer.toString());
    }

    @Test
    void evaluateMacroIsolation() throws Exception
    {
        this.engine.initialize(new Properties());
        Context context = new XWikiVelocityContext();
        this.engine.evaluate(context, new StringWriter(), "template1", "#macro(mymacro)test#end");
        assertEvaluate("#mymacro", "#mymacro", "template2");
    }

    @Test
    void evaluateConfigureMacrosToBeGlobal() throws Exception
    {
        Properties properties = new Properties();
        // Force macros to be global
        properties.put(RuntimeConstants.VM_PERM_INLINE_LOCAL, "false");
        this.engine.initialize(properties);
        Context context = new XWikiVelocityContext();
        this.engine.evaluate(context, new StringWriter(), "template1", "#macro(mymacro)test#end");
        assertEvaluate("test", "#mymacro", "template2");
    }

    @Test
    void evaluateMacroScope() throws XWikiVelocityException
    {
        this.engine.initialize(new Properties());

        assertEvaluate("{}", "#macro (testMacro)$macro#end#testMacro()");
    }

    @Test
    void evaluateMacroWithMissingParameter() throws XWikiVelocityException
    {
        this.engine.initialize(new Properties());

        assertEvaluate("$param", "#macro (testMacro $param)$param#end#testMacro()");
    }

    @Test
    void evaluateMacroWithLiteralBooleanParameter() throws XWikiVelocityException
    {
        this.engine.initialize(new Properties());

        assertEvaluate("true", "#macro (testMacro $param)$param#end#testMacro(true)");
    }

    @Test
    void evaluateMacroWithLiteralMapParameter() throws XWikiVelocityException
    {
        this.engine.initialize(new Properties());

        assertEvaluate("{}", "#macro (testMacro $param)$param#end#testMacro({})");
    }

    @Test
    void evaluateMacroWithLiteralArrayParameter() throws XWikiVelocityException
    {
        this.engine.initialize(new Properties());

        assertEvaluate("[]", "#macro (testMacro $param)$param#end#testMacro([])");
    }

    @Test
    void evaluateUseGlobalMacroUseLocalMacro() throws XWikiVelocityException
    {
        this.engine.initialize(new Properties());

        // Register a global macro
        evaluate("#macro (globalMacro)#localMacro()#end", "");

        // Can the global macro call local macros
        assertEvaluate("locallocal", "#macro (localMacro)local#end#localMacro()#globalMacro()");
    }

    @Test
    void evaluateGlobalMacroUseLocalMacroAfterInclude() throws XWikiVelocityException
    {
        this.engine.initialize(new Properties());

        // Register a global macro
        evaluate("#macro (globalMacro)#localMacro()#end", "");

        Context context = new XWikiVelocityContext();
        context.put("test", new TestClass(context));

        // Can the global macro still call the local macro after executing another script in the same namespace
        assertEvaluate("locallocallocal",
            "#globalMacro()#macro (localMacro)local#end$test.evaluate('')#globalMacro()#localMacro()", context);
    }

    @Test
    void evaluateWithncludeMacro() throws XWikiVelocityException
    {
        this.engine.initialize(new Properties());

        Context context = new XWikiVelocityContext();
        context.put("test", new TestClass(context));

        // Can the script use an included macro
        assertEvaluate("included", "$test.evaluate('#macro(includedmacro)included#end')#includedmacro()", context);
    }

    @Test
    void evaluateWithOverwrittenIncludeMacro() throws XWikiVelocityException
    {
        this.engine.initialize(new Properties());

        Context context = new XWikiVelocityContext();
        context.put("test", new TestClass(context));

        // Can the script overwrite an included macro
        // No because macros are registered before executing the script (TODO: fix that)
        assertEvaluate("included",
            "$test.evaluate('#macro(includedmacro)included#end')#macro(includedmacro)ovewritten#end#includedmacro()",
            context);
    }

    @Test
    void evauateTemplateScope() throws XWikiVelocityException
    {
        this.engine.initialize(new Properties());

        assertEvaluate("{}", "$template");
    }

    /**
     * Verify namespace is properly cleared when not needed anymore.
     */
    @Test
    void evaluateMacroNamespaceCleanup() throws Exception
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
    void evaluateWithStopCommand() throws Exception
    {
        this.engine.initialize(new Properties());

        assertEvaluate("hello world", "hello world#stop", DEFAULT_TEMPLATE_NAME);
    }

    @Test
    void evaluateVelocityCount() throws XWikiVelocityException
    {
        this.engine.initialize(new Properties());

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
        this.engine.initialize(new Properties());

        assertEvaluate("#mymacro", "#mymacro", "namespace");

        this.engine.evaluate(new XWikiVelocityContext(), new StringWriter(), "", "#macro(mymacro)test#end");

        assertEvaluate("test", "#mymacro", "namespace");
    }

    @Test
    void evaluateOverrideMacros() throws Exception
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

        // Override in the same script
        assertEvaluate("test4", "#macro(mymacro)test3#end#macro(mymacro)test4#end#mymacro", "namespace");

        // Mark namespace "namespace" as not used anymore
        this.engine.stoppedUsingMacroNamespace("namespace");
    }

    @Test
    void evaluateMacroParameters() throws Exception
    {
        this.engine.initialize(new Properties());

        assertEvaluate("$caller", "#macro (testMacro $called)$called#end#testMacro($caller)");
        assertEvaluate("$caller",
            "#macro (testMacro $called)#set($called = $NULL)$called#set($called = 'value')#end#testMacro($caller)");
        assertEvaluate("$caller",
            "#macro (testMacro $called)#set($called = $NULL)$called#set($called = 'value')#end#set($caller = 'value')#testMacro($caller)");
        assertEvaluate("$caller$caller2$caller",
            "#macro(macro1 $called)$called#macro2($caller2)$called#end#macro(macro2 $called)$called#end#macro1($caller)");

        Context context = new XWikiVelocityContext();
        context.put("test", new TestClass(context));
        context.put("other", new TestClass(context, "othername"));

        assertEvaluate("namename", "#macro (testMacro $test $name)$name#end$test.name#testMacro($other, $test.name)",
            context);
    }

    @Test
    void evaluateMissingMacroParameter() throws XWikiVelocityException
    {
        this.engine.initialize(new Properties());

        assertEvaluate("global", "#macro(mymacro $var)$var#end#set($var = 'global')#mymacro()");
    }

    @Test
    void evaluateSetSharpString() throws Exception
    {
        this.engine.initialize(new Properties());

        assertEvaluate("#", "#set($var = \"#\")$var", DEFAULT_TEMPLATE_NAME);
        assertEvaluate("test#", "#set($var = \"test#\")$var", DEFAULT_TEMPLATE_NAME);
    }

    @Test
    void evaluateSetDollarString() throws Exception
    {
        this.engine.initialize(new Properties());

        assertEvaluate("$", "#set($var = \"$\")$var", DEFAULT_TEMPLATE_NAME);
        assertEvaluate("test$", "#set($var = \"test$\")$var", DEFAULT_TEMPLATE_NAME);
    }

    @Test
    void evaluateExpressionFollowedByTwoPipes() throws XWikiVelocityException
    {
        this.engine.initialize(new Properties());

        assertEvaluate("$var||", "$var||");
    }

    @Test
    void evaluateClassMethods() throws XWikiVelocityException
    {
        this.engine.initialize(new Properties());

        Context context = new XWikiVelocityContext();
        context.put("var", new TestClass());

        assertEvaluate("org.xwiki.velocity.internal.DefaultVelocityEngineTest$TestClass name",
            "$var.class.getName() $var.getName()", context);
    }

    @Test
    void evaluateWithSubEvaluate() throws XWikiVelocityException
    {
        this.engine.initialize(new Properties());

        Context context = new XWikiVelocityContext();
        context.put("test", new TestClass(context));

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
        this.engine.initialize(new Properties());

        assertEvaluate("value\nvalueline", "#macro (testMacro)value#end#testMacro\n#testMacro()\nline");
    }

    @Test
    void evaluateWhenNotInitialized()
    {
        Throwable exception = assertThrows(XWikiVelocityException.class, () -> {
            this.engine.evaluate(null, new StringWriter(), null, (Reader) null);
        });
        assertEquals("This Velocity Engine has not yet been initialized. "
            + "You must call its initialize() method before you can use it.", exception.getMessage());
    }

    // Note that this test is useless in Java 11 since it's not impacted by the same access restriction than more recent
    // versions of Java but keeping it anyway for when we move to Java 17.
    @Test
    void evaluateMethodCallFromUnaccessibleImplemetation() throws Exception
    {
        this.engine.initialize(new Properties());

        assertDoesNotThrow(() -> evaluate("$datetool.timeZone.getOffset($datetool.date.time)"));
    }
}
