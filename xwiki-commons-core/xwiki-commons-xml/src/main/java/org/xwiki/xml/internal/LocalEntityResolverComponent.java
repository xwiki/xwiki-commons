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
/*
 This class was copied from https://github.com/css4j/xml-dtd
 See https://github.com/css4j/xml-dtd/issues/7 for the reason why we copied it instead of having a dependency on it.

 Copyright (c) 1998-2022, Carlos Amengual.
 Originally Licensed under a BSD-style License but relicensed under LGPL for XWiki by Carlos Amengual.
 You can find the original license here:
 https://css4j.github.io/LICENSE.txt
 */
package org.xwiki.xml.internal;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.xml.EntityResolver;

/**
 * Implements EntityResolver2.
 * <p>
 * Has common W3C DTDs/entities built-in and loads others via the supplied
 * <code>SYSTEM</code> URL, provided that certain conditions are met:
 * </p>
 * <ul>
 * <li>URL protocol is <code>http</code>/<code>https</code>.</li>
 * <li>Either the mime type is valid for a DTD or entity, or the filename ends
 * with <code>.dtd</code>, <code>.ent</code> or <code>.mod</code>.</li>
 * <li>The whitelist is either disabled (no host added to it) or contains the
 * host from the URL.</li>
 * </ul>
 * <p>
 * If the whitelist was enabled (e.g. default constructor), any attempt to
 * download data from a remote URL not present in the whitelist is going to
 * produce an exception. You can use that to determine whether your documents
 * are referencing a DTD resource that is not bundled with this resolver.
 * </p>
 * <p>
 * If the constructor with a <code>false</code> argument was used, the whitelist
 * can still be enabled by adding a hostname via
 * {@link #addHostToWhiteList(String)}.
 * </p>
 * <p>
 * Although this resolver should protect you from most information leaks (see
 * <a href="https://owasp.org/www-community/attacks/Server_Side_Request_Forgery">SSRF
 * attacks</a>) and also from <code>jar:</code>
 * <a href="https://en.wikipedia.org/wiki/Zip_bomb">decompression bombs</a>, DoS
 * attacks based on entity expansion/recursion like the
 * <a href="https://en.wikipedia.org/wiki/Billion_laughs_attack">'billion laughs
 * attack'</a> may still be possible and should be prevented at the XML parser.
 * Be sure to use a properly configured, recent version of your parser.
 * </p>
 *
 * @version $Id$
 */
@Component
@Singleton
public class LocalEntityResolverComponent extends LocalEntityResolver implements EntityResolver
{
    /**
     * Construct a resolver with the whitelist enabled.
     */
    public LocalEntityResolverComponent()
    {
        super(true);
    }
}
