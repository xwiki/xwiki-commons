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

import org.apache.commons.lang3.StringUtils;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Verify that the format of the {@code @since} javadoc tag is correct, i.e. that it obeys
 * <a href="http://dev.xwiki.org/xwiki/bin/view/Community/JavaCodeStyle#HUseone40sinceperversion">this rule</a>.
 *
 * @version $Id$
 * @since 8.3
 */
public class SinceFormatCheck extends AbstractCheck
{
    private String packageName;

    private String classOrInterfaceName;

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

    @Override
    public void visitToken(DetailAST ast)
    {
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

        String elementName = ast.findFirstToken(TokenTypes.IDENT).getText();
        FileContents contents = getFileContents();
        TextBlock javadoc = contents.getJavadocBefore(ast.getLineNo());

        if (javadoc != null) {
            for (String javadocLine : javadoc.getText()) {
                int pos = javadocLine.indexOf("@since");
                if (pos > -1) {
                    String text = javadocLine.substring(pos + "@since".length() + 1);
                    if (StringUtils.containsAny(text, ',', '/', '\\', ';', ':', '+')) {
                        log(ast.getLineNo(), ast.getColumnNo(),
                            String.format("There must be only a single version per @since tag for [%s]. Got [%s]",
                                computeElementName(elementName), text));
                        return;
                    }
                }
            }
        }
    }

    private String computeElementName(String annotatedElementName)
    {
        return String.format("%s.%s%s", this.packageName, this.classOrInterfaceName,
            annotatedElementName.equals(this.classOrInterfaceName) ? "" : "." + annotatedElementName + "()");
    }
}
