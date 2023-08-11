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
package jakarta.servlet.http;

import java.util.Enumeration;

/**
 * @version $Id$
 * @deprecated deleted in Servlet 6
 * @see HttpSession
 * @see HttpSessionBindingEvent
 * @see HttpSessionBindingListener
 */
public interface HttpSessionContext
{
    /**
     * @deprecated As of Java Servlet API 2.1 with no replacement. This method must return null and will be removed in a
     *             future version of this API.
     * @param sessionId the id of the session to be returned
     * @return null in all cases
     */
    @Deprecated
    HttpSession getSession(String sessionId);

    /**
     * @deprecated As of Java Servlet API 2.1 with no replacement. This method must return an empty
     *             <code>Enumeration</code> and will be removed in a future version of this API.
     * @return null
     */
    @Deprecated
    Enumeration<String> getIds();
}
