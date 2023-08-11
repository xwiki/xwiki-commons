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
public class JavaxToJakartaCollection<C extends Collection<K>, X, K> extends AbstractJavaxToJakartaWrapper<C>
    implements Collection<X>
{
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    protected final Function<K, X> javaxProvider;

    protected final Function<X, K> jakartaProvider;

    /**
     * @param wrapped the wrapped version
     * @param javaxProvider the javax wrapper provider
     * @param jakartaProvider the jakarta wrapper provider
     */
    public JavaxToJakartaCollection(C wrapped, Function<K, X> javaxProvider, Function<X, K> jakartaProvider)
    {
        super(wrapped);

        this.javaxProvider = javaxProvider;
        this.jakartaProvider = jakartaProvider;
    }

    @Override
    public int size()
    {
        return getJakarta().size();
    }

    @Override
    public boolean isEmpty()
    {
        return getJakarta().isEmpty();
    }

    @Override
    public boolean contains(Object o)
    {
        return getJakarta().contains(this.jakartaProvider.apply((X) o));
    }

    @Override
    public Iterator<X> iterator()
    {
        return JakartaBridge.toJavax(getJakarta().iterator(), this.javaxProvider, this.jakartaProvider);
    }

    @Override
    public Object[] toArray()
    {
        return toArray(EMPTY_OBJECT_ARRAY);
    }

    @Override
    public <T> T[] toArray(T[] a)
    {
        Object[] jakartaArray = getJakarta().toArray();

        if (jakartaArray.length == 0) {
            return a;
        }

        T[] javaxArray =
            (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), jakartaArray.length);
        for (int i = 0; i < jakartaArray.length; ++i) {
            Object jakartaObject = jakartaArray[i];
            javaxArray[i] = jakartaObject != null ? (T) this.javaxProvider.apply((K) jakartaObject) : null;
        }

        return javaxArray;
    }

    @Override
    public boolean add(X e)
    {
        return getJakarta().add(this.jakartaProvider.apply(e));
    }

    @Override
    public boolean remove(Object o)
    {
        return getJakarta().remove(this.jakartaProvider.apply((X) o));
    }

    @Override
    public boolean containsAll(Collection<?> c)
    {
        return getJakarta()
            .containsAll(JakartaBridge.toJakarta((Collection<X>) c, this.javaxProvider, this.jakartaProvider));
    }

    @Override
    public boolean addAll(Collection<? extends X> c)
    {
        return getJakarta().addAll(JakartaBridge.toJakarta(c, this.javaxProvider, this.jakartaProvider));
    }

    @Override
    public boolean retainAll(Collection<?> c)
    {
        return getJakarta()
            .retainAll(JakartaBridge.toJakarta((Collection<X>) c, this.javaxProvider, this.jakartaProvider));
    }

    @Override
    public boolean removeAll(Collection<?> c)
    {
        return getJakarta()
            .removeAll(JakartaBridge.toJakarta((Collection<X>) c, this.javaxProvider, this.jakartaProvider));
    }

    @Override
    public void clear()
    {
        getJakarta().clear();
    }
}
