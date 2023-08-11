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

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

import org.xwiki.jakartabridge.JakartaBridge;

/**
 * @param <X> the javax type
 * @param <K> the jakarta type
 * @version $Id$
 * @since 42.0.0
 */
public class JakartaToJavaxList<K, X> extends JakartaToJavaxCollection<List<X>, K, X> implements List<K>
{
    /**
     * @param wrapped the wrapped version
     * @param javaxProvider the javax wrapper provider
     * @param jakartaProvider the jakarta wrapper provider
     */
    public JakartaToJavaxList(List<X> wrapped, Function<K, X> javaxProvider, Function<X, K> jakartaProvider)
    {
        super(wrapped, javaxProvider, jakartaProvider);
    }

    @Override
    public boolean addAll(int index, Collection<? extends K> c)
    {
        return getJavax().addAll(index, JakartaBridge.toJavax(c, this.javaxProvider, this.jakartaProvider));
    }

    @Override
    public K get(int index)
    {
        return this.jakartaProvider.apply(getJavax().get(index));
    }

    @Override
    public K set(int index, K element)
    {
        return this.jakartaProvider.apply(getJavax().set(index, this.javaxProvider.apply(element)));
    }

    @Override
    public void add(int index, K element)
    {
        getJavax().add(index, this.javaxProvider.apply(element));
    }

    @Override
    public K remove(int index)
    {
        return this.jakartaProvider.apply(getJavax().remove(index));
    }

    @Override
    public int indexOf(Object o)
    {
        return getJavax().indexOf(this.jakartaProvider.apply((X) o));
    }

    @Override
    public int lastIndexOf(Object o)
    {
        return getJavax().lastIndexOf(this.jakartaProvider.apply((X) o));
    }

    @Override
    public ListIterator<K> listIterator()
    {
        return JakartaBridge.toJakarta(getJavax().listIterator(), this.javaxProvider, this.jakartaProvider);
    }

    @Override
    public ListIterator<K> listIterator(int index)
    {
        return JakartaBridge.toJakarta(getJavax().listIterator(index), this.javaxProvider, this.jakartaProvider);
    }

    @Override
    public List<K> subList(int fromIndex, int toIndex)
    {
        return JakartaBridge.toJakarta(getJavax().subList(fromIndex, toIndex), this.javaxProvider,
            this.jakartaProvider);
    }
}
