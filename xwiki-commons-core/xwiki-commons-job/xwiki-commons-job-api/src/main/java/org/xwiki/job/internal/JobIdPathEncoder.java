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
package org.xwiki.job.internal;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Encodes job identifiers into file-system-safe path segments.
 *
 * @version $Id$
 * @since 18.3.0RC1
 */
public final class JobIdPathEncoder
{
    private static final int RADIX = 16;

    private static final BitSet SAFE_CHARACTERS = new BitSet(256);

    static {
        // Only allow lowercase characters to make sure that there are no conflicts on case-insensitive file systems.
        for (int i = 'a'; i <= 'z'; i++) {
            SAFE_CHARACTERS.set(i);
        }

        for (int i = '0'; i <= '9'; i++) {
            SAFE_CHARACTERS.set(i);
        }

        SAFE_CHARACTERS.set('-');
        SAFE_CHARACTERS.set('.');
        SAFE_CHARACTERS.set('_');
    }

    private JobIdPathEncoder()
    {
    }

    /**
     * @param content the string to encode
     * @return the encoded string
     */
    public static String encode(String content)
    {
        return String.join("", encodeAndSplit(content, Integer.MAX_VALUE));
    }

    /**
     * @param content the string to encode
     * @param partLimit the maximum length of each returned segment
     * @return the encoded string split into path-size-safe segments
     */
    public static List<String> encodeAndSplit(String content, int partLimit)
    {
        if (partLimit <= 0) {
            throw new IllegalArgumentException("The part limit must be greater than 0.");
        }

        List<String> result = new ArrayList<>();
        StringBuilder partBuilder = new StringBuilder();

        // Code adapted from org.apache.hc.core5.net.PercentCodec of Apache HttpCore 5.
        ByteBuffer inputBuffer = StandardCharsets.UTF_8.encode(content);
        while (inputBuffer.hasRemaining()) {
            int b = inputBuffer.get() & 0xff;
            if (SAFE_CHARACTERS.get(b)
                && (b != '.' || shallKeepPeriod(partBuilder.length(), inputBuffer.hasRemaining(), partLimit)))
            {
                partBuilder.append((char) b);
            } else {
                partBuilder.append('%');
                partBuilder.append(Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, RADIX)));
                partBuilder.append(Character.toUpperCase(Character.forDigit(b & 0xF, RADIX)));
            }

            // Leave a margin of 5 characters so it can be safely split at fully encoded characters.
            if (partBuilder.length() >= partLimit) {
                result.add(partBuilder.toString());
                partBuilder.setLength(0);
            }
        }

        if (!partBuilder.isEmpty()) {
            result.add(partBuilder.toString());
        }

        return result;
    }

    private static boolean shallKeepPeriod(int outputLength, boolean hasRemaining, int partLimit)
    {
        // Encode "." at the beginning or end of the string as it's not allowed/has special meaning, see
        // https://learn.microsoft.com/en-us/windows/win32/fileio/naming-a-file#naming-conventions
        return outputLength > 0 && outputLength < partLimit - 1 && hasRemaining;
    }
}
