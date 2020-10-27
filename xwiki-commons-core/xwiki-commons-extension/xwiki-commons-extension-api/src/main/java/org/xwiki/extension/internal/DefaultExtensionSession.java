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
package org.xwiki.extension.internal;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.extension.ExtensionSession;

/**
 * Default implementation of {@link ExtensionSession}.
 * 
 * @version $Id$
 * @since 12.10RC1
 */
public class DefaultExtensionSession implements ExtensionSession
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExtensionSession.class);

    private Map<String, Object> map = new HashMap<>();

    @Override
    public void set(String key, Object value)
    {
        this.map.put(key, value);
    }

    @Override
    public <T> T get(String key)
    {
        return (T) this.map.get(key);
    }

    /**
     * Release resources used by the session.
     */
    public void dispose()
    {
        for (Map.Entry<String, Object> entry : this.map.entrySet()) {
            if (entry.getValue() instanceof Closeable) {
                try {
                    ((Closeable) entry.getValue()).close();
                } catch (IOException e) {
                    LOGGER.warn("Failed to close the value associated with the key [{}]: {}", entry.getKey(),
                        ExceptionUtils.getRootCauseMessage(e));
                }
            }
        }
    }
}
