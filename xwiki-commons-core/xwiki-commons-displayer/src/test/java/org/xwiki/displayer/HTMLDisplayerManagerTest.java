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
package org.xwiki.displayer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Provider;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.displayer.internal.DefaultDisplayerManager;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.junit5.mockito.MockComponent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Test the {@link HTMLDisplayerManager}.
 *
 * @version $Id$
 */
@ComponentTest
public class HTMLDisplayerManagerTest
{
    @MockComponent
    private HTMLDisplayer<String> stringHTMLDisplayer;

    @MockComponent
    @Named("context")
    private Provider<ComponentManager> componentManagerProvider;

    @InjectMockComponents
    private DefaultDisplayerManager htmlDisplayerManager;

    @BeforeEach
    public void configure(ComponentManager componentManager) throws Exception
    {
        when(componentManagerProvider.get()).thenReturn(componentManager);
        when(stringHTMLDisplayer.display(anyString())).thenAnswer(i -> "<input>" + i.getArgument(0) + "</input>");
        Answer answer = i -> {
            String attributes = i.<Map<String, String>>getArgument(1).entrySet().stream()
                    .map(entry -> entry.getKey() + "='" + entry.getValue() + "'").collect(Collectors.joining(" "));
            return "<input " + attributes + ">" + i.getArgument(0) + "</input>";
        };
        when(stringHTMLDisplayer.display(anyString(), anyMap())).thenAnswer(answer);
        when(stringHTMLDisplayer.display(anyString(), anyMap(), anyString())).thenAnswer(answer);
    }

    @Test
    public void testStringHTMLDisplay() throws Exception
    {
        HTMLDisplayer<String> htmlDisplayer = this.htmlDisplayerManager.getHTMLDisplayer(String.class);
        assertEquals(this.stringHTMLDisplayer, htmlDisplayer);

        assertEquals("<input>test</input>", htmlDisplayer.display("test"));

        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("id", "testid");
        parameters.put("class", "testclass");
        assertEquals("<input id='testid' class='testclass'>test</input>", htmlDisplayer.display("test", parameters));
        assertEquals("<input>test</input>",
                this.htmlDisplayerManager.display(String.class, "test"));
        assertEquals("<input id='testid' class='testclass'>test</input>",
                this.htmlDisplayerManager.display(String.class, "test", parameters));
        assertEquals("<input id='testid' class='testclass'>test</input>",
                this.htmlDisplayerManager.display(String.class, "test", parameters, "view"));

        assertThrows(HTMLDisplayerException.class, () -> this.htmlDisplayerManager.getHTMLDisplayer(Boolean.class));
    }
}
