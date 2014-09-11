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
package org.xwiki.velocity.introspection;

import java.util.Iterator;

import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.Uberspect;
import org.apache.velocity.util.introspection.UberspectImpl;
import org.apache.velocity.util.introspection.VelMethod;
import org.apache.velocity.util.introspection.VelPropertyGet;
import org.apache.velocity.util.introspection.VelPropertySet;

/**
 * Default implementation of a {@link ChainableUberspector chainable uberspector} that forwards all calls to the wrapped
 * uberspector (when that is possible). It should be used as the base class for all chainable uberspectors.
 *
 * @version $Id$
 * @since 1.5M1
 * @see ChainableUberspector
 */
public abstract class AbstractChainableUberspector extends UberspectImpl implements ChainableUberspector
{
    /** The wrapped (decorated) uberspector. */
    protected Uberspect inner;

    @Override
    public void wrap(Uberspect inner)
    {
        this.inner = inner;
    }

    @Override
    public void init()
    {
        if (this.inner != null) {
            try {
                this.inner.init();
            } catch (Exception e) {
                this.log.error(e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator getIterator(Object obj, Info i) throws Exception
    {
        return (this.inner != null) ? this.inner.getIterator(obj, i) : null;
    }

    @Override
    public VelMethod getMethod(Object obj, String methodName, Object[] args, Info i) throws Exception
    {
        return (this.inner != null) ? this.inner.getMethod(obj, methodName, args, i) : null;
    }

    @Override
    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i) throws Exception
    {
        return (this.inner != null) ? this.inner.getPropertyGet(obj, identifier, i) : null;
    }

    @Override
    public VelPropertySet getPropertySet(Object obj, String identifier, Object arg, Info i) throws Exception
    {
        return (this.inner != null) ? this.inner.getPropertySet(obj, identifier, arg, i) : null;
    }
}
