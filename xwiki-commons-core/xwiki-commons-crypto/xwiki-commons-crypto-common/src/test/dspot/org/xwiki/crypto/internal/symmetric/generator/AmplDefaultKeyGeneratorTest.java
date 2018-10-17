package org.xwiki.crypto.internal.symmetric.generator;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.xwiki.crypto.KeyGenerator;
import org.xwiki.crypto.internal.DefaultSecureRandomProvider;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.mockito.MockitoComponentMockingRule;


@ComponentList({ DefaultSecureRandomProvider.class })
public class AmplDefaultKeyGeneratorTest {
    @Rule
    public final MockitoComponentMockingRule<KeyGenerator> mocker = new MockitoComponentMockingRule<KeyGenerator>(DefaultKeyGenerator.class);

    private KeyGenerator generator;

    @Before
    public void configure() throws Exception {
        generator = mocker.getComponentUnderTest();
    }

    @Test(timeout = 10000)
    public void testGenerateWithoutArgument_failAssert0() throws Exception {
        try {
            generator.generate();
            org.junit.Assert.fail("testGenerateWithoutArgument should have thrown UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            Assert.assertEquals("Knowing the key strength is required to generate a key.", expected.getMessage());
        }
    }
}

