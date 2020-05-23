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
package org.xwiki.crypto.internal.encoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xwiki.crypto.BinaryStringEncoder;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public abstract class AbstractBinaryStringEncoderTest
{
    private static final String CHARSET = "UTF-8";

    private static final String TEXT = "Kryptographie (von griechisch: \u03ba\u03c1\u03c5\u03c0\u03c4\u03cc\u03c2,"
        + " \u201everborgen\u201c und \u03b3\u03c1\u03ac\u03c6\u03b5\u03b9\u03bd,"
        + " \u201eschreiben\u201c) ist die Wissenschaft der Verschl\u00fcsselung von"
        + " Informationen.";

    private static byte[] BYTES;

    protected String ENCODED_BYTES;

    protected String WRAPPED_ENCODED_BYTES;

    protected abstract BinaryStringEncoder getEncoder();

    @BeforeAll
    public static void initialize() throws Exception
    {
        BYTES = TEXT.getBytes(CHARSET);
    }

    @Test
    void encode() throws Exception
    {
        assertThat(getEncoder().encode(BYTES), equalTo(ENCODED_BYTES));
        assertThat(getEncoder().encode(BYTES, 64), equalTo(WRAPPED_ENCODED_BYTES));
        assertThat(getEncoder().encode(BYTES, 0, BYTES.length),
            equalTo(ENCODED_BYTES));
        assertThat(getEncoder().encode(BYTES, 0, BYTES.length, 64),
            equalTo(WRAPPED_ENCODED_BYTES));

    }

    @Test
    void decode() throws Exception
    {
        assertThat(getEncoder().decode(ENCODED_BYTES), equalTo(BYTES));
        assertThat(getEncoder().decode(WRAPPED_ENCODED_BYTES), equalTo(BYTES));
    }

    @Test
    void encoderStream() throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream encos = getEncoder().getEncoderOutputStream(baos);

        encos.write(BYTES, 0, 17);
        encos.write(BYTES, 17, 7);
        encos.write(BYTES, 24, 1);
        encos.write(BYTES, 25, 1);
        encos.write(BYTES, 26, 1);
        encos.write(BYTES, 27, 1);
        encos.write(BYTES, 28, 1);
        encos.write(BYTES, 29, BYTES.length - 29);
        encos.close();

        assertThat(baos.toString(), equalTo(ENCODED_BYTES));
    }

    @Test
    void encoderWrappedStream() throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStream encos = getEncoder().getEncoderOutputStream(baos, 64);

        encos.write(BYTES, 0, 17);
        encos.write(BYTES, 17, 7);
        encos.write(BYTES, 24, 1);
        encos.write(BYTES, 25, 1);
        encos.write(BYTES, 26, 1);
        encos.write(BYTES, 27, 1);
        encos.write(BYTES, 28, 1);
        encos.write(BYTES, 29, BYTES.length - 29);
        encos.close();

        assertThat(baos.toString(), equalTo(WRAPPED_ENCODED_BYTES));
    }

    private int readAll(InputStream is, byte[] out) throws IOException
    {
        int readLen, len = 0;
        int blen = 17;
        while( (readLen = is.read(out, len, blen)) > 0 ) {
            len += readLen;
        }
        is.close();
        return len;
    }

    @Test
    void decoderStream() throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(ENCODED_BYTES.getBytes());
        InputStream decis = getEncoder().getDecoderInputStream(bais);
        byte[] buf = new byte[187];
        assertThat(readAll(decis, buf), equalTo(BYTES.length));
        byte[] buf2 = new byte[BYTES.length];
        System.arraycopy(buf, 0, buf2, 0, BYTES.length);
        assertThat(buf2, equalTo(BYTES));
    }

    @Test
    void decoderWrappedStream() throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(WRAPPED_ENCODED_BYTES.getBytes());
        InputStream decis = getEncoder().getDecoderInputStream(bais);
        byte[] buf = new byte[187];
        assertThat(readAll(decis, buf), equalTo(BYTES.length));
        byte[] buf2 = new byte[BYTES.length];
        System.arraycopy(buf, 0, buf2, 0, BYTES.length);
        assertThat(buf2, equalTo(BYTES));
    }

    @Test
    void decoderStreamNoMark() throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(ENCODED_BYTES.getBytes()) {
            @Override
            public boolean markSupported()
            {
                return false;
            }
        };
        InputStream decis = getEncoder().getDecoderInputStream(bais);
        byte[] buf = new byte[187];
        assertThat(readAll(decis, buf), equalTo(BYTES.length));
        byte[] buf2 = new byte[BYTES.length];
        System.arraycopy(buf, 0, buf2, 0, BYTES.length);
        assertThat(buf2, equalTo(BYTES));
    }

    @Test
    void decoderWrappedStreamNoMark() throws Exception
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(WRAPPED_ENCODED_BYTES.getBytes()) {
            @Override
            public boolean markSupported()
            {
                return false;
            }
        };
        InputStream decis = getEncoder().getDecoderInputStream(bais);
        byte[] buf = new byte[187];
        assertThat(readAll(decis, buf), equalTo(BYTES.length));
        byte[] buf2 = new byte[BYTES.length];
        System.arraycopy(buf, 0, buf2, 0, BYTES.length);
        assertThat(buf2, equalTo(BYTES));
    }
}
