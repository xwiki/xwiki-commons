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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.velocity.VelocityContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.properties.ConverterManager;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentManagerRule;
import org.xwiki.velocity.VelocityEngine;
import org.xwiki.velocity.XWikiVelocityException;
import org.xwiki.velocity.internal.DefaultVelocityConfiguration;
import org.xwiki.velocity.internal.DefaultVelocityContextFactory;
import org.xwiki.velocity.internal.DefaultVelocityEngine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link org.xwiki.velocity.introspection.MethodArgumentsUberspector}.
 *
 * @version $Id$
 * @since 6.4M3
 */
@ComponentList({ DefaultVelocityEngine.class, DefaultVelocityConfiguration.class, DefaultVelocityContextFactory.class })
public class MethodArgumentUberspectorTest
{
    @Rule
    public MockitoComponentManagerRule componentManager = new MockitoComponentManagerRule();

    private ConverterManager converterManager;

    private StringWriter writer;

    private VelocityEngine engine;

    private VelocityContext context;

    @BeforeComponent
    public void setUpComponents() throws Exception
    {
        this.componentManager.registerMemoryConfigurationSource();
        this.converterManager = this.componentManager.registerMockComponent(ConverterManager.class);
    }

    public class InnerClass
    {
        public String method()
        {
            return "inner";
        }
    }

    public class ExtendingClass extends InnerClass
    {
        public String method(List parameter)
        {
            if (parameter.get(0).equals("converted")) {
                return "success";
            } else {
                return "failure";
            }
        }

        public String methodWithGeneric(List<Locale> genericParameter)
        {
            if (genericParameter.get(0) instanceof Locale) {
                return "success";
            } else {
                return "failure";
            }
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
    }

    @Before
    public void setUp() throws Exception
    {
        this.engine = this.componentManager.getInstance(VelocityEngine.class);
        this.engine.initialize(new Properties());
        this.writer = new StringWriter();
        this.context = new VelocityContext();
        this.context.put("var", new ExtendingClass());
    }

    @After
    public void tearDown() throws Exception
    {
        if (this.writer != null) {
            this.writer.close();
        }
    }

    @Test
    public void getMethodWhenVarargsWithNoConversionAndNoVarargParamPassed() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template", new StringReader("$var.methodWithVararg(10)"));
        assertEquals("success", writer.toString());
    }

    @Test
    public void getMethodWhenVarargsWithNoConversionAndOneVarargParamPassed() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("$var.methodWithVararg(10, 10.0)"));
        assertEquals("success 10.0", this.writer.toString());
    }

    @Test
    public void getMethodWhenVarargsWithNoConversionAndTwoVarargParamsPassed() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("$var.methodWithVararg(10, 10.0, 10.0)"));
        assertEquals("success 10.0 10.0", this.writer.toString());
    }

    @Test
    public void getMethodWhenVarargsWithConversionAndNoVarargParamPassed() throws Exception
    {
        when(this.converterManager.convert(Integer.class, "10")).thenReturn(10);
        this.engine.evaluate(this.context, this.writer, "template", new StringReader("$var.methodWithVararg('10')"));
        assertEquals("success", writer.toString());
    }

    @Test
    public void getMethodWhenVarargsWithConversionAndOneVarargParamPassed() throws Exception
    {
        when(this.converterManager.convert(Integer.class, "10")).thenReturn(10);
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("$var.methodWithVararg('10', 10.0)"));
        assertEquals("success 10.0", writer.toString());
    }

    @Test
    public void getMethodWhenVarargsWithConversionAndTwoVarargParamsPassed() throws Exception
    {
        when(this.converterManager.convert(Integer.class, "10")).thenReturn(10);
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("$var.methodWithVararg('10', 10.0, 10.0)"));
        assertEquals("success 10.0 10.0", writer.toString());
    }

    @Test
    public void getMethodWhenVarargsWithConversionAndVarargParamPassedNeedingConversion() throws Exception
    {
        when(this.converterManager.convert(Integer.class, "10")).thenReturn(10);
        when(this.converterManager.convert(Double.class, "10.0")).thenReturn(10.0);
        this.engine.evaluate(this.context, this.writer, "template",
            new StringReader("$var.methodWithVararg('10', 10.0, '10.0')"));
        assertEquals("success 10.0 10.0", writer.toString());
    }

    /**
     * This used to fail, see <a href="https://jira.xwiki.org/browse/XCOMMONS-710">XCOMMONS-710</a>.
     */
    @Test
    public void getMethodWhenAddingSameMethodNameToExtendingClassAndConversion() throws Exception
    {
        when(this.converterManager.convert(List.class, "test")).thenReturn(Arrays.asList("converted"));
        this.engine.evaluate(this.context, this.writer, "template", new StringReader("$var.method('test')"));
        assertEquals("success", this.writer.toString());
    }

    @Test
    public void getMethodWhenInnerMethodAndNoConversion() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template", new StringReader("$var.method()"));
        assertEquals("inner", this.writer.toString());
    }

    @Test
    public void getMethodWhenNoConversion() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template", new StringReader("$var.method(['converted'])"));
        assertEquals("success", this.writer.toString());
    }

    @Test
    public void getMethodWhenNoMatchingMethod() throws Exception
    {
        this.engine.evaluate(this.context, this.writer, "template", new StringReader("$var.notexisting()"));
        assertEquals("$var.notexisting()", this.writer.toString());
    }

    @Test
    public void getMethodWhenExistingMethodNameButInvalidSignature() throws Exception
    {
        try {
            this.engine.evaluate(this.context, this.writer, "template", new StringReader("$var.method('a', 'b')"));
            fail("Should have raised an exception");
        } catch (XWikiVelocityException expected) {
            assertEquals("Failed to evaluate content with id [template]", expected.getMessage());
            assertEquals("IllegalArgumentException: wrong number of arguments",
                ExceptionUtils.getRootCauseMessage(expected));
        }
    }

    @Test
    public void getMethodWithGeneric() throws Exception
    {
        when(this.converterManager.convert(new DefaultParameterizedType(null, List.class, Locale.class), "en, fr"))
            .thenReturn(Arrays.asList(Locale.ENGLISH, Locale.FRENCH));
        this.engine.evaluate(this.context, this.writer, "template", new StringReader("$var.methodWithGeneric('en, fr')"));
        assertEquals("success", this.writer.toString());
    }
}
