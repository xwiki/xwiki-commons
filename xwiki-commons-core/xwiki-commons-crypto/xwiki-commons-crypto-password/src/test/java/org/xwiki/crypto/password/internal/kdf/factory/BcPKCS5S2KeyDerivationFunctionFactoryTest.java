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

import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.Test;
import org.xwiki.crypto.params.cipher.symmetric.KeyParameter;
import org.xwiki.crypto.params.cipher.symmetric.KeyWithIVParameters;
import org.xwiki.crypto.password.KeyDerivationFunction;
import org.xwiki.crypto.password.PasswordToByteConverter;
import org.xwiki.crypto.password.params.PBKDF2Parameters;
import org.xwiki.test.annotation.AllComponents;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.xwiki.crypto.password.PasswordToByteConverter.ToBytesMode.PKCS12;
import static org.xwiki.crypto.password.PasswordToByteConverter.ToBytesMode.PKCS5;

/**
 * PKCS5S2 Key Derivation Function Factory Test.
 *
 * @version $Id$
 */
@ComponentTest
@AllComponents
class BcPKCS5S2KeyDerivationFunctionFactoryTest
{
    @InjectMockComponents
    private BcPKCS5S2KeyDerivationFunctionFactory factory;

    private KeyDerivationFunction getKDFInstance(PBKDF2Parameters parameters)
    {
        return this.factory.getInstance(parameters);
    }

    @Test
    void pbkdf2PropertiesTest()
    {
        assertThat(this.factory.getKDFAlgorithmName(), equalTo("PKCS5S2"));
    }

    /**
     * from: http://www.ietf.org/rfc/rfc3211.txt
     */
    @Test
    void pbkdf2ConformanceTest1()
    {
        byte[] salt = Hex.decode("12 34 56 78 78 56 34 12");
        byte[] password = PasswordToByteConverter.convert("password", PKCS5);
        byte[] key = Hex.decode("D1 DA A7 86 15 F2 87 E6");

        assertThat(getKDFInstance(new PBKDF2Parameters(8, 5, salt)).derive(password).getKey(), equalTo(key));
    }

    /**
     * from: http://www.ietf.org/rfc/rfc3211.txt
     */
    @Test
    void pbkdf2ConformanceTest2()
    {
        byte[] salt = Hex.decode("12 34 56 78 78 56 34 12");
        byte[] password = PasswordToByteConverter.convert(
            "All n-entities must communicate with other n-entities via n-1 entiteeheehees", PKCS5);
        byte[] key = Hex.decode("6A 89 70 BF 68 C9 2C AE A8 4A 8D F2 85 10 85 86");

        assertThat(getKDFInstance(new PBKDF2Parameters(16, 500, salt)).derive(password).getKey(), equalTo(key));
    }

    /**
     * from: http://pythonhosted.org/passlib/lib/passlib.hash.atlassian_pbkdf2_sha1.html
     */
    @Test
    void pbkdf2ConfluenceTest()
    {
        byte[] salt = Hex.decode("0d0217254d37f2ee0fec576cb854d8ff");
        byte[] password = PasswordToByteConverter.convert("password");
        byte[] key = Hex.decode("edf96e6e3591f8d96b9ed4addc47a7632edea176bb2fa8a03fa3179b75b5bf09");

        assertThat(getKDFInstance(new PBKDF2Parameters(32, 10000, salt)).derive(password).getKey(), equalTo(key));
    }

    @Test
    void pbkdf2PKCS12Test()
    {
        byte[] salt = Hex.decode("12 34 56 78 78 56 34 12");
        byte[] password = PasswordToByteConverter.convert("password", PKCS12);
        byte[] key = new byte[] { 5, 54, -36, -24, 96, -76, 7, -128 };

        assertThat(getKDFInstance(new PBKDF2Parameters(8, 5, salt)).derive(password).getKey(), equalTo(key));
    }

    @Test
    void pbkdf2WithIVTest()
    {
        byte[] salt = Hex.decode("12 34 56 78 78 56 34 12");
        byte[] password = PasswordToByteConverter.convert("password");
        byte[] key = Hex.decode("d1daa78615f287e6a1c8b120d7062a493f98d203e6be49a6adf4fa574b6e64ee");
        byte[] iv = Hex.decode("df377ef2e8ad463fb711f1b4ff27139a");

        KeyWithIVParameters params = getKDFInstance(new PBKDF2Parameters(32, 5, salt)).derive(password, 16);

        assertThat(params.getKey(), equalTo(key));
        assertThat(params.getIV(), equalTo(iv));
    }

    @Test
    void pbkdf2KeyWithRandomSalt()
    {
        byte[] password = PasswordToByteConverter.convert("password");

        PBKDF2Parameters kdfParam1 = new PBKDF2Parameters(32, 5);
        KeyParameter params1 = getKDFInstance(kdfParam1).derive(password);

        PBKDF2Parameters kdfParam2 = new PBKDF2Parameters(32, 5);
        KeyParameter params2 = getKDFInstance(kdfParam2).derive(password);

        assertThat(params1.getKey(), not(equalTo(params2.getKey())));
        assertThat(params1.getKey().length, equalTo(32));
        assertThat(kdfParam1.getIterationCount(), equalTo(kdfParam2.getIterationCount()));
        assertThat(kdfParam1.getSalt(), not(equalTo(kdfParam2.getSalt())));
    }

