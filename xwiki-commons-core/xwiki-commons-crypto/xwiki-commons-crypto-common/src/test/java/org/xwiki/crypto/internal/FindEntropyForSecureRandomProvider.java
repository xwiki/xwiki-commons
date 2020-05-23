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
package org.xwiki.crypto.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

import javax.inject.Provider;

import org.bouncycastle.crypto.prng.FixedSecureRandom;
import org.bouncycastle.util.encoders.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.crypto.KeyPairGenerator;
import org.xwiki.crypto.KeyParametersGenerator;
import org.xwiki.crypto.internal.asymmetric.generator.BcDSAKeyPairGenerator;
import org.xwiki.crypto.internal.asymmetric.generator.BcDSAKeyParameterGenerator;
import org.xwiki.crypto.internal.asymmetric.generator.BcRSAKeyPairGenerator;
import org.xwiki.crypto.internal.asymmetric.keyfactory.BcDSAKeyFactory;
import org.xwiki.crypto.internal.asymmetric.keyfactory.BcRSAKeyFactory;
import org.xwiki.crypto.internal.digest.factory.BcSHA1DigestFactory;
import org.xwiki.crypto.internal.digest.factory.BcSHA224DigestFactory;
import org.xwiki.crypto.params.generator.asymmetric.DSAKeyParametersGenerationParameters;
import org.xwiki.crypto.params.generator.asymmetric.RSAKeyGenerationParameters;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;
import org.xwiki.test.junit5.mockito.InjectMockComponents;
import org.xwiki.test.mockito.MockitoComponentManager;

/**
 * THIS IS NOT A TEST, BUT A TOOL.
 *
 * This test is a tool for determining a good entropy source of reasonable size to feed the
 * {@link FixedSecureRandom} generator for the RSA and DSA generator tests.
 *
 * To execute, run the following line on a machine with the best possible random generator:
 *
 *     mvn -Dtest=FindEntropyForSecureRandomProvider -Dxwiki.surefire.captureconsole.skip test
 *
 * Depending on the quality of your entropy, you may need to run this tools several time (not in a raw)
 * before getting a truly reasonable sized result.
 *
 * PS: This Class name does not end with Test, so the maven surefire plugin will not run it during normal test run.
 * However, your IDE may do, you should be careful. Putting @Ignore is not an option since maven will not
 * allow running it from the command-line.
 */
// @formatter:off
@ComponentTest
@ComponentList({
    BcRSAKeyFactory.class,
    FixedSecureRandomProvider.class,
    BcDSAKeyParameterGenerator.class,
    BcDSAKeyPairGenerator.class,
    BcDSAKeyFactory.class,
    BcSHA1DigestFactory.class,
    BcSHA224DigestFactory.class
})
// @formatter:on
class FindEntropyForSecureRandomProvider
{
    @InjectMockComponents
    private BcRSAKeyPairGenerator rsaGenerator;

    @InjectComponentManager
    private MockitoComponentManager componentManager;

    private KeyPairGenerator dsaGenerator;
    private KeyParametersGenerator dsaParameterGenerator;

    @BeforeEach
    void configure() throws Exception
    {
        dsaGenerator = this.componentManager.getInstance(KeyPairGenerator.class, "DSA");;
        dsaParameterGenerator = this.componentManager.getInstance(KeyParametersGenerator.class, "DSA");
    }

    @Test
    void findGoodEntropySource() throws Exception
    {
        FixedSecureRandomProvider rndprov =
            this.componentManager.getInstance(new DefaultParameterizedType(null, Provider.class, SecureRandom.class));

        RecordingSecureRandom rnd = new RecordingSecureRandom();

        boolean succeed = false;
        byte[] rndSource = null;
        long maxsize = 4096;
        while(!succeed) {
            rndprov.setRandomSource(rnd);
            rsaGenerator.generate();
            rsaGenerator.generate();
            while (rnd.counter > maxsize) {
                int rejectCount = 0;
                while (rejectCount < 10 && rnd.counter > maxsize) {
                    rejectCount++;
                    System.out.println(String.format("Rejected %d > %d", rnd.counter, maxsize));
                    rnd = new RecordingSecureRandom();
                    rndprov.setRandomSource(rnd);
                    rsaGenerator.generate();
                    rsaGenerator.generate();
                }
                if (rejectCount >= 10) {
                    maxsize <<= 1;
                }
            }
            rndSource = rnd.baos.toByteArray();

            System.out.print(String.format("Testing %d candidate", rndSource.length));
            succeed = true;
            try {
                System.out.print(" - RSAstrength");
                rndprov.setRandomSource(new FixedSecureRandom(rndSource));
                rsaGenerator.generate(new RSAKeyGenerationParameters(64));
                rsaGenerator.generate(new RSAKeyGenerationParameters(128));

                System.out.print(" - RSAexp");
                rndprov.setRandomSource(new FixedSecureRandom(rndSource));
                rsaGenerator.generate(new RSAKeyGenerationParameters(64, BigInteger.valueOf(0x11)));

                System.out.print(" - RSAcertainty");
                rndprov.setRandomSource(new FixedSecureRandom(rndSource));
                rsaGenerator.generate(new RSAKeyGenerationParameters(64, 1));

                System.out.print(" - DSA");
                rndprov.setRandomSource(new FixedSecureRandom(rndSource));
                dsaGenerator.generate();
                dsaGenerator.generate();

                System.out.print(" - FIPS186_2");
                rndprov.setRandomSource(new FixedSecureRandom(rndSource));
                dsaGenerator.generate(dsaParameterGenerator.generate());

                System.out.print(" - FIPS186_3");
                rndprov.setRandomSource(new FixedSecureRandom(rndSource));
                dsaGenerator.generate(dsaParameterGenerator.generate(new DSAKeyParametersGenerationParameters(256, 28, 1)));
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println(" - Failed");
                succeed = false;
            }
        }
        System.out.println(" - Succeed");

        OutputStream os = new LineWrapperOutputStream(System.out, 100);
        os.write(Base64.encode(rndSource));
        os.write("\n".getBytes());
    }

    public static class RecordingSecureRandom extends SecureRandom
    {
        private static final long serialVersionUID = 1L;

        public int counter = 0;

        public ByteArrayOutputStream baos = new ByteArrayOutputStream();

        protected void internalNextByte(byte[] bytes) {
            super.nextBytes(bytes);
        }

        @Override
        public synchronized void nextBytes(byte[] bytes)
        {
            counter += bytes.length;
            internalNextByte(bytes);
            try {
                baos.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        protected byte[] internalGeneratedSeed(int i) {
            return super.generateSeed(i);
        }

        @Override
        public byte[] generateSeed(int i)
        {
            counter += i;
            byte[] b = internalGeneratedSeed(i);
            try {
                baos.write(b);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return b;
        }
    }
}
