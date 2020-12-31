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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.velocity.util.introspection.SecureIntrospectorImpl;
import org.slf4j.Logger;

/**
 * {@link SecureIntrospectorImpl} is way too restrictive with allowed {@link Class} methods.
 *
 * @version $Id$
 * @since 5.4RC1
 */
public class SecureIntrospector extends SecureIntrospectorImpl
{
    private static final String GETNAME = "getname";
    private final Map<Class, Set<String>> whitelistedMethods;

    /**
     * @param badClasses forbidden classes
     * @param badPackages forbidden packages
     * @param log the log
     */
    public SecureIntrospector(String[] badClasses, String[] badPackages, Logger log)
    {
        super(badClasses, badPackages, log);

        this.whitelistedMethods = new HashMap<>();
        this.prepareWhitelistClass();
        this.prepareWhiteListFile();
    }

    private void prepareWhitelistClass()
    {
        Set<String> whitelist = new HashSet<>();
        whitelist.add(GETNAME);
        whitelist.add("getsimpleName");
        whitelist.add("isarray");
        whitelist.add("isassignablefrom");
        whitelist.add("isenum");
        whitelist.add("isinstance");
        whitelist.add("isinterface");
        whitelist.add("islocalclass");
        whitelist.add("ismemberclass");
        whitelist.add("isprimitive");
        whitelist.add("issynthetic");
        whitelist.add("getenumconstants");
        this.whitelistedMethods.put(Class.class, whitelist);
    }

    private void prepareWhiteListFile()
    {
        Set<String> whitelist = new HashSet<>();
        whitelist.add("canexecute");
        whitelist.add("canread");
        whitelist.add("canwrite");
        whitelist.add("compareto");
        whitelist.add("createtempfile");
        whitelist.add("equals");
        whitelist.add("getabsolutefile");
        whitelist.add("getabsolutePath");
        whitelist.add("getcanonicalfile");
        whitelist.add("getcanonicalpath");
        whitelist.add("getfreespace");
        whitelist.add(GETNAME);
        whitelist.add("getparent");
        whitelist.add("getparentFile");
        whitelist.add("getpath");
        whitelist.add("gettotalspace");
        whitelist.add("getusablespace");
        whitelist.add("hashcode");
        whitelist.add("isabsolute");
        whitelist.add("isdirectory");
        whitelist.add("isfile");
        whitelist.add("ishidden");
        whitelist.add("lastmodified");
        whitelist.add("length");
        whitelist.add("topath");
        whitelist.add("tostring");
        whitelist.add("touri");
        whitelist.add("tourl");
        whitelist.add("getclass");
        this.whitelistedMethods.put(File.class, whitelist);
    }

    @Override
    public boolean checkObjectExecutePermission(Class clazz, String methodName)
    {
        Boolean result = null;
        if (methodName != null) {
            for (Map.Entry<Class, Set<String>> classSetEntry : this.whitelistedMethods.entrySet()) {
                if (classSetEntry.getKey().isAssignableFrom(clazz)) {
                    result = classSetEntry.getValue().contains(methodName.toLowerCase());
                    break;
                }
            }
        }

        if (result == null) {
            result = super.checkObjectExecutePermission(clazz, methodName);
        }
        return result;
    }
}
