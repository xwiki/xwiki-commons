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

import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import spoon.processing.Property;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtType;
import spoon.reflect.reference.CtTypeReference;

/**
 * Verify that the Java macros of a Maven module are declared in its {@code xwiki.extension.components} Maven
 * property, which the Extension Manager indexes so that the editor can suggest installing the extension providing an
 * unknown macro. On top of the checks of {@link MacroDeclarationVerifier}, verify that the type hierarchy of every
 * component can be resolved, since a macro cannot be recognized otherwise.
 * <p>
 * A macro component is a non-abstract class annotated with {@code @Component} (with a static registration) which
 * implements {@code org.xwiki.rendering.macro.Macro} and which is not limited by {@code @Component(roles)} to other
 * roles. Its hints come from {@code @Component(hints)}, or from {@code @Named}, or are {@code default}.
 * <p>
 * The checks only run on modules with a {@code jar} packaging: wiki macros of XAR modules are verified by the XAR
 * plugin.
 *
 * @version $Id$
 * @since 18.10.0RC1
 */
public class MacroDeclarationProcessor extends AbstractXWikiProcessor<CtClass<?>>
{
    private static final String COMPONENT_ANNOTATION = "org.xwiki.component.annotation.Component";

    private static final Set<String> NAMED_ANNOTATIONS = Set.of("javax.inject.Named", "jakarta.inject.Named");

    private static final String DEFAULT_HINT = "default";

    private static final String JAR_PACKAGING = "jar";

    /**
     * The macros found in the module, with the class providing them, indexed by hint.
     */
    private final Map<String, String> foundMacros = new LinkedHashMap<>();

    @Property
    private String declaredComponents;

    @Property
    private String packaging;

    @Property
    private String skip;

    @Override
    public boolean isToBeProcessed(CtClass<?> ctClass)
    {
        return isEnabled() && !ctClass.isAbstract();
    }

    @Override
    public void process(CtClass<?> ctClass)
    {
        CtAnnotation<? extends Annotation> componentAnnotation = null;
        CtAnnotation<? extends Annotation> namedAnnotation = null;
        for (CtAnnotation<? extends Annotation> annotation : SpoonUtils.getAnnotationsIncludingFromSuperclasses(
            ctClass)) {
            String annotationName = annotation.getAnnotationType().getQualifiedName();
            if (COMPONENT_ANNOTATION.equals(annotationName)) {
                componentAnnotation = annotation;
            } else if (NAMED_ANNOTATIONS.contains(annotationName)) {
                namedAnnotation = annotation;
            }
        }

        if (componentAnnotation == null || "false".equals(String.valueOf(
            componentAnnotation.getValue("staticRegistration")))) {
            return;
        }

        if (!implementsMacro(ctClass) || !hasMacroRole(componentAnnotation)) {
            return;
        }

        for (String hint : getHints(componentAnnotation, namedAnnotation)) {
            this.foundMacros.put(hint, ctClass.getQualifiedName());
        }
    }

    @Override
    public void processingDone()
    {
        if (isEnabled()) {
            MacroDeclarationVerifier.verify(this.declaredComponents, this.foundMacros).forEach(this::registerError);
        }

        super.processingDone();
    }

    private boolean isEnabled()
    {
        return !Boolean.parseBoolean(this.skip) && (this.packaging == null || JAR_PACKAGING.equals(this.packaging));
    }

    private boolean implementsMacro(CtClass<?> ctClass)
    {
        Deque<CtTypeReference<?>> toVisit = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        toVisit.add(ctClass.getReference());
        while (!toVisit.isEmpty()) {
            CtTypeReference<?> reference = toVisit.pop();
            String qualifiedName = reference.getQualifiedName();
            if (ExtensionComponentsParser.MACRO_ROLE.equals(qualifiedName)) {
                return true;
            }
            if (visited.add(qualifiedName)) {
                CtType<?> type = getTypeDeclaration(reference);
                if (type == null) {
                    registerError(String.format("Failed to resolve the type [%s] in the hierarchy of the component "
                        + "[%s]. It is thus impossible to verify whether it is a macro which must be declared in the "
                        + "[%s] Maven property.", qualifiedName, ctClass.getQualifiedName(),
                        ExtensionComponentsParser.PROPERTY));
                    return false;
                }
                if (type.getSuperclass() != null) {
                    toVisit.add(type.getSuperclass());
                }
                toVisit.addAll(type.getSuperInterfaces());
            }
        }
        return false;
    }

    private CtType<?> getTypeDeclaration(CtTypeReference<?> reference)
    {
        try {
            return reference.getTypeDeclaration();
        } catch (RuntimeException e) {
            // Spoon fails when the type is neither in the sources nor in the classpath.
            return null;
        }
    }

    private boolean hasMacroRole(CtAnnotation<? extends Annotation> componentAnnotation)
    {
        List<String> roles = new ArrayList<>();
        for (Object role : toList(componentAnnotation.getValueAsObject("roles"))) {
            // Class values are evaluated into type references, or into classes when they come from the classpath.
            roles.add(role instanceof Class<?> roleClass ? roleClass.getName() : role.toString());
        }
        return roles.isEmpty() || roles.stream().anyMatch(ExtensionComponentsParser.MACRO_ROLE::equals);
    }

    private List<String> getHints(CtAnnotation<? extends Annotation> componentAnnotation,
        CtAnnotation<? extends Annotation> namedAnnotation)
    {
        List<String> hints = new ArrayList<>();
        for (Object hint : toList(componentAnnotation.getValueAsObject("hints"))) {
            hints.add(hint.toString());
        }
        if (hints.isEmpty() && namedAnnotation != null) {
            hints.add(String.valueOf(namedAnnotation.getValueAsObject("value")));
        }
        if (hints.isEmpty()) {
            hints.add(DEFAULT_HINT);
        }
        return hints;
    }

    private List<Object> toList(Object value)
    {
        List<Object> list = new ArrayList<>();
        if (value instanceof Object[] array) {
            Collections.addAll(list, array);
        } else if (value != null) {
            list.add(value);
        }
        return list;
    }
}
