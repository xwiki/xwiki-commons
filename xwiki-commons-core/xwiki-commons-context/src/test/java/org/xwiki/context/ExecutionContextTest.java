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


/**
 * @version $Id$ 
 * @since 4.3M1
 */
public class ExecutionContextTest
{

    @Test
    public void inheritance()
    {
        ExecutionContext context = new ExecutionContext();
        ExecutionContext parent = new ExecutionContext();

        ExecutionContextProperty inherited = new ExecutionContextProperty("inherited");
        inherited.setInherited(true);
        inherited.setValue("test");

        parent.declareProperty(inherited);

        ExecutionContextProperty original = new ExecutionContextProperty("shadowed");
        original.setInherited(true);
        original.setValue("original");

        ExecutionContextProperty shadowed = new ExecutionContextProperty("shadowed");
        shadowed.setInherited(true);
        shadowed.setValue("shadowed");

        parent.declareProperty(original);
        context.declareProperty(shadowed);


        ExecutionContextProperty cloned = new ExecutionContextProperty("cloned");
        cloned.setValue("cloned");
        cloned.setInherited(true);
        cloned.setFinal(true);

        parent.declareProperty(cloned);
        context.declareProperty(cloned.clone());

        context.inheritFrom(parent);

        Assert.assertTrue(context.getProperty("inherited").equals("test"));
        Assert.assertTrue(context.getProperty("shadowed").equals("shadowed"));
        Assert.assertTrue(context.getProperty("cloned").equals("cloned"));
    }

    @Test(expected=IllegalStateException.class)
    public void illegalInheritance()
    {
        ExecutionContext context = new ExecutionContext();
        ExecutionContext parent = new ExecutionContext();

        ExecutionContextProperty inherited = new ExecutionContextProperty("inherited");
        inherited.setInherited(true);
        inherited.setValue("test");
        inherited.setFinal(true);

        context.declareProperty(inherited);
        parent.declareProperty(inherited);

        context.inheritFrom(parent);
    }
}