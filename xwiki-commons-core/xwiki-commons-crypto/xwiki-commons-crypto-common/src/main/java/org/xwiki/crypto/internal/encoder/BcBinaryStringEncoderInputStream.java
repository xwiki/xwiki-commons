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

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream wrapper that will decode string data to binary in block.
 *
 * @version $Id$
 * @since 5.4M1
 */
class BcBinaryStringEncoderInputStream extends FilterInputStream
{
    /** Number of bytes of binary data that is produced from {@code charSize} chars of encoded data. */
    private final int blockSize;

    /** Number of bytes of string data that should be decoded together. */
    private final int charSize;

    /** A one byte buffer to optimize one byte writing. */
    private byte[] oneByte = new byte[1];

    /** An overflow buffer to collate data read in advance. */
    private byte[] ofBuf;

    /** Current offset in byte of valid data in the overflow buffer. */
    private int ofOff;

    /** Current length in byte of valid data in the overflow buffer. */
    private int ofLen;

    /** Encoder used to decode data. */
    private final InternalBinaryStringEncoder encoder;

    /**
     * Buffer reading input data for a given number of input blocks (including blanks).
     */
    class InputBuffer
    {
        /** Buffer to containing read data. */
        private byte[] buf;

        /** Length in bytes of valid data in the buffer. */
        private int bufLen;

        /** Count in bytes of blanks chars in the buffer. */
        private int bcount;

        /** Count in bytes of blanks chars read during the last read operation. */
        private int rblank;

        /**
         * Construct an new buffer initialized with the amount of data requested, or less if EOF is reach.
         *
         * @param blen number of block of input string data to read.
         * @throws IOException on error.
         */
        InputBuffer(int blen) throws IOException
        {
            int rlen = 0;
            int rbl = blen * BcBinaryStringEncoderInputStream.this.charSize;
            this.buf = new byte[rbl];

            // Try to read the number of block requested, read more as needed if some blank char has been read.
            while (rbl > 0 && rlen >= 0) {
                rlen = read(rbl);
                rbl = this.rblank;
            }

            rlen = this.bufLen - this.bcount;

            // Some non-blank data found ?
            if (rlen > 0) {
                // Wait until reaching EOF or being aligned on block size
                // Remember that some input stream may be lazy,
                // but should at least return one byte, else EOF is reached.
                int rblen = (rlen + BcBinaryStringEncoderInputStream.this.charSize - 1)
                    / BcBinaryStringEncoderInputStream.this.charSize;
                int runder = (rblen * BcBinaryStringEncoderInputStream.this.charSize) - rlen;
                int rrlen = 0;
                while (runder > 0 && rrlen >= 0) {
                    rrlen = read(runder);
                    if (rrlen > 0) {
                        runder -= rrlen - this.rblank;
                    }
                }
            }
        }

        /**
         * @return the data buffer.
         */
        byte[] getBuffer()
        {
            return this.buf;
        }

        /**
         * @return the length in bytes of valid data in the buffer.
         */
        int getReadLength()
        {
            return this.bufLen;
        }

        /**
         * @return the count in bytes of non-blank data in the buffer.
         */
        int getEffectiveLength()
        {
            return this.bufLen - this.bcount;
        }

        /**
         * Count blanks char in a given area of the buffer.
         *
         * @param off starting offset
         * @param len length of the area
         * @return number of bytes containing blank characters.
         */
        private int countBlank(int off, int len)
        {
            int blank = 0;
            for (int i = off; i < len; i++) {
                if (isBlank(this.buf[i])) {
                    blank++;
                }
            }
            return blank;
        }

        private boolean isBlank(byte b)
        {
            return (b == '\n' || b == '\r' || b == '\t' || b == ' ');
        }

        /**
         * Ensure the buffer is large enough to contains a given amount of bytes.
         *
         * @param len the number of bytes the buffer should be able to contains.
         */
        private void ensureSize(int len)
        {
            if (len > this.buf.length) {
                byte[] nbuf = new byte[len];
                System.arraycopy(this.buf, 0, nbuf, 0, this.buf.length);
                this.buf = nbuf;
            }
        }

        /**
         * Appends some data from the input stream to valid data in the buffer.
         *
         * @param len number of bytes to be read.
         * @return number of bytes effectively read.
         * @throws IOException on error.
         */
        private int read(int len) throws IOException
        {
            ensureSize(this.bufLen + len);
            return readBase64(len);
        }

        /**
         * Append only valid base64 data from the input stream in the buffer.
         *
         * The buffer may be affected over len size even if the returned len is lower.
         *
         * @param len number of bytes to be read.
         * @return number of bytes effectively read.
         * @throws IOException on error.
         */
        private int readBase64(int len) throws IOException
        {
            int rlen;
            if (BcBinaryStringEncoderInputStream.this.in.markSupported()) {
                rlen = readBase64WithMark(len);
            } else {
                rlen = readBase64WithoutMark(len);
            }
            if (rlen > 0) {
                this.rblank = countBlank(this.bufLen, rlen);
                this.bufLen += rlen;
                this.bcount += this.rblank;
                return rlen;
            }
            return -1;
        }

        private int readBase64WithoutMark(int len) throws IOException
        {
            int rlen;
            rlen = this.bufLen;
            while (rlen < (this.bufLen + len)) {
                int c = BcBinaryStringEncoderInputStream.this.in.read();
                if (c < 0) {
                    break;
                }
                byte b = (byte) c;
                if (!isBlank(b) && !BcBinaryStringEncoderInputStream.this.encoder.isValidEncoding(b)) {
                    break;
                }
                this.buf[rlen++] = b;
            }
            rlen -= this.bufLen;
            return rlen;
        }

        private int readBase64WithMark(int len) throws IOException
        {
            int rlen;
            BcBinaryStringEncoderInputStream.this.in.mark(len);
            rlen = BcBinaryStringEncoderInputStream.this.in.read(this.buf, this.bufLen, len);
            int i = this.bufLen;
            while (i < (this.bufLen + rlen)) {
                byte b = this.buf[i];
                if (!isBlank(b) && !BcBinaryStringEncoderInputStream.this.encoder.isValidEncoding(b)) {
                    break;
                }
                i++;
            }
            i -= this.bufLen;
            if (i < rlen) {
                BcBinaryStringEncoderInputStream.this.in.reset();
                rlen = (int) BcBinaryStringEncoderInputStream.this.in.skip(i);
            }
            return rlen;
        }
    }

    /**
     * Construct a filter stream for the given encoder, following the specifications.
     *
     * @param inputStream the underlying input stream where data will be read.
     * @param encoder the encoder used to decode data.
     */
    BcBinaryStringEncoderInputStream(InputStream inputStream, InternalBinaryStringEncoder encoder)
    {
        super(inputStream);
        this.encoder = encoder;
        this.blockSize = encoder.getEncodingBlockSize();
        this.charSize = encoder.getDecodingBlockSize();
        this.ofBuf = new byte[this.blockSize];
    }

    @Override
    public int read() throws IOException
    {
        if (read(this.oneByte, 0, 1) > 0) {
            return this.oneByte[0];
        }
        return -1;
    }

    @Override
    public int read(byte[] out, int offset, int length) throws IOException
    {
        if ((offset | length | (out.length - (length + offset)) | (offset + length)) < 0) {
            throw new IndexOutOfBoundsException();
        }

        if (length == 0) {
            return 0;
        }

        int readLen = 0;

        int off = offset;
        int len = length;

        // Is some pending data of a previous call available ?
        if (this.ofLen > 0) {
            int clen = copyData(this.ofBuf, this.ofOff, this.ofLen, out, off, len);
            this.ofLen -= clen;
            this.ofOff += clen;
            off += clen;
            len -= clen;
            readLen += clen;
        }

        // Still some data to read ?
        if (len > 0) {
            // Try to read the needed block to get -len- bytes decoded
            int blen = (len + this.blockSize - 1) / this.blockSize;
            InputBuffer inBuf = new InputBuffer(blen);
            int rlen = inBuf.getEffectiveLength();
            if (rlen > 0) {
                int rblen = (rlen + this.charSize - 1) / this.charSize;

                // Decode read data
                ByteArrayOutputStream baos = new ByteArrayOutputStream(rblen * this.blockSize);
                this.encoder.decode(inBuf.getBuffer(), 0, inBuf.getReadLength(), baos);
                baos.close();

                int clen = copyData(baos.toByteArray(), 0, baos.size(), out, off, len);
                readLen += clen;
                this.ofLen = baos.size() - clen;
                this.ofOff = 0;

                if (this.ofLen > 0) {
                    System.arraycopy(baos.toByteArray(), clen, this.ofBuf, this.ofOff, this.ofLen);
                }
            }
        }

        return (readLen > 0) ? readLen : -1;
    }

    @Override
    public int available() throws IOException
    {
        int len = super.available();
        return ((len + this.charSize - 1) / this.charSize) * this.blockSize;
    }

    @Override
    public boolean markSupported()
    {
        return false;
    }

    private int copyData(byte[] inBuf, int inOff, int inLen, byte[] outBuf, int outOff, int outLen)
    {
        int clen = inLen;
        if (clen > outLen) {
            clen = outLen;
        }
        System.arraycopy(inBuf, inOff, outBuf, outOff, clen);
        return clen;
    }
}
