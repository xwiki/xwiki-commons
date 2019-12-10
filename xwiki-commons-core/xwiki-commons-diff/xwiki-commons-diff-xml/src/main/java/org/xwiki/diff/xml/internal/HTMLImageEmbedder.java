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

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;
import org.xwiki.component.annotation.Component;
import org.xwiki.diff.xml.XMLDiffFilter;

/**
 * Embeds the images in the HTML before computing the changes and restores the original image location afterwards. The
 * goal is to compute the changes using the image content (ignoring the image location).
 * 
 * @version $Id$
 * @since 11.10.1
 * @since 12.0RC1
 */
@Component
@Singleton
@Named("html/imageEmbedder")
public class HTMLImageEmbedder implements XMLDiffFilter
{
    private static final String ATTRIBUTE_SRC = "src";

    private static final String USER_DATA_IMAGE_SRC = "xwiki-html-diff-image-src";

    private static final String USER_DATA_IMAGE_DATA_URI = "xwiki-html-diff-image-dataURI";

    private static final UserDataHandler USER_DATA_HANDLER = new UserDataHandler()
    {
        @Override
        public void handle(short operation, String key, Object data, Node source, Node destination)
        {
            if (operation == NODE_CLONED) {
                destination.setUserData(key, data, this);
            }
        }
    };

    @Inject
    private Logger logger;

    /**
     * We use a provider because ATM there's no {@link DataURIConverter} implementation in {@code xwiki-commons} and we
     * want to be able to access this filter in spite of this (e.g. to remove it from the diff configuration).
     */
    @Inject
    private Provider<DataURIConverter> dataURIConverterProvider;

    @Override
    public void before(Document document)
    {
        getImages(document).forEach(this::before);
    }

    @Override
    public void after(Document document)
    {
        getImages(document).forEach(this::after);
    }

    private List<Element> getImages(Document document)
    {
        return XMLDiffUtils.asList(document.getElementsByTagName("img")).stream().map(image -> (Element) image)
            .collect(Collectors.toList());
    }

    private void before(Element image)
    {
        String source = image.getAttribute(ATTRIBUTE_SRC);
        try {
            String dataURI = this.dataURIConverterProvider.get().convert(source);
            image.setAttribute(ATTRIBUTE_SRC, dataURI);
            // The user data handler is needed in order to copy the user data when nodes are cloned, which happens when
            // DOM changes are being marked.
            // We duplicate the data URI on the node user data because the src attribute can be modified while the DOM
            // changes are being marked and this should invalidate the original image source.
            image.setUserData(USER_DATA_IMAGE_DATA_URI, dataURI, USER_DATA_HANDLER);
            image.setUserData(USER_DATA_IMAGE_SRC, source, USER_DATA_HANDLER);
        } catch (Exception e) {
            this.logger.warn("Failed to embed image [{}]. Root cause is [{}].", source,
                ExceptionUtils.getRootCauseMessage(e));
        }
    }

    private void after(Element image)
    {
        // Restore the original image source if the current source is still matching the data URI.
        String source = (String) image.getUserData(USER_DATA_IMAGE_SRC);
        String dataURI = (String) image.getUserData(USER_DATA_IMAGE_DATA_URI);
        if (source != null && image.getAttribute(ATTRIBUTE_SRC).equals(dataURI)) {
            image.setAttribute(ATTRIBUTE_SRC, source);
        }
    }
}
