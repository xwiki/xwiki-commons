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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import spoon.SpoonException;
import spoon.processing.Property;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;

/**
 * Perform checks about Component declaration. The following checks are performed:
 * <ul>
 *   <li>Check 1<ul>
 *     <li>A - Verify that if there's at least one {@code @Component} annotation and {@code staticRegistration = true"}
 *       (the default when not specified) then there needs to be a {@code components.txt} file</li>
 *   </ul></li>
 *   <li>Check 2<ul>
 *     <li>A - Verify that Classes annotated with {@code @Component} are defined in {@code components.txt} (unless the
 *       {@code staticRegistration = false"} annotation parameter is specified)</li>
 *     <li>B - Verify that if the {@code staticRegistration = false"} annotation parameter is specified then the
 *       Component must not be declared in {@code components.txt}</li>
 *     <li>C - Verify that components listed in {@code components.txt} are not listed several times</li>
 *     <li>D - Verify that all components listed in {@code components.txt} have been found in the current module.
 *       Incidentally this also checks that any class declared in {@code components.txt} has a {@code @Component}
 *       defined.</li>
 *   </ul></li>
 *   <li>Check 3<ul>
 *     <li>A - Verify that either {@code @Singleton} or {@code @InstantiationStrategy} are used on any class annotated
 *       with {@code @Component}</li>
 *   </ul></li>
 * </ul>
 *
 * @version $Id$
 */
public class ComponentAnnotationProcessor extends AbstractXWikiProcessor<CtClass<?>>
{
    private static final String COMPONENT_ANNOTATION = "org.xwiki.component.annotation.Component";

    private static final String SINGLETON_ANNOTATION = "javax.inject.Singleton";

    private static final String INSTANTIATION_STRATEGY_ANNOTATION =
        "org.xwiki.component.annotation.InstantiationStrategy";

    private Path resolvedComponentsTxtPath;

    private List<String> registeredComponentNames;

    private List<String> assertedComponentNames = new ArrayList<>();

    private String componentsDeclarationLocation;

    @Property
    private String componentsTxtPath;

    @Property
    private String skipForeignDeclarations;

    @Override
    public void process(CtClass<?> ctClass)
    {
        String qualifiedName = ctClass.getQualifiedName();
        boolean hasComponentAnnotation = false;
        boolean isStaticRegistration = true;
        boolean hasInstantiationStrategyAnnotation = false;
        boolean hasSingletonAnnotation = false;
        for (CtAnnotation annotation : SpoonUtils.getAnnotationsIncludingFromSuperclasses(ctClass)) {
            // Is it a Component annotation?
            if (COMPONENT_ANNOTATION.equals(annotation.getAnnotationType().getQualifiedName())) {
                hasComponentAnnotation = true;
                if ("false".equals(annotation.getValue("staticRegistration").toString())) {
                    isStaticRegistration = false;
                }
            } else if (INSTANTIATION_STRATEGY_ANNOTATION.equals(annotation.getAnnotationType().getQualifiedName())) {
                hasInstantiationStrategyAnnotation = true;
            } else if (SINGLETON_ANNOTATION.equals(annotation.getAnnotationType().getQualifiedName())) {
                hasSingletonAnnotation = true;
            }
        }

        if (hasComponentAnnotation) {
            // Parse the components.txt if not already parsed for the current maven module
            if (this.registeredComponentNames == null) {
                this.registeredComponentNames =
                    parseComponentsTxtFile(qualifiedName, ctClass, isStaticRegistration);
            }
            if (!isStaticRegistration) {
                // This is check 2-B
                check2B(qualifiedName);
            } else {
                // This is check 2-A
                check2A(qualifiedName);
            }

            // This is check 3-A
            check3A(qualifiedName, hasInstantiationStrategyAnnotation, hasSingletonAnnotation);

            this.assertedComponentNames.add(qualifiedName);
        }
    }

    @Override
    public void processingDone()
    {
        // Check 2-D
        // Note: We have some legacy modules which merge the components.txt since they use AspectJ to weave a new JAR.
        // We allow skipping the check for foreign declarations for them.
        if (this.skipForeignDeclarations == null || !Boolean.valueOf(this.skipForeignDeclarations)) {
            check2D();
        }

        super.processingDone();
    }

