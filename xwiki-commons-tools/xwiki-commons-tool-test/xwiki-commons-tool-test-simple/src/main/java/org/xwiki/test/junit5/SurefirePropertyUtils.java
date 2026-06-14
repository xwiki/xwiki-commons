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
package org.xwiki.test.junit5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resolves a property value used to configure our JUnit5 extensions, looking it up first in the System properties and
 * then in the current module's {@code pom.xml} file.
 *
 * @version $Id$
 * @since 18.5.0RC1
 */
final class SurefirePropertyUtils
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SurefirePropertyUtils.class);

    private SurefirePropertyUtils()
    {
        // Utility class.
    }

    /**
     * @param propertyName the property to look for
     * @return the property value by first looking for it in System properties and then in the current {@code pom.xml}
     *         file. Returns null if not found in either.
     */
    static String getPropertyValue(String propertyName)
    {
        String value = System.getProperty(propertyName);
        if (value == null) {
            value = getPropertyValueFromPOM(propertyName);
        }
        return value;
    }

    private static String getPropertyValueFromPOM(String propertyName)
    {
        String value = null;
        // Low tech (doesn't bring any additional dependencies that could cause conflicts with tests) and fast.
        // Note: doesn't support inheritance: the property needs to be set in each pom.xml where it's used.
        if (Files.exists(getPOMPath())) {
            try {
                String content = new String(Files.readAllBytes(getPOMPath()));
                Pattern regex = Pattern.compile(String.format("<%s>(.*)</%s>", propertyName, propertyName),
                    Pattern.DOTALL);
                Matcher regexMatcher = regex.matcher(content);
                if (regexMatcher.find()) {
                    value = regexMatcher.group(1).trim();
                }
            } catch (IOException e) {
                throw new RuntimeException(String.format("Error reading file [%s]", getPOMPath()), e);
            }
        } else {
            LOGGER.warn("No [{}] file in current directory [{}]", getPOMPath(), Paths.get("").toAbsolutePath());
        }
        return value;
    }

    private static Path getPOMPath()
    {
        return Paths.get("pom.xml");
    }
}
