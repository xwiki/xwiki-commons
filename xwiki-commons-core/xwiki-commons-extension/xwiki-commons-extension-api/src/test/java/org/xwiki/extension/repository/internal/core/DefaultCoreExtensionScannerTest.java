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
package org.xwiki.extension.repository.internal.core;

import java.net.URL;

import org.junit.jupiter.api.Test;
import org.xwiki.environment.Environment;
import org.xwiki.extension.repository.internal.ExtensionSerializer;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test of {@link DefaultCoreExtensionScanner}.
 *
 * @version $Id$
 * @since 15.10
 * @since 15.5.4
 * @since 14.10.20
 */
@ComponentTest
class DefaultCoreExtensionScannerTest
{
    @InjectMockComponents
    private DefaultCoreExtensionScanner scanner;

    @MockComponent
    private Environment environment;

    @MockComponent
    private ExtensionSerializer parser;

    @Test
    void loadEnvironmentExtension() throws Exception
    {
        DefaultCoreExtensionRepository repository = new DefaultCoreExtensionRepository();
        
        when(this.environment.getResource("/META-INF/extension.xed"))
            .thenReturn(new URL("file:/segment1/segment2/META-INF/extension.xed"));
        
        this.scanner.loadEnvironmentExtension(repository);
        
        verify(this.parser).loadCoreExtensionDescriptor(eq(repository),
            eq(new URL("file:/segment1/segment2")), any());
    }
}
