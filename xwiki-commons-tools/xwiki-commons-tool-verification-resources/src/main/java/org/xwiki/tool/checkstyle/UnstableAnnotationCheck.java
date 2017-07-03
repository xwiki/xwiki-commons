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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtility;

/**
 * Verify if Unstable annotations should be removed.
 *
 * @version $Id$
 * @since 7.0M1
 */
public class UnstableAnnotationCheck extends AbstractCheck
{
    private String packageName;

    private String classOrInterfaceName;

    private String currentVersion;

    private int currentVersionMajor;

    @Override
    public int[] getDefaultTokens()
    {
        return new int[]{
            TokenTypes.PACKAGE_DEF, TokenTypes.INTERFACE_DEF, TokenTypes.CLASS_DEF, TokenTypes.METHOD_DEF,
        };
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return getDefaultTokens();
    }

    public void setCurrentVersion(String currentVersion) throws CheckstyleException
    {
        if (currentVersion != null && !currentVersion.isEmpty()) {
            this.currentVersion = currentVersion;
            this.currentVersionMajor = extractMajor(currentVersion);
            if (this.currentVersionMajor == -1) {
                throw new CheckstyleException("The passed version [" + this.currentVersionMajor
                    + "] must be of the type Major.* (e.g. 7.0-SNAPSHOT)");
            }
        }
    }

    @Override
    public void visitToken(DetailAST ast)
    {
        if (this.currentVersion == null) {
            // If not current version is set, just ignore this check

            return ;
        }

        switch (ast.getType()) {
            case TokenTypes.PACKAGE_DEF:
                // Save the package
                FullIdent ident = FullIdent.createFullIdent(ast.getLastChild().getPreviousSibling());
                this.packageName = ident.getText();
                return;
            case TokenTypes.CLASS_DEF:
            case TokenTypes.INTERFACE_DEF:
                this.classOrInterfaceName = ast.findFirstToken(TokenTypes.IDENT).getText();
                break;
        }

        if (AnnotationUtility.containsAnnotation(ast)) {
            DetailAST holder = AnnotationUtility.getAnnotationHolder(ast);
            for (DetailAST annotation : findAllTokens(holder, TokenTypes.ANNOTATION)) {
                String annotationName = annotation.findFirstToken(TokenTypes.IDENT).getText();
                if (annotationName.equals("Unstable")) {
                    FileContents contents = getFileContents();
                    String annotatedElementName = ast.findFirstToken(TokenTypes.IDENT).getText();
                    // Get the Javadoc before the annotation in order to locate a @Since annotation and to extract
                    // the XWiki version mentioned there.
                    List<String> sinceVersions = Collections.emptyList();
                    TextBlock cmt = contents.getJavadocBefore(ast.getLineNo());
                    if (cmt != null) {
                        sinceVersions = extractSinceVersionsFromJavadoc(cmt.getText(), annotation, annotatedElementName);
                    }
                    if (sinceVersions.isEmpty()) {
                        log(annotation.getLineNo(), annotation.getColumnNo(), String.format("There is an @Unstable "
                            + "annotation for [%s] but the @since javadoc tag is missing, you must add it!",
                            computeElementName(annotatedElementName)));
                        return;
                    }
                    checkSinceVersions(sinceVersions, annotation, annotatedElementName);
                }
            }
        }
    }

    private void checkSinceVersions(List<String> sinceVersions, DetailAST annotation, String annotatedElementName)
    {
        List<String> versions = new ArrayList<>();
        boolean failing = false;
        for (String sinceVersion : sinceVersions) {
            int sinceMajor = extractMajor(sinceVersion);
            if (sinceMajor == -1) {
                log(annotation.getLineNo(), annotation.getColumnNo(), String.format("The @since version [%s] "
                    + "must be of the type Major.* (e.g. 7.0-SNAPSHOT)", sinceVersion));
                return;
            } else {
                versions.add(sinceVersion);
                // We fail only if all since are failing since when we introduce a new API and backport it in an
                // older version, we don't want to start the grace period to be that of the backport version.
                if (this.currentVersionMajor - 2 >= sinceMajor) {
                    failing = true;
                } else {
                    failing = false;
                    break;
                }
            }
        }

        if (failing) {
            log(annotation.getLineNo(), annotation.getColumnNo(),
                String.format("The @Unstable annotation "
                        + "for [%s] must be removed since it''s been there for more than a full "
                        + "development cycle (was introduced in %s and current version is [%s])",
                    computeElementName(annotatedElementName), StringUtils.join(versions), this.currentVersion));
        }
    }

    private String computeElementName(String annotatedElementName)
    {
        return String.format("%s.%s%s", this.packageName, this.classOrInterfaceName,
            annotatedElementName.equals(this.classOrInterfaceName) ? "" : "." + annotatedElementName + "()");
    }

    /**
     * @return null if the since format is wrong
     */
    private List<String> extractSinceVersionsFromJavadoc(String[] javadocLines, DetailAST annotation,
        String annotatedElementName)
    {
        List<String> sinceVersions = new ArrayList<>();
        for (String javadocLine : javadocLines) {
            int pos = javadocLine.indexOf("@since");
            if (pos > -1) {
                String sinceVersion = javadocLine.substring(pos + "@since".length() + 1);
                sinceVersions.add(sinceVersion);
            }
        }
        return sinceVersions;
    }

    private int extractMajor(String version)
    {
        if (version == null) {
            return -1;
        }

        int major;
        int pos = version.indexOf(".");
        if (pos > -1) {
            try {
                major = Integer.parseInt(version.substring(0, pos));
            } catch (NumberFormatException e) {
                major = -1;
            }
        } else {
            major = -1;
        }
        return major;
    }

    public List<DetailAST> findAllTokens(DetailAST ast, int aType)
    {
        List<DetailAST> results = new ArrayList<>();
        DetailAST firstToken = ast.findFirstToken(aType);
        DetailAST token = firstToken;
        while (token != null) {
            results.add(token);
            token = token.getNextSibling();
            if (token == null || token.getType() != aType) {
                break;
            }
        }
        return results;
    }

}
