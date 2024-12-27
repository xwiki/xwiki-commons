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

import java.util.ListIterator;
import java.util.function.Function;

/**
 * @param <X> the javax type
 * @param <K> the jakarta type
 * @version $Id$
 */
public class JakartaToJavaxListIterator<X, K> extends JakartaToJavaxIterator<ListIterator<X>, X, K>
    implements ListIterator<K>
{
    protected final Function<K, X> javaxProvider;

    /**
     * @param wrapped the wrapped version
     * @param javaxProvider the javax wrapper provider
     * @param jakartaProvider the jakarta wrapper provider
     */
    public JakartaToJavaxListIterator(ListIterator<X> wrapped, Function<K, X> javaxProvider,
        Function<X, K> jakartaProvider)
    {
        super(wrapped, jakartaProvider);

        this.javaxProvider = javaxProvider;
    }

    @Override
    public boolean hasPrevious()
    {
        return getJavax().hasPrevious();
    }

    @Override
    public K previous()
    {
        return this.jakartaProvider.apply(getJavax().previous());
    }

    @Override
    public int nextIndex()
    {
        return getJavax().nextIndex();
    }

    @Override
    public int previousIndex()
    {
        return getJavax().previousIndex();
    }

    @Override
    public void remove()
    {
        getJavax().remove();
    }

    @Override
    public void set(K e)
    {
        getJavax().set(this.javaxProvider.apply(e));
    }

    @Override
    public void add(K e)
    {
        getJavax().add(this.javaxProvider.apply(e));
    }
}
