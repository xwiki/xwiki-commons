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
package org.xwiki.extension.repository.xwiki.internal;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.extension.ExtensionFile;
import org.xwiki.extension.ExtensionId;

/**
 * @version $Id$
 * @since 4.0M1
 */
public class XWikiExtensionFile implements ExtensionFile
{
    private static final Logger LOGGER = LoggerFactory.getLogger(XWikiExtensionFile.class);

    private XWikiExtensionRepository repository;

    private ExtensionId id;

    static class XWikiExtensionFileInputStream extends FilterInputStream
    {
        private CloseableHttpResponse response;

        public XWikiExtensionFileInputStream(CloseableHttpResponse response) throws IllegalStateException, IOException
        {
            super(response.getEntity().getContent());

            this.response = response;
        }

        @Override
        public void close() throws IOException
        {
            try {
                super.close();
            } finally {
                this.response.close();
            }
        }
    }

    public XWikiExtensionFile(XWikiExtensionRepository repository, ExtensionId id)
    {
        this.repository = repository;
        this.id = id;
    }

    @Override
    public long getLength()
    {
        CloseableHttpResponse response = null;
        try {
            try {
                response = this.repository.getRESTResource(this.repository.getExtensionFileUriBuider(), this.id.getId(),
                    this.id.getVersion().getValue());
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to access extension [%s]", this), e);
            }

            HttpEntity entity = response.getEntity();

            return entity.getContentLength();
        } finally {
            // When there's an error in closing the response, consider it's ok since we got the length but log a
            // warning as it could be the sign of something not right happening.
            IOUtils.closeQuietly(response, e ->
                LOGGER.warn("Failed to close response after accessing extension [{}]. Root error: [{}]", this,
                    ExceptionUtils.getRootCauseMessage(e)));
        }
    }

    @Override
    public InputStream openStream() throws IOException
    {
        CloseableHttpResponse response =
            this.repository.getRESTResource(this.repository.getExtensionFileUriBuider(), this.id.getId(), this.id
                .getVersion().getValue());

        return new XWikiExtensionFileInputStream(response);
    }
}
