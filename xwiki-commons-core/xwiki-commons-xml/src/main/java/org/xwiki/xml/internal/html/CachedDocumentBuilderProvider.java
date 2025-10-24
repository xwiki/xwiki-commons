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

import javax.inject.Inject;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import jakarta.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

/**
 * Provides a cached DocumentBuilder instance.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@Component(roles = CachedDocumentBuilderProvider.class)
@Singleton
public class CachedDocumentBuilderProvider
{
    @Inject
    private Execution execution;

    /**
     * @return a potentially cached DocumentBuilder instance
     * @throws ParserConfigurationException if the document builder cannot be created
     */
    public DocumentBuilder getAvailableDocumentBuilder() throws ParserConfigurationException
    {
        ExecutionContext econtext = this.execution.getContext();

        if (econtext != null) {
            DocumentBuilder documentBuilder = (DocumentBuilder) econtext.getProperty(DocumentBuilder.class.getName());

            if (documentBuilder == null) {
                documentBuilder = createSecureDocumentBuilder();
                econtext.setProperty(DocumentBuilder.class.getName(), documentBuilder);
            }

            return documentBuilder;
        }

        return createSecureDocumentBuilder();
    }

    private static DocumentBuilder createSecureDocumentBuilder() throws ParserConfigurationException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Disable secure processing features for good security (might not be necessary as we control the DTD).
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        // Disable downloading of the external DTD to avoid hitting w3.org. This might not matter in practice as we
        // don't use this document builder to actually parse any content, but it's better to be safe.
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        return factory.newDocumentBuilder();
    }
}
