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
        Set<String> whitelist = prepareSet(
            GETNAME,
            "getsimplename",
            "isarray",
            "isassignablefrom",
            "isenum",
            "isinstance",
            "isinterface",
            "islocalclass",
            "ismemberclass",
            "isprimitive",
            "issynthetic",
            "getenumconstants"
        );
        this.whitelistedMethods.put(Class.class, whitelist);
    }

    private void prepareWhiteListFile()
    {
        Set<String> whitelist = prepareSet(
            "canexecute",
            "canread",
            "canwrite",
            "compareto",
            "createtempfile",
            "equals",
            "getabsolutefile",
            "getabsolutepath",
            "getcanonicalfile",
            "getcanonicalpath",
            "getfreespace",
            GETNAME,
            "getparent",
            "getparentfile",
            "getpath",
            "gettotalspace",
            "getusablespace",
            "hashcode",
            "isabsolute",
            "isdirectory",
            "isfile",
            "ishidden",
            "lastmodified",
            "length",
            "topath",
            "tostring",
            "touri",
            "tourl",
            "getclass"
        );
        this.whitelistedMethods.put(File.class, whitelist);
    }

    /**
     * @return a copy of the whitelisted methods used in this introspector.
     */
    protected Map<Class, Set<String>> getWhitelistedMethods()
    {
        return new HashMap<>(this.whitelistedMethods);
    }

    private static Set<String> prepareSet(String... methodNames)
    {
        Set<String> result = new HashSet<>();
        if (methodNames != null) {
            for (String methodName : methodNames) {
                result.add(methodName.toLowerCase());
            }
        }
        return result;
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
