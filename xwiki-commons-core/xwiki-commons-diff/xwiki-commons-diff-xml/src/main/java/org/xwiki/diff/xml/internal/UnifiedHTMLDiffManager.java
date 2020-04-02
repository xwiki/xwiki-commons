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
package org.xwiki.diff.xml.internal;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.xml.XMLDiffConfiguration;
import org.xwiki.diff.xml.XMLDiffFilter;
import org.xwiki.diff.xml.XMLDiffManager;
import org.xwiki.diff.xml.XMLDiffMarker;
import org.xwiki.xml.XMLUtils;
import org.xwiki.xml.html.HTMLCleaner;
import org.xwiki.xml.html.HTMLCleanerConfiguration;
import org.xwiki.xml.html.HTMLUtils;

/**
 * Computes the changes between two HTML fragments and shows them in the unified format.
 * 
 * @version $Id$
 * @since 11.10.1
 * @since 12.0RC1
 */
@Component
@Singleton
@Named("html/unified")
public class UnifiedHTMLDiffManager implements XMLDiffManager, Initializable
{
    @Inject
    private Logger logger;

    @Inject
    private HTMLCleaner htmlCleaner;

    @Inject
    @Named("html")
    private XMLDiffMarker htmlDiffMarker;

    /**
     * Helper object for manipulating DOM Level 3 Load and Save APIs.
     **/
    private DOMImplementationLS lsImpl;

    private Map<String, String> htmlCleanerParametersMap;

    @Override
    public void initialize() throws InitializationException
    {
        try {
            this.lsImpl = (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS 3.0");

            htmlCleanerParametersMap = new HashMap<>();
            // We need to parse the clean HTML as XML later and we don't want to resolve the entity references from the DTD.
            htmlCleanerParametersMap.put(HTMLCleanerConfiguration.USE_CHARACTER_REFERENCES, "true");

            // We need to translate special entities to properly use the XML parser afterwards.
            htmlCleanerParametersMap.put(HTMLCleanerConfiguration.TRANSLATE_SPECIAL_ENTITIES, "true");
        } catch (Exception exception) {
            throw new InitializationException("Failed to initialize DOM Level 3 Load and Save APIs.", exception);
        }
    }

    @Override
    public String diff(String previousHTML, String nextHTML, XMLDiffConfiguration config) throws DiffException
    {
        List<XMLDiffFilter> filters = config.getFilters();
        Document previousDocument = filterBeforeDiff(parseHTML(previousHTML), filters);
        Document nextDocument = filterBeforeDiff(parseHTML(nextHTML), filters);
        if (!this.htmlDiffMarker.markDiff(previousDocument, nextDocument, config)) {
            // No changes detected.
            return "";
        }
        return unwrap(HTMLUtils.toString(filterAfterDiff(previousDocument, filters), false, false).trim());
    }

    private Document parseHTML(String html)
    {
        // We need to clean the HTML because it may have been generated with the HTML macro using clean=false.
        return parseXML(cleanHTML(html));
    }

    private String cleanHTML(String html)
    {
        HTMLCleanerConfiguration config = this.htmlCleaner.getDefaultConfiguration();
        config.setParameters(htmlCleanerParametersMap);
        Document htmlDoc = this.htmlCleaner.clean(new StringReader(wrap(html)), config);
        // We serialize and parse again the HTML as XML because the HTML Cleaner doesn't handle entity and character
        // references very well: they all end up as plain text (they are included in the value returned by
        // Node#getNodeValue()).
        return HTMLUtils.toString(htmlDoc);
    }

    private Document parseXML(String xml)
    {
        LSInput input = this.lsImpl.createLSInput();
        input.setStringData(xml);
        return XMLUtils.parse(input);
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

    private Document filterBeforeDiff(Document document, List<XMLDiffFilter> filters)
    {
        for (XMLDiffFilter filter : filters) {
            try {
                filter.before(document);
            } catch (Exception e) {
                this.logger.warn("Failed to apply filter before diff.", e);
            }
        }
        return document;
    }

    private Document filterAfterDiff(Document document, List<XMLDiffFilter> filters)
    {
        for (XMLDiffFilter filter : filters) {
            try {
                filter.after(document);
            } catch (Exception e) {
                this.logger.warn("Failed to apply filter after diff.", e);
            }
        }
        return document;
    }
}
