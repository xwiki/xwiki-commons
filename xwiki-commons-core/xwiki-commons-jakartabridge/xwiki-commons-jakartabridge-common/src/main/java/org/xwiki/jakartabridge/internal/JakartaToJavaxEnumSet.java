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
package org.xwiki.jakartabridge.internal;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.function.Function;

import org.xwiki.jakartabridge.JakartaBridge;

/**
 * @param <I> the type of javax {@link Iterator}
 * @param <X> the javax type
 * @param <K> the jakarta type
 * @version $Id$
 * @since 42.0.0
 */
public class JakartaToJavaxEnumSet<I extends Enumeration<X>, X, K> extends AbstractJakartaToJavaxWrapper<I>
    implements Enumeration<K>
{
    protected final Function<K, X> javaxProvider;

    protected final Function<X, K> jakartaProvider;

    /**
     * @param wrapped the wrapped version
     * @param javaxProvider the javax wrapper provider
     * @param jakartaProvider the jakarta wrapper provider
     */
    public JakartaToJavaxEnumSet(I wrapped, Function<K, X> javaxProvider, Function<X, K> jakartaProvider)
    {
        super(wrapped);

        this.javaxProvider = javaxProvider;
        this.jakartaProvider = jakartaProvider;
    }

    @Override
    public boolean hasMoreElements()
    {
        return getJavax().hasMoreElements();
    }

    @Override
    public Iterator<K> asIterator()
    {
        return JakartaBridge.toJakarta(getJavax().asIterator(), this.javaxProvider, this.jakartaProvider);
    }

    @Override
    public K nextElement()
    {
        return this.jakartaProvider.apply(getJavax().nextElement());
    }
}
