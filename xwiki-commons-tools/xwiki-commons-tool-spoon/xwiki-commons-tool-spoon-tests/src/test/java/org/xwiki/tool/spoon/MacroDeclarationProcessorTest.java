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

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import spoon.Launcher;
import spoon.SpoonException;
import spoon.processing.ProcessorProperties;
import spoon.processing.ProcessorPropertiesImpl;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link MacroDeclarationProcessor}.
 *
 * @version $Id$
 */
class MacroDeclarationProcessorTest
{
    private static final String MACROS = "./src/test/java/org/xwiki/tool/spoon/macro/";

    private static final String MACRO_ROLE = "./src/test/java/org/xwiki/rendering/macro/";

    private static final String ALL_DECLARED = """
        org.xwiki.rendering.macro.Macro/named
        org.xwiki.rendering.macro.Macro/constant|org.xwiki.rendering.macro.Macro/hint1
        org.xwiki.rendering.macro.Macro/hint2
        org.xwiki.rendering.macro.Macro
        org.xwiki.rendering.macro.Macro/explicitRole
        org.xwiki.component.phase.Initializable/other
        """;

    @Test
    void processWithAllMacrosDeclared()
    {
        assertDoesNotThrow(() -> run(ALL_DECLARED, "", "jar", "false", MACROS, MACRO_ROLE));
    }

    @Test
    void processWithMissingStaleAndGenericDeclarations()
    {
        String declared = """
            org.xwiki.rendering.macro.Macro/named
            org.xwiki.rendering.macro.Macro/constant|org.xwiki.rendering.macro.Macro/hint1
            org.xwiki.rendering.macro.Macro/
            org.xwiki.rendering.macro.Macro<?>/explicitRole
            org.xwiki.rendering.macro.Macro/removed
            """;

        Throwable exception =
            assertThrows(SpoonException.class, () -> run(declared, null, "jar", "false", MACROS, MACRO_ROLE));

        assertEquals(Set.of(
            "The following errors were found:",
            "- Component [org.xwiki.rendering.macro.Macro<?>/explicitRole] is declared in the "
                + "[xwiki.extension.components] Maven property with a generic role, which the extension index does "
                + "not find. Declare it as [org.xwiki.rendering.macro.Macro/explicitRole] instead.",
            "- Macro [hint2] provided by [org.xwiki.tool.spoon.macro.SeveralHintsMacro] is not declared in the "
                + "[xwiki.extension.components] Maven property. Add [org.xwiki.rendering.macro.Macro/hint2] to it, or "
                + "add [hint2] to the [xwiki.extension.components.check.excludedMacros] Maven property if it is an "
                + "internal macro.",
            "- Macro [explicitRole] provided by [org.xwiki.tool.spoon.macro.ExplicitRoleMacro] is not declared in "
                + "the [xwiki.extension.components] Maven property. Add "
                + "[org.xwiki.rendering.macro.Macro/explicitRole] to it, or add [explicitRole] to the "
                + "[xwiki.extension.components.check.excludedMacros] Maven property if it is an internal macro.",
            "- Macro [org.xwiki.rendering.macro.Macro/removed] is declared in the [xwiki.extension.components] Maven "
                + "property but no macro with this hint was found in the current Maven module. Remove it."),
            Set.copyOf(List.of(exception.getMessage().split("\n"))));
    }

    @Test
    void processWithExcludedMacros()
    {
        String declared = """
            org.xwiki.rendering.macro.Macro/named
            org.xwiki.rendering.macro.Macro/constant|org.xwiki.rendering.macro.Macro/hint1
            org.xwiki.rendering.macro.Macro
            """;

        assertDoesNotThrow(() -> run(declared, """
            hint2,
            explicitRole
            """, "jar", "false", MACROS, MACRO_ROLE));
    }

    @Test
    void processWithExcludedMacrosDeclaredOrNotProvided()
    {
        Throwable exception = assertThrows(SpoonException.class,
            () -> run(ALL_DECLARED, "named, missing", "jar", "false", MACROS, MACRO_ROLE));

        assertEquals("""
            The following errors were found:
            - Macro [named] is both declared in the [xwiki.extension.components] Maven property and excluded in the \
            [xwiki.extension.components.check.excludedMacros] Maven property. Remove it from one of them.
            - Macro [missing] is excluded in the [xwiki.extension.components.check.excludedMacros] Maven property but \
            no macro with this hint was found in the current Maven module. Remove it.
            """, exception.getMessage());
    }

    @Test
    void processWithUnresolvedHierarchy()
    {
        Throwable exception = assertThrows(SpoonException.class,
            () -> run("", "", "jar", "false", "./src/test/resources/macro/unresolved/"));

        assertEquals("""
            The following errors were found:
            - Failed to resolve the type [some.missing.AbstractMissingMacro] in the hierarchy of the component \
            [org.xwiki.tool.spoon.macro.unresolved.UnresolvedHierarchyComponent]. It is thus impossible to verify \
            whether it is a macro which must be declared in the [xwiki.extension.components] Maven property.
            """, exception.getMessage());
    }

    @Test
    void processWhenSkipped()
    {
        assertDoesNotThrow(() -> run("", "", "jar", "true", MACROS, MACRO_ROLE));
    }

    @Test
    void processWhenNotAJar()
    {
        assertDoesNotThrow(() -> run("", "", "xar", "false", MACROS, MACRO_ROLE));
    }

    private void run(String declaredComponents, String excludedMacros, String packaging, String skip,
        String... inputs)
    {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.setArgs(new String[] {"--output-type", "nooutput" });
        for (String input : inputs) {
            launcher.addInputResource(input);
        }
        MacroDeclarationProcessor processor = new MacroDeclarationProcessor();
        ProcessorProperties properties = new ProcessorPropertiesImpl();
        properties.set("declaredComponents", declaredComponents);
        properties.set("excludedMacros", excludedMacros);
        properties.set("packaging", packaging);
        properties.set("skip", skip);
        processor.initProperties(properties);
        launcher.addProcessor(processor);
        launcher.run();
    }
}
