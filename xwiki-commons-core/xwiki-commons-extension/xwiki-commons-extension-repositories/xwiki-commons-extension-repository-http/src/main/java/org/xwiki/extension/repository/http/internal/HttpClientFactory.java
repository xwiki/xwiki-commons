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
package org.xwiki.extension.repository.http.internal;

import org.apache.http.client.HttpClient;
import org.xwiki.component.annotation.Role;

/**
 * Constructs {@link HttpClient} objects that can be used to perform request on HTTP-based Extension Repositories.
 *
 * @version $Id$
 * @since 5.2M1
 */
@Role
public interface HttpClientFactory
{
    /**
     * @param user the user if the remote repository requires authentication, or null if no authentication is required
     * @param password the password if the remote repository requires authentication, or null if no authentication is
     *        required
     * @return the {@link HttpClient} object that can be used to perform HTTP calls to an HTTP-based Extension
     *         Repository
     */
    HttpClient createClient(String user, String password);
}
