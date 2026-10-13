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
package org.xwiki.extension.repository.maven.internal.handler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.artifact.handler.ArtifactHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MavenArtifactHandlers}.
 *
 * @version $Id$
 */
class MavenArtifactHandlersTest
{
    private static ArtifactHandler handler(String type, String packaging)
    {
        ArtifactHandler handler = mock(ArtifactHandler.class, type);
        when(handler.getPackaging()).thenReturn(packaging);

        return handler;
    }

    private static MavenArtifactHandlers handlers(String... typesAndPackagings)
    {
        Map<String, ArtifactHandler> map = new LinkedHashMap<>();
        for (int i = 0; i < typesAndPackagings.length; i += 2) {
            map.put(typesAndPackagings[i], handler(typesAndPackagings[i], typesAndPackagings[i + 1]));
        }

        MavenArtifactHandlers handlers = new MavenArtifactHandlers();
        handlers.add(map);

        return handlers;
    }

    @Test
    void getByPackagingWhenAliasAddedAfter()
    {
        MavenArtifactHandlers handlers = handlers("webjar", "webjar", "default", "webjar");

        assertEquals("webjar", handlers.getByPackaging("webjar").getType());
        assertEquals("default", handlers.getByType("default").getType());
    }

    @Test
    void getByPackagingWhenAliasAddedBefore()
    {
        MavenArtifactHandlers handlers = handlers("default", "webjar", "webjar", "webjar");

        assertEquals("webjar", handlers.getByPackaging("webjar").getType());
    }

    @Test
    void getByPackagingWhenOtherTypeSharesThePackaging()
    {
        MavenArtifactHandlers handlers = handlers("jar", "jar", "test-jar", "jar");

        assertEquals("jar", handlers.getByPackaging("jar").getType());
        assertEquals("test-jar", handlers.getByType("test-jar").getType());
    }

    @Test
    void copyKeepsTheHandlerMatchingThePackaging()
    {
        List<MavenArtifactHandler> managerHandlers = List.of(
            new MavenArtifactHandler("webjar", handler("webjar", "webjar")),
            new MavenArtifactHandler("default", handler("default", "webjar")));
        MavenArtifactHandlerManager manager = mock(MavenArtifactHandlerManager.class);
        when(manager.getHandlers()).thenReturn(managerHandlers);

        MavenArtifactHandlers handlers = new MavenArtifactHandlers(manager);

        assertEquals("webjar", handlers.getByPackaging("webjar").getType());
    }

    @Test
    void getByPackagingWhenNoTypeMatchesThePackaging()
    {
        MavenArtifactHandlers handlers = handlers("ejb-client", "ejb");

        assertEquals("ejb-client", handlers.getByPackaging("ejb").getType());
        assertNull(handlers.getByPackaging("unknown"));
    }
}
