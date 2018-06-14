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
package org.xwiki.velocity.internal.directive;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.directive.MacroParseException;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.Token;
import org.apache.velocity.runtime.parser.node.ASTDirective;
import org.apache.velocity.runtime.parser.node.ASTReference;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * Allow "returning" values in a macro.
 *
 * @version $Id$
 * @since 10.6RC1
 */
public class SetVariableDirective extends Directive
{
    private Map<String, ASTReference> variables = new HashMap<>();

    @Override
    public String getName()
    {
        return "setVariable";
    }

    @Override
    public int getType()
    {
        return LINE;
    }

    @Override
    public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException
    {
        super.init(rs, context, node);

        for (Node currentNode = node.jjtGetParent(); currentNode != null; currentNode = currentNode.jjtGetParent()) {
            if (currentNode instanceof ASTDirective) {
                ASTDirective currentDirective = (ASTDirective) currentNode;

                // Only need to do something for macros
                if (currentDirective.getDirectiveName().equals("macro")) {
                    // Get macro name
                    String macroName = currentDirective.jjtGetChild(0).getFirstTokenImage();

                    // Find caller side of the macro
                    ASTDirective callerDirective = getCallerDirective(currentDirective, macroName);

                    if (callerDirective != null) {
                        // Find caller reference of each macro parameter
                        putCallerReferences(currentDirective, callerDirective);
                    }
                }

                break;
            }
        }
    }

    private void putCallerReferences(ASTDirective macro, ASTDirective callerDirective)
    {
        for (int i = 1; i < macro.jjtGetNumChildren(); ++i) {
            Node child = macro.jjtGetChild(i);

            if (child instanceof ASTReference) {
                Node sourceParameter = callerDirective.jjtGetChild(i - 1);

                if (sourceParameter instanceof ASTReference) {
                    this.variables.put(child.getFirstTokenImage(), (ASTReference) sourceParameter);
                }
            }
        }
    }

    private ASTDirective getCallerDirective(ASTDirective currentDirective, String directiveName)
    {
        Node parent = currentDirective.jjtGetParent();

        for (int i = 0; i < parent.jjtGetNumChildren(); ++i) {
            Node callerChild = parent.jjtGetChild(i);

            if (callerChild instanceof ASTDirective) {
                ASTDirective callerDirective = (ASTDirective) callerChild;
                if (callerDirective.getDirectiveName().equals(directiveName)) {
                    return callerDirective;
                }
            }
        }

        return null;
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node)
        throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException
    {
        // Get the name of the variable
        String key = (String) node.jjtGetChild(0).value(context);

        if (StringUtils.isEmpty(key)) {
            // null or empty variable name
            return false;
        }

        if (key.charAt(0) != '$') {
            key = '$' + key;
        }

        // Get the value to set
        Object value = node.jjtGetChild(1).value(context);

        // Set the value of the passed variable
        context.put(key.substring(1), value);

        // Set the value of the corresponding caller variable
        ASTReference reference = this.variables.get(key);
        if (reference != null) {
            reference.setValue(context, value);
        }

        return true;
    }

    @Override
    public void checkArgs(ArrayList<Integer> argtypes, Token t, String templateName) throws ParseException
    {
        if (argtypes.size() != 2) {
            throw new MacroParseException("The #setVariable directive requires two arguments", templateName, t);
        }
    }
}
