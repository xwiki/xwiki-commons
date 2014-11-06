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
package org.xwiki.velocity.internal.jmx;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;

import org.apache.velocity.VelocityContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentManagerRule;
import org.xwiki.velocity.VelocityConfiguration;
import org.xwiki.velocity.VelocityEngine;
import org.xwiki.velocity.internal.DefaultVelocityContextFactory;
import org.xwiki.velocity.internal.DefaultVelocityEngine;

import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link JMXVelocityEngine}.
 *
 * @version $Id$
 * @since 2.4M2
 */
@ComponentList({DefaultVelocityEngine.class, DefaultVelocityContextFactory.class})
public class JMXVelocityEngineTest
{
    @Rule
    public MockitoComponentManagerRule componentManager = new MockitoComponentManagerRule();

    @Before
    public void setUp() throws Exception
    {
        VelocityConfiguration vc = this.componentManager.registerMockComponent(VelocityConfiguration.class);
        Properties props = new Properties();
        props.setProperty("velocimacro.permissions.allow.inline.local.scope", Boolean.TRUE.toString());
        when(vc.getProperties()).thenReturn(props);
        when(vc.getTools()).thenReturn(new Properties());
    }

    @Test
    public void testGetTemplates() throws Exception
    {
        VelocityEngine engine = this.componentManager.getInstance(VelocityEngine.class);
        engine.initialize(new Properties());
        JMXVelocityEngine jmxBean = new JMXVelocityEngine(engine);

        TabularData data = jmxBean.getTemplates();

        Assert.assertEquals(1, data.values().size());
        CompositeData cd = ((CompositeData) data.values().iterator().next());
        Assert.assertEquals("<global>", cd.get("templateName"));
        Assert.assertEquals(0, ((String[]) cd.get("macroNames")).length);

        engine.startedUsingMacroNamespace("testmacronamespace");

        try {
            StringWriter out = new StringWriter();
            engine.evaluate(new VelocityContext(), out, "testmacronamespace", "#macro(testmacro)#end");
            data = jmxBean.getTemplates();

            Assert.assertEquals(2, data.values().size());
            Map<String, String[]> retrievedData = new HashMap<String, String[]>();
            for (CompositeData cdata : (Collection<CompositeData>) data.values()) {
                retrievedData.put((String) cdata.get("templateName"), (String[]) cdata.get("macroNames"));
            }
            Assert.assertEquals(0, retrievedData.get("<global>").length);

            String[] namespace = retrievedData.get(Thread.currentThread().getId() + ":testmacronamespace");
            Assert.assertNotNull(namespace);
            Assert.assertEquals(1, namespace.length);
            Assert.assertEquals("testmacro", namespace[0]);
        } finally {
            engine.stoppedUsingMacroNamespace("testmacronamespace");
        }
    }
}
