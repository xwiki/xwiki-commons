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
package org.xwiki.tool.component;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Annotation Processor that generates the META-INF/components.txt file by adding FQN of classes annotated with
 * {@code org.xwiki.component.annotation.Component}.
 *
 * @version $Id$
 * @since 6.4M1
 */
@SupportedAnnotationTypes("org.xwiki.component.annotation.Component")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class ComponentAnnotationProcessor extends AbstractProcessor
{
    private BufferedWriter writer;

    private boolean skip;

    private URI targetResourceURI;

    private boolean isFileOpenForWriting;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment)
    {
        super.init(processingEnvironment);

        // Check if a META-INF/components.txt file already exists
        if (!shouldExecute(processingEnvironment)) {
            this.skip = true;
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
        Class<? extends Annotation> componentClass;
        try {
            componentClass = getClass().getClassLoader().loadClass(
                "org.xwiki.component.annotation.Component").asSubclass(Annotation.class);
        } catch (Exception e) {
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, String.format(
                "Failed to load Component Annotation class. Not components.txt file was generated. Reason: [%s]",
                getThrowableString(e)));
            return false;
        }

        try {

            for (Element element : environment.getElementsAnnotatedWith(componentClass)) {
                if (element.getKind() == ElementKind.CLASS) {
                    if (!this.isFileOpenForWriting) {
                        FileObject fo = this.processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "",
                            "META-INF/components.txt");
                        this.targetResourceURI = fo.toUri();
                        this.writer = new BufferedWriter(fo.openWriter());
                        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, String.format(
                            "Start generation of [%s]", this.targetResourceURI));
                        this.isFileOpenForWriting = true;
                    }
                    TypeElement classElement = (TypeElement) element;
                    this.writer.append(classElement.getQualifiedName());
                    this.writer.append("\n");
                    this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, String.format(
                        "Adding component [%s]", classElement.getQualifiedName()));
                }
            }

            // Note that this process() method is going to be called again but only if we have generated some resource.
            if (environment.processingOver() && this.writer != null) {
                this.writer.close();
                this.processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, String.format(
                    "End generation of [%s]", this.targetResourceURI));
            }
        } catch (Exception e) {
            handleException(e);
        }

        // No further processing of this annotation type
        return true;
    }

    private void handleException(Throwable t)
    {
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, String.format(
            "Failure to generate components.txt file. Reason: [%s]", getThrowableString(t)));
    }

    private String getThrowableString(Throwable t)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    private boolean shouldExecute(ProcessingEnvironment processingEnvironment)
    {
        boolean shouldExecute;
        try {
            // Note: For StandardLocation.SOURCE_PATH to work we need to configure the Maven Compiler Plugin to pass
            // ${basedir}/src/main/resources since it's not currently passed by default, see
            // http://jira.codehaus.org/browse/MCOMPILER-122
            FileObject existing = processingEnvironment.getFiler().getResource(StandardLocation.SOURCE_PATH, "",
                "META-INF/components.txt");
            existing.getCharContent(true);
            this.processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "File META-INF/components.txt already exists! Consider removing it to auto-generate it!");
            shouldExecute = false;
        } catch (Exception e) {
            // File doesn't exist, we're good!
            shouldExecute = true;
        }
        return shouldExecute;
    }
}
