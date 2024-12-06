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
import java.util.Iterator;
import java.util.function.Function;

import org.xwiki.jakartabridge.JakartaBridge;

/**
 * @param <C> the type of {@link Collection}
 * @param <X> the javax type
 * @param <K> the jakarta type
 * @version $Id$
 * @since 42.0.0
 */
public class JakartaToJavaxCollection<C extends Collection<X>, K, X> extends AbstractJakartaToJavaxWrapper<C>
    implements Collection<K>
{
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    protected final Function<K, X> javaxProvider;

    protected final Function<X, K> jakartaProvider;

    /**
     * @param wrapped the wrapped version
     * @param javaxProvider the javax wrapper provider
     * @param jakartaProvider the jakarta wrapper provider
     */
    public JakartaToJavaxCollection(C wrapped, Function<K, X> javaxProvider, Function<X, K> jakartaProvider)
    {
        super(wrapped);

        this.javaxProvider = javaxProvider;
        this.jakartaProvider = jakartaProvider;
    }

    @Override
    public int size()
    {
        return getJavax().size();
    }

    @Override
    public boolean isEmpty()
    {
        return getJavax().isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        return getJavax().contains(this.jakartaProvider.apply((X) o));
    }

    @Override
    public Iterator<K> iterator()
    {
        return JakartaBridge.toJakarta(getJavax().iterator(), this.javaxProvider, this.jakartaProvider);
    }

    @Override
    public Object[] toArray()
    {
        return toArray(EMPTY_OBJECT_ARRAY);
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        if (a.length == 0) {
            return a;
        }

        Object[] javaxArray = getJavax().toArray();

        T[] jakartaArray =
            (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), javaxArray.length);
        for (int i = 0; i < javaxArray.length; ++i) {
            Object javaxObject = javaxArray[i];
            jakartaArray[i] = javaxObject != null ? (T) this.jakartaProvider.apply((X) javaxObject) : null;
        }

        return jakartaArray;
    }

    @Override
    public boolean add(K e)
    {
        return getJavax().add(this.javaxProvider.apply(e));
    }

    @Override
    public boolean remove(Object o)
    {
        return getJavax().remove(this.javaxProvider.apply((K) o));
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return getJavax().remove(JakartaBridge.toJavax((Collection<K>) c, this.javaxProvider, this.jakartaProvider));
    }

    @Override
    public boolean addAll(Collection<? extends K> c)
    {
        return getJavax().addAll(JakartaBridge.toJavax(c, this.javaxProvider, this.jakartaProvider));
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        return getJavax().retainAll(JakartaBridge.toJavax((Collection<K>) c, this.javaxProvider, this.jakartaProvider));
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        return getJavax().removeAll(JakartaBridge.toJavax((Collection<K>) c, this.javaxProvider, this.jakartaProvider));
    }

    @Override
    public void clear()
    {
        getJavax().clear();
    }
}
