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
package org.xwiki.http.internal;

import java.io.IOException;

import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;

/**
 * Handler that encapsulates the process of generating a response object from a {@link ClassicHttpResponse}.
 *
 * @param <T> the type of the response.
 * @version $Id$
 * @since 17.10.0RC1
 * @since 17.4.8
 * @since 16.10.15
 */
@FunctionalInterface
public interface XWikiHTTPClientResponseHandler<T>
{
    /**
     * Processes an {@link ClassicHttpResponse} and returns some value corresponding to that response.
     *
     * @param response The response to process
     * @param context The HTTP context
     * @return A value determined by the response
     * @throws IOException in case of a problem or the connection was aborted
     * @throws HttpException in case of an HTTP protocol violation.
     */
    T handleResponse(ClassicHttpResponse response, HttpClientContext context) throws HttpException, IOException;
}
