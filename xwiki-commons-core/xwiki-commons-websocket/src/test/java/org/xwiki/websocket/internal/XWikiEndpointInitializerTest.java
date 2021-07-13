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
package org.xwiki.websocket.internal;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;

import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.environment.Environment;
import org.xwiki.environment.internal.ServletEnvironment;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.BeforeComponent;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.websocket.EndpointComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link XWikiEndpointInitializer}.
 * 
 * @version $Id$
 */
@ComponentTest
class XWikiEndpointInitializerTest
{
    static class StringDecoder implements Decoder.Text<String>
    {
        @Override
        public void init(EndpointConfig config)
        {
        }

        @Override
        public void destroy()
        {
        }

        @Override
        public String decode(String s) throws DecodeException
        {
            return null;
        }

        @Override
        public boolean willDecode(String s)
        {
            return false;
        }
    }

    static class StringEncoder implements Encoder.Text<String>
    {
        @Override
        public void init(EndpointConfig config)
        {
        }

        @Override
        public void destroy()
        {
        }

        @Override
        public String encode(String object) throws EncodeException
        {
            return null;
        }
    }

    @ServerEndpoint(value = "/test", decoders = {StringDecoder.class}, encoders = {
        StringEncoder.class}, subprotocols = {"foo", "bar"})
    static class TestEndpoint implements EndpointComponent
    {
    }

    static class InvalidEndpoint implements EndpointComponent
    {
    }

    @RegisterExtension
    static LogCaptureExtension logCapture = new LogCaptureExtension(LogLevel.INFO);

    @InjectMockComponents
    private XWikiEndpointInitializer initializer;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    @MockComponent
    @Named("context")
    private ComponentManager contextComponentManager;

    @MockComponent
    private XWikiEndpointConfigurator configurator;

    private ServerContainer serverContainer;

    @Captor
    ArgumentCaptor<ServerEndpointConfig> endPointConfigCaptor;

    @BeforeComponent
    void setup() throws Exception
    {
        this.serverContainer = mock(ServerContainer.class);
        ServletContext servletContext = mock(ServletContext.class);
        when(servletContext.getAttribute(ServerContainer.class.getName())).thenReturn(this.serverContainer);

        ServletEnvironment environment = mock(ServletEnvironment.class);
        when(environment.getServletContext()).thenReturn(servletContext);
        this.componentManager.registerComponent(Environment.class, environment);
    }

    @BeforeComponent("initialize")
    void setupInitialize() throws Exception
    {
        @SuppressWarnings("unchecked")
        ComponentDescriptor<? extends EndpointComponent> endPointOne = mock(ComponentDescriptor.class);
        doReturn(XWikiEndpointDispatcher.class).when(endPointOne).getImplementation();

        @SuppressWarnings("unchecked")
        ComponentDescriptor<? extends EndpointComponent> endPointTwo = mock(ComponentDescriptor.class);
        doReturn(TestEndpoint.class).when(endPointTwo).getImplementation();

        @SuppressWarnings("unchecked")
        ComponentDescriptor<? extends EndpointComponent> endPointThree = mock(ComponentDescriptor.class);
        doReturn(InvalidEndpoint.class).when(endPointThree).getImplementation();

        doReturn(Arrays.asList(endPointOne, endPointTwo, endPointThree)).when(this.contextComponentManager)
            .getComponentDescriptorList((Type) EndpointComponent.class);
    }

    @Test
    void initialize() throws Exception
    {
        verify(this.serverContainer, times(2)).addEndpoint(this.endPointConfigCaptor.capture());

        ServerEndpointConfig dispatcherConfig = this.endPointConfigCaptor.getAllValues().get(0);
        assertSame(XWikiEndpointDispatcher.class, dispatcherConfig.getEndpointClass());
        assertEquals("/websocket/{wiki}/{roleHint}", dispatcherConfig.getPath());
        assertSame(this.configurator, dispatcherConfig.getConfigurator());
        assertEquals(Collections.emptyList(), dispatcherConfig.getDecoders());
        assertEquals(Collections.emptyList(), dispatcherConfig.getEncoders());
        assertEquals(Collections.emptyList(), dispatcherConfig.getExtensions());
        assertEquals(Collections.emptyList(), dispatcherConfig.getSubprotocols());
        assertEquals(Collections.emptyMap(), dispatcherConfig.getUserProperties());

        ServerEndpointConfig testConfig = this.endPointConfigCaptor.getAllValues().get(1);
        assertEquals(TestEndpoint.class, testConfig.getEndpointClass());
        assertEquals("/websocket/test", testConfig.getPath());
        assertSame(this.configurator, testConfig.getConfigurator());
        assertEquals(Collections.singletonList(StringDecoder.class), testConfig.getDecoders());
        assertEquals(Collections.singletonList(StringEncoder.class), testConfig.getEncoders());
        assertEquals(Collections.emptyList(), testConfig.getExtensions());
        assertEquals(Arrays.asList("foo", "bar"), testConfig.getSubprotocols());
        assertEquals(Collections.emptyMap(), testConfig.getUserProperties());
    }

    @AfterAll
    static void verifyLog() throws Exception
    {
        // Assert log happening in the initialize() call.
        assertEquals(
            String.format("The [%s] end-point should either use the @ServerEndpoint annotation or extend Endpoint.",
                InvalidEndpoint.class.getName()),
            logCapture.getMessage(0));
    }
}
