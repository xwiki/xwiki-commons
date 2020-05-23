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
package org.xwiki.crypto;

public abstract class AbstractPKIXTest
{
    protected static final String RSA_PRIVATE_KEY =
        // Link to decoded ASN.1: https://goo.gl/kgV0IB
        "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDCmjim/3likJ4"
        + "VF564UyygqPjIX/z090AImLl0fDLUkIyCVTSd18wJ3axr1qjLtSgNPWet0puSxO"
        + "FH0AzFKRCJOjUkQRU8iAkz64MLAf9xrx4nBECciqeB941s01kLtG8C/UqC3O9Sw"
        + "HSdhtUpUU8V/91SiD09yNJsnODi3WqM3oLg1QYzKhoaD2mVo2xJLQ/QXqr2XIc5"
        + "i2Mlpfq6S5JNbFD/I+UFhBUlBNuDOEV7ttIt2eFMEUsfkCestGo0YoQYOpTLPcP"
        + "GRS7MnSY1CLWGUYqaMSnes0nS8ke2PPD4Q0suAZz4msnhNufanscstM8tcNtsZF"
        + "6hj0JvbZok89szAgMBAAECggEBAKWJ1SlR5ysORDtDBXRc5HiiZEbnSGIFtYXaj"
        + "N/nCsJBWBVCb+jZeirmU9bEGoB20OQ6WOjHYCnAqraQ51wMK5HgXvZBGtSMD/AH"
        + "pkiF4YsOYULlXiUL2aQ4NijdvEC1sz1Cw9CAKmElb83UtZ1ZGkJnjhi35giZvU5"
        + "BQRgbK5k57DFY66yv9VDg8tuD/enI9sRsCUZfCImuShGv4nLqhPMPg+1UxDPGet"
        + "Vs8uEaJQ017E14wLKLA0DlED13icelU1A7ufkEdeBSv/yZ7ENjervzPwa9nITK/"
        + "19uzqaHOcYZxmDQn6UHTnaLpIEaUvpp/pbed5S97ETSsqUBC8fqEUECgYEA/Sba"
        + "o6efydhlXDHbXtyvaJWao19sbI9OfxGC6dR2fZiBx8Do9kVDDbMtb1PYEfLhYbi"
        + "urmKGbUtcLSFgxNbZifUmG54M92nBsnsetMCqvMVNzYl2Je83V+NrIsLJjFIZ2C"
        + "BvZa/FKOLDTwSe35fNqaS0ExdwcGNMIT//bDQCmyECgYEAxMq6rN+HpBRuhvvst"
        + "V99zV+lI/1DzZuXExd+c3PSchiqkJrTLaQDvcaHQir9hK7RqF9vO7tvdluJjgX+"
        + "f/CMPNQuC5k6vY/0fS4V2NQWtln9BBSzHtocTnZzFNq8tAZqyEhZUHIbkncroXv"
        + "eUXqtlfOnKB2aYI/+3gPEMYJlH9MCgYA4exjA9r9B65QB0+Xb7mT8cpSD6uBoAD"
        + "lFRITu4sZlE0exZ6sSdzWUsutqMUy+BHCguvHOWpEfhXbVYuMSR9VVYGrWMpc2B"
        + "FSBG9MoBOyTHXpUZ10C7bJtW4IlyUvqkM7PV71C9MqKar2kvaUswdPTC7pZoBso"
        + "GB9+M6crXxdNwQKBgDUVMlGbYi1CTaYfonQyM+8IE7WnhXiatZ+ywKtH3MZmHOw"
        + "wtzIigdfZC3cvvX7i4S73vztvjdtxSaODvmiobEukOF9sj8m+YQa7Pa1lWFML5x"
        + "IIu2BhGS2ZCeXgMvKkoH0x9tWaUhGqD5zZmtiDrPs75CUQBypw7SDaBzwLnld9A"
        + "oGBAPgUh90PvUzbVVkzpVCPI82cmOIVMI1rDE6uCeNzIlN6Xu80RimCSaaDsESi"
        + "tBtoVWLRWWmuCINyqr6e9AdyvbvT6mQCjbn9+y7t6ZAhLaya5ZMUVEBLyLLqMzr"
        + "y oi/huj7m4nV4kPZz9LKxDRu3r6o0Pah+daDsTxEYObtsKa7e";

