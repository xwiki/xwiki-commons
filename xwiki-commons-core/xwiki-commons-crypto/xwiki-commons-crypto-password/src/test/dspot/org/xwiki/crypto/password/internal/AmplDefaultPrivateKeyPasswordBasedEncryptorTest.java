package org.xwiki.crypto.password.internal;


import org.bouncycastle.util.encoders.Base64;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.crypto.AsymmetricKeyFactory;
import org.xwiki.crypto.cipher.internal.symmetric.factory.BcAesCbcPaddedCipherFactory;
import org.xwiki.crypto.cipher.internal.symmetric.factory.BcBlowfishCbcPaddedCipherFactory;
import org.xwiki.crypto.cipher.internal.symmetric.factory.BcDesCbcPaddedCipherFactory;
import org.xwiki.crypto.cipher.internal.symmetric.factory.BcDesEdeCbcPaddedCipherFactory;
import org.xwiki.crypto.cipher.internal.symmetric.factory.BcRc2CbcPaddedCipherFactory;
import org.xwiki.crypto.internal.DefaultSecureRandomProvider;
import org.xwiki.crypto.internal.asymmetric.keyfactory.BcRSAKeyFactory;
import org.xwiki.crypto.internal.asymmetric.keyfactory.DefaultKeyFactory;
import org.xwiki.crypto.params.cipher.asymmetric.PrivateKeyParameters;
import org.xwiki.crypto.password.PasswordToByteConverter;
import org.xwiki.crypto.password.PrivateKeyPasswordBasedEncryptor;
import org.xwiki.crypto.password.internal.kdf.factory.BcPKCS5S2KeyDerivationFunctionFactory;
import org.xwiki.crypto.password.internal.kdf.factory.BcScryptKeyDerivationFunctionFactory;
import org.xwiki.crypto.password.internal.kdf.factory.DefaultKeyDerivationFunctionFactory;
import org.xwiki.crypto.password.internal.pbe.factory.BcPBES2AesCipherFactory;
import org.xwiki.crypto.password.internal.pbe.factory.BcPBES2BlowfishCipherFactory;
import org.xwiki.crypto.password.internal.pbe.factory.BcPBES2CipherFactory;
import org.xwiki.crypto.password.internal.pbe.factory.BcPBES2DesCipherFactory;
import org.xwiki.crypto.password.internal.pbe.factory.BcPBES2DesEdeCipherFactory;
import org.xwiki.crypto.password.internal.pbe.factory.BcPBES2Rc2CipherFactory;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;


@ComponentList({ DefaultKeyDerivationFunctionFactory.class, BcPKCS5S2KeyDerivationFunctionFactory.class, BcPBES2Rc2CipherFactory.class, BcRc2CbcPaddedCipherFactory.class, BcPBES2DesCipherFactory.class, BcDesCbcPaddedCipherFactory.class, BcPBES2DesEdeCipherFactory.class, BcDesEdeCbcPaddedCipherFactory.class, BcPBES2BlowfishCipherFactory.class, BcBlowfishCbcPaddedCipherFactory.class, BcPBES2AesCipherFactory.class, BcAesCbcPaddedCipherFactory.class, BcScryptKeyDerivationFunctionFactory.class, BcPBES2CipherFactory.class, BcRSAKeyFactory.class, DefaultKeyFactory.class, DefaultSecureRandomProvider.class })
public class AmplDefaultPrivateKeyPasswordBasedEncryptorTest {
    private static final byte[] PASSWORD = PasswordToByteConverter.convert("changeit");

