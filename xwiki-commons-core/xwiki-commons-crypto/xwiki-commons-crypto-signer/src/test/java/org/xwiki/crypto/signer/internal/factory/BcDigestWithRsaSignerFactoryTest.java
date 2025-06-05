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
package org.xwiki.crypto.signer.internal.factory;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.crypto.internal.asymmetric.keyfactory.BcRSAKeyFactory;
import org.xwiki.crypto.internal.encoder.Base64BinaryStringEncoder;
import org.xwiki.crypto.signer.SignerFactory;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;

import static org.xwiki.crypto.signer.internal.factory.RsaSignerFactoryTestUtil.assertSignatureVerification;
import static org.xwiki.crypto.signer.internal.factory.RsaSignerFactoryTestUtil.privateKey;
import static org.xwiki.crypto.signer.internal.factory.RsaSignerFactoryTestUtil.publicKey;
import static org.xwiki.crypto.signer.internal.factory.RsaSignerFactoryTestUtil.setupTest;

@ComponentList({
    Base64BinaryStringEncoder.class,
    BcRSAKeyFactory.class,
    BcSHA1withRsaSignerFactory.class,
    BcSHA224withRsaSignerFactory.class,
    BcSHA256withRsaSignerFactory.class,
    BcSHA384withRsaSignerFactory.class,
    BcSHA512withRsaSignerFactory.class,
    BcMD5withRsaSignerFactory.class
})
@ComponentTest
class BcDigestWithRsaSignerFactoryTest
{
    @InjectComponentManager
    private ComponentManager componentManager;

    @BeforeEach
    void setUp() throws Exception
    {
        setupTest(this.componentManager);
    }

    @ParameterizedTest
    @CsvSource({
        "SHA1withRSAEncryption",
        "SHA224withRSAEncryption",
        "SHA256withRSAEncryption",
        "SHA384withRSAEncryption",
        "SHA512withRSAEncryption",
        "MD5withRSAEncryption"
    })
    void signatureVerification(String hint) throws Exception
    {
        AbstractBcSignerFactory abstractBcSignerFactory = this.componentManager.getInstance(SignerFactory.class, hint);
        assertSignatureVerification(
            abstractBcSignerFactory.getInstance(true, privateKey),
            abstractBcSignerFactory.getInstance(false, publicKey)
        );
    }
}
