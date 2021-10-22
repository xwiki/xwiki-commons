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
package org.xwiki.groovy.internal;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.customizers.CompilationCustomizer;

/**
 * Simulate a Compilation Customizer that throws an error. This would happen for example with a Secure
 * Customizer that would prevent executing some statements for example.
 *
 * @version $Id$
 * @since 13.10
 */
public class ErrorThrowingTestCompilationCustomizer extends CompilationCustomizer
{
    /**
     * @param phase the compilation phase during which this customizer operates
     */
    public ErrorThrowingTestCompilationCustomizer(CompilePhase phase)
    {
        super(phase);
    }

    @Override
    public void call(SourceUnit sourceUnit, GeneratorContext generatorContext, ClassNode classNode)
        throws CompilationFailedException
    {
        throw new SecurityException("test exception");
    }

    @Override
    public boolean needSortedInput()
    {
        return false;
    }
}
