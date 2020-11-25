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
package org.xwiki.diff.script;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.xml.XMLDiffConfiguration;
import org.xwiki.diff.xml.XMLDiffManager;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;

/**
 * Provides script oriented APIs to compute and display the changes between HTML documents.
 * 
 * @version $Id$
 * @since 11.6RC1
 */
@Component
@Named("diff.html")
@Singleton
public class DiffHTMLScriptService implements ScriptService
{
    @Inject
    @Named("html/unified")
    private XMLDiffManager unifiedHTMLDiffManager;

    @Inject
    @Named("html/script")
    private Provider<XMLDiffConfiguration> configurationProvider;

    /**
     * Computes the changes between the given HTML fragments using the default configuration, and returns them in the
     * unified format.
     * 
     * @param previousHTML the previous version of the HTML
     * @param nextHTML the next version of the HTML
     * @return the changes between the given HTML fragments in unified format
     */
    public String unified(String previousHTML, String nextHTML)
    {
        return unified(previousHTML, nextHTML, getDefaultConfiguration());
    }

    /**
     * Computes the changes between the given HTML fragments using the given configuration and returns them in the
     * unified format.
     * 
     * @param previousHTML the previous version of the HTML
     * @param nextHTML the next version of the HTML
     * @param config the configuration
     * @return the changes between the given HTML fragments in unified format
     * @since 11.10.1
     * @since 12.0RC1
     */
    @Unstable
    public String unified(String previousHTML, String nextHTML, XMLDiffConfiguration config)
    {
        try {
            return this.unifiedHTMLDiffManager.diff(previousHTML, nextHTML, config);
        } catch (DiffException e) {
            // Failed to compute the changes.
            return null;
        }
    }

    /**
     * @return the default configuration
     * @since 11.10.1
     * @since 12.0RC1
     */
    @Unstable
    public XMLDiffConfiguration getDefaultConfiguration()
    {
        return this.configurationProvider.get();
    }
}
