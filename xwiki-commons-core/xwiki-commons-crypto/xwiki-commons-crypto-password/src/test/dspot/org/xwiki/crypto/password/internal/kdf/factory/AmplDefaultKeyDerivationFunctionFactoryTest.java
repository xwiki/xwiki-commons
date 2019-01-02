package org.xwiki.crypto.password.internal.kdf.factory;


import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.crypto.params.cipher.symmetric.KeyWithIVParameters;
import org.xwiki.crypto.password.KeyDerivationFunction;
import org.xwiki.crypto.password.KeyDerivationFunctionFactory;
import org.xwiki.crypto.password.PasswordToByteConverter;
import org.xwiki.crypto.password.params.KeyDerivationFunctionParameters;
import org.xwiki.crypto.password.params.PBKDF2Parameters;
import org.xwiki.crypto.password.params.ScryptParameters;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;


@ComponentList({ BcScryptKeyDerivationFunctionFactory.class, BcPKCS5S2KeyDerivationFunctionFactory.class })
public class AmplDefaultKeyDerivationFunctionFactoryTest {
    @Rule
    public final MockitoComponentMockingRule<KeyDerivationFunctionFactory> mocker = new MockitoComponentMockingRule<KeyDerivationFunctionFactory>(DefaultKeyDerivationFunctionFactory.class);

    KeyDerivationFunctionFactory factory;

    @Before
    public void configure() throws Exception {
        factory = mocker.getComponentUnderTest();
    }

    KeyDerivationFunction getKDFInstance(KeyDerivationFunctionParameters parameters) {
        return factory.getInstance(parameters);
    }

    @Test
    public void Pbkdf2DecodingTest() throws Exception {
        byte[] password = PasswordToByteConverter.convert("password");
        byte[] encoded = Base64.decode("MCYGCSqGSIb3DQEFDDAZBBAcO15L0rQ01O2RJ5UhyqCdAgID6AIBIA==");
        KeyDerivationFunction kdf = getKDFInstance(new PBKDF2Parameters(32, 1000, Hex.decode("1c3b5e4bd2b434d4ed91279521caa09d")));
        KeyWithIVParameters params = kdf.derive(password, 8);
        KeyDerivationFunction kdf2 = factory.getInstance(encoded);
        KeyWithIVParameters params2 = kdf2.derive(password, 8);
        Assert.assertThat(kdf.getEncoded(), CoreMatchers.equalTo(encoded));
        Assert.assertThat(params.getKey(), CoreMatchers.equalTo(params2.getKey()));
        Assert.assertThat(params2.getIV(), CoreMatchers.equalTo(params2.getIV()));
    }

    @Test(timeout = 10000)
    public void ScryptDecodingTest() throws Exception {
        byte[] password = PasswordToByteConverter.convert("password");
        byte[] encoded = Base64.decode("MCwGCSsGAQQB2kcECzAfBBAcO15L0rQ01O2RJ5UhyqCdAgICAAIBEAIBAgIBIA==");
        KeyDerivationFunction kdf = getKDFInstance(new ScryptParameters(32, 512, 2, 16, Hex.decode("1c3b5e4bd2b434d4ed91279521caa09d")));
        KeyWithIVParameters params2 = factory.getInstance(encoded).derive(password, 8);
        kdf.getEncoded();
        Matcher<byte[]> o_ScryptDecodingTest__16 = CoreMatchers.equalTo(encoded);
        Assert.assertEquals("[<48>, <44>, <6>, <9>, <43>, <6>, <1>, <4>, <1>, <-38>, <71>, <4>, <11>, <48>, <31>, <4>, <16>, <28>, <59>, <94>, <75>, <-46>, <-76>, <52>, <-44>, <-19>, <-111>, <39>, <-107>, <33>, <-54>, <-96>, <-99>, <2>, <2>, <2>, <0>, <2>, <1>, <16>, <2>, <1>, <2>, <2>, <1>, <32>]", ((IsEqual) (o_ScryptDecodingTest__16)).toString());
        kdf.derive(password, 8).getKey();
        Matcher<byte[]> o_ScryptDecodingTest__18 = CoreMatchers.equalTo(params2.getKey());
        Assert.assertEquals("[<103>, <-10>, <11>, <9>, <-6>, <107>, <-51>, <70>, <-94>, <45>, <-116>, <127>, <17>, <-8>, <80>, <-70>, <-96>, <24>, <-78>, <-43>, <-31>, <-96>, <25>, <-5>, <67>, <-55>, <7>, <100>, <27>, <-84>, <-109>, <66>]", ((IsEqual) (o_ScryptDecodingTest__18)).toString());
        params2.getIV();
        Matcher<byte[]> o_ScryptDecodingTest__21 = CoreMatchers.equalTo(params2.getIV());
        Assert.assertEquals("[<64>, <-58>, <70>, <49>, <112>, <-37>, <25>, <86>]", ((IsEqual) (o_ScryptDecodingTest__21)).toString());
    }
}