    private void check2D()
    {
        if (this.registeredComponentNames != null) {
            for (String componentName : this.registeredComponentNames) {
                if (!this.assertedComponentNames.contains(componentName)) {
                    registerError(String.format("Component [%s] is declared in [%s] but it's missing a @Component "
                            + "declaration or its source code wasn't found in the current Maven module", componentName,
                        this.resolvedComponentsTxtPath));
                }
            }
        }
    }

    private void check3A(String qualifiedName, boolean hasInstantiationStrategyAnnotation,
        boolean hasSingletonAnnotation)
    {
        if (!hasInstantiationStrategyAnnotation && !hasSingletonAnnotation) {
            registerError(String.format(
                "Component class [%s] must have either the [%s] or the [%s] annotation defined on it.",
                qualifiedName, SINGLETON_ANNOTATION, INSTANTIATION_STRATEGY_ANNOTATION));
        }
    }

    private void check2A(String qualifiedName)
    {
        if (!this.registeredComponentNames.contains(qualifiedName)) {
            registerError(String.format(
                "Component [%s] is not declared in [%s]! Consider adding it or if it is normal use "
                    + "the \"staticRegistration\" parameter as in "
                    + "\"@Component(staticRegistration = false)\"",
                qualifiedName, this.componentsDeclarationLocation));
        }
    }

    private void check2B(String qualifiedName)
    {
        if (this.registeredComponentNames.contains(qualifiedName)) {
            registerError(String.format(
                "Component [%s] is declared in [%s] but it is also declared with a "
                    + "\"staticRegistration\" parameter with a [false] value, e.g. "
                    + "\"@Component(staticRegistration = false\". You need to fix that!",
                qualifiedName, this.componentsDeclarationLocation));
        }
    }

    private List<String> parseComponentsTxtFile(String qualifiedName, CtClass<?> ctClass, boolean isStaticRegistration)
    {
        List<String> results = new ArrayList<>();

        // Get the components.txt file from the current directory, in target/META-INF/components.txt
        this.resolvedComponentsTxtPath = getComponentsTxtPath(ctClass);
        if (Files.exists(this.resolvedComponentsTxtPath)) {
            this.componentsDeclarationLocation = this.resolvedComponentsTxtPath.toString();
            try (Stream<String> stream = Files.lines(this.resolvedComponentsTxtPath)) {
                stream.forEach((line) -> {
                    // Make sure we don't include empty lines
                    if (line.trim().length() > 0) {
                        try {
                            String[] chunks = line.split(":");
                            String componentName;
                            if (chunks.length > 1) {
                                componentName = chunks[1];
                            } else {
                                componentName = chunks[0];
                            }
                            // Check 2-C
                            if (results.contains(componentName)) {
                                registerError(String.format("Component [%s] is registered several times in [%s]",
                                    componentName, this.resolvedComponentsTxtPath.toAbsolutePath()));
                            }
                            results.add(componentName);
                        } catch (Exception e) {
                            throw new SpoonException(String.format(
                                "Invalid format [%s] in [%s]", line, this.resolvedComponentsTxtPath), e);
                        }
                    }
                });
            } catch (IOException e) {
                throw new SpoonException(
                    String.format("Failed to read the [%s] file", this.resolvedComponentsTxtPath), e);
            }
        } else {
            // Check 1-A
            if (isStaticRegistration) {
                throw new SpoonException(String.format(
                    "There is no [%s] file and thus Component [%s] isn't declared! Consider "
                        + "adding a components.txt file or if it is normal use the \"staticRegistration\" parameter as "
                        + "in \"@Component(staticRegistration = false)\"",
                        this.resolvedComponentsTxtPath.toAbsolutePath(), qualifiedName));
            }
        }

        return results;
    }

    private Path getComponentsTxtPath(CtClass<?> ctClass)
    {
        String path = this.componentsTxtPath;
        if (path == null) {
            path = "target/classes/META-INF/components.txt";
        }
        return Paths.get(computeMavenModulePath(ctClass), path);
    }

    private String computeMavenModulePath(CtClass<?> ctClass)
    {
        // Try to find the maven module base directory.

        // Find how may packages there are in the source class FQN (+1 for the parent).
        int levels = StringUtils.countMatches(ctClass.getQualifiedName(), ".") + 1;

        // Go up as many times as there are levels in the source path + 3 levels
        // (for "src/main/java" or "src/test/java").
        File sourcePath = ctClass.getPosition().getFile();
        for (int i = 0; i < levels + 3; i++) {
            sourcePath = sourcePath.getParentFile();
        }

        return sourcePath.getPath();
    }
}
