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
package org.xwiki.diff.xml;

import org.xwiki.component.annotation.Role;
import org.xwiki.diff.DiffException;

/**
 * Computes and marks the differences between two XML documents.
 * 
 * @version $Id$
 * @since 11.10.1
 * @since 12.0RC1
 */
@Role
public interface XMLDiffManager
{
    /**
     * Computes and marks the differences between two XML documents.
     * 
     * @param left the left side of the comparison
     * @param right the right side of the comparison
     * @param config the configuration
     * @return the differences between the two XML documents
     * @throws DiffException if the difference can't be computed
     */
    String diff(String left, String right, XMLDiffConfiguration config) throws DiffException;
}
