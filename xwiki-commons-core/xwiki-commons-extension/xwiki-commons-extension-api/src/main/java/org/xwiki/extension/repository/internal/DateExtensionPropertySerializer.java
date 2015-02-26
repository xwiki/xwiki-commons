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

import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Serialize and unserialize {@link Date} properties.
 * 
 * @version $Id$
 * @since 7.0M2
 */
public class DateExtensionPropertySerializer extends AbstractExtensionPropertySerializer<Date>
{
    /**
     * Default constructor.
     */
    public DateExtensionPropertySerializer()
    {
        super("date");
    }

    @Override
    public Date toValue(Element element)
    {
        return new Date(Long.valueOf(element.getTextContent()));
    }

    @Override
    public Element toElement(Document document, String elementName, Date elementValue)
    {
        return super.createRootElement(document, elementName, String.valueOf(elementValue.getTime()));
    }
}
