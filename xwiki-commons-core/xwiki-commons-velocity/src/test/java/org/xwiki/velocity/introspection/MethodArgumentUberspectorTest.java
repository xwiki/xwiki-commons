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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.velocity.VelocityContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.logging.LoggerConfiguration;
import org.xwiki.properties.internal.DefaultConverterManager;
import org.xwiki.properties.internal.converter.CollectionConverter;
import org.xwiki.properties.internal.converter.ConvertUtilsConverter;
import org.xwiki.properties.internal.converter.EnumConverter;
import org.xwiki.properties.internal.converter.ListConverter;
import org.xwiki.properties.internal.converter.LocaleConverter;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.velocity.VelocityEngine;
import org.xwiki.velocity.VelocityManager;
import org.xwiki.velocity.internal.DefaultVelocityConfiguration;
import org.xwiki.velocity.internal.DefaultVelocityContextFactory;
import org.xwiki.velocity.internal.DefaultVelocityManager;
import org.xwiki.velocity.internal.InternalVelocityEngine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link org.xwiki.velocity.introspection.MethodArgumentsUberspector}.
 *
 * @version $Id$
 * @since 6.4M3
 */
@ComponentTest
// @formatter:off
@ComponentList({
    InternalVelocityEngine.class,
    DefaultVelocityConfiguration.class,
    DefaultVelocityContextFactory.class,
    DefaultVelocityManager.class,
    DefaultConverterManager.class,
    EnumConverter.class,
    ConvertUtilsConverter.class,
    ListConverter.class,
    CollectionConverter.class,
    LocaleConverter.class
})
// @formatter:on
class MethodArgumentUberspectorTest
{
    @InjectComponentManager
    MockitoComponentManager componentManager;

    @MockComponent
    private LoggerConfiguration loggerConfiguration;

    @MockComponent
    private ScriptContextManager scriptContextManager;

    @Inject
    private VelocityManager velocityManager;

    private StringWriter writer;

    private VelocityEngine engine;

    private VelocityContext context;

    @BeforeComponent
    void setUpComponents() throws Exception
    {
        this.componentManager.registerMemoryConfigurationSource();

        this.componentManager.registerComponent(ComponentManager.class, "context", this.componentManager);

        ExecutionContext executionContext = new ExecutionContext();
        Execution execution = this.componentManager.registerMockComponent(Execution.class);
        when(execution.getContext()).thenReturn(executionContext);
    }

    public class InnerClass
    {
        public String method()
        {
            return "inner";
        }

        public Optional<String> methodReturningOptional(String value)
        {
            return Optional.ofNullable(value);
        }
    }

    public class ExtendingClass extends InnerClass
    {
        public String method(List parameter)
        {
            return "method list " + parameter;
        }

        public String methodWithGeneric(List<Locale> genericParameter)
        {
            return "methode list Locale " + genericParameter;
        }

        public String methodWithVararg(Integer param1, Double... params)
        {
            StringBuilder builder = new StringBuilder("success");
            for (Double param : params) {
                builder.append(' ');
                builder.append(param);
            }
            return builder.toString();
        }

        public String conflictingcasemethod(Integer param)
        {
            return "conflictlowercase";
        }

        public String conflictingCaseMethod(Integer param)
        {
            return "conflictuppercase";
        }

        public String conflictingMethod(String param1, String param2)
        {
            return "conflict1";
        }

        public String conflictingMethod(Integer param1, Integer param2)
        {
            return "conflict2";
        }

        public String conflictingMethod(String param1, String param2, String... vararg)
        {
            return "conflict3";
        }
    }

    public class IntegerList extends ArrayList<Integer>
    {
    	
    }

    @BeforeEach
    void setUp() throws Exception
    {
        this.engine = this.velocityManager.getVelocityEngine();
        this.writer = new StringWriter();
        this.context = new VelocityContext();
        this.context.put("var", new ExtendingClass());
        this.context.put("integerlist", new IntegerList());
    }

    @AfterEach
    void tearDown() throws Exception
    {
        if (this.writer != null) {
            this.writer.close();
        }
    }

