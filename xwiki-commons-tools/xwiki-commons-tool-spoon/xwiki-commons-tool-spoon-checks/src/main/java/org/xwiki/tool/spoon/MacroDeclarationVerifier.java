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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.xwiki.tool.spoon.ExtensionComponentsParser.Component;

/**
 * Compares the macros provided by a Maven module with the macros declared in its {@code xwiki.extension.components}
 * Maven property. The following checks are performed:
 * <ul>
 *   <li>A - Verify that every macro provided by the module is declared, unless it is excluded</li>
 *   <li>B - Verify that every declared macro is provided by the module</li>
 *   <li>C - Verify that the declared macros use the exact role the extension index searches for (no generics)</li>
 *   <li>D - Verify that no excluded macro is declared</li>
 *   <li>E - Verify that every excluded macro is provided by the module</li>
 * </ul>
 * The excluded macros are listed in the {@code xwiki.extension.components.check.excludedMacros} Maven property. They
 * are the internal macros that must not be indexed: the macro selector of the editor lists the macros of the indexed
 * extensions which are not installed without filtering them by category, so it would suggest them to the users.
 * <p>
 * Used both for Java macros (by {@link MacroDeclarationProcessor}) and for wiki macros (by the XAR plugin). This class
 * has no Spoon dependency so that other build tools can reuse it.
 *
 * @version $Id$
 * @since 18.10.0RC1
 */
public final class MacroDeclarationVerifier
{
    /**
     * The name of the Maven property listing the hints of the macros which must not be declared, separated by commas or
     * white spaces.
     */
    public static final String EXCLUDED_MACROS_PROPERTY = "xwiki.extension.components.check.excludedMacros";

    private static final String NOT_PROVIDED =
        "no macro with this hint was found in the current Maven module. Remove it.";

    private static final Pattern HINT_SEPARATORS = Pattern.compile("[,\\s]+");

    private MacroDeclarationVerifier()
    {
        // Utility class
    }

    /**
     * @param declaredComponents the value of the {@code xwiki.extension.components} Maven property, can be
     *     {@code null}
     * @param excludedMacros the value of the {@code xwiki.extension.components.check.excludedMacros} Maven property,
     *     can be {@code null}
     * @param providedMacros the macros provided by the module, as a map from the macro hint to a description of what
     *     provides it (e.g. a class name or a page reference)
     * @return the errors found, empty when the declaration matches the provided macros
     */
    public static List<String> verify(String declaredComponents, String excludedMacros,
        Map<String, String> providedMacros)
    {
        List<String> errors = new ArrayList<>();

        Set<String> declaredHints = new LinkedHashSet<>();
        for (Component component : ExtensionComponentsParser.parse(declaredComponents)) {
            if (ExtensionComponentsParser.MACRO_ROLE.equals(component.roleType())) {
                declaredHints.add(component.roleHint());
            } else if (component.roleType().startsWith(ExtensionComponentsParser.MACRO_ROLE + '<')) {
                // This is check C
                errors.add(String.format("Component [%s] is declared in the [%s] Maven property with a generic role, "
                    + "which the extension index does not find. Declare it as [%s] instead.", component,
                    ExtensionComponentsParser.PROPERTY,
                    ExtensionComponentsParser.toMacroDeclaration(component.roleHint())));
            }
        }

        Set<String> excludedHints = parseHints(excludedMacros);
        verifyProvidedMacros(providedMacros, declaredHints, excludedHints, errors);
        verifyDeclaredMacros(providedMacros, declaredHints, errors);
        verifyExcludedMacros(providedMacros, declaredHints, excludedHints, errors);

        return errors;
    }

    private static void verifyProvidedMacros(Map<String, String> providedMacros, Set<String> declaredHints,
        Set<String> excludedHints, List<String> errors)
    {
        // This is check A
        providedMacros.forEach((hint, provider) -> {
            if (!declaredHints.contains(hint) && !excludedHints.contains(hint)) {
                errors.add(String.format("Macro [%s] provided by [%s] is not declared in the [%s] Maven property. "
                    + "Add [%s] to it, or add [%s] to the [%s] Maven property if it is an internal macro.", hint,
                    provider, ExtensionComponentsParser.PROPERTY, ExtensionComponentsParser.toMacroDeclaration(hint),
                    hint, EXCLUDED_MACROS_PROPERTY));
            }
        });
    }

    private static void verifyDeclaredMacros(Map<String, String> providedMacros, Set<String> declaredHints,
        List<String> errors)
    {
        // This is check B
        for (String hint : declaredHints) {
            if (!providedMacros.containsKey(hint)) {
                errors.add(String.format("Macro [%s] is declared in the [%s] Maven property but %s",
                    ExtensionComponentsParser.toMacroDeclaration(hint), ExtensionComponentsParser.PROPERTY,
                    NOT_PROVIDED));
            }
        }
    }

    private static void verifyExcludedMacros(Map<String, String> providedMacros, Set<String> declaredHints,
        Set<String> excludedHints, List<String> errors)
    {
        for (String hint : excludedHints) {
            // This is check D
            if (declaredHints.contains(hint)) {
                errors.add(String.format("Macro [%s] is both declared in the [%s] Maven property and excluded in the "
                    + "[%s] Maven property. Remove it from one of them.", hint, ExtensionComponentsParser.PROPERTY,
                    EXCLUDED_MACROS_PROPERTY));
            }
            // This is check E
            if (!providedMacros.containsKey(hint)) {
                errors.add(String.format("Macro [%s] is excluded in the [%s] Maven property but %s", hint,
                    EXCLUDED_MACROS_PROPERTY, NOT_PROVIDED));
            }
        }
    }

    private static Set<String> parseHints(String value)
    {
        Set<String> hints = new LinkedHashSet<>();
        if (value != null) {
            for (String hint : HINT_SEPARATORS.split(value)) {
                if (!hint.isEmpty()) {
                    hints.add(hint);
                }
            }
        }
        return hints;
    }
}
