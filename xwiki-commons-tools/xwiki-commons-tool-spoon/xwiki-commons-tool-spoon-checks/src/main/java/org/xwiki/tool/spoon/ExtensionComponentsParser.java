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
package org.xwiki.tool.spoon;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Parses the {@code xwiki.extension.components} Maven property the same way the Extension Manager does when it reads
 * an extension's POM. The Extension Manager code lives in {@code xwiki-commons-extension-api}, which cannot be used
 * here: every core module runs the Spoon checks, so depending on a core module would create a reactor cycle. The
 * rules mirrored are those of {@code ExtensionUtils#importPropertyStringList}, {@code ExtensionConverterParser} and
 * {@code DefaultExtensionComponent}.
 * <p>
 * This class has no Spoon dependency so that other build tools (e.g. the XAR plugin) can reuse it.
 *
 * @version $Id$
 * @since 18.10.0RC1
 */
public final class ExtensionComponentsParser
{
    /**
     * The name of the Maven property holding the components provided by an extension.
     */
    public static final String PROPERTY = "xwiki.extension.components";

    /**
     * The component role of rendering macros, as the extension index expects it.
     */
    public static final String MACRO_ROLE = "org.xwiki.rendering.macro.Macro";

    private static final String DEFAULT_HINT = "default";

    private static final int[] DELIMITERS = {'|', '\t', '\n', '\r'};

    private static final Pattern WHITE_SPACES = Pattern.compile("\\s");

    private static final char SEPARATOR = '/';

    private static final char ESCAPE = '\\';

    /**
     * A component declared in the {@code xwiki.extension.components} Maven property.
     *
     * @param roleType the canonical role type (white spaces removed)
     * @param roleHint the role hint, {@code default} when empty
     */
    public record Component(String roleType, String roleHint)
    {
        @Override
        public String toString()
        {
            return this.roleType + SEPARATOR + this.roleHint;
        }
    }

    private ExtensionComponentsParser()
    {
        // Utility class
    }

    /**
     * @param value the value of the {@code xwiki.extension.components} Maven property, can be {@code null}
     * @return the declared components, in declaration order
     */
    public static List<Component> parse(String value)
    {
        List<Component> components = new ArrayList<>();
        if (value != null) {
            for (String token : tokenize(value.trim())) {
                components.add(parseComponent(token));
            }
        }
        return components;
    }

    /**
     * @param roleHint the hint of a macro
     * @return the line to add to the {@code xwiki.extension.components} Maven property to declare the macro
     */
    public static String toMacroDeclaration(String roleHint)
    {
        return new Component(MACRO_ROLE, toCanonicalHint(roleHint)).toString();
    }

    private static List<String> tokenize(String value)
    {
        StreamTokenizer tokenizer = new StreamTokenizer(new StringReader(value));
        tokenizer.ordinaryChars(0, 255);
        tokenizer.wordChars(0, 255);
        tokenizer.quoteChar('"');
        tokenizer.quoteChar('\'');
        for (int delimiter : DELIMITERS) {
            tokenizer.whitespaceChars(delimiter, delimiter);
        }

        List<String> tokens = new ArrayList<>();
        try {
            while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                if (tokenizer.sval != null) {
                    tokens.add(tokenizer.sval);
                }
            }
        } catch (IOException e) {
            // Cannot happen when reading from a String.
            throw new IllegalStateException(e);
        }
        return tokens;
    }

    private static Component parseComponent(String token)
    {
        // The role type ends at the first unescaped separator. The hint is the rest, taken as is.
        StringBuilder roleType = new StringBuilder();
        int index = 0;
        while (index < token.length() && token.charAt(index) != SEPARATOR) {
            if (token.charAt(index) == ESCAPE) {
                index++;
            }
            if (index < token.length()) {
                roleType.append(token.charAt(index));
            }
            index++;
        }
        String roleHint = index < token.length() ? token.substring(index + 1) : null;

        return new Component(WHITE_SPACES.matcher(roleType).replaceAll(""), toCanonicalHint(roleHint));
    }

    private static String toCanonicalHint(String roleHint)
    {
        return roleHint == null || roleHint.isEmpty() ? DEFAULT_HINT : roleHint;
    }
}