    @Test
    void getMethodWhenVarargsWithNoConversionAndNoVarargParamPassed() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template", new StringReader("$var.methodWithVararg(10)"));
        assertEquals("success", writer.toString());
    }

    @Test
    void getMethodWhenVarargsWithNoConversionAndOneVarargParamPassed() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("$var.methodWithVararg(10, 10.0)"));
        assertEquals("success 10.0", this.writer.toString());
    }

    @Test
    void getMethodWhenVarargsWithNoConversionAndTwoVarargParamsPassed() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("$var.methodWithVararg(10, 10.0, 10.0)"));
        assertEquals("success 10.0 10.0", this.writer.toString());
    }

    @Test
    void getMethodWhenVarargsWithConversionAndNoVarargParamPassed() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template", new StringReader("$var.methodWithVararg('10')"));
        assertEquals("success", writer.toString());
    }

    @Test
    void getMethodWhenVarargsWithConversionAndOneVarargParamPassed() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("$var.methodWithVararg('10', 10.0)"));
        assertEquals("success 10.0", writer.toString());
    }

    @Test
    void getMethodWhenVarargsWithConversionAndTwoVarargParamsPassed() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("$var.methodWithVararg('10', 10.0, 10.0)"));
        assertEquals("success 10.0 10.0", writer.toString());
    }

    @Test
    void getMethodWhenVarargsWithConversionAndVarargParamPassedNeedingConversion() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("$var.methodWithVararg('10', 10.0, '10.0')"));
        assertEquals("success 10.0 10.0", writer.toString());
    }

    /**
     * This used to fail, see <a href="https://jira.xwiki.org/browse/XCOMMONS-710">XCOMMONS-710</a>.
     */
    @Test
    void getMethodWhenAddingSameMethodNameToExtendingClassAndConversion() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template", new StringReader("$var.method('test')"));
        assertEquals("method list [test]", this.writer.toString());
    }

    @Test
    void getMethodWhenInnerMethodAndNoConversion() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template", new StringReader("$var.method()"));
        assertEquals("inner", this.writer.toString());
    }

    @Test
    void getMethodWhenNoConversion() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template", new StringReader("$var.method(['converted'])"));
        assertEquals("method list [converted]", this.writer.toString());
    }

    @Test
    void getMethodWhenNoMatchingMethod() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template", new StringReader("$var.notexisting()"));
        assertEquals("$var.notexisting()", this.writer.toString());
    }

    @Test
    void getMethodWithGeneric() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("$var.methodWithGeneric('en, fr')"));
        assertEquals("methode list Locale [en, fr]", this.writer.toString());
    }

    @Test
    void addAllWithWildcardKnownGenericArgument() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template", new StringReader("""
            #set ($myString = '1 2 3')
            #set ($myArray = $myString.split(' '))
            #set ($discard = $integerlist.addAll($myArray))
            $integerlist $integerlist[0].class"""));
        assertEquals("[1, 2, 3] class java.lang.Integer", this.writer.toString());
    }

    @Test
    void addAllWithWildcardUnnownGenericArgument() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template", new StringReader("""
            #set ($myList = [])
            #set ($myString = '1 2 3')
            #set ($myArray = $myString.split(' '))
            #set ($discard = $myList.addAll($myArray))
            $myList $myList[0].class"""));
        assertEquals("[1, 2, 3] class java.lang.String", this.writer.toString());
    }

    @Test
    void getConflictingLowercaseMethod() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("$var.conflictingcasemethod('10')"));
        assertEquals("conflictlowercase", this.writer.toString());
    }

    @Test
    void getConflictingUppercaseMethod() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("$var.conflictingCaseMethod('10')"));
        assertEquals("conflictuppercase", this.writer.toString());
    }

    @Test
    void getConflictingMethodWithMatchingStringArguments() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("$var.conflictingMethod('test1', true)"));
        assertEquals("conflict1", this.writer.toString());
    }

    @Test
    void getConflictingMethodWithMatchingIntegerArguments() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("$var.conflictingMethod(10, true)"));
        assertEquals("conflict2", this.writer.toString());
    }

    @Test
    void getConflictingMethodWithMatchingIntArguments() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("#set($integer = 10)$var.conflictingMethod($integer.intValue(), true)"));
        assertEquals("conflict2", this.writer.toString());
    }

    @Test
    void getConflictingMethodWithMatchingDoubleArguments() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("$var.conflictingMethod(10.0, 10.0)"));
        assertEquals("conflict2", this.writer.toString());
    }
}