    protected static final String RSA_PUBLIC_KEY =
        // Link to decoded ASN.1: https://goo.gl/2YsSco
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwpo4pv95YpCeFReeuFM"
        + "soKj4yF/89PdACJi5dHwy1JCMglU0ndfMCd2sa9aoy7UoDT1nrdKbksThR9AMxS"
        + "kQiTo1JEEVPIgJM+uDCwH/ca8eJwRAnIqngfeNbNNZC7RvAv1KgtzvUsB0nYbVK"
        + "VFPFf/dUog9PcjSbJzg4t1qjN6C4NUGMyoaGg9plaNsSS0P0F6q9lyHOYtjJaX6"
        + "ukuSTWxQ/yPlBYQVJQTbgzhFe7bSLdnhTBFLH5AnrLRqNGKEGDqUyz3DxkUuzJ0"
        + "mNQi1hlGKmjEp3rNJ0vJHtjzw+ENLLgGc+JrJ4Tbn2p7HLLTPLXDbbGReoY9Cb2"
        + "2aJPPbMwIDAQAB";

    protected static final String INTERCA_DSA_PRIVATE_KEY =
        // Link to decoded ASN.1: https://goo.gl/Bn03lH
        "MIIBSwIBADCCASwGByqGSM44BAEwggEfAoGBALjHlfmpKj8BiEfekiLTnbYdZlo5"
        + "Hz6E2dAjx+ryqv3jeGYbPTxh+pxrD0MIIUKF+3o8Y+TBwBpbKnnZ/G2T/P6QXs8+"
        + "l7H7Q4CUJKShdQ+PhpK8JXYaICN4VAtKsP4PVhBMWLw/3VANh67JDwZz1Oa5soci"
        + "3dAVQDWN8mc4PdbhAhUAoWrfRj14AUQT759T/Men1dQ9o0ECgYEAgWPlEWkpvgfk"
        + "CvyBMRiRWchS0suOUUL5RyqYKmVFpDE2aKRMMFO5owlluJ1lm57f4zaddY8zAsT7"
        + "2tv0tTxz7nFAAPoX4QPOcSxYYapvEGRZklJRU4qrrXOPlXTia6jsWlgjnMaJ43zC"
        + "BXteK2AdZ2DF7Yr9UPRuNukIzSYc4pcEFgIULVbclkmz+d+shls7gXvWJD6Z1Pc=";

    protected static final String INTERCA_DSA_PUBLIC_KEY =
        // Link to decoded ASN.1: https://goo.gl/kjEIyJ
        "MIIBtzCCASwGByqGSM44BAEwggEfAoGBALjHlfmpKj8BiEfekiLTnbYdZlo5Hz6E"
        + "2dAjx+ryqv3jeGYbPTxh+pxrD0MIIUKF+3o8Y+TBwBpbKnnZ/G2T/P6QXs8+l7H7"
        + "Q4CUJKShdQ+PhpK8JXYaICN4VAtKsP4PVhBMWLw/3VANh67JDwZz1Oa5soci3dAV"
        + "QDWN8mc4PdbhAhUAoWrfRj14AUQT759T/Men1dQ9o0ECgYEAgWPlEWkpvgfkCvyB"
        + "MRiRWchS0suOUUL5RyqYKmVFpDE2aKRMMFO5owlluJ1lm57f4zaddY8zAsT72tv0"
        + "tTxz7nFAAPoX4QPOcSxYYapvEGRZklJRU4qrrXOPlXTia6jsWlgjnMaJ43zCBXte"
        + "K2AdZ2DF7Yr9UPRuNukIzSYc4pcDgYQAAoGAEH/cX4auYYjapwPvipulmUPLPB9G"
        + "TPcZfcefLYH4FlAs/W1/vfer1kGZL/+urSu+5D/FonOGNE9VRnLhVO4SyOremfJT"
        + "O0ZLA7w5ciQwcQRxwXX3vvYzxtiFA2H7G7SHVcg8GDzyikHePQnyDwjgXf2C8dxc"
        + "yasUA5FJb62YKo0=";

