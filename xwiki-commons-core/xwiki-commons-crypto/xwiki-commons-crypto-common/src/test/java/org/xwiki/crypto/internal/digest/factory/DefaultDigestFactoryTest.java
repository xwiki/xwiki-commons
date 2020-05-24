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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.crypto.BinaryStringEncoder;
import org.xwiki.crypto.internal.encoder.Base64BinaryStringEncoder;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ComponentTest
// @formatter:off
@ComponentList({
    Base64BinaryStringEncoder.class,
    BcMD5DigestFactory.class,
    BcSHA1DigestFactory.class,
    BcSHA224DigestFactory.class,
    BcSHA256DigestFactory.class,
    BcSHA384DigestFactory.class,
    BcSHA512DigestFactory.class
})
// @formatter:on
class DefaultDigestFactoryTest extends AbstractDigestFactoryTestConstants
{
    private static final byte[] BYTES = TEXT.getBytes();

    @InjectMockComponents
    private DefaultDigestFactory factory;

    @InjectComponentManager
    private ComponentManager componentManager;

    private byte[] md5Digest;

    private byte[] sha1Digest;

    private byte[] sha224Digest;

    private byte[] sha256Digest;

    private byte[] sha384Digest;

    private byte[] sha512Digest;

    @BeforeEach
    void configure() throws Exception
    {
        if (md5Digest == null) {
            BinaryStringEncoder base64encoder = componentManager.getInstance(BinaryStringEncoder.class, "Base64");
            md5Digest = base64encoder.decode(MD5_DIGEST);
            sha1Digest = base64encoder.decode(SHA1_DIGEST);
            sha224Digest = base64encoder.decode(SHA224_DIGEST);
            sha256Digest = base64encoder.decode(SHA256_DIGEST);
            sha384Digest = base64encoder.decode(SHA384_DIGEST);
            sha512Digest = base64encoder.decode(SHA512_DIGEST);
        }
    }

    @Test
    void testDefaultDigestFactory() throws Exception
    {
        assertThat(factory.getInstance(MD5_DIGEST_ALGO.getEncoded()).digest(BYTES), equalTo(md5Digest));
        assertThat(factory.getInstance(SHA1_DIGEST_ALGO.getEncoded()).digest(BYTES), equalTo(sha1Digest));
        assertThat(factory.getInstance(SHA224_DIGEST_ALGO.getEncoded()).digest(BYTES), equalTo(sha224Digest));
        assertThat(factory.getInstance(SHA256_DIGEST_ALGO.getEncoded()).digest(BYTES), equalTo(sha256Digest));
        assertThat(factory.getInstance(SHA384_DIGEST_ALGO.getEncoded()).digest(BYTES), equalTo(sha384Digest));
        assertThat(factory.getInstance(SHA512_DIGEST_ALGO.getEncoded()).digest(BYTES), equalTo(sha512Digest));
    }
}
