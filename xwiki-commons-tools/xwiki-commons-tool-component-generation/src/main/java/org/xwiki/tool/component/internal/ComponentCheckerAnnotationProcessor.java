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
package org.xwiki.tool.component.internal;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Annotation Processor that checks if all classes annotated with {@code org.xwiki.component.annotation.Component} are
 * declared in {@code }META-INF/components.txt} files.
 *
 * @version $Id$
 * @since 6.4M1
 */
@SupportedAnnotationTypes("org.xwiki.component.annotation.Component")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ComponentCheckerAnnotationProcessor extends AbstractProcessor
{
    private static final String SINGLETON_CLASS_NAME = "javax.inject.Singleton";

    private static final String INSTANTIATION_STRATEGY_CLASS_NAME =
        "org.xwiki.component.annotation.InstantiationStrategy";

    private List<String> declarations;

    private URI componentsDeclarationLocation;

    private boolean skip;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment)
    {
        super.init(processingEnvironment);

        processingEnvironment.getMessager().printMessage(Diagnostic.Kind.NOTE,
            "Checking validity of components.txt files...");

        try {
            this.declarations = parseComponentsTxtFile(processingEnvironment);
        } catch (FileNotFoundException e) {
            // No components.txt, don't generate an error but continue
            this.declarations = Collections.emptyList();
        } catch (IllegalArgumentException e) {
            // Normally we should generate an error and stop here.
            // However, there's a problem with maven projects using the AspectJ plugin and AspectJ itself.
            // It seems AJC doesn't support a sourcepath as JavaC does. Thus the Annotation Processor will fail
            // with an error like the following when called without any sourcepath set:
            // "java.lang.IllegalArgumentException: Unknown location : SOURCE_PATH"
            // Thus, when this happens we simply stop processing annotations and just raise a WARNING but we don't
            // fail the build...
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "Failed to locate any [META-INF/components.txt]. Could be caused by a compilation with the AspectJ "
                + "compiler. Aborting check.");
            this.skip = true;
        } catch (IOException e) {
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(
                "Failed to parse [META-INF/components.txt]. Reason: [%s]", getThrowableString(e)));
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment environment)
    {
        if (this.skip || annotations.size() == 0) {
            return false;
        }

        // Important: We use reflection to load the Component annotation class since otherwise we would need to
        // depend on the xwiki-commons-component-api module and this would create a dependency cycle in
        // xwiki-commons-core, preventing the build of the Commons reactor project.
        Class<? extends Annotation> componentAnnotationClass =
            loadAnnotationClass("org.xwiki.component.annotation.Component");
        if (componentAnnotationClass == null) {
            return false;
        }

        for (Element element : environment.getElementsAnnotatedWith(componentAnnotationClass)) {
            TypeElement classElement = (TypeElement) element;
            String binaryName = this.processingEnv.getElementUtils().getBinaryName(classElement).toString();
            // Check if the element is present in the declarations and if not raise a warning, unless
            // the Component annotation has staticRegistration set to false.
            boolean isStaticRegistration = isStaticRegistration(classElement, componentAnnotationClass);

            // Check 1:
            // - Verify that Classes annotated with @Component are defined in components.txt (unless the
            //   staticRegistration = false annotation parameter is specified)
            // - Verify that if the staticRegistration = false annotation parameter is specified then the Component
            //   must not be declared in components.txt
            if (!this.declarations.contains(binaryName) && isStaticRegistration) {
                if (this.componentsDeclarationLocation == null) {
                    this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(
                        "There's no [META-INF/components.txt] file and thus Component [%s] isn't declared! Consider "
                        + "adding a components.txt file or if it's normal use the \"staticRegistration\" parameter as "
                        + "in \"@Component(staticRegistration = false)\"", binaryName));
                } else {
                    this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(
                        "Component [%s] isn't declared in [%s]! Consider adding it or if it's normal use "
                            + "the \"staticRegistration\" parameter as in \"@Component(staticRegistration = false)\"",
                        binaryName, this.componentsDeclarationLocation));
                }
            } else if (this.declarations.contains(binaryName) && !isStaticRegistration) {
                this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(
                    "Component [%s] is declared in [%s] but it's also declared with a \"staticRegistration\" "
                    + "parameter with a [false] value, e.g. \"@Component(staticRegistration = false\". "
                    + "You need to fix that!", binaryName, this.componentsDeclarationLocation));
            }

            // Check 2:
            // - Verify that either @Singleton or @InstantiationStrategy are used on any class annotated with @Component
            Class<? extends Annotation> singletonAnnotationClass =
                loadAnnotationClass(SINGLETON_CLASS_NAME);
            Class<? extends Annotation> instantationStrategyAnnotationClass =
                loadAnnotationClass(INSTANTIATION_STRATEGY_CLASS_NAME);
            if (singletonAnnotationClass == null || instantationStrategyAnnotationClass == null) {
                return false;
            }
            Annotation singletonAnnotation = classElement.getAnnotation(singletonAnnotationClass);
            Annotation instantationStrategyAnnotation = classElement.getAnnotation(instantationStrategyAnnotationClass);
            if (singletonAnnotation == null && instantationStrategyAnnotation == null) {
                this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(
                    "Component class [%s] must have either the [%s] or the [%s] annotation defined on it.",
                    binaryName, SINGLETON_CLASS_NAME, INSTANTIATION_STRATEGY_CLASS_NAME));
            }
        }

        // No further processing of this annotation type
        return true;
    }

    private Class<? extends Annotation> loadAnnotationClass(String annotationClassAsString)
    {
        Class<? extends Annotation> annotationClass;
        try {
            annotationClass = getClass().getClassLoader().loadClass(
                annotationClassAsString).asSubclass(Annotation.class);
        } catch (Exception e) {
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, String.format(
                "Failed to load Annotation class [%s]. No check was done on it. Reason: [%s]", annotationClassAsString,
                getThrowableString(e)));
            annotationClass = null;
        }
        return annotationClass;
    }

    private boolean isStaticRegistration(TypeElement classElement, Class<? extends Annotation> componentAnnotationClass)
    {
        Boolean isStaticRegistration = true;
        try {
            Annotation annotation = classElement.getAnnotation(componentAnnotationClass);
            isStaticRegistration =
                (Boolean) componentAnnotationClass.getMethod("staticRegistration").invoke(annotation);
        } catch (Exception e) {
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(
                "Failed to find out if Component annotation is statically registered or not! Reason: [%s]",
                getThrowableString(e)));
        }
        return isStaticRegistration;
    }

    private List<String> parseComponentsTxtFile(ProcessingEnvironment processingEnvironment) throws IOException
    {
        List<String> results = new ArrayList<>();

        // Note: For StandardLocation.SOURCE_PATH to work we need to configure the Maven Compiler Plugin to pass
        // ${basedir}/src/main/resources since it's not currently passed by default, see
        // http://jira.codehaus.org/browse/MCOMPILER-122
        FileObject fo = processingEnvironment.getFiler().getResource(StandardLocation.SOURCE_PATH, "",
            "META-INF/components.txt");

        this.componentsDeclarationLocation = fo.toUri();

        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, String.format("Checking content of [%s]",
            fo.toUri()));

        try (BufferedReader in = new BufferedReader(fo.openReader(true))) {
            // If the previous called succeeded it means the resource exists!
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Make sure we don't include empty lines
                if (inputLine.trim().length() > 0) {
                    try {
                        String[] chunks = inputLine.split(":");
                        if (chunks.length > 1) {
                            results.add(chunks[1]);
                        } else {
                            results.add(chunks[0]);
                        }
                    } catch (Exception e) {
                        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(
                            "Failed to parse [%s] from [%s]. Reason [%s]", inputLine, fo.toUri(),
                            getThrowableString(e)));
                    }
                }
            }
        } catch (Exception e) {
            // If the resource exists and there's an exception, then stop!
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(
                "Failed to read [%s]. Reason: [%s]", fo.toUri(), getThrowableString(e)));
            throw e;
        }

        return results;
    }

    private String getThrowableString(Throwable t)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