    protected static final String DSA_PRIVATE_KEY =
        // Link to decoded ASN.1: https://goo.gl/n6abEQ
        "MIIBTAIBADCCASwGByqGSM44BAEwggEfAoGBANQ9Oa1j9sWAhdXNyqz8HL/bA/e"
        + "d2VrBw6TPkgMyV1Upix58RSjOHMQNrgemSGkb80dRcLqVDYbI3ObnIJh83Zx6ze"
        + "aTpvUohGLyTa0F7UY15LkbJpyz8WFJaVykH85nz3Zo6Md9Z4X95yvF1+h9qYuak"
        + "jWcHW31+pN4u3cJNg5FAhUAj986cVG9NgWgWzFVSLbB9pEPbFUCgYEAmQrZFH3M"
        + "X5CLX/5vDvxyTeeRPZHLWc0ik3GwrIJExuVrOkuFInpx0aVbuJTxrEnY2fuc/+B"
        + "yj/F56DDO31+qPu7ZxbSvvD33OOk8eFEfn+Hia3QmA+dGrhUqoMpfDf4/GBgJhn"
        + "yQtFzMddHmYB0QnS9yX1n6DOWj/CSX0PvrlMYEFwIVAIO1GUQjAddL4btiFQnhe"
        + "N4fxBTa";

    protected static final String DSA_PUBLIC_KEY =
        // Link to decoded ASN.1: https://goo.gl/0fLEBU
        "MIIBtzCCASwGByqGSM44BAEwggEfAoGBANQ9Oa1j9sWAhdXNyqz8HL/bA/ed2VrB"
        + "w6TPkgMyV1Upix58RSjOHMQNrgemSGkb80dRcLqVDYbI3ObnIJh83Zx6zeaTpvUo"
        + "hGLyTa0F7UY15LkbJpyz8WFJaVykH85nz3Zo6Md9Z4X95yvF1+h9qYuakjWcHW31"
        + "+pN4u3cJNg5FAhUAj986cVG9NgWgWzFVSLbB9pEPbFUCgYEAmQrZFH3MX5CLX/5v"
        + "DvxyTeeRPZHLWc0ik3GwrIJExuVrOkuFInpx0aVbuJTxrEnY2fuc/+Byj/F56DDO"
        + "31+qPu7ZxbSvvD33OOk8eFEfn+Hia3QmA+dGrhUqoMpfDf4/GBgJhnyQtFzMddHm"
        + "YB0QnS9yX1n6DOWj/CSX0PvrlMYDgYQAAoGAJvnuTm8oI/RRI2tiZHtPkvSQaA3F"
        + "P4PRsVx6z1oIGg9OAxrtSS/aiQa+HWFg7fjHlMJ30Vh0yqt7igj70jaLGyDvr3MP"
        + "DyiO++72IiGUluc6yHg6m9cQ53eeJt9i44LJfTOw1S3YMU1ST7alokSnJRTICp5W"
        + "By0m1scwheuTo0E=";

