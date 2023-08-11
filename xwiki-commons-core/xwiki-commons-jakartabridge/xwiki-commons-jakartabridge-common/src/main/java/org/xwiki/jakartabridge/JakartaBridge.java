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
package org.xwiki.jakartabridge;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

import org.xwiki.jakartabridge.internal.JakartaToJavaxCollection;
import org.xwiki.jakartabridge.internal.JakartaToJavaxEnumeration;
import org.xwiki.jakartabridge.internal.JakartaToJavaxIterator;
import org.xwiki.jakartabridge.internal.JakartaToJavaxList;
import org.xwiki.jakartabridge.internal.JakartaToJavaxListIterator;
import org.xwiki.jakartabridge.internal.JavaxToJakartaCollection;
import org.xwiki.jakartabridge.internal.JavaxToJakartaEnumeration;
import org.xwiki.jakartabridge.internal.JavaxToJakartaIterator;
import org.xwiki.jakartabridge.internal.JavaxToJakartaList;
import org.xwiki.jakartabridge.internal.JavaxToJakartaListIterator;

/**
 * @version $Id$
 * @since 42.0.0
 */
public final class JakartaBridge
{
    private JakartaBridge()
    {
    }

    //////////////////////////////////////////////////
    // Wrapped

    /**
     * @param <K> the jakarta type
     * @param <X> the javax type
     * @param jakarta the jakarta version
     * @param javaxProvider the reference to the {@link JakartaToJavaxWrapper} implementation's constructor
     * @return the javax version
     */
    public static <X, K> X toJavax(K jakarta, Function<K, X> javaxProvider)
    {
        X javax = null;

        if (jakarta != null) {
            if (jakarta instanceof JakartaToJavaxWrapper wrapper) {
                javax = (X) wrapper.getJavax();
            }

            if (javax == null) {
                javax = javaxProvider.apply(jakarta);
            }
        }

        return javax;
    }

    /**
     * @param <K> the javax type
     * @param <X> the jakarta type
     * @param javax the javax version
     * @param jakartaConstructor the reference to the {@link JavaxToJakartaWrapper} implementation's constructor
     * @return the jakarta version
     */
    public static <X, K> K toJakarta(X javax, Function<X, K> jakartaConstructor)
    {
        K jakarta = null;

        if (javax != null) {
            if (javax instanceof JavaxToJakartaWrapper wrapper) {
                jakarta = (K) wrapper.getJakarta();
            }

            if (jakarta == null) {
                jakarta = jakartaConstructor.apply(javax);
            }
        }

        return jakarta;
    }

    //////////////////////////////////////////////////
    // Multi

    /**
     * @param <X> the javax type
     * @param <K> the jakarta type
     * @param jakarta the jakarta version
     * @param javaxProvider the reference to the {@link JakartaToJavaxWrapper} implementation's provider
     * @param jakartaProvider the reference to the {@link JavaxToJakartaWrapper} implementation's provider
     * @return the javax version
     */
    public static <X, K> Collection<X> toJavax(Collection<? extends K> jakarta, Function<K, X> javaxProvider,
        Function<X, K> jakartaProvider)
    {
        return toJavax(jakarta, k -> new JavaxToJakartaCollection(k, javaxProvider, jakartaProvider));
    }

    /**
     * @param <X> the javax type
     * @param <K> the jakarta type
     * @param javax the javax version
     * @param javaxProvider the reference to the {@link JakartaToJavaxWrapper} implementation's provider
     * @param jakartaProvider the reference to the {@link JavaxToJakartaWrapper} implementation's provider
     * @return the jakarta version
     */
    public static <X, K> Collection<K> toJakarta(Collection<? extends X> javax, Function<K, X> javaxProvider,
        Function<X, K> jakartaProvider)
    {
        return toJakarta(javax, x -> new JakartaToJavaxCollection(x, javaxProvider, jakartaProvider));
    }

    /**
     * @param <X> the javax type
     * @param <K> the jakarta type
     * @param jakarta the jakarta version
     * @param javaxProvider the reference to the {@link JakartaToJavaxWrapper} implementation's provider
     * @param jakartaProvider the reference to the {@link JavaxToJakartaWrapper} implementation's provider
     * @return the javax version
     */
    public static <X, K> List<X> toJavax(List<? extends K> jakarta, Function<K, X> javaxProvider,
        Function<X, K> jakartaProvider)
    {
        return toJavax(jakarta, k -> new JavaxToJakartaList(k, javaxProvider, jakartaProvider));
    }

