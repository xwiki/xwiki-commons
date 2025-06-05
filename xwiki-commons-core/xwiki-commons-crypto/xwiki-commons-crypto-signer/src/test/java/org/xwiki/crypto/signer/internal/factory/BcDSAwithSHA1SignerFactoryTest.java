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

import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.crypto.AsymmetricKeyFactory;
import org.xwiki.crypto.BinaryStringEncoder;
import org.xwiki.crypto.internal.asymmetric.keyfactory.BcDSAKeyFactory;
import org.xwiki.crypto.internal.encoder.Base64BinaryStringEncoder;
import org.xwiki.crypto.params.cipher.asymmetric.PrivateKeyParameters;
import org.xwiki.crypto.params.cipher.asymmetric.PublicKeyParameters;
import org.xwiki.crypto.signer.Signer;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ComponentList({
    Base64BinaryStringEncoder.class,
    BcDSAKeyFactory.class
})
@ComponentTest
class BcDSAwithSHA1SignerFactoryTest
{
    @InjectComponentManager
    private ComponentManager componentManager;

    @InjectMockComponents
    private BcDSAwithSHA1SignerFactory bcDSAwithSHA1SignerFactory;

    @Inject
    @Named("Base64")
    private BinaryStringEncoder base64encoder;

    @Inject
    @Named("DSA")
    private AsymmetricKeyFactory keyFactory;

    private static final String PRIVATE_KEY = "MIIBTAIBADCCASwGByqGSM44BAEwggEfAoGBANQ9Oa1j9sWAhdXNyqz8HL/bA/e"
        + "d2VrBw6TPkgMyV1Upix58RSjOHMQNrgemSGkb80dRcLqVDYbI3ObnIJh83Zx6ze"
        + "aTpvUohGLyTa0F7UY15LkbJpyz8WFJaVykH85nz3Zo6Md9Z4X95yvF1+h9qYuak"
        + "jWcHW31+pN4u3cJNg5FAhUAj986cVG9NgWgWzFVSLbB9pEPbFUCgYEAmQrZFH3M"
        + "X5CLX/5vDvxyTeeRPZHLWc0ik3GwrIJExuVrOkuFInpx0aVbuJTxrEnY2fuc/+B"
        + "yj/F56DDO31+qPu7ZxbSvvD33OOk8eFEfn+Hia3QmA+dGrhUqoMpfDf4/GBgJhn"
        + "yQtFzMddHmYB0QnS9yX1n6DOWj/CSX0PvrlMYEFwIVAIO1GUQjAddL4btiFQnhe"
        + "N4fxBTa";

    private static final String PUBLIC_KEY = "MIIBtzCCASwGByqGSM44BAEwggEfAoGBANQ9Oa1j9sWAhdXNyqz8HL/bA/ed2VrB"
        + "w6TPkgMyV1Upix58RSjOHMQNrgemSGkb80dRcLqVDYbI3ObnIJh83Zx6zeaTpvUo"
        + "hGLyTa0F7UY15LkbJpyz8WFJaVykH85nz3Zo6Md9Z4X95yvF1+h9qYuakjWcHW31"
        + "+pN4u3cJNg5FAhUAj986cVG9NgWgWzFVSLbB9pEPbFUCgYEAmQrZFH3MX5CLX/5v"
        + "DvxyTeeRPZHLWc0ik3GwrIJExuVrOkuFInpx0aVbuJTxrEnY2fuc/+Byj/F56DDO"
        + "31+qPu7ZxbSvvD33OOk8eFEfn+Hia3QmA+dGrhUqoMpfDf4/GBgJhnyQtFzMddHm"
        + "YB0QnS9yX1n6DOWj/CSX0PvrlMYDgYQAAoGAJvnuTm8oI/RRI2tiZHtPkvSQaA3F"
        + "P4PRsVx6z1oIGg9OAxrtSS/aiQa+HWFg7fjHlMJ30Vh0yqt7igj70jaLGyDvr3MP"
        + "DyiO++72IiGUluc6yHg6m9cQ53eeJt9i44LJfTOw1S3YMU1ST7alokSnJRTICp5W"
        + "By0m1scwheuTo0E=";

    private static final String TEXT = "Congress shall make no law respecting an establishment of religion, or "
        + "prohibiting the free exercise thereof; or abridging the freedom of speech, "
        + "or of the press; or the right of the people peaceably to assemble, and to "
        + "petition the Government for a redress of grievances.";

    protected static PrivateKeyParameters privateKey;

    protected static PublicKeyParameters publicKey;

    protected static byte[] text;

    protected void runTestSignatureVerification(Signer signer, Signer verifier) throws Exception
    {
        byte[] signature = signer.generate(text);

        assertThat(signer.getEncoded(), equalTo(verifier.getEncoded()));
        assertTrue(verifier.verify(signature, text));
    }

    @BeforeEach
    void setUp() throws Exception
    {
        // Decode keys once for all tests.
        if (privateKey == null) {
            privateKey = this.keyFactory.fromPKCS8(this.base64encoder.decode(PRIVATE_KEY));
            publicKey = this.keyFactory.fromX509(this.base64encoder.decode(PUBLIC_KEY));
            text = TEXT.getBytes(StandardCharsets.UTF_8);
        }
    }

    @Test
    void dsaSignatureVerification() throws Exception
    {
        runTestSignatureVerification(
            this.bcDSAwithSHA1SignerFactory.getInstance(true, privateKey),
            this.bcDSAwithSHA1SignerFactory.getInstance(false, publicKey)
        );
    }
}
