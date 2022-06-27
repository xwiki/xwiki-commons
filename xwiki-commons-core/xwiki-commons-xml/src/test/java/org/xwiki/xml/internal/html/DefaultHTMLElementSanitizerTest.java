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

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.configuration.internal.RestrictedConfigurationSourceProvider;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.test.LogLevel;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.LogCaptureExtension;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.MockComponent;
import org.xwiki.test.mockito.MockitoComponentManager;
import org.xwiki.xml.html.HTMLConstants;
import org.xwiki.xml.html.HTMLElementSanitizer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Test the {@link DefaultHTMLElementSanitizer}.
 *
 * @version $Id$
 * @since 14.6RC1
 */
@ComponentTest
@ComponentList({ DefaultHTMLElementSanitizer.class,
    SecureHTMLElementSanitizer.class,
    InsecureHTMLElementSanitizer.class,
    HTMLElementSanitizerConfiguration.class,
    RestrictedConfigurationSourceProvider.class,
    HTMLDefinitions.class,
    MathMLDefinitions.class,
    SVGDefinitions.class
})
class DefaultHTMLElementSanitizerTest
{
    private static final String EXPECTED_ERROR_LOADING_FOO =
        "Couldn't load the configured HTMLElementSanitizer with hint [foo], falling back to "
            + "the default secure implementation: ComponentLookupException: Can't find descriptor for the "
            + "component with type [interface org.xwiki.xml.html.HTMLElementSanitizer] and hint [foo]";

    private static final String EXPECTED_ERROR_LOADING_FOO_FROM_EXECUTION = "Couldn't load the HTMLElementSanitizer "
        + "with hint [foo] from the execution context, falling back to the configured implementation: "
        + "ComponentLookupException: Can't find descriptor for the component with type "
        + "[interface org.xwiki.xml.html.HTMLElementSanitizer] and hint [foo]";

    private static final String FOO = "foo";

    private static final String INSECURE = "insecure";

    @RegisterExtension
    private final LogCaptureExtension logCaptureExtension = new LogCaptureExtension(LogLevel.ERROR);

    @MockComponent
    @Named("restricted")
    private ConfigurationSource configurationSource;

    @MockComponent
    private Execution execution;

    @BeforeEach
    void mockConfiguration()
    {
        when(this.configurationSource.getProperty(any(), eq(List.class), eq(Collections.emptyList())))
            .thenReturn(Collections.emptyList());
        when(this.configurationSource.getProperty(any(), eq(Boolean.class), eq(true))).thenReturn(true);
    }

    @Test
    void secure(MockitoComponentManager componentManager) throws ComponentLookupException
    {
        when(this.configurationSource.getProperty(any(), eq(SecureHTMLElementSanitizer.HINT)))
            .thenReturn(SecureHTMLElementSanitizer.HINT);
        HTMLElementSanitizer htmlElementSanitizer = componentManager.getInstance(HTMLElementSanitizer.class);
        assertFalse(htmlElementSanitizer.isElementAllowed("no-such-element"));
        assertFalse(htmlElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_IMG, "onerror", "hello"));
        assertTrue(htmlElementSanitizer.isAttributeAllowed(HTMLConstants.TAG_SPAN, "data-xwiki", "true"));
    }

    @Test
    void insecure(MockitoComponentManager componentManager) throws ComponentLookupException
    {
        when(this.configurationSource.getProperty(any(), eq(SecureHTMLElementSanitizer.HINT)))
            .thenReturn(INSECURE);
        HTMLElementSanitizer htmlElementSanitizer = componentManager.getInstance(HTMLElementSanitizer.class);
        assertTrue(htmlElementSanitizer.isElementAllowed(HTMLConstants.TAG_SCRIPT));
    }

    @Test
    void fallback(MockitoComponentManager componentManager) throws ComponentLookupException
    {
        when(this.configurationSource.getProperty(any(), eq(SecureHTMLElementSanitizer.HINT))).thenReturn(FOO);

        HTMLElementSanitizer htmlElementSanitizer = componentManager.getInstance(HTMLElementSanitizer.class);

        assertEquals(EXPECTED_ERROR_LOADING_FOO, this.logCaptureExtension.getMessage(0));
        assertFalse(htmlElementSanitizer.isElementAllowed(FOO));
    }

    @Test
    void throwingWhenFailure(MockitoComponentManager componentManager)
    {
        componentManager.unregisterComponent((Type) HTMLElementSanitizer.class, SecureHTMLElementSanitizer.HINT);

        when(this.configurationSource.getProperty(any(), eq(SecureHTMLElementSanitizer.HINT))).thenReturn(FOO);

        ComponentLookupException exception = assertThrows(ComponentLookupException.class,
            () -> componentManager.getInstance(HTMLElementSanitizer.class));
        assertEquals("Couldn't initialize the default secure HTMLElementSanitizer",
            exception.getCause().getMessage());

        assertEquals(EXPECTED_ERROR_LOADING_FOO, this.logCaptureExtension.getMessage(0));
    }

    @Test
    void customFromExecutionContext(MockitoComponentManager componentManager) throws ComponentLookupException
    {
        when(this.configurationSource.getProperty(any(), eq(SecureHTMLElementSanitizer.HINT)))
            .thenReturn(SecureHTMLElementSanitizer.HINT);

        HTMLElementSanitizer htmlElementSanitizer = componentManager.getInstance(HTMLElementSanitizer.class);
        assertFalse(htmlElementSanitizer.isElementAllowed(FOO));

        ExecutionContext context = new ExecutionContext();
        context.setProperty(HTMLElementSanitizer.EXECUTION_CONTEXT_HINT_KEY, INSECURE);
        when(this.execution.getContext()).thenReturn(context);

        assertTrue(htmlElementSanitizer.isElementAllowed(FOO));
    }

    @Test
    void fallBackToConfiguredWhenExecutionContextHintIsInvalid(MockitoComponentManager componentManager)
        throws ComponentLookupException
    {
        when(this.configurationSource.getProperty(any(), eq(SecureHTMLElementSanitizer.HINT)))
            .thenReturn(SecureHTMLElementSanitizer.HINT);

        HTMLElementSanitizer htmlElementSanitizer = componentManager.getInstance(HTMLElementSanitizer.class);
        assertFalse(htmlElementSanitizer.isElementAllowed(FOO));

        ExecutionContext context = new ExecutionContext();
        context.setProperty(HTMLElementSanitizer.EXECUTION_CONTEXT_HINT_KEY, FOO);
        when(this.execution.getContext()).thenReturn(context);

        assertFalse(htmlElementSanitizer.isElementAllowed(FOO));

        assertEquals(EXPECTED_ERROR_LOADING_FOO_FROM_EXECUTION, this.logCaptureExtension.getMessage(0));
    }
}
