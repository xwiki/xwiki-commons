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
package org.xwiki.crypto.internal.digest.factory;

import javax.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.xwiki.crypto.BinaryStringEncoder;
import org.xwiki.crypto.internal.encoder.Base64BinaryStringEncoder;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

@ComponentTest
// @formatter:off
@ComponentList({
    Base64BinaryStringEncoder.class
})
// @formatter:on
public class BcSHA384DigestFactoryTest extends AbstractDigestFactoryTest
{
    @InjectMockComponents
    @Named("SHA-384")
    private BcSHA384DigestFactory bcSHA384DigestFactory;

    @BeforeEach
    void configure() throws Exception
    {
        factory = bcSHA384DigestFactory;

        if (digestResult == null) {
            digestAlgo = "SHA-384";
            digestAlgId = SHA384_DIGEST_ALGO;
            digestSize = 48;

            BinaryStringEncoder base64encoder = componentManager.getInstance(BinaryStringEncoder.class, "Base64");
            digestResult = base64encoder.decode(SHA384_DIGEST);
        }
    }
}
