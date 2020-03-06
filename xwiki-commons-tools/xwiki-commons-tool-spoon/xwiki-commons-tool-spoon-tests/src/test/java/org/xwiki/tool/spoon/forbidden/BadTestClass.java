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
package org.xwiki.tool.spoon.forbidden;

import java.io.File;
import java.net.URL;

import org.xwiki.tool.spoon.ForbiddenInvocationProcessorTest;

/**
 * Test class for {@link ForbiddenInvocationProcessorTest}.
 *
 * @version $Id$
 */
public class BadTestClass
{
    public void method() throws Exception
    {
        File file = new File("whatever");
        file.deleteOnExit();
        URL url = new URL("whatever");
        url.equals(url);
    }
}
