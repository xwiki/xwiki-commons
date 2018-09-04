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

import org.junit.Assert;
import org.junit.Test;

public class AmplExecutionContextTest
{
    @Test(timeout = 10000)
    public void illegalInheritance_failAssert0() throws Exception
    {
        try {
            ExecutionContext context = new ExecutionContext();
            ExecutionContext parent = new ExecutionContext();
            parent.newProperty("inherited").inherited().initial("test").makeFinal().declare();
            context.newProperty("inherited").inherited().initial("test").makeFinal().declare();
            context.inheritFrom(parent);
            org.junit.Assert.fail("illegalInheritance should have thrown IllegalStateException");
        } catch (IllegalStateException expected) {
            Assert.assertEquals(
                "Execution context cannot be inherited because it already contains property [inherited] which must be inherited because it is an inherited final property.",
                expected.getMessage());
        }
    }
}

