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
import java.util.ArrayList;
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
    private static final String PERIOD = ".";

    private static final String ENCODED_PERIOD = "%2E";

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

    private List<String> encodeAndSplit(String name)
    {
        String encoded = nullAwareURLEncode(name)
            // Replace * as it's not allowed on Windows.
            .replace("*", "%2A");

        List<String> result = new ArrayList<>();

        // Cut each element if it's bigger than 250 bytes (and not characters) since 255 is a very common
        // limit for a single element of the path among file systems.
        // To be sure to deal with characters not taking more than 1 byte, we split the encoded string.
        // Leave a margin of 5 characters so a period at the start and end of each part can be safely encoded.
        while (encoded.length() > 250) {
            String part = encoded.substring(0, 250);
            encoded = encoded.substring(250);

            result.add(encodePeriods(part));
        }

        result.add(encodePeriods(encoded));

        return result;
    }

    private String encodePeriods(String input)
    {
        String output = input;

        // Replace "." at the beginning or end of the string as they're not allowed/have special meaning, see
        // https://learn.microsoft.com/en-us/windows/win32/fileio/naming-a-file#naming-conventions
        if (output.startsWith(PERIOD)) {
            output = ENCODED_PERIOD + output.substring(1);
        }
        if (output.endsWith(PERIOD)) {
            output = output.substring(0, output.length() - 1) + ENCODED_PERIOD;
        }

        return output;
    }
}
