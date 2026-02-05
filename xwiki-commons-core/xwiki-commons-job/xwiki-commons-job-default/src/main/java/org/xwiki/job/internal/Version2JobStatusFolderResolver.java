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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Priority;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;
import org.xwiki.component.annotation.Component;

/**
 * Implementation of {@link JobStatusFolderResolver} that gives the folder that has been introduced in XWiki 16.10.0.
 *
 * @version $Id$
 * @since 17.2.0RC1
 * @since 16.10.6
 */
@Component
@Singleton
@Named("version2")
@Priority(9900)
public class Version2JobStatusFolderResolver extends AbstractJobStatusFolderResolver
{
    @Override
    protected List<String> encodeAndSplit(String idElement)
    {
        List<String> result = new ArrayList<>();
        // Cut each element if is it's bigger than 255 bytes (and not characters) since it's a very common
        // limit for a single element of the path among file systems
        // To be sure to deal with characters not taking more than 1 byte, we start by encoding it in base 64
        String encodedIdElement = idElement == null ? null
            : Base64.encodeBase64String(idElement.getBytes());
        if (encodedIdElement != null && encodedIdElement.length() > 255) {
            do {
                result.add(nullAwareURLEncode(encodedIdElement.substring(0, 255)));
                encodedIdElement = encodedIdElement.substring(255);
            } while (encodedIdElement.length() > 255);
        } else {
            result.add(nullAwareURLEncode(encodedIdElement));
        }
        return result;
    }
}