    /**
     * @param <X> the javax type
     * @param <K> the jakarta type
     * @param javax the javax version
     * @param javaxProvider the reference to the {@link JakartaToJavaxWrapper} implementation's provider
     * @param jakartaProvider the reference to the {@link JavaxToJakartaWrapper} implementation's provider
     * @return the jakarta version
     */
    public static <X, K> List<K> toJakarta(List<? extends X> javax, Function<K, X> javaxProvider,
        Function<X, K> jakartaProvider)
    {
        return toJakarta(javax, x -> new JakartaToJavaxList(x, javaxProvider, jakartaProvider));
    }

    /**
     * @param <X> the javax type
     * @param <K> the jakarta type
     * @param jakarta the jakarta version
     * @param javaxProvider the reference to the {@link JakartaToJavaxWrapper} implementation's provider
     * @param jakartaProvider the reference to the {@link JavaxToJakartaWrapper} implementation's provider
     * @return the javax version
     */
    public static <X, K> Iterator<X> toJavax(Iterator<? extends K> jakarta, Function<K, X> javaxProvider,
        Function<X, K> jakartaProvider)
    {
        return toJavax(jakarta, k -> new JavaxToJakartaIterator(k, javaxProvider));
    }

    /**
     * @param <X> the javax type
     * @param <K> the jakarta type
     * @param javax the javax version
     * @param javaxProvider the reference to the {@link JakartaToJavaxWrapper} implementation's provider
     * @param jakartaProvider the reference to the {@link JavaxToJakartaWrapper} implementation's provider
     * @return the jakarta version
     */
    public static <X, K> Iterator<K> toJakarta(Iterator<? extends X> javax, Function<K, X> javaxProvider,
        Function<X, K> jakartaProvider)
    {
        return toJakarta(javax, x -> new JakartaToJavaxIterator(x, jakartaProvider));
    }

    /**
     * @param <X> the javax type
     * @param <K> the jakarta type
     * @param jakarta the jakarta version
     * @param javaxProvider the reference to the {@link JakartaToJavaxWrapper} implementation's provider
     * @param jakartaProvider the reference to the {@link JavaxToJakartaWrapper} implementation's provider
     * @return the javax version
     */
    public static <X, K> Enumeration<X> toJavax(Enumeration<? extends K> jakarta, Function<K, X> javaxProvider,
        Function<X, K> jakartaProvider)
    {
        return toJavax(jakarta, k -> new JavaxToJakartaEnumeration(k, javaxProvider, jakartaProvider));
    }

    /**
     * @param <X> the javax type
     * @param <K> the jakarta type
     * @param javax the javax version
     * @param javaxProvider the reference to the {@link JakartaToJavaxWrapper} implementation's provider
     * @param jakartaProvider the reference to the {@link JavaxToJakartaWrapper} implementation's provider
     * @return the jakarta version
     */
    public static <X, K> Enumeration<K> toJakarta(Enumeration<? extends X> javax, Function<K, X> javaxProvider,
        Function<X, K> jakartaProvider)
    {
        return toJakarta(javax, x -> new JakartaToJavaxEnumeration(x, javaxProvider, jakartaProvider));
    }

    /**
     * @param <X> the javax type
     * @param <K> the jakarta type
     * @param jakarta the jakarta version
     * @param javaxProvider the reference to the {@link JakartaToJavaxWrapper} implementation's provider
     * @param jakartaProvider the reference to the {@link JavaxToJakartaWrapper} implementation's provider
     * @return the javax version
     */
    public static <X, K> ListIterator<X> toJavax(ListIterator<? extends K> jakarta, Function<K, X> javaxProvider,
        Function<X, K> jakartaProvider)
    {
        return toJavax(jakarta, k -> new JavaxToJakartaListIterator(k, javaxProvider, jakartaProvider));
    }

    /**
     * @param <X> the javax type
     * @param <K> the jakarta type
     * @param javax the javax version
     * @param javaxProvider the reference to the {@link JakartaToJavaxWrapper} implementation's provider
     * @param jakartaProvider the reference to the {@link JavaxToJakartaWrapper} implementation's provider
     * @return the jakarta version
     */
    public static <X, K> ListIterator<K> toJakarta(ListIterator<? extends X> javax, Function<K, X> javaxProvider,
        Function<X, K> jakartaProvider)
    {
        return toJakarta(javax, x -> new JakartaToJavaxListIterator(x, javaxProvider, jakartaProvider));
    }
}
