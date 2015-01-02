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
package org.xwiki.extension.repository.internal;

import org.w3c.dom.Element;

/**
 * Serialize and unserialize {@link String} properties.
 *
 * @version $Id$
 */
public class StringExtensionPropertySerializer extends AbstractExtensionPropertySerializer<String>
{
    /**
     * Default constructor.
     */
    public StringExtensionPropertySerializer()
    {
        super(null);
    }

    @Override
    public String toValue(Element element)
    {
        return element.getTextContent();
    }
}
