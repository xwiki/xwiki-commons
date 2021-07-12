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
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.descriptor.ComponentDescriptor;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.environment.Environment;
import org.xwiki.environment.internal.ServletEnvironment;
import org.xwiki.websocket.EndpointComponent;

/**
 * Initializes the WebSocket end-points defined using {@link EndpointComponent}.
 * 
 * @version $Id$
 * @since 13.6RC1
 */
@Component(roles = XWikiEndpointInitializer.class)
@Singleton
public class XWikiEndpointInitializer implements Initializable
{
    @Inject
    private Logger logger;

    @Inject
    private Environment environment;

    @Inject
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private XWikiEndpointConfigurator configurator;

    @Override
    public void initialize() throws InitializationException
    {
        ServerContainer serverContainer = getServerContainer();
        if (serverContainer != null) {
            initialize(serverContainer);
        } else {
            this.logger.warn("The servlet container doesn't support the Java API for WebSocket (JSR 356).");
        }
    }

    private ServerContainer getServerContainer()
    {
        if (this.environment instanceof ServletEnvironment) {
            return (ServerContainer) ((ServletEnvironment) this.environment).getServletContext()
                .getAttribute(ServerContainer.class.getName());
        } else {
            this.logger.warn("We can't initialize the WebSocket end-points in a non-servlet environment.");
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void initialize(ServerContainer container)
    {
        List<ComponentDescriptor<EndpointComponent>> endPoints =
            this.componentManagerProvider.get().getComponentDescriptorList((Type) EndpointComponent.class);
        for (ComponentDescriptor<EndpointComponent> endPoint : endPoints) {
            initialize((Class<EndpointComponent>) endPoint.getImplementation(), container);
        }
    }

    private void initialize(Class<EndpointComponent> implementation, ServerContainer container)
    {
        ServerEndpoint endPointConfig = implementation.getAnnotation(ServerEndpoint.class);
        if (endPointConfig != null) {
            initialize(ServerEndpointConfig.Builder.create(implementation, "/websocket" + endPointConfig.value())
                .subprotocols(Arrays.asList(endPointConfig.subprotocols()))
                .encoders(Arrays.asList(endPointConfig.encoders())).decoders(Arrays.asList(endPointConfig.decoders())),
                container);
        } else if (XWikiEndpointDispatcher.class.equals(implementation)) {
            initialize(ServerEndpointConfig.Builder.create(implementation, "/websocket/{wiki}/{roleHint}"), container);
        } else if (!Endpoint.class.isAssignableFrom(implementation)) {
            // The end-point is marked with XWikiEndpointComponent but it doesn't have the @ServerEndpoint annotation
            // (for static end-points) nor does it extend Endpoint (for dynamic end-points). Something is wrong.
            this.logger.warn("The [{}] end-point should either use the @ServerEndpoint annotation or extend Endpoint.",
                implementation.getName());
        }
    }

    private void initialize(ServerEndpointConfig.Builder configBuilder, ServerContainer container)
    {
        // Make sure the end-points are going to be created using our factory as XWiki components (because they
        // implement XWikiEndpointComponent).
        ServerEndpointConfig config = configBuilder.configurator(this.configurator).build();
        try {
            container.addEndpoint(config);
        } catch (DeploymentException e) {
            this.logger.warn("Failed to deploy WebSocket end-point implemented by []. Root cause is [{}].",
                config.getEndpointClass().getName(), ExceptionUtils.getRootCauseMessage(e));
        }
    }
}