    @Test
    void pbkdf2KeyWithRandomIterationCount()
    {
        byte[] salt = Hex.decode("12 34 56 78 78 56 34 12");
        byte[] password = PasswordToByteConverter.convert("password");

        // Make sure the two PBKDF2Parameters instance have different 'iterationCount' values as otherwise the two 
        // produced keys would be identical 
        PBKDF2Parameters kdfParam1 = new PBKDF2Parameters(24, salt);
        KeyParameter params1 = getKDFInstance(kdfParam1).derive(password);

        // Re-instantiate kdfParam2 using PBKDF2Parameters.getRandomIterationCount to compute the iterationCount. We
        // repeat the process until the iterationCount of kdfParam2 and kdfParam1 are different, as otherwise the 
        // resulting keys would be equals.
        PBKDF2Parameters kdfParam2;
        do {
            kdfParam2 = new PBKDF2Parameters(24, salt);
        } while (kdfParam2.getIterationCount() == kdfParam1.getIterationCount());

        KeyParameter params2 = getKDFInstance(kdfParam2).derive(password);

        assertThat(params1.getKey(), not(equalTo(params2.getKey())));
        assertThat(params1.getKey().length, equalTo(24));
        assertThat(kdfParam1.getSalt(), equalTo(kdfParam2.getSalt()));
        assertThat(kdfParam1.getIterationCount(), not(equalTo(kdfParam2.getIterationCount())));
    }

    @Test
    void pbkdf2KeyWithRandomSaltAndIterationCount()
    {
        byte[] password = PasswordToByteConverter.convert("password");

        PBKDF2Parameters kdfParam1 = new PBKDF2Parameters(16);
        KeyParameter params1 = getKDFInstance(kdfParam1).derive(password);

        PBKDF2Parameters kdfParam2 = new PBKDF2Parameters(16);
        KeyParameter params2 = getKDFInstance(kdfParam2).derive(password);

        assertThat(params1.getKey(), not(equalTo(params2.getKey())));
        assertThat(params1.getKey().length, equalTo(16));
        assertThat(kdfParam1.getSalt(), not(equalTo(kdfParam2.getSalt())));
        assertThat(kdfParam1.getIterationCount(), not(equalTo(kdfParam2.getIterationCount())));
    }

    @Test
    void pbkdf2KeyWithIVWithRandomSalt()
    {
        byte[] password = PasswordToByteConverter.convert("password");

        PBKDF2Parameters kdfParam1 = new PBKDF2Parameters(32, 5);
        KeyWithIVParameters params1 = getKDFInstance(kdfParam1).derive(password, 16);

        PBKDF2Parameters kdfParam2 = new PBKDF2Parameters(32, 5);
        KeyWithIVParameters params2 = getKDFInstance(kdfParam2).derive(password, 16);

        assertThat(params1.getKey(), not(equalTo(params2.getKey())));
        assertThat(params1.getKey().length, equalTo(32));
        assertThat(params1.getIV().length, equalTo(16));
        assertThat(kdfParam1.getIterationCount(), equalTo(kdfParam2.getIterationCount()));
        assertThat(kdfParam1.getSalt(), not(equalTo(kdfParam2.getSalt())));
    }

    @Test
    void pbkdf2KeyWithIVWithRandomIterationCount()
    {
        byte[] salt = Hex.decode("12 34 56 78 78 56 34 12");
        byte[] password = PasswordToByteConverter.convert("password");

        PBKDF2Parameters kdfParam1 = new PBKDF2Parameters(24, salt);
        KeyWithIVParameters params1 = getKDFInstance(kdfParam1).derive(password, 12);

        PBKDF2Parameters kdfParam2 = new PBKDF2Parameters(24, salt);
        KeyWithIVParameters params2 = getKDFInstance(kdfParam2).derive(password, 12);

        assertThat(params1.getKey(), not(equalTo(params2.getKey())));
        assertThat(params1.getKey().length, equalTo(24));
        assertThat(params1.getIV().length, equalTo(12));
        assertThat(kdfParam1.getSalt(), equalTo(kdfParam2.getSalt()));
        assertThat(kdfParam1.getIterationCount(), not(equalTo(kdfParam2.getIterationCount())));
    }

    @Test
    void pbkdf2KeyWithIVWithRandomSaltAndIterationCount()
    {
        byte[] password = PasswordToByteConverter.convert("password");

        PBKDF2Parameters kdfParam1 = new PBKDF2Parameters(16);
        KeyWithIVParameters params1 = getKDFInstance(kdfParam1).derive(password, 8);

        PBKDF2Parameters kdfParam2 = new PBKDF2Parameters(16);
        KeyWithIVParameters params2 = getKDFInstance(kdfParam2).derive(password, 8);

        assertThat(params1.getKey(), not(equalTo(params2.getKey())));
        assertThat(params1.getKey().length, equalTo(16));
        assertThat(params1.getIV().length, equalTo(8));
        assertThat(kdfParam1.getSalt(), not(equalTo(kdfParam2.getSalt())));
        assertThat(kdfParam1.getIterationCount(), not(equalTo(kdfParam2.getIterationCount())));
    }

    @Test
    void pbkdf2SerializationDeserializationTest() throws Exception
    {
        byte[] password = PasswordToByteConverter.convert("password");
        KeyDerivationFunction kdf = getKDFInstance(new PBKDF2Parameters(32, 1000));
        KeyWithIVParameters params = kdf.derive(password, 8);

        KeyDerivationFunction kdf2 = this.factory.getInstance(kdf.getEncoded());
        KeyWithIVParameters params2 = kdf2.derive(password, 8);

        assertThat(params.getKey(), equalTo(params2.getKey()));
        assertThat(params2.getIV(), equalTo(params2.getIV()));
    }
}
