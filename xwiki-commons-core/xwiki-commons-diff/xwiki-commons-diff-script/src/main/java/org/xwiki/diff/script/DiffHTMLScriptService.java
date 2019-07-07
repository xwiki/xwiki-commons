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

import java.io.StringReader;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.w3c.dom.Document;
import org.xwiki.component.annotation.Component;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.xml.XMLDiffMarker;
import org.xwiki.diff.xml.XMLDiffPruner;
import org.xwiki.script.service.ScriptService;
import org.xwiki.stability.Unstable;
import org.xwiki.xml.html.HTMLCleaner;
import org.xwiki.xml.html.HTMLUtils;

/**
 * Provides script oriented APIs to compute and display the changes between HTML documents.
 * 
 * @version $Id$
 * @since 11.6RC1
 */
@Component
@Named("diff.html")
@Singleton
@Unstable
public class DiffHTMLScriptService implements ScriptService
{
    @Inject
    private HTMLCleaner htmlCleaner;

    @Inject
    @Named("html")
    private XMLDiffMarker htmlDiffMarker;

    @Inject
    @Named("html")
    private XMLDiffPruner htmlDiffPruner;

    /**
     * Computes the changes between the given HTML fragments and returns them in the unified format.
     * 
     * @param previousHTML the previous version of the HTML
     * @param nextHTML the next version of the HTML
     * @return the changes between the given HTML fragments in unified format
     */
    public String unified(String previousHTML, String nextHTML)
    {
        Document previousDocument = parseHTML(previousHTML);
        try {
            if (!this.htmlDiffMarker.markDiff(previousDocument, parseHTML(nextHTML))) {
                // No changes detected.
                return "";
            }
        } catch (DiffException e) {
            // Failed to compute the changes.
            return null;
        }
        this.htmlDiffPruner.prune(previousDocument);
        return unwrap(HTMLUtils.toString(previousDocument, false, false).trim());
    }

    private Document parseHTML(String html)
    {
        return this.htmlCleaner.clean(new StringReader(wrap(html)));
    }

    private String wrap(String fragment)
    {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE html>"
            + "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head></head><body>" + fragment + "</body></html>";
    }

    private String unwrap(String html)
    {
        int start = html.indexOf("<body>") + 6;
        int end = html.indexOf("</body>");
        return html.substring(start, end);
    }
}