    private static final byte[] RSAKEY = Base64.decode(("MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDIY6+Wgj6MqdEd" + (((((((((((((((((((((((("Yq6FgH5xMgTBmFqAonR/eshjxY2C6MHs+WmCmNSDik2NgZWIaODvOF9uOEK2U0Zf" + "JEG2LcZxoeIEgg/mfII2f4DLy1JYajm/llzwFBzAd/Rkcs3qwP2ba5VKn/pSqNLl") + "nKHMXkXO+9SjfHDx95x2dK1dB8eGQGculOMcTm3uK7UlWNO4TSlwG9qHZ1aoM3GI") + "g5C1fIpbxJqDVjFq6fFAapE3KRIWIQmKd3E5ICcDErqr/AapxnfO8UFNxVWSOLW7") + "ZAfis4w/c8/EAgyQHw42R0dNyjUOZsToF8McCsOpRjGolSU8aUyqspvd8IWJPd5d") + "6HBHueXNAgMBAAECggEAV3q9MpVVPQ79TTjBO2Km0D+nt+QMzk8dUHGHfZbGejmm") + "Pw96shqJ24rK5FWHs+8lEwmnD3TcGsAr3mjzjtZY5U5oXtNwoYwFRElRLqZqIlLt") + "NugrVltRWeyD8j30CuGJVQoYOGWyX9d3ielg8NjO3NcvMtembttLoKK68/vrbH11") + "9W7wr5p8/xyMfyl9curnmCFk5QqJ1FBpjPWY05NDIBCUJB0tGAqViCpxEeWPSlvb") + "xcElqWfdbtnsYUxYU+iOTHHotoKnz4nLHYK2/njMhlCEyMXfu1DJOd8rg5yXewJF") + "v6NhXgWStSexAT1bZ17LROazVcHfWB9QmXF1Fm7vOQKBgQD+dZxPDOi3Y4gCFegn") + "Z+epNyl2aPTkseEZxrIqPKLHsGxUfYjQqkX2RdfTrq2vf4vFlN6uCXhSlZKXfLH/") + "iQ8FAzqenhVVHK2fv5xB0SE5zNmcHDrHshl+/zUNI2u5AMFECVO2SVbgoFjvgkou") + "FolK8XUXfHfb4f732LUyYI0lEwKBgQDJmkWHhzekz3P5iWaAt1SH8bZpt2hqa6Bx") + "A4VvMdtmjCxEDETN0Rb3CPYxw3qa3xGfW1y1j/49xi4gr69yaT2Tbca7PFGUmWRo") + "OJwfCUB5uBUi6UVytK19OVKReOm4666x8P3YO4cxxSI/HeoSU0HR1kkX9rGmrsGN") + "MgUQ15+FnwKBgAKf6/DUzUG3ARwkZbSiWb1hGEhkZMJHI29EoWnWHke5BiUI9nRQ") + "jVAxADzqvFfnFOYA1xssddVEPbLaUmu0WjdPBTfFoaqzFQdkzpPPOGyENGpr0B9n") + "MuQgdceg6eeKnnO5NOfYcdD3VnOCAInhKaFgRDjty7604hBkZ9oRLOOJAoGBAIJ+") + "dmUMlGr80XADjTLh+DhqsA1r542DDv44LkXUetS9BOYjHuIuZnQO+/UoOBNJMsn4") + "xGDNzN7FihQkRCeFkZL9arbFi3TpeUGw6vV38qEXE69eWVKvOuEkmpqJLphBDfom") + "KNmvZopDtTAvt9SWybL+xp9ZUpK26ZfwebD2MU63AoGBAOa2Kt01DxoqiWiQP/ds") + "Mc9wOw1zJsSmIK7wEiW3FkCz8uw3UgjF9ymYY/YAW3eZtuUpuxEzyENb9f21p4b2") + "zYoZ7nCUo4TmVXxgCjiEWglg3b/R3xjQr1dAABhTeI8bXMv5r/tMUsnS79uKqwGD") + "2Gc1syc3+055K4qcfZHH0XWu")));

    @Rule
    public final MockitoComponentMockingRule<PrivateKeyPasswordBasedEncryptor> mocker =
        new MockitoComponentMockingRule<>(DefaultPrivateKeyPasswordBasedEncryptor.class);

    PrivateKeyPasswordBasedEncryptor encryptor;

    PrivateKeyParameters keyParameters;

    @Before
    public void configure() throws Exception {
        this.encryptor = this.mocker.getComponentUnderTest();
        this.keyParameters = ((AsymmetricKeyFactory) (this.mocker.getInstance(AsymmetricKeyFactory.class))).fromPKCS8(RSAKEY);
    }

    @Test(timeout = 10000)
    public void testEncryptDecryptDefault() throws Exception {
        byte[] encoded =
            this.encryptor.decrypt(PASSWORD, this.encryptor.encrypt(PASSWORD, this.keyParameters)).getEncoded();
        MatcherAssert.assertThat(encoded, CoreMatchers.equalTo(RSAKEY));
    }
}
