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
package org.xwiki.configuration.internal;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;

/**
 * Environment variables based configuration source.
 *
 * @version $Id$
 * @since 17.3.0RC1
 */
@Component
@Singleton
@Named(EnvVarsConfigurationSource.HINT)
public class EnvVarsConfigurationSource extends AbstractPropertiesConfigurationSource
{
    /**
     * The hint to use to get this configuration source.
     */
    public static final String HINT = "envvars";

    private static final String PREFIX = "XCONF_";

    Map<String, String> getenv()
    {
        return System.getenv();
    }

    String getenv(String name)
    {
        return System.getenv(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getProperty(String key)
    {
        return (T) System.getenv(toEnvKey(key));
    }

    @Override
    public List<String> getKeys()
    {
        return getKeys("");
    }

    @Override
    public List<String> getKeys(String prefix)
    {
        return System.getenv().keySet().stream().filter(k -> isEnvKey(k, prefix)).map(k -> fromEnvKey(k, prefix))
            .toList();
    }

    @Override
    public boolean containsKey(String key)
    {
        return System.getenv().containsKey(toEnvKey(key));
    }

    @Override
    public boolean isEmpty()
    {
        return isEmpty("");
    }

    @Override
    public boolean isEmpty(String prefix)
    {
        Map<String, String> env = System.getenv();

        if (env.isEmpty()) {
            return true;
        }

        return env.keySet().stream().filter(k -> isEnvKey(k, prefix)).findFirst().isEmpty();
    }

    private String toEnvKey(String property)
    {
        StringBuilder builder = new StringBuilder(property.length() * 3);

        builder.append(PREFIX);

        for (int i = 0; i < property.length(); ++i) {
            char c = property.charAt(i);

            boolean encode = false;

            if (i == 0) {
                // Environment variable keys cannot start with a digit
                if (Character.isDigit(c)) {
                    encode = true;
                }
            } else {
                // Environment variable keys are only allowed to contain underscore or alphanumeric characters
                if (c != '_' && !Character.isAlphabetic(c) && !Character.isDigit(c)) {
                    encode = true;
                }
            }

            if (encode) {
                encode(c, builder);
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    /**
     * @param c the character to encode
     * @param builder the builder to append the encoded character to
     */
    private void encode(char c, StringBuilder builder)
    {
        builder.append('_');

        switch (c) {
            case '.', ':', '-':
                break;
            default:
                byte[] ba = String.valueOf(c).getBytes(StandardCharsets.UTF_8);

                for (int j = 0; j < ba.length; j++) {
                    char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
                    // Make it upper case
                    ch = Character.toUpperCase(ch);
                    builder.append(ch);

                    ch = Character.forDigit(ba[j] & 0xF, 16);
                    // Make it upper case
                    ch = Character.toUpperCase(ch);
                    builder.append(ch);
                }
        }
    }

    private boolean isEnvKey(String key, String prefix)
    {
        return key.startsWith(PREFIX + prefix);
    }

    private String fromEnvKey(String key, String prefix)
    {
        String property = key.substring(PREFIX.length() + prefix.length());

        // The conversion of the environment variable name to a property key may not be fully accurate (because several
        // characters are, by default, converted to "_"). But for this conversion we will assume that "_" means "."
        // (since that's the most common).
        return property.replace('_', '.');
    }
}
