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
import java.util.Properties;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.velocity.VelocityContext;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.properties.ConverterManager;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentManagerRule;
import org.xwiki.velocity.VelocityEngine;
import org.xwiki.velocity.XWikiVelocityException;
import org.xwiki.velocity.internal.DefaultVelocityConfiguration;
import org.xwiki.velocity.internal.DefaultVelocityContextFactory;
import org.xwiki.velocity.internal.DefaultVelocityEngine;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link org.xwiki.velocity.introspection.MethodArgumentsUberspector}.
 *
 * @version $Id$
 * @since 6.4M3
 */
@ComponentList({
    DefaultVelocityEngine.class,
    DefaultVelocityConfiguration.class,
    DefaultVelocityContextFactory.class
})
public class MethodArgumentUberspectorTest
{
    @Rule
    public MockitoComponentManagerRule componentManager = new MockitoComponentManagerRule();

    private ConverterManager converterManager;

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
    }

    /**
     * This used to fail, see <a href="http://jira.xwiki.org/browse/XCOMMONS-710">XCOMMONS-710</a>.
     */
    @Test
    public void getMethodWhenAddingSameMethodNameToExtendingClassAndConversion() throws Exception
    {
        VelocityEngine velocityEngine = this.componentManager.getInstance(VelocityEngine.class);
        velocityEngine.initialize(new Properties());
        StringWriter writer = new StringWriter();
        VelocityContext context = new VelocityContext();
        context.put("var", new ExtendingClass());
        when(this.converterManager.convert(List.class, "test")).thenReturn(Arrays.asList("converted"));
        velocityEngine.evaluate(context, writer, "template", new StringReader("$var.method('test')"));
        assertEquals("success", writer.toString());
    }

    @Test
    public void getMethodWhenInnerMethodAndNoConversion() throws Exception
    {
        VelocityEngine velocityEngine = this.componentManager.getInstance(VelocityEngine.class);
        velocityEngine.initialize(new Properties());
        StringWriter writer = new StringWriter();
        VelocityContext context = new VelocityContext();
        context.put("var", new ExtendingClass());
        velocityEngine.evaluate(context, writer, "template", new StringReader("$var.method()"));
        assertEquals("inner", writer.toString());
    }

    @Test
    public void getMethodWhenNoConversion() throws Exception
    {
        VelocityEngine velocityEngine = this.componentManager.getInstance(VelocityEngine.class);
        velocityEngine.initialize(new Properties());
        StringWriter writer = new StringWriter();
        VelocityContext context = new VelocityContext();
        context.put("var", new ExtendingClass());
        velocityEngine.evaluate(context, writer, "template", new StringReader("$var.method(['converted'])"));
        assertEquals("success", writer.toString());
    }

    @Test
    public void getMethodWhenNoMatchingMethod() throws Exception
    {
        VelocityEngine velocityEngine = this.componentManager.getInstance(VelocityEngine.class);
        velocityEngine.initialize(new Properties());
        StringWriter writer = new StringWriter();
        VelocityContext context = new VelocityContext();
        context.put("var", new ExtendingClass());
        velocityEngine.evaluate(context, writer, "template", new StringReader("$var.notexisting()"));
        assertEquals("$var.notexisting()", writer.toString());
    }

    @Test
    public void getMethodWhenExistingMethodNameButInvalidSignature() throws Exception
    {
        VelocityEngine velocityEngine = this.componentManager.getInstance(VelocityEngine.class);
        velocityEngine.initialize(new Properties());
        StringWriter writer = new StringWriter();
        VelocityContext context = new VelocityContext();
        context.put("var", new ExtendingClass());
        try {
            velocityEngine.evaluate(context, writer, "template", new StringReader("$var.method('a', 'b')"));
            fail("Should have raised an exception");
        } catch (XWikiVelocityException expected) {
            assertEquals("Failed to evaluate content with id [template]", expected.getMessage());
            assertEquals("IllegalArgumentException: wrong number of arguments",
                ExceptionUtils.getRootCauseMessage(expected));
        }
    }
}
