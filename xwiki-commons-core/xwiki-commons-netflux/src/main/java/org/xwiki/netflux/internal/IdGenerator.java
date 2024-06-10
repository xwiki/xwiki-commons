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
package org.xwiki.netflux.internal;

import java.util.concurrent.ThreadLocalRandom;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

/**
 * Generates identifiers for various Netflux entities.
 *
 * @version $Id$
 * @since 15.10.11
 * @since 16.4.1
 * @since 16.5.0RC1
 */
@Component(roles = IdGenerator.class)
@Singleton
public class IdGenerator
{
    /**
     * @return a randomly generated channel id
     */
    public String generateChannelId()
    {
        return getRandomHexString(48);
    }

    /**
     * @return a randomly generated user id
     */
    public String generateUserId()
    {
        return getRandomHexString(32);
    }

    /**
     * @return a randomly generated bot id
     */
    public String generateBotId()
    {
        return getRandomHexString(24);
    }

    /**
     * @param length the length of the hex string to generate
     * @return the generated random hex string
     */
    private String getRandomHexString(int length)
    {
        StringBuilder hexString = new StringBuilder();
        while (hexString.length() < length) {
            hexString.append(Integer.toHexString(ThreadLocalRandom.current().nextInt()));
        }
        return hexString.toString().substring(0, length);
    }
}
