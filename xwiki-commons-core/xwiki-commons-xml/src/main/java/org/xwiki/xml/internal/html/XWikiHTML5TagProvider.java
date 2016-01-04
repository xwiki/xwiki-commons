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
package org.xwiki.xml.internal.html;

import org.htmlcleaner.Html4TagProvider;
import org.htmlcleaner.Html5TagProvider;
import org.xwiki.xml.html.HTMLConstants;

/**
 * List the tags allowed in HTML 5 with some additions to handle some legacy tags such as "font" or "tt".
 *  
 * @version $Id$
 */
public class XWikiHTML5TagProvider extends Html5TagProvider
{
    private static Html4TagProvider html4TagProvider = new Html4TagProvider();
    
    /**
     * Default constructor.
     */
    public XWikiHTML5TagProvider()
    { 
        super();
        
        // Add the "font" tag to be able to filter on it afterwards
        addTag(HTMLConstants.TAG_FONT);   
    }
    
    private void addTag(String tagName)
    {
        this.put(tagName, html4TagProvider.getTagInfo(tagName));
    }
}