    protected static final String V1_CA_CERT =
        // Generated from BcX509CertificateGeneratorFactoryTest#testGenerateEndEntitySignedCertificateVersion1()
        // caCertificate with adapted validity
        // Link to decoded ASN.1: https://goo.gl/ztT2OZ
        "MIICqDCCAZACEQDrYWVzWMEc+z95IS0t2lvrMA0GCSqGSIb3DQEBBQUAMBIxEDAO"
        + "BgNVBAMMB1Rlc3QgQ0EwHhcNMTUwOTEwMTAwMDAwWhcNNDkxMjMxMTEwMDAwWjAS"
        + "MRAwDgYDVQQDDAdUZXN0IENBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKC"
        + "AQEAwpo4pv95YpCeFReeuFMsoKj4yF/89PdACJi5dHwy1JCMglU0ndfMCd2sa9ao"
        + "y7UoDT1nrdKbksThR9AMxSkQiTo1JEEVPIgJM+uDCwH/ca8eJwRAnIqngfeNbNNZ"
        + "C7RvAv1KgtzvUsB0nYbVKVFPFf/dUog9PcjSbJzg4t1qjN6C4NUGMyoaGg9plaNs"
        + "SS0P0F6q9lyHOYtjJaX6ukuSTWxQ/yPlBYQVJQTbgzhFe7bSLdnhTBFLH5AnrLRq"
        + "NGKEGDqUyz3DxkUuzJ0mNQi1hlGKmjEp3rNJ0vJHtjzw+ENLLgGc+JrJ4Tbn2p7H"
        + "LLTPLXDbbGReoY9Cb22aJPPbMwIDAQABMA0GCSqGSIb3DQEBBQUAA4IBAQCE21dm"
        + "cllKcNs9weq25l+l0ldbJ17NXWSaEmtzFgAW26/m19DBpP5+52Y6KirhIs1dTJPU"
        + "kHneNSVoo5gNr7MbOMngwhNaND2PrqtP0UJanCyM+Egknd7tooPRXcv2XJYd880k"
        + "EcRzOjzN4h1aW0noN/R6KEz0FQZU+d1BXT/H6VAKyfYkdSy40BrYdj4rvCaMFZVt"
        + "QPhfGqNeyWQCmTAVlVsxu31ydMvIoaE4Cle5xXwN7tKTyLWkfd/tbA2DKUgjMuG0"
        + "b+zjuNs72VAFZGAr7tzbbAexM9MeTsHjl/lj2U2ljPPQojlb0XxnMNL1G/cArhaE"
        + "YfyQlA4q7tvUehy8";

    protected static final String V1_CERT =
        // Generated from BcX509CertificateGeneratorFactoryTest#testGenerateEndEntitySignedCertificateVersion1()
        // certificate with adapted validity
        // Link to decoded ASN.1: https://goo.gl/bvp09M
        "MIIDRDCCAiwCEAjkgt3VsI1SyG7IVMtG2RkwDQYJKoZIhvcNAQEFBQAwEjEQMA4G"
        + "A1UEAwwHVGVzdCBDQTAeFw0xNTA5MTAxMDAwMDBaFw00OTEyMzExMTAwMDBaMBox"
        + "GDAWBgNVBAMMD1Rlc3QgRW5kIEVudGl0eTCCAbcwggEsBgcqhkjOOAQBMIIBHwKB"
        + "gQDUPTmtY/bFgIXVzcqs/By/2wP3ndlawcOkz5IDMldVKYsefEUozhzEDa4Hpkhp"
        + "G/NHUXC6lQ2GyNzm5yCYfN2ces3mk6b1KIRi8k2tBe1GNeS5Gyacs/FhSWlcpB/O"
        + "Z892aOjHfWeF/ecrxdfofamLmpI1nB1t9fqTeLt3CTYORQIVAI/fOnFRvTYFoFsx"
        + "VUi2wfaRD2xVAoGBAJkK2RR9zF+Qi1/+bw78ck3nkT2Ry1nNIpNxsKyCRMblazpL"
        + "hSJ6cdGlW7iU8axJ2Nn7nP/gco/xeegwzt9fqj7u2cW0r7w99zjpPHhRH5/h4mt0"
        + "JgPnRq4VKqDKXw3+PxgYCYZ8kLRczHXR5mAdEJ0vcl9Z+gzlo/wkl9D765TGA4GE"
        + "AAKBgCb57k5vKCP0USNrYmR7T5L0kGgNxT+D0bFces9aCBoPTgMa7Ukv2okGvh1h"
        + "YO34x5TCd9FYdMqre4oI+9I2ixsg769zDw8ojvvu9iIhlJbnOsh4OpvXEOd3nibf"
        + "YuOCyX0zsNUt2DFNUk+2paJEpyUUyAqeVgctJtbHMIXrk6NBMA0GCSqGSIb3DQEB"
        + "BQUAA4IBAQCtDu4e2a2rHfACv53OrZsYK8GAbJfkVvj7hGcoCauJvuXHoymJuYDA"
        + "9zUzbQAPEWmsY1OYrKlT2MwndCT6Ikr/LU8Gso3R2V4Kngg74Iy9b9BziqXPp794"
        + "9oJEUb+0mHEdmsEEEtxPxUJjpEWiiGrk49GQt3jOL3wee6eEMJlXpRKODpll3rQa"
        + "ZJwWrqnu5uyTr37yajO/T/QXDu7oQrIGMwucWVWhAm5E/FOTsTiA354jTd2F46/L"
        + "RSZiWaqt8qGPhJOgFbZ880y5mps7HsT/bebctMVim1jQuc8qTxUE0Lba32MLu3Qv"
        + "4fs0zr/FQpWY04M5Tvb5zVNzaEmLoaH1";


