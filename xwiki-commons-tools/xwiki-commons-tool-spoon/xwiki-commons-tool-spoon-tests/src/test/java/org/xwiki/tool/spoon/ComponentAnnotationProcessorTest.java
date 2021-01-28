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

import org.junit.jupiter.api.Test;

import spoon.Launcher;
import spoon.SpoonException;
import spoon.processing.ProcessorProperties;
import spoon.processing.ProcessorPropertiesImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit test for {@link ComponentAnnotationProcessor}.
 *
 * @version $Id$
 */
public class ComponentAnnotationProcessorTest
{
    @Test
    void process()
    {
        Throwable exception = assertThrows(SpoonException.class, () -> run());
        assertThat(exception.getMessage(), matchesPattern("\\QThe following errors were found:\\E\n"
            + "\\Q- Missing final blank line in [\\E.*\\Q]. Last character is [n].\\E\n"
            + "\\Q- Component [org.xwiki.tool.spoon.component.ComponentAnnotationWithOverrideAndDeclared] is "
                + "registered several times in [\\E.*\\Q]\\E\n"
            + "\\Q- Component class [org.xwiki.tool.spoon.component.ComponentAnnotationWithoutSingletonOrInstantation"
                + "StrategyAndDeclared] must have either the [javax.inject.Singleton] or the "
                + "[org.xwiki.component.annotation.InstantiationStrategy] annotation defined on it.\\E\n"
            + "\\Q- Component [org.xwiki.tool.spoon.component.ComponentAnnotationWithoutStaticRegistrationAndDeclared] "
                + "is declared in [\\E.*\\Q] but it is also declared with a \"staticRegistration\" parameter with a "
                + "[false] value, e.g. \"@Component(staticRegistration = false\". You need to fix that!\\E\n"
            + "\\Q- Component class [org.xwiki.tool.spoon.component.ComponentAnnotationWithoutStaticRegistrationAnd"
                + "Declared] must have either the [javax.inject.Singleton] or the "
                + "[org.xwiki.component.annotation.InstantiationStrategy] annotation defined on it.\\E\n"
            + "\\Q- Component [some.component.class.NotInThisModule1] is declared in [\\E.*\\Q] but it's missing a "
                + "@Component declaration or its source code wasn't found in the current Maven module\\E\n"
            + "\\Q- Component [some.component.class.NotInThisModule2] is declared in [\\E.*\\Q] but it's missing a "
                + "@Component declaration or its source code wasn't found in the current Maven module\\E\n"
            + "\\Q- Component [org.xwiki.tool.spoon.component.ComponentDeclaredButMissingComponentAnnotation] is "
                + "declared in [\\E.*\\Q] but it's missing a @Component declaration or its source code wasn't found "
                + "in the current Maven module\\E\n"
        ));
    }

    private void run()
    {
        Launcher launcher = new Launcher();
        launcher.getEnvironment().setNoClasspath(true);
        launcher.setArgs(new String[] {"--output-type", "nooutput" });
        launcher.addInputResource("./src/test/java/org/xwiki/tool/spoon/component/");
        ComponentAnnotationProcessor processor = new ComponentAnnotationProcessor();
        ProcessorProperties properties = new ProcessorPropertiesImpl();
        properties.set("componentsTxtPath", "target/test-classes/META-INF/components.txt");
        properties.set("skipForeignDeclarations", "false");
        processor.initProperties(properties);
        launcher.addProcessor(processor);
        launcher.run();
    }
}
