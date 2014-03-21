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
    protected static final String RSA_PRIVATE_KEY = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDCmjim/3likJ4"
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

    protected static final String RSA_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwpo4pv95YpCeFReeuFM"
        + "soKj4yF/89PdACJi5dHwy1JCMglU0ndfMCd2sa9aoy7UoDT1nrdKbksThR9AMxS"
        + "kQiTo1JEEVPIgJM+uDCwH/ca8eJwRAnIqngfeNbNNZC7RvAv1KgtzvUsB0nYbVK"
        + "VFPFf/dUog9PcjSbJzg4t1qjN6C4NUGMyoaGg9plaNsSS0P0F6q9lyHOYtjJaX6"
        + "ukuSTWxQ/yPlBYQVJQTbgzhFe7bSLdnhTBFLH5AnrLRqNGKEGDqUyz3DxkUuzJ0"
        + "mNQi1hlGKmjEp3rNJ0vJHtjzw+ENLLgGc+JrJ4Tbn2p7HLLTPLXDbbGReoY9Cb2"
        + "2aJPPbMwIDAQAB";

    protected static final String INTERCA_DSA_PRIVATE_KEY =
        "MIIBSwIBADCCASwGByqGSM44BAEwggEfAoGBALjHlfmpKj8BiEfekiLTnbYdZlo5"
            + "Hz6E2dAjx+ryqv3jeGYbPTxh+pxrD0MIIUKF+3o8Y+TBwBpbKnnZ/G2T/P6QXs8+"
            + "l7H7Q4CUJKShdQ+PhpK8JXYaICN4VAtKsP4PVhBMWLw/3VANh67JDwZz1Oa5soci"
            + "3dAVQDWN8mc4PdbhAhUAoWrfRj14AUQT759T/Men1dQ9o0ECgYEAgWPlEWkpvgfk"
            + "CvyBMRiRWchS0suOUUL5RyqYKmVFpDE2aKRMMFO5owlluJ1lm57f4zaddY8zAsT7"
            + "2tv0tTxz7nFAAPoX4QPOcSxYYapvEGRZklJRU4qrrXOPlXTia6jsWlgjnMaJ43zC"
            + "BXteK2AdZ2DF7Yr9UPRuNukIzSYc4pcEFgIULVbclkmz+d+shls7gXvWJD6Z1Pc=";

    protected static final String INTERCA_DSA_PUBLIC_KEY =
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

    protected static final String DSA_PRIVATE_KEY = "MIIBTAIBADCCASwGByqGSM44BAEwggEfAoGBANQ9Oa1j9sWAhdXNyqz8HL/bA/e"
        + "d2VrBw6TPkgMyV1Upix58RSjOHMQNrgemSGkb80dRcLqVDYbI3ObnIJh83Zx6ze"
        + "aTpvUohGLyTa0F7UY15LkbJpyz8WFJaVykH85nz3Zo6Md9Z4X95yvF1+h9qYuak"
        + "jWcHW31+pN4u3cJNg5FAhUAj986cVG9NgWgWzFVSLbB9pEPbFUCgYEAmQrZFH3M"
        + "X5CLX/5vDvxyTeeRPZHLWc0ik3GwrIJExuVrOkuFInpx0aVbuJTxrEnY2fuc/+B"
        + "yj/F56DDO31+qPu7ZxbSvvD33OOk8eFEfn+Hia3QmA+dGrhUqoMpfDf4/GBgJhn"
        + "yQtFzMddHmYB0QnS9yX1n6DOWj/CSX0PvrlMYEFwIVAIO1GUQjAddL4btiFQnhe"
        + "N4fxBTa";

    protected static final String DSA_PUBLIC_KEY = "MIIBtzCCASwGByqGSM44BAEwggEfAoGBANQ9Oa1j9sWAhdXNyqz8HL/bA/ed2VrB"
        + "w6TPkgMyV1Upix58RSjOHMQNrgemSGkb80dRcLqVDYbI3ObnIJh83Zx6zeaTpvUo"
        + "hGLyTa0F7UY15LkbJpyz8WFJaVykH85nz3Zo6Md9Z4X95yvF1+h9qYuakjWcHW31"
        + "+pN4u3cJNg5FAhUAj986cVG9NgWgWzFVSLbB9pEPbFUCgYEAmQrZFH3MX5CLX/5v"
        + "DvxyTeeRPZHLWc0ik3GwrIJExuVrOkuFInpx0aVbuJTxrEnY2fuc/+Byj/F56DDO"
        + "31+qPu7ZxbSvvD33OOk8eFEfn+Hia3QmA+dGrhUqoMpfDf4/GBgJhnyQtFzMddHm"
        + "YB0QnS9yX1n6DOWj/CSX0PvrlMYDgYQAAoGAJvnuTm8oI/RRI2tiZHtPkvSQaA3F"
        + "P4PRsVx6z1oIGg9OAxrtSS/aiQa+HWFg7fjHlMJ30Vh0yqt7igj70jaLGyDvr3MP"
        + "DyiO++72IiGUluc6yHg6m9cQ53eeJt9i44LJfTOw1S3YMU1ST7alokSnJRTICp5W"
        + "By0m1scwheuTo0E=";

    protected static final String V1_CA_CERT = "MIICpzCCAY8CEBySdlSTKgwuylJNlQxTMNIwDQYJKoZIhvcNAQEFBQAwEjEQMA4G"
        + "A1UEAwwHVGVzdCBDQTAeFw0xNDAyMDMxMTAwMDBaFw0xNTA2MTgxMDAwMDBaMBIx"
        + "EDAOBgNVBAMMB1Rlc3QgQ0EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIB"
        + "AQDCmjim/3likJ4VF564UyygqPjIX/z090AImLl0fDLUkIyCVTSd18wJ3axr1qjL"
        + "tSgNPWet0puSxOFH0AzFKRCJOjUkQRU8iAkz64MLAf9xrx4nBECciqeB941s01kL"
        + "tG8C/UqC3O9SwHSdhtUpUU8V/91SiD09yNJsnODi3WqM3oLg1QYzKhoaD2mVo2xJ"
        + "LQ/QXqr2XIc5i2Mlpfq6S5JNbFD/I+UFhBUlBNuDOEV7ttIt2eFMEUsfkCestGo0"
        + "YoQYOpTLPcPGRS7MnSY1CLWGUYqaMSnes0nS8ke2PPD4Q0suAZz4msnhNufanscs"
        + "tM8tcNtsZF6hj0JvbZok89szAgMBAAEwDQYJKoZIhvcNAQEFBQADggEBAB2M29kY"
        + "IwXRNpqM/CnRhc8MFCKO5XDQI35CrECFFXOyfGJsWb2W/O2FQFpi3bNHdKgt5BN6"
        + "ZVjTdr8veHPr5bQ9IrZgoAAL41xwMThQjGDvomiZri0WtulP8VfX0axFGhdde4mg"
        + "iYpYyCLYvTg5Mp8FuEW9XPtgJSumKYTNhk0prKyN7UfLxrhdI1sG3Y1/2/a8Bz3m"
        + "xzPB6DMYMNPD1rB6R/mU+QUBPCPlUSCm+zQf+gTL0Uu2r4jlUiHSVywAPcEWfGFP"
        + "/qb05hjvU8mYDbwPd3kX/mKHBUYKVqGemz9UPJqF0Yg9y7qtlivdiv7o7VaoykdK"
        + "mDzNKbH1jnI/azc=";

    protected static final String V1_CERT = "MIIDRDCCAiwCEGTodh45ecrZyNhs/OCWTMQwDQYJKoZIhvcNAQEFBQAwEjEQMA4G"
        + "A1UEAwwHVGVzdCBDQTAeFw0xNDAzMTcxMTAwMDBaFw0xNTA3MzAxMDAwMDBaMBox"
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
        + "BQUAA4IBAQC8bGVK2vFXZGGVOSxvvXXkDNFL9H8cW4i4efrBJ8xGVgtRRm211Mkn"
        + "uziXcHi2pAPdDnmdrgz0xnj+RxTJDLltYbRgh6R7QCywKH+Wqsqq6D+p/D9wm5FI"
        + "BYSjS3UMjB9ALSKC4x09Plp+6ooU9tpKGHlmkNCCH9tkkK2p2GcjbOv9o+tM5BWa"
        + "nRyNfdeKrfWSPVxchHovIEG2ytfBVsd8UNMgIIOM8QBFHqzbGjT2jTcIWoAlwaRL"
        + "wJLu0jsLxRl+pRqced1xaz42KYHwZceBIVjg1SP6goowbwfgaQKfteGcpZk27KME"
        + "x3zaPAb+IcpeOs3gOMfdiAk77nlo3mxo";


    protected static final String V3_CA_CERT = "MIIDEjCCAfqgAwIBAgIRANKASb61Xw7K0Oz9c034VPswDQYJKoZIhvcNAQEFBQAw"
        + "EjEQMA4GA1UEAwwHVGVzdCBDQTAeFw0xNDAzMTcyMzAwMDBaFw0xNTA3MzAyMjAw"
        + "MDBaMBIxEDAOBgNVBAMMB1Rlc3QgQ0EwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAw"
        + "ggEKAoIBAQDCmjim/3likJ4VF564UyygqPjIX/z090AImLl0fDLUkIyCVTSd18wJ"
        + "3axr1qjLtSgNPWet0puSxOFH0AzFKRCJOjUkQRU8iAkz64MLAf9xrx4nBECciqeB"
        + "941s01kLtG8C/UqC3O9SwHSdhtUpUU8V/91SiD09yNJsnODi3WqM3oLg1QYzKhoa"
        + "D2mVo2xJLQ/QXqr2XIc5i2Mlpfq6S5JNbFD/I+UFhBUlBNuDOEV7ttIt2eFMEUsf"
        + "kCestGo0YoQYOpTLPcPGRS7MnSY1CLWGUYqaMSnes0nS8ke2PPD4Q0suAZz4msnh"
        + "NufanscstM8tcNtsZF6hj0JvbZok89szAgMBAAGjYzBhMB8GA1UdIwQYMBaAFIgW"
        + "dvPuXSJklQkwvPTIhpgTzhVhMB0GA1UdDgQWBBSIFnbz7l0iZJUJMLz0yIaYE84V"
        + "YTAPBgNVHRMBAf8EBTADAQH/MA4GA1UdDwEB/wQEAwIBBjANBgkqhkiG9w0BAQUF"
        + "AAOCAQEAs8QhTcl1gIczPhb0JFFB44Uvv5PSGzmnZev9PRstdHzyElCXngM3DVDc"
        + "RfoukW0FUcStw7r7XrVKxMAQPHiFmmfESr8M5uX/RNvp9Vm/x3NsCZfYXFsj7P89"
        + "kRfZOrld+IEVlKa/OhMiG9uOvT3ZZQIUON7pLZEfiVgMTnzKKAyJ1uE0cD18wXrZ"
        + "XkzvVwyBX61ALnLA49ZvE9IWpzJEk9F+hgFnJox1GNuQn6JC9ibdEW+FQVuThCVI"
        + "axcICn4Ek9fPos990Ehd9EdMx+tWgz+6URtkpRYLPGCFZ3ygejGvCh6FqRfLMFoc"
        + "DdEVSTgcutG5PEOLctckxeHsS6yJkg==";

    protected static final String V3_ITERCA_CERT = "MIID4zCCAsugAwIBAgIQTAHcJ2NjsKKdlkNAWCJaxTANBgkqhkiG9w0BAQUFADAS"
        + "MRAwDgYDVQQDDAdUZXN0IENBMB4XDTE0MDMxNzIzMDAwMFoXDTE1MDczMDIyMDAw"
        + "MFowHzEdMBsGA1UEAwwUVGVzdCBJbnRlcm1lZGlhdGUgQ0EwggG3MIIBLAYHKoZI"
        + "zjgEATCCAR8CgYEAuMeV+akqPwGIR96SItOdth1mWjkfPoTZ0CPH6vKq/eN4Zhs9"
        + "PGH6nGsPQwghQoX7ejxj5MHAGlsqedn8bZP8/pBezz6XsftDgJQkpKF1D4+Gkrwl"
        + "dhogI3hUC0qw/g9WEExYvD/dUA2HrskPBnPU5rmyhyLd0BVANY3yZzg91uECFQCh"
        + "at9GPXgBRBPvn1P8x6fV1D2jQQKBgQCBY+URaSm+B+QK/IExGJFZyFLSy45RQvlH"
        + "KpgqZUWkMTZopEwwU7mjCWW4nWWbnt/jNp11jzMCxPva2/S1PHPucUAA+hfhA85x"
        + "LFhhqm8QZFmSUlFTiqutc4+VdOJrqOxaWCOcxonjfMIFe14rYB1nYMXtiv1Q9G42"
        + "6QjNJhzilwOBhAACgYAQf9xfhq5hiNqnA++Km6WZQ8s8H0ZM9xl9x58tgfgWUCz9"
        + "bX+996vWQZkv/66tK77kP8Wic4Y0T1VGcuFU7hLI6t6Z8lM7RksDvDlyJDBxBHHB"
        + "dfe+9jPG2IUDYfsbtIdVyDwYPPKKQd49CfIPCOBd/YLx3FzJqxQDkUlvrZgqjaOB"
        + "kjCBjzBKBgNVHSMEQzBBgBSIFnbz7l0iZJUJMLz0yIaYE84VYaEWpBQwEjEQMA4G"
        + "A1UEAwwHVGVzdCBDQYIRANKASb61Xw7K0Oz9c034VPswHQYDVR0OBBYEFG663aJM"
        + "aoS1UB532ZKStTze5FG6MBIGA1UdEwEB/wQIMAYBAf8CAQAwDgYDVR0PAQH/BAQD"
        + "AgEGMA0GCSqGSIb3DQEBBQUAA4IBAQAfcpvFEzTGdqcdg/3XUS7PDnuP40O+Alli"
        + "5Vqt77Ldh3a11+g6HBxSrk+MrqB/0gC2SQ1id0FCyJxfXAameZpqqXt2DnjwOWX/"
        + "cWcDBws0VbZDisq37eg/LwPAAxnJvF9ap625Vwmr+Gr7B/zehegdOajYj5Iufk0Z"
        + "72ZJEXs39KX9g4+95YxRX8wRgASv3qSl1rtdR7jEo8T2+Ca5CjueNtk7uGxzPzUV"
        + "93dfky41j1UKXlCndj0vqTvlRe7pJ9OPFR5I9P2afQWOKELVR67yDsQnD9MRblkF"
        + "/zEecnWSgPNAJi/YywA3ImvwlVJADrIOsAwbAOm6SHADgHgNAJ2n";

    protected static final String V3_CERT = "MIIDMTCCAu+gAwIBAgIRANthik4fzttcoxdetKvv3g0wCwYHKoZIzjgEAwUAMB8x"
        + "HTAbBgNVBAMMFFRlc3QgSW50ZXJtZWRpYXRlIENBMB4XDTE0MDMxNzIzMDAwMFoX"
        + "DTE1MDczMDIyMDAwMFowGjEYMBYGA1UEAwwPVGVzdCBFbmQgRW50aXR5MIIBtzCC"
        + "ASwGByqGSM44BAEwggEfAoGBANQ9Oa1j9sWAhdXNyqz8HL/bA/ed2VrBw6TPkgMy"
        + "V1Upix58RSjOHMQNrgemSGkb80dRcLqVDYbI3ObnIJh83Zx6zeaTpvUohGLyTa0F"
        + "7UY15LkbJpyz8WFJaVykH85nz3Zo6Md9Z4X95yvF1+h9qYuakjWcHW31+pN4u3cJ"
        + "Ng5FAhUAj986cVG9NgWgWzFVSLbB9pEPbFUCgYEAmQrZFH3MX5CLX/5vDvxyTeeR"
        + "PZHLWc0ik3GwrIJExuVrOkuFInpx0aVbuJTxrEnY2fuc/+Byj/F56DDO31+qPu7Z"
        + "xbSvvD33OOk8eFEfn+Hia3QmA+dGrhUqoMpfDf4/GBgJhnyQtFzMddHmYB0QnS9y"
        + "X1n6DOWj/CSX0PvrlMYDgYQAAoGAJvnuTm8oI/RRI2tiZHtPkvSQaA3FP4PRsVx6"
        + "z1oIGg9OAxrtSS/aiQa+HWFg7fjHlMJ30Vh0yqt7igj70jaLGyDvr3MPDyiO++72"
        + "IiGUluc6yHg6m9cQ53eeJt9i44LJfTOw1S3YMU1ST7alokSnJRTICp5WBy0m1scw"
        + "heuTo0Gjga8wgawwSQYDVR0jBEIwQIAUbrrdokxqhLVQHnfZkpK1PN7kUbqhFqQU"
        + "MBIxEDAOBgNVBAMMB1Rlc3QgQ0GCEEwB3CdjY7CinZZDQFgiWsUwHQYDVR0OBBYE"
        + "FJ0i7GBYsbjkyTTVEdohFS7ZFp0eMA4GA1UdDwEB/wQEAwIEkDATBgNVHSUEDDAK"
        + "BggrBgEFBQcDBDAbBgNVHREEFDASgRB0ZXN0QGV4YW1wbGUuY29tMAsGByqGSM44"
        + "BAMFAAMvADAsAhQJWXbMhGtsgc8EbXoXWWlk+QNuoQIURvSp4HxJ8oFD8RbmiRsc"
        + "4jiu93Q=";

    protected static final String TEXT = "Congress shall make no law respecting an establishment of religion, or "
        + "prohibiting the free exercise thereof; or abridging the freedom of speech, "
        + "or of the press; or the right of the people peaceably to assemble, and to "
        + "petition the Government for a redress of grievances.";

}