    protected static final String V3_CA_CERT =
        // Generated from BcX509CertificateGeneratorFactoryTest#testGenerateIntermediateCertificateVersion3()
        // caCertificate with adapted validity
        // Link to decoded ASN.1: https://goo.gl/XP56Ct
        "MIIDEDCCAfigAwIBAgIPXnmirmTeiBhYy1/i/twrMA0GCSqGSIb3DQEBBQUAMBIx"
        + "EDAOBgNVBAMMB1Rlc3QgQ0EwHhcNMTUwOTEwMTAwMDAwWhcNNDkxMjMxMTEwMDAw"
        + "WjASMRAwDgYDVQQDDAdUZXN0IENBMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB"
        + "CgKCAQEAwpo4pv95YpCeFReeuFMsoKj4yF/89PdACJi5dHwy1JCMglU0ndfMCd2s"
        + "a9aoy7UoDT1nrdKbksThR9AMxSkQiTo1JEEVPIgJM+uDCwH/ca8eJwRAnIqngfeN"
        + "bNNZC7RvAv1KgtzvUsB0nYbVKVFPFf/dUog9PcjSbJzg4t1qjN6C4NUGMyoaGg9p"
        + "laNsSS0P0F6q9lyHOYtjJaX6ukuSTWxQ/yPlBYQVJQTbgzhFe7bSLdnhTBFLH5An"
        + "rLRqNGKEGDqUyz3DxkUuzJ0mNQi1hlGKmjEp3rNJ0vJHtjzw+ENLLgGc+JrJ4Tbn"
        + "2p7HLLTPLXDbbGReoY9Cb22aJPPbMwIDAQABo2MwYTAfBgNVHSMEGDAWgBSIFnbz"
        + "7l0iZJUJMLz0yIaYE84VYTAdBgNVHQ4EFgQUiBZ28+5dImSVCTC89MiGmBPOFWEw"
        + "DwYDVR0TAQH/BAUwAwEB/zAOBgNVHQ8BAf8EBAMCAQYwDQYJKoZIhvcNAQEFBQAD"
        + "ggEBAEQnUNI1REuJGNrnEIxqLJX6w8TmgUp6g+4ytqh8fZoy1tHIH6+dhPhP87Gk"
        + "E4QVfq5AZDb/mMuZCdGjOAaWqXsaUfJNFcJcrbKxnu0MBbfcPrHkhTTlDpoPV+N7"
        + "P+iLBHJB3IAVcic+b/xXqeUn50Yp7VmfIVYzrIS3diA57hHntm72eMsYHLjuToxh"
        + "QD9E/PVo3eqAH2MW71+RY0X75gZytu3in9/1v0+IbFBeTG7KrVxMAt+x2pE7CybA"
        + "pNxs5wkFxwU0wF2jjTgef6ivAS3gn+KOk9YR1xpMk5FkHAjwCUWEve8GKvEIU/83"
        + "0z4rffrSX2Y9mYm4gF/GtjAkgLA=";

    protected static final String V3_ITERCA_CERT =
        // Generated from BcX509CertificateGeneratorFactoryTest#testGenerateIntermediateCertificateVersion3()
        // interCAcert with adapted validity
        // Link to decoded ASN.1: https://goo.gl/ahbPjf
        "MIID4jCCAsqgAwIBAgIRALpeBEAtiuOWnOU9EQEPV5wwDQYJKoZIhvcNAQEFBQAw"
        + "EjEQMA4GA1UEAwwHVGVzdCBDQTAeFw0xNTA5MTAxMDAwMDBaFw00OTEyMzExMTAw"
        + "MDBaMB8xHTAbBgNVBAMMFFRlc3QgSW50ZXJtZWRpYXRlIENBMIIBtzCCASwGByqG"
        + "SM44BAEwggEfAoGBALjHlfmpKj8BiEfekiLTnbYdZlo5Hz6E2dAjx+ryqv3jeGYb"
        + "PTxh+pxrD0MIIUKF+3o8Y+TBwBpbKnnZ/G2T/P6QXs8+l7H7Q4CUJKShdQ+PhpK8"
        + "JXYaICN4VAtKsP4PVhBMWLw/3VANh67JDwZz1Oa5soci3dAVQDWN8mc4PdbhAhUA"
        + "oWrfRj14AUQT759T/Men1dQ9o0ECgYEAgWPlEWkpvgfkCvyBMRiRWchS0suOUUL5"
        + "RyqYKmVFpDE2aKRMMFO5owlluJ1lm57f4zaddY8zAsT72tv0tTxz7nFAAPoX4QPO"
        + "cSxYYapvEGRZklJRU4qrrXOPlXTia6jsWlgjnMaJ43zCBXteK2AdZ2DF7Yr9UPRu"
        + "NukIzSYc4pcDgYQAAoGAEH/cX4auYYjapwPvipulmUPLPB9GTPcZfcefLYH4FlAs"
        + "/W1/vfer1kGZL/+urSu+5D/FonOGNE9VRnLhVO4SyOremfJTO0ZLA7w5ciQwcQRx"
        + "wXX3vvYzxtiFA2H7G7SHVcg8GDzyikHePQnyDwjgXf2C8dxcyasUA5FJb62YKo2j"
        + "gZAwgY0wSAYDVR0jBEEwP4AUiBZ28+5dImSVCTC89MiGmBPOFWGhFqQUMBIxEDAO"
        + "BgNVBAMMB1Rlc3QgQ0GCD155oq5k3ogYWMtf4v7cKzAdBgNVHQ4EFgQUbrrdokxq"
        + "hLVQHnfZkpK1PN7kUbowEgYDVR0TAQH/BAgwBgEB/wIBADAOBgNVHQ8BAf8EBAMC"
        + "AQYwDQYJKoZIhvcNAQEFBQADggEBALuttCfxVloX+cVZw97ny2C0Gx1gF2iQXPUq"
        + "7QjsTs9xWB2X2j9hMYS8x4oB8x2w59PRZpdtdlFvjxeLR9xrX1yFeqcGMZ2opEqx"
        + "htFhSE28Inv+A+VWo/Je1T986XEgGMrIgPkW46Bc8xsNy63WHdcpj7U9xWU2Qcs8"
        + "EPzTNV3ibevNdCS6bwXEpgn2fSV7dsscaZGt9O43Co2iyGYqXZXqjRXOJkQmLFc/"
        + "p5xPpsPhFXERE65Ihdtl17XLiIt88mvOyfNl/X9c28lLf0+hlEqnmE1CDrvitKAy"
        + "hHU7w1nXlQZZz6RWvilHqn0NkT3vTYhQBVXq5XiQwT8xLBjJJtw=";

    protected static final String V3_CERT =
        // Generated from BcX509CertificateGeneratorFactoryTest#testGenerateIntermediateCertificateVersion3()
        // certificate with adapted validity
        // Link to decoded ASN.1: https://goo.gl/OPAFci
        "MIIDMjCCAvCgAwIBAgIRAKm/hze1CY3FuwXuKd86eXgwCwYHKoZIzjgEAwUAMB8x"
        + "HTAbBgNVBAMMFFRlc3QgSW50ZXJtZWRpYXRlIENBMB4XDTE1MDkxMDEwMDAwMFoX"
        + "DTQ5MTIzMTExMDAwMFowGjEYMBYGA1UEAwwPVGVzdCBFbmQgRW50aXR5MIIBtzCC"
        + "ASwGByqGSM44BAEwggEfAoGBANQ9Oa1j9sWAhdXNyqz8HL/bA/ed2VrBw6TPkgMy"
        + "V1Upix58RSjOHMQNrgemSGkb80dRcLqVDYbI3ObnIJh83Zx6zeaTpvUohGLyTa0F"
        + "7UY15LkbJpyz8WFJaVykH85nz3Zo6Md9Z4X95yvF1+h9qYuakjWcHW31+pN4u3cJ"
        + "Ng5FAhUAj986cVG9NgWgWzFVSLbB9pEPbFUCgYEAmQrZFH3MX5CLX/5vDvxyTeeR"
        + "PZHLWc0ik3GwrIJExuVrOkuFInpx0aVbuJTxrEnY2fuc/+Byj/F56DDO31+qPu7Z"
        + "xbSvvD33OOk8eFEfn+Hia3QmA+dGrhUqoMpfDf4/GBgJhnyQtFzMddHmYB0QnS9y"
        + "X1n6DOWj/CSX0PvrlMYDgYQAAoGAJvnuTm8oI/RRI2tiZHtPkvSQaA3FP4PRsVx6"
        + "z1oIGg9OAxrtSS/aiQa+HWFg7fjHlMJ30Vh0yqt7igj70jaLGyDvr3MPDyiO++72"
        + "IiGUluc6yHg6m9cQ53eeJt9i44LJfTOw1S3YMU1ST7alokSnJRTICp5WBy0m1scw"
        + "heuTo0GjgbAwga0wSgYDVR0jBEMwQYAUbrrdokxqhLVQHnfZkpK1PN7kUbqhFqQU"
        + "MBIxEDAOBgNVBAMMB1Rlc3QgQ0GCEQC6XgRALYrjlpzlPREBD1ecMB0GA1UdDgQW"
        + "BBSdIuxgWLG45Mk01RHaIRUu2RadHjAOBgNVHQ8BAf8EBAMCBJAwEwYDVR0lBAww"
        + "CgYIKwYBBQUHAwQwGwYDVR0RBBQwEoEQdGVzdEBleGFtcGxlLmNvbTALBgcqhkjO"
        + "OAQDBQADLwAwLAIUe6xUKxqsupL5MWKKdsaY0FiGWVgCFHG3MW1tmgQ6CChIdA9K"
        + "5fBjULkT";

    protected static final String TEXT = "Congress shall make no law respecting an establishment of religion, or "
        + "prohibiting the free exercise thereof; or abridging the freedom of speech, "
        + "or of the press; or the right of the people peaceably to assemble, and to "
        + "petition the Government for a redress of grievances.";
}
