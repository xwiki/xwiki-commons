package org.xwiki.crypto.pkix.params.x509certificate;


import java.io.IOException;
import org.bouncycastle.asn1.x500.X500Name;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.xwiki.crypto.pkix.params.PrincipalIndentifier;


public class AmplDistinguishedNameTest {
    @Test(timeout = 10000)
    public void testEquality() throws Exception {
        PrincipalIndentifier john = new DistinguishedName("CN=John Doe");
        PrincipalIndentifier jane = new DistinguishedName("CN=Jane Doe");
        Assert.assertEquals("CN=John Doe", ((X500Name) (((DistinguishedName) (john)).getX500Name())).toString());
        Assert.assertEquals(215056289, ((int) (((X500Name) (((DistinguishedName) (john)).getX500Name())).hashCode())));
        byte[] array_863235762 = new byte[]{48, 19, 49, 17, 48, 15, 6, 3, 85, 4, 3, 12, 8, 74, 111, 104, 110, 32, 68, 111, 101};
        	byte[] array_1837747737 = (byte[])((org.bouncycastle.asn1.x500.X500Name)((org.xwiki.crypto.pkix.params.x509certificate.DistinguishedName)john).getX500Name()).getEncoded();
        	for(int ii = 0; ii <array_863235762.length; ii++) {
        		org.junit.Assert.assertEquals(array_863235762[ii], array_1837747737[ii]);
        	};
        Assert.assertEquals(215056289, ((int) (((DistinguishedName) (john)).hashCode())));
        Assert.assertEquals("CN=John Doe", ((DistinguishedName) (john)).getName());
        byte[] array_981265085 = new byte[]{48, 19, 49, 17, 48, 15, 6, 3, 85, 4, 3, 12, 8, 74, 111, 104, 110, 32, 68, 111, 101};
        	byte[] array_216865762 = (byte[])((org.xwiki.crypto.pkix.params.x509certificate.DistinguishedName)john).getEncoded();
        	for(int ii = 0; ii <array_981265085.length; ii++) {
        		org.junit.Assert.assertEquals(array_981265085[ii], array_216865762[ii]);
        	};
        Assert.assertEquals("CN=Jane Doe", ((X500Name) (((DistinguishedName) (jane)).getX500Name())).toString());
        Assert.assertEquals(1773829196, ((int) (((X500Name) (((DistinguishedName) (jane)).getX500Name())).hashCode())));
        byte[] array_2005107434 = new byte[]{48, 19, 49, 17, 48, 15, 6, 3, 85, 4, 3, 12, 8, 74, 97, 110, 101, 32, 68, 111, 101};
        	byte[] array_1074991441 = (byte[])((org.bouncycastle.asn1.x500.X500Name)((org.xwiki.crypto.pkix.params.x509certificate.DistinguishedName)jane).getX500Name()).getEncoded();
        	for(int ii = 0; ii <array_2005107434.length; ii++) {
        		org.junit.Assert.assertEquals(array_2005107434[ii], array_1074991441[ii]);
        	};
        Assert.assertEquals(1773829196, ((int) (((DistinguishedName) (jane)).hashCode())));
        Assert.assertEquals("CN=Jane Doe", ((DistinguishedName) (jane)).getName());
        byte[] array_1692892407 = new byte[]{48, 19, 49, 17, 48, 15, 6, 3, 85, 4, 3, 12, 8, 74, 97, 110, 101, 32, 68, 111, 101};
        	byte[] array_72198292 = (byte[])((org.xwiki.crypto.pkix.params.x509certificate.DistinguishedName)jane).getEncoded();
        	for(int ii = 0; ii <array_1692892407.length; ii++) {
        		org.junit.Assert.assertEquals(array_1692892407[ii], array_72198292[ii]);
        	};
        CoreMatchers.equalTo(john);
        CoreMatchers.equalTo(new DistinguishedName(new X500Name("CN=John Doe")));
        CoreMatchers.equalTo(new PrincipalIndentifier() {
            @Override
            public byte[] getEncoded() throws IOException {
                return new X500Name("CN=John Doe").getEncoded();
            }

            @Override
            public String getName() {
                return "CN=John Doe";
            }
        });
        CoreMatchers.not(CoreMatchers.equalTo(new DistinguishedName("CN=Jane Doe")));
        CoreMatchers.not(CoreMatchers.equalTo(john));
        CoreMatchers.not(CoreMatchers.equalTo(new PrincipalIndentifier() {
            @Override
            public byte[] getEncoded() throws IOException {
                return new X500Name("CN=Jane Doe").getEncoded();
            }

            @Override
            public String getName() {
                return "CN=Jane Doe";
            }
        }));
        CoreMatchers.not(CoreMatchers.equalTo(new Object()));
        byte[] array_352628246 = new byte[]{48, 19, 49, 17, 48, 15, 6, 3, 85, 4, 3, 12, 8, 74, 111, 104, 110, 32, 68, 111, 101};
        	byte[] array_1637010670 = (byte[])((org.bouncycastle.asn1.x500.X500Name)((org.xwiki.crypto.pkix.params.x509certificate.DistinguishedName)john).getX500Name()).getEncoded();
        	for(int ii = 0; ii <array_352628246.length; ii++) {
        		org.junit.Assert.assertEquals(array_352628246[ii], array_1637010670[ii]);
        	};
        byte[] array_285837890 = new byte[]{48, 19, 49, 17, 48, 15, 6, 3, 85, 4, 3, 12, 8, 74, 111, 104, 110, 32, 68, 111, 101};
        	byte[] array_1289466678 = (byte[])((org.xwiki.crypto.pkix.params.x509certificate.DistinguishedName)john).getEncoded();
        	for(int ii = 0; ii <array_285837890.length; ii++) {
        		org.junit.Assert.assertEquals(array_285837890[ii], array_1289466678[ii]);
        	};
        byte[] array_57601349 = new byte[]{48, 19, 49, 17, 48, 15, 6, 3, 85, 4, 3, 12, 8, 74, 97, 110, 101, 32, 68, 111, 101};
        	byte[] array_679015983 = (byte[])((org.bouncycastle.asn1.x500.X500Name)((org.xwiki.crypto.pkix.params.x509certificate.DistinguishedName)jane).getX500Name()).getEncoded();
        	for(int ii = 0; ii <array_57601349.length; ii++) {
        		org.junit.Assert.assertEquals(array_57601349[ii], array_679015983[ii]);
        	};
        byte[] array_1418443332 = new byte[]{48, 19, 49, 17, 48, 15, 6, 3, 85, 4, 3, 12, 8, 74, 97, 110, 101, 32, 68, 111, 101};
        	byte[] array_586039462 = (byte[])((org.xwiki.crypto.pkix.params.x509certificate.DistinguishedName)jane).getEncoded();
        	for(int ii = 0; ii <array_1418443332.length; ii++) {
        		org.junit.Assert.assertEquals(array_1418443332[ii], array_586039462[ii]);
        	};
    }
}

