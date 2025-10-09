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
package org.xwiki.crypto.password.internal.kdf.factory;

import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;
import org.xwiki.crypto.params.cipher.symmetric.KeyWithIVParameters;
import org.xwiki.crypto.password.KeyDerivationFunction;
import org.xwiki.crypto.password.PasswordToByteConverter;
import org.xwiki.crypto.password.params.KeyDerivationFunctionParameters;
import org.xwiki.crypto.password.params.PBKDF2Parameters;
import org.xwiki.crypto.password.params.ScryptParameters;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ComponentTest
@ComponentList({ BcScryptKeyDerivationFunctionFactory.class, BcPKCS5S2KeyDerivationFunctionFactory.class })
class DefaultKeyDerivationFunctionFactoryTest
{
    @InjectMockComponents
    private DefaultKeyDerivationFunctionFactory factory;

    private KeyDerivationFunction getKDFInstance(KeyDerivationFunctionParameters parameters)
    {
        return this.factory.getInstance(parameters);
    }

    @Test
    void pbkdf2DecodingTest() throws Exception
    {
        byte[] password = PasswordToByteConverter.convert("password");
        byte[] encoded = Base64.decode("MCYGCSqGSIb3DQEFDDAZBBAcO15L0rQ01O2RJ5UhyqCdAgID6AIBIA==");

        KeyDerivationFunction kdf =
            getKDFInstance(new PBKDF2Parameters(32, 1000, Hex.decode("1c3b5e4bd2b434d4ed91279521caa09d")));
        KeyWithIVParameters params = kdf.derive(password, 8);

        KeyDerivationFunction kdf2 = this.factory.getInstance(encoded);
        KeyWithIVParameters params2 = kdf2.derive(password, 8);

        assertThat(kdf.getEncoded(), equalTo(encoded));
        assertThat(params.getKey(), equalTo(params2.getKey()));
        assertThat(params2.getIV(), equalTo(params2.getIV()));
    }

    @Test
    void scryptDecodingTest() throws Exception
    {
        byte[] password = PasswordToByteConverter.convert("password");
        byte[] encoded = Base64.decode("MCwGCSsGAQQB2kcECzAfBBAcO15L0rQ01O2RJ5UhyqCdAgICAAIBEAIBAgIBIA==");

        KeyDerivationFunction kdf =
            getKDFInstance(new ScryptParameters(32, 512, 2, 16, Hex.decode("1c3b5e4bd2b434d4ed91279521caa09d")));
        KeyWithIVParameters params = kdf.derive(password, 8);

        KeyDerivationFunction kdf2 = this.factory.getInstance(encoded);
        KeyWithIVParameters params2 = kdf2.derive(password, 8);

        assertThat(kdf.getEncoded(), equalTo(encoded));
        assertThat(params.getKey(), equalTo(params2.getKey()));
        assertThat(params2.getIV(), equalTo(params2.getIV()));
    }
}
