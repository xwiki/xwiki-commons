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
package org.xwiki.context;

import org.junit.Test;
import org.junit.Assert;
import org.xwiki.test.AbstractTestCase;

/**
 * @version $Id$ 
 * @since 4.3M1
 */
public class ExecutionContextTest extends AbstractTestCase
{
    @Test
    public void inheritance()
    {
        ExecutionContext context = new ExecutionContext();
        ExecutionContext parent = new ExecutionContext();

        parent.newProperty("inherited").inherited().initial("test").declare();

        parent.newProperty("shadowed").inherited().initial("original").declare();

        context.newProperty("shadowed").inherited().initial("shadowed").declare();

        context.inheritFrom(parent);

        Assert.assertTrue(context.getProperty("inherited").equals("test"));
        Assert.assertTrue(context.getProperty("shadowed").equals("shadowed"));
    }

    @Test(expected=IllegalStateException.class)
    public void illegalInheritance()
    {
        ExecutionContext context = new ExecutionContext();
        ExecutionContext parent = new ExecutionContext();

        parent.newProperty("inherited").inherited().initial("test").makeFinal().declare();
        context.newProperty("inherited").inherited().initial("test").makeFinal().declare();

        context.inheritFrom(parent);
    }
}