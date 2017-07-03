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

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Verify that ScriptService classes are not located in the internal package
 * (see https://jira.xwiki.org/browse/XWIKI-9482).
 *
 * @version $Id$
 * @since 5.3M1
 */
public class ScriptServiceCheck extends AbstractCheck
{
    private String packageName;

    @Override
    public int[] getDefaultTokens()
    {
        return new int[] {TokenTypes.CLASS_DEF, TokenTypes.PACKAGE_DEF};
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
                // Save the fact that we're in an internal package or not
                FullIdent ident = FullIdent.createFullIdent(ast.getLastChild().getPreviousSibling());
                this.packageName = ident.getText();
                break;
            case TokenTypes.CLASS_DEF:
                // Check if the class name ends with ScriptService and report an error if it does and if we're in an
                // internal package unless the name ends with InternalScriptService which makes it on purpose.
                String className = ast.findFirstToken(TokenTypes.IDENT).getText();
                if (className.endsWith("ScriptService") && this.packageName.contains("internal")
                    && !className.endsWith("InternalScriptService")) {
                    log(ast.getLineNo(), "Script Service implementation [" + className + "] in package ["
                        + this.packageName + "] should not be located in the internal package");
                }
                break;
            default:
                throw new IllegalStateException(ast.toString());
        }
    }
}
