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

import org.xwiki.tool.spoon.ExtensionComponentsParser.Component;

/**
 * Compares the macros provided by a Maven module with the macros declared in its {@code xwiki.extension.components}
 * Maven property. The following checks are performed:
 * <ul>
 *   <li>A - Verify that every macro provided by the module is declared</li>
 *   <li>B - Verify that every declared macro is provided by the module</li>
 *   <li>C - Verify that the declared macros use the exact role the extension index searches for (no generics)</li>
 * </ul>
 * Used both for Java macros (by {@link MacroDeclarationProcessor}) and for wiki macros (by the XAR plugin). This class
 * has no Spoon dependency so that other build tools can reuse it.
 *
 * @version $Id$
 * @since 18.10.0RC1
 */
public final class MacroDeclarationVerifier
{
    private MacroDeclarationVerifier()
    {
        // Utility class
    }

    /**
     * @param declaredComponents the value of the {@code xwiki.extension.components} Maven property, can be
     *     {@code null}
     * @param providedMacros the macros provided by the module, as a map from the macro hint to a description of what
     *     provides it (e.g. a class name or a page reference)
     * @return the errors found, empty when the declaration matches the provided macros
     */
    public static List<String> verify(String declaredComponents, Map<String, String> providedMacros)
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

        // This is check A
        providedMacros.forEach((hint, provider) -> {
            if (!declaredHints.contains(hint)) {
                errors.add(String.format("Macro [%s] provided by [%s] is not declared in the [%s] Maven property. "
                    + "Add [%s] to it.", hint, provider, ExtensionComponentsParser.PROPERTY,
                    ExtensionComponentsParser.toMacroDeclaration(hint)));
            }
        });

        // This is check B
        for (String hint : declaredHints) {
            if (!providedMacros.containsKey(hint)) {
                errors.add(String.format("Macro [%s] is declared in the [%s] Maven property but no macro with this "
                    + "hint was found in the current Maven module. Remove it.",
                    ExtensionComponentsParser.toMacroDeclaration(hint), ExtensionComponentsParser.PROPERTY));
            }
        }

        return errors;
    }
}
