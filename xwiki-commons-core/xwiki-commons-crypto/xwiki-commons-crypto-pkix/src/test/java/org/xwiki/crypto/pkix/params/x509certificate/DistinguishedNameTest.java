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
package org.xwiki.crypto.pkix.params.x509certificate;

import java.io.IOException;

import org.bouncycastle.asn1.x500.X500Name;
import org.junit.jupiter.api.Test;
import org.xwiki.crypto.pkix.params.PrincipalIndentifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class DistinguishedNameTest
{
    @Test
    void equality()
    {
        PrincipalIndentifier john = new DistinguishedName("CN=John Doe");
        PrincipalIndentifier jane = new DistinguishedName("CN=Jane Doe");
        PrincipalIndentifier johnAlias = new DistinguishedName(new X500Name("CN=John Doe"));
        PrincipalIndentifier johnOtherImpl = new PrincipalIndentifier()
        {
            @Override
            public byte[] getEncoded() throws IOException
            {
                return new X500Name("CN=John Doe").getEncoded();
            }

            @Override
            public String getName()
            {
                return "CN=John Doe";
            }
        };
        PrincipalIndentifier janeOtherImpl = new PrincipalIndentifier()
        {
            @Override
            public byte[] getEncoded() throws IOException
            {
                return new X500Name("CN=Jane Doe").getEncoded();
            }

            @Override
            public String getName()
            {
                return "CN=Jane Doe";
            }
        };

        assertEquals(john, john);
        assertEquals(johnAlias, john);
        assertEquals(john, johnOtherImpl);
        assertNotEquals(jane, john);
        assertNotEquals(john, jane);
        assertNotEquals(janeOtherImpl, john);
        assertNotEquals(new Object(), john);
    }
}
