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
import org.xwiki.configuration.ConfigurationSource;

/**
 * Environment variables based configuration source.
 * <p>
 * Environment variable are expected to be prefixed with {@code XCONF_} to be taken into account by this
 * {@link ConfigurationSource}. Also, to support all systems, we cannot expect environment variable to allow anything
 * else than {@code [a-zA-Z_]+[a-zA-Z0-9_]*} so we apply the following encoding:
 * <ul>
 * <li>{@code .}, {@code :} and {@code -} are converted to {@code _}</li>
 * <li>{@code _} remains {@code _}</li>
 * <li>any other forbidden character is converted to {@code _<UTF8, URL style, code>}</li>
 * </ul>
 * <p>
 * For example the {@link ConfigurationSource} property key "configuration.1Key@" will lead the the environment variable
 * "XCONF_configuration__31Key_40".
 *
 * @version $Id$
 * @since 17.4.0RC1
 */
@Component
@Singleton
@Named(SystemEnvConfigurationSource.HINT)
public class SystemEnvConfigurationSource extends AbstractPropertiesConfigurationSource
{
    /**
     * The hint to use to get this configuration source.
     */
    public static final String HINT = "systemenv";

    /**
     * The prefix used to identify env variables used to overwrite the configuration.
     */
    public static final String PREFIX = "XCONF_";

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
    public <T> T getPropertyInternal(String key)
    {
        if (key == null) {
            return null;
        }

        return (T) getenv(toEnvKey(key));
    }

    @Override
    public List<String> getKeysInternal()
    {
        return getKeys("");
    }

    @Override
    public List<String> getKeysInternal(String prefix)
    {
        String encodedPrefix = encode(prefix);
        String encodedCompletePrefix = PREFIX + encodedPrefix;

        return getenv().keySet().stream().filter(k -> isEnvKey(k, encodedCompletePrefix))
            .map(k -> fromEnvKey(k, prefix, encodedPrefix)).toList();
    }

    @Override
    public boolean containsKeyInternal(String key)
    {
        if (key == null) {
            return false;
        }

        return getenv().containsKey(toEnvKey(key));
    }

    @Override
    public boolean isEmptyInternal()
    {
        return isEmpty("");
    }

    @Override
    public boolean isEmptyInternal(String prefix)
    {
        Map<String, String> env = getenv();

        if (env.isEmpty()) {
            return true;
        }

        String encodedCompletePrefix = toEnvKey(prefix);

        return env.keySet().stream().noneMatch(k -> isEnvKey(k, encodedCompletePrefix));
    }

    private String encode(String property)
    {
        if (property.isEmpty()) {
            return property;
        }

        StringBuilder builder = new StringBuilder(property.length() * 3);

        encode(property, builder);

        return builder.toString();
    }

    private void encode(String property, StringBuilder builder)
    {
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
    }

    private String toEnvKey(String property)
    {
        if (property.isEmpty()) {
            return PREFIX;
        }

        StringBuilder builder = new StringBuilder(property.length() * 3);

        builder.append(PREFIX);

        encode(property, builder);

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
                // For anything else, use URL encoding (but without the "%")
                byte[] ba = String.valueOf(c).getBytes(StandardCharsets.UTF_8);

                for (int j = 0; j < ba.length; j++) {
                    urlEncode(c, builder);
                }
        }
    }

    private void urlEncode(char c, StringBuilder builder)
    {
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

    private boolean isEnvKey(String key, String encodedCompletePrefix)
    {
        return key.startsWith(encodedCompletePrefix);
    }

    private String fromEnvKey(String key, String prefix, String encodedPrefix)
    {
        String property = key.substring(PREFIX.length() + encodedPrefix.length());

        // It's not really possible to fully accurately convert from env to property key but we are doing our best based
        // on the most common use cases (the separator that leaded to have a "_" is generally ".").
        property = property.replace('_', '.');

        return prefix + property;
    }
}
