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

import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * The number of other classes a given class relies on. Also the square of this has been shown to indicate the amount of
 * maintenance required in functional programs (on a file basis) at least.
 * <p/>
 * Copied from the Checkstyle source (because it cannot be extended, see
 * https://sourceforge.net/p/checkstyle/feature-requests/575/) to fix the following issues:
 * <ul>
 *   <li>https://sourceforge.net/p/checkstyle/feature-requests/607/</li>
 *   <li>https://sourceforge.net/p/checkstyle/bugs/684/</li>
 * </ul>
 *
 * @version $Id$
 * @since 5.0M1
 */
public final class XWikiClassFanOutComplexityCheck extends AbstractXWikiClassCouplingCheck
{
    /**
     * default value of max value.
     */
    private static final int DEFAULT_MAX = 20;

    /**
     * Creates new instance of this check.
     */
    public XWikiClassFanOutComplexityCheck()
    {
        super(DEFAULT_MAX);
    }

    @Override
    public int[] getRequiredTokens()
    {
        return new int[]{
            TokenTypes.PACKAGE_DEF,
            TokenTypes.CLASS_DEF,
            TokenTypes.INTERFACE_DEF,
            TokenTypes.ENUM_DEF,
            TokenTypes.TYPE,
            TokenTypes.LITERAL_NEW,
            TokenTypes.LITERAL_THROWS,
            TokenTypes.ANNOTATION_DEF,
        };
    }

    @Override
    protected String getLogMessageId()
    {
        return "classFanOutComplexity";
    }
}
