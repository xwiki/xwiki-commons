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
package org.xwiki.xml.internal.html;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.xml.html.HTMLElementSanitizer;

/**
 * Default {@link HTMLElementSanitizer} that loads the implementation chosen by the configuration.
 *
 * @version $Id$
 * @since 14.6RC1
 */
@Component
@Singleton
public class DefaultHTMLElementSanitizer implements HTMLElementSanitizer, Initializable
{
    private static final String CONFIGURATION_KEY = "xml.htmlElementSanitizer";

    private HTMLElementSanitizer implementation;

    @Inject
    @Named("restricted")
    private Provider<ConfigurationSource> configurationSourceProvider;

    @Inject
    private Execution execution;

    @Inject
    private Provider<ComponentManager> componentManagerProvider;

    @Inject
    private Logger logger;

    @Override
    public void initialize() throws InitializationException
    {

        ConfigurationSource configurationSource = this.configurationSourceProvider.get();

        String hint;
        if (configurationSource != null) {
            hint = configurationSource.getProperty(CONFIGURATION_KEY, SecureHTMLElementSanitizer.HINT);
        } else {
            hint = SecureHTMLElementSanitizer.HINT;
        }

        try {
            this.implementation = loadImplementationWithSecureFallback(hint);
        } catch (ComponentLookupException ex) {
            throw new InitializationException("Couldn't initialize the default secure HTMLElementSanitizer", ex);
        }
    }

    private HTMLElementSanitizer loadImplementationWithSecureFallback(String hint) throws ComponentLookupException
    {
        ComponentManager componentManager = this.componentManagerProvider.get();
        HTMLElementSanitizer result;

        try {
            result = componentManager.getInstance(HTMLElementSanitizer.class, hint);
        } catch (ComponentLookupException e) {
            this.logger.error("Couldn't load the configured HTMLElementSanitizer with hint [{}], falling back to the "
                + "default secure implementation: {}", hint, ExceptionUtils.getRootCauseMessage(e));
            result = componentManager.getInstance(HTMLElementSanitizer.class, SecureHTMLElementSanitizer.HINT);
        }

        return result;
    }

    private HTMLElementSanitizer getImplementation()
    {
        ExecutionContext context = this.execution.getContext();

        HTMLElementSanitizer result = this.implementation;

        if (context != null && context.hasProperty(HTMLElementSanitizer.EXECUTION_CONTEXT_HINT_KEY)) {
            String hint = (String) context.getProperty(HTMLElementSanitizer.EXECUTION_CONTEXT_HINT_KEY);

            try {
                result = this.componentManagerProvider.get().getInstance(HTMLElementSanitizer.class, hint);
            } catch (ComponentLookupException e) {
                this.logger.error("Couldn't load the HTMLElementSanitizer with hint [{}] from the execution context, "
                    + "falling back to the configured implementation: {}", hint, ExceptionUtils.getRootCauseMessage(e));
            }
        }

        return result;
    }

    @Override
    public boolean isElementAllowed(String elementName)
    {
        return getImplementation().isElementAllowed(elementName);
    }

    @Override
    public boolean isAttributeAllowed(String elementName, String attributeName, String value)
    {
        return getImplementation().isAttributeAllowed(elementName, attributeName, value);
    }
}
