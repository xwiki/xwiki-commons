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
package org.xwiki.tool.checkstyle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Verify that all classes annotated with {@code org.xwiki.component.annotation.Component} are in sync with the
 * {@code META-INF/components.txt} file.
 *
 * @version $Id$
 * @since 8.1M1
 */
public class ComponentAnnotationCheck extends AbstractCheck
{
    private static final String COMPONENTS_TXT_LOCATION = "META-INF/components.txt";

    private static final String COMPONENT_CLASS_NAME = "org.xwiki.component.annotation.Component";

    private static final String SINGLETON_CLASS_NAME = "javax.inject.Singleton";

    private static final String INSTANTIATION_STRATEGY_CLASS_NAME =
        "org.xwiki.component.annotation.InstantiationStrategy";

    private String packageName;

    private String className;

    private List<String> registeredComponentNames;

    private URL componentsDeclarationLocation;

    private Class<? extends Annotation> componentAnnotationClass;

    private Class<? extends Annotation> singletonAnnotationClass;

    private Class<? extends Annotation> instantiationStrategyAnnotationClass;

    @Override
    public int[] getDefaultTokens()
    {
        return new int[]{
            TokenTypes.PACKAGE_DEF, TokenTypes.CLASS_DEF
        };
    }

    @Override
    public int[] getAcceptableTokens()
    {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens()
    {
        return getDefaultTokens();
    }

    @Override
    public void init()
    {
        super.init();

        // Important: We use reflection to load the annotation classes since otherwise we would need to
        // depend on the xwiki-commons-component-api module and this would create a dependency cycle in
        // xwiki-commons-core, preventing the build of the Commons reactor project.
        this.componentAnnotationClass = loadAnnotationClass(COMPONENT_CLASS_NAME);
        this.instantiationStrategyAnnotationClass = loadAnnotationClass(INSTANTIATION_STRATEGY_CLASS_NAME);
        this.singletonAnnotationClass = loadAnnotationClass(SINGLETON_CLASS_NAME);
    }

    @Override
    public void visitToken(DetailAST ast)
    {
        if (this.componentAnnotationClass == null || this.instantiationStrategyAnnotationClass == null
            || this.singletonAnnotationClass == null)
        {
            return;
        }

        switch (ast.getType()) {
            case TokenTypes.PACKAGE_DEF:
                // Save the package
                FullIdent ident = FullIdent.createFullIdent(ast.getLastChild().getPreviousSibling());
                this.packageName = ident.getText();
                return;
            case TokenTypes.CLASS_DEF:
                // Only handle root classes (and not nested classes). This would be more complex to handle and we do not
                // put nested components in general
                if (ast.getParent() == null) {
                    this.className = ast.findFirstToken(TokenTypes.IDENT).getText();
                } else {
                    return;
                }
        }

        // Check 1:
        // A - Verify that if there's at least one @Component annotation and "staticRegistration = true" then there
        //     needs to be a components.txt file
        // Check 2:
        // A- Verify that Classes annotated with @Component are defined in components.txt (unless the
        //   "staticRegistration = false" annotation parameter is specified)
        // B- Verify that if the "staticRegistration = false" annotation parameter is specified then the Component
        //   must not be declared in components.txt
        // Check 3:
        // A- Verify that either @Singleton or @InstantiationStrategy are used on any class annotated with @Component

        Class<?> componentClass;
        try {
            componentClass = Thread.currentThread().getContextClassLoader().loadClass(getFullClassName());
        } catch (ClassNotFoundException e) {
            log(ast.getLineNo(), ast.getColumnNo(), String.format(
                "Failed to load class in package [%s]: [%s]", this.packageName, getThrowableString(e)));
            return;
        }

        Annotation componentAnnotation = componentClass.getAnnotation(this.componentAnnotationClass);
        if (componentAnnotation != null) {
            // Parse the components.txt if not already parsed for the current maven module
            if (this.registeredComponentNames == null) {
                this.registeredComponentNames = parseComponentsTxtFile(ast);
            }
            if (!isStaticRegistration(componentAnnotation)) {
                // This is check 2-B
                if (this.registeredComponentNames.contains(getFullClassName())) {
                    log(ast.getLineNo(), ast.getColumnNo(), String.format(
                        "Component [%s] is declared in [%s] but it is also declared with a "
                            + "\"staticRegistration\" parameter with a [false] value, e.g. "
                            + "\"@Component(staticRegistration = false\". You need to fix that!",
                        getFullClassName(), this.componentsDeclarationLocation));
                }
            } else {
                // This is check 2-A
                if (!this.registeredComponentNames.contains(getFullClassName())) {
                    log(ast.getLineNo(), ast.getColumnNo(), String.format(
                        "Component [%s] is not declared in [%s]! Consider adding it or if it is normal use "
                            + "the \"staticRegistration\" parameter as in "
                            + "\"@Component(staticRegistration = false)\"",
                        getFullClassName(), this.componentsDeclarationLocation));
                }
            }

            // This is check 3-A
            Annotation instantiationStrategyAnnotation =
                componentClass.getAnnotation(this.instantiationStrategyAnnotationClass);
            Annotation singletonAnnotation = componentClass.getAnnotation(this.singletonAnnotationClass);
            if (instantiationStrategyAnnotation == null && singletonAnnotation == null) {
                log(ast.getLineNo(), ast.getColumnNo(), String.format(
                    "Component class [%s] must have either the [%s] or the [%s] annotation defined on it.",
                    getFullClassName(), SINGLETON_CLASS_NAME, INSTANTIATION_STRATEGY_CLASS_NAME));
            }
        }
    }

    private boolean isStaticRegistration(Annotation componentAnnotation)
    {
        boolean isStaticRegistration = true;
        try {
            isStaticRegistration =
                (Boolean) componentAnnotation.getClass().getMethod("staticRegistration").invoke(componentAnnotation);
        } catch (Exception e) {
            log(1, 1, String.format("Failed to find out if Component annotation is statically registered or not! "
                + "Reason: [%s]", getThrowableString(e)));
        }
        return isStaticRegistration;
    }

    private List<String> parseComponentsTxtFile(DetailAST ast)
    {
        List<String> results = new ArrayList<>();

        try {
            Enumeration<URL> urls =
                Thread.currentThread().getContextClassLoader().getResources(COMPONENTS_TXT_LOCATION);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                // We find the right components.txt by checking that the URL is using a "file" scheme (maven points
                // to the target directory). For dependencies the URL scheme will be "jar".
                if (url.getProtocol().equals("file")) {
                    this.componentsDeclarationLocation = url;
                    break;
                }
            }
        } catch (Exception e) {
            log(1, 1, String.format("Failed to locate [%s]. Error [%s]", COMPONENTS_TXT_LOCATION,
                getThrowableString(e)));
            return Collections.emptyList();
        }

        try (BufferedReader in = new BufferedReader(
            new InputStreamReader(this.componentsDeclarationLocation.openStream())))
        {
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
                        log(ast.getLineNo(), ast.getColumnNo(), String.format(
                            "Invalid format [%s] in [%s]", inputLine, this.componentsDeclarationLocation));
                    }
                }
            }
        } catch (Exception e) {
            // Since this current method is called only if there's at least one @Component annotation with static
            // registration, report an error if the components.txt file cannot be found
            // Ths is check 1-A
            log(ast.getLineNo(), ast.getColumnNo(), String.format(
                "There is no [%s] file and thus Component [%s] isn't declared! Consider "
                    + "adding a components.txt file or if it is normal use the \"staticRegistration\" parameter as "
                    + "in \"@Component(staticRegistration = false)\"", this.componentsDeclarationLocation,
                getFullClassName()));
        }

        return results;
    }

    private String getFullClassName()
    {
        return String.format("%s.%s", this.packageName, this.className);
    }

    private String getThrowableString(Throwable t)
    {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    private Class<? extends Annotation> loadAnnotationClass(String annotationClassString)
    {
        Class<? extends Annotation> annotationClass;
        try {
            annotationClass = Thread.currentThread().getContextClassLoader().loadClass(annotationClassString)
                .asSubclass(Annotation.class);
        } catch (Exception e) {
            // This means that we're in a module that doesn't have a dependency on xwiki-commons-component-api (where
            // Component and InstantiationStrategy annotations are located) and thus this module cannot define
            // components... So we just ignore those modules.
            annotationClass = null;
        }
        return annotationClass;
    }
}
