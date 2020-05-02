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
package org.xwiki.component.guice;

import java.lang.reflect.Field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.ProvisionException;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Add support for injecting SLF4J Loggers.
 * <p>
 * Note that the original source for this implementation (we've modified it) came from the <a
 * href="http://code.google.com/p/google-sitebricks/">Sitebricks</a> project and was under an Apache License v2.0.
 * </p>
 *
 * @version $Id$
 * @since 4.0RC1
 */
public class Slf4jInjectionTypeListener implements TypeListener
{
    @Override
    public <I> void hear(final TypeLiteral<I> type, TypeEncounter<I> encounter)
    {
        for (final Field field : type.getRawType().getDeclaredFields()) {
            Class<?> typeOfField = field.getType();
            if (Logger.class.isAssignableFrom(typeOfField)) {
                encounter.register((InjectionListener<I>) injectee -> {
                    boolean isAccessible = field.isAccessible();
                    field.setAccessible(true);
                    try {
                        field.set(injectee, LoggerFactory.getLogger(type.getRawType()));
                    } catch (IllegalAccessException e) {
                        throw new ProvisionException("Unable to inject SLF4J logger", e);
                    } finally {
                        field.setAccessible(isAccessible);
                    }
                });
            }
        }
    }
}
