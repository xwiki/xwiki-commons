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
package org.xwiki.velocity.introspection;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link SecureIntrospector}.
 *
 * @version $Id$
 */
class SecureIntrospectorTest
{
    class CustomFile extends File
    {
        public CustomFile(String s)
        {
            super(s);
        }
    }

    @Test
    void checkObjectExecutePermissionWithClass()
    {
        SecureIntrospector secureIntrospector = new SecureIntrospector(new String[] {}, new String[] {}, null);
        assertTrue(secureIntrospector.checkObjectExecutePermission(Class.class, "isLocalClass"));
    }

    @Test
    void checkObjectExecutePermissionWithFile()
    {
        SecureIntrospector secureIntrospector = new SecureIntrospector(new String[] {}, new String[] {}, null);
        assertTrue(secureIntrospector.checkObjectExecutePermission(File.class, "toString"));
        assertFalse(secureIntrospector.checkObjectExecutePermission(File.class, "mkdir"));

        assertTrue(secureIntrospector.checkObjectExecutePermission(File.class, "tostring"));
        assertFalse(secureIntrospector.checkObjectExecutePermission(File.class, "renameto"));
        assertFalse(secureIntrospector.checkObjectExecutePermission(File.class, "renameTo"));

        assertTrue(secureIntrospector.checkObjectExecutePermission(CustomFile.class, "toString"));
        assertFalse(secureIntrospector.checkObjectExecutePermission(CustomFile.class, "mkdir"));
    }

    @Test
    void checkObjectExecutePermissionBlacklistedClass()
    {
        SecureIntrospector secureIntrospector = new SecureIntrospector(
            new String[] { "java.util.ArrayList" }, new String[] {}, null);
        assertTrue(secureIntrospector.checkObjectExecutePermission(File.class, "toString"));
        assertFalse(secureIntrospector.checkObjectExecutePermission(ArrayList.class, "toString"));
    }

    @Test
    void whiteListMethodAreStoredLowercase()
    {
        SecureIntrospector secureIntrospector = new SecureIntrospector(new String[] {}, new String[] {}, null);
        Map<Class, Set<String>> whitelistedMethods = secureIntrospector.getWhitelistedMethods();

        for (Map.Entry<Class, Set<String>> classSetEntry : whitelistedMethods.entrySet()) {
            for (String methodName : classSetEntry.getValue()) {
                assertTrue(StringUtils.isAllLowerCase(methodName));
            }
        }
    }
}
