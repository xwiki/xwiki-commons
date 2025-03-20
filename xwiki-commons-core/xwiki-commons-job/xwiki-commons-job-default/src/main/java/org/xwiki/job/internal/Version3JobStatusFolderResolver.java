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

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import javax.annotation.Priority;
import javax.inject.Named;

import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;

/**
 * Version 3 of {@link JobStatusFolderResolver} that uses URL encoding but with length limit and additional encoding
 * for reserved characters. Introduced in XWiki 16.10.6 and 17.2.0.
 *
 * @version $Id$
 * @since 17.2.0RC1
 * @since 16.10.6
 */
@Component
@Singleton
@Named("version3")
@Priority(9800)
public class Version3JobStatusFolderResolver extends AbstractJobStatusFolderResolver
{
    private static final int RADIX = 16;

    private static final BitSet SAFE_CHARACTERS = new BitSet(256);

    private static final int PART_LIMIT = 250;

    static {
        // Only allow lowercase characters to make sure that there are no conflict on case-insensitive file systems.
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

    @Override
    protected File getBaseFolder()
    {
        return new File(super.getBaseFolder(), "3");
    }

    protected File addIDElement(String fullIdElement, File folder)
    {
        File result = folder;

        for (String encodedPart : encodeAndSplit(fullIdElement)) {
            result = new File(result, encodedPart);
        }

        return result;
    }

    private List<String> encodeAndSplit(String content)
    {
        if (content == null) {
            return List.of(FOLDER_NULL);
        }

        List<String> result = new ArrayList<>();
        StringBuilder partBuilder = new StringBuilder();

        // Code adapted from org.apache.hc.core5.net.PercentCodec of Apache HttpCore 5.
        ByteBuffer inputBuffer = StandardCharsets.UTF_8.encode(content);
        while (inputBuffer.hasRemaining()) {
            int b = inputBuffer.get() & 0xff;
            if (SAFE_CHARACTERS.get(b)
                && (b != '.' || shallEncodePeriod(partBuilder.length(), inputBuffer.hasRemaining())))
            {
                partBuilder.append((char) b);
            } else {
                partBuilder.append("%");
                char hex1 = Character.toUpperCase(Character.forDigit((b >> 4) & 0xF, RADIX));
                char hex2 = Character.toUpperCase(Character.forDigit(b & 0xF, RADIX));
                partBuilder.append(hex1);
                partBuilder.append(hex2);
            }

            // Cut each element if it's bigger than 250 bytes (and not characters) since 255 is a very common
            // limit for a single element of the path among file systems.
            // Leave a margin of 5 characters so it can be safely split at fully encoded characters.
            if (partBuilder.length() >= PART_LIMIT) {
                result.add(partBuilder.toString());
                partBuilder.setLength(0);
            }
        }

        if (!partBuilder.isEmpty()) {
            result.add(partBuilder.toString());
        }

        return result;
    }

    private static boolean shallEncodePeriod(int outputLength, boolean hasRemaining)
    {
        // Encode "." at the beginning or end of the string as it's not allowed/has special meaning, see
        // https://learn.microsoft.com/en-us/windows/win32/fileio/naming-a-file#naming-conventions
        return outputLength > 0 && outputLength < PART_LIMIT - 1 && hasRemaining;
    }
}
