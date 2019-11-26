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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.util.ClassUtils;
import org.apache.velocity.util.RuntimeServicesAware;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.Uberspect;
import org.apache.velocity.util.introspection.UberspectImpl;
import org.apache.velocity.util.introspection.VelMethod;
import org.apache.velocity.util.introspection.VelPropertyGet;
import org.apache.velocity.util.introspection.VelPropertySet;

/**
 * <p>
 * Since the current version of the Velocity engine (1.5) does not allow more than one uberspector, this class is a
 * workaround. It manually constructs an <strong>array of uberspectors</strong>, loading the classes in the order
 * defined in the <code>"runtime.introspector.uberspect.arrayClasses"</code> property, and after that forwards calls to
 * each of the uberspectors, in order, until one of them returns something different than <code>null</code>. Note that
 * the calls will be made from the leftmost class to the rightmost one. This allows building and combining different
 * small uberspectors that perform a specialised search for methods, instead of bloating a single class with different
 * introspection tricks.
 * </p>
 * <p>
 * This is not actually part of the array, but is more of a handle that allows the calls intended for only one
 * uberspector to reach the array. It duplicates some of the code from the velocity runtime initialization code, hoping
 * that a future version of the engine will support this natively.
 * </p>
 * <p>
 * The array is defined using the configuration parameter <code>runtime.introspector.uberspect.arrayClasses</code>. This
 * property should contain a list of canonical class names. Any wrong entry in the list will be ignored. If this
 * property is not defined or contains only wrong classnames, then by default a <code>SecureUberspector</code> is used
 * as the only entry in the array.
 * </p>
 *
 * @since 1.5RC1
 * @see ChainingUberspector
 * @version $Id$
 * @deprecated since 8.0M1; chaining uberspectors is much more powerful, this class was never more than a proof of
 *             concept
 */
@Deprecated
public class LinkingUberspector extends UberspectImpl implements Uberspect, RuntimeServicesAware
{
    /** The key of the parameter that allows defining the array of uberspectors. */
    public static final String UBERSPECT_ARRAY_CLASSNAMES = "runtime.introspector.uberspect.arrayClasses";

    /** The array of uberspectors to use. */
    private List<Uberspect> uberspectors;

    /**
     * {@inheritDoc}
     * <p>
     * This implementation initializes the array of uberspectors.
     * </p>
     *
     * @see org.apache.velocity.util.introspection.Uberspect#init()
     */
    @Override
    public void init()
    {
        this.log.debug("Initializing the linking uberspector.");
        // Create the array
        String[] arrayClassnames = this.rsvc.getConfiguration().getStringArray(UBERSPECT_ARRAY_CLASSNAMES);
        this.uberspectors = new ArrayList<>(arrayClassnames.length);
        for (String classname : arrayClassnames) {
            initializeUberspector(classname);
        }
        // If the chain is empty, use a SecureUberspector
        if (this.uberspectors.isEmpty()) {
            this.log.error("No uberspectors defined! "
                + "This uberspector is just a placeholder that relies on at least one real uberspector "
                + "to actually allow method calls. Using SecureUberspector instead as a fallback.");
            initializeUberspector(SecureUberspector.class.getCanonicalName());
        }
    }

    /**
     * Instantiates and initializes an uberspector class and adds it to the array. Also set the log and runtime
     * services, if the class implements the proper interfaces.
     *
     * @param classname The name of the uberspector class to add to the chain.
     */
    protected void initializeUberspector(String classname)
    {
        // Avoids direct recursive calls
        if (!StringUtils.isEmpty(classname) && !classname.equals(this.getClass().getCanonicalName())) {
            Uberspect u = instantiateUberspector(classname);
            if (u == null) {
                return;
            }

            if (u instanceof RuntimeServicesAware) {
                ((RuntimeServicesAware) u).setRuntimeServices(this.rsvc);
            }

            // Initialize the uberspector
            try {
                u.init();
                // Add it to the array
                this.uberspectors.add(u);
            } catch (Exception e) {
                this.log.warn(e.getMessage());
                // If the initialization failed, don't add this uberspector to the chain.
            }
        }
    }

    /**
     * Tries to create an uberspector instance using reflection.
     *
     * @param classname The name of the uberspector class to instantiate.
     * @return An instance of the specified Uberspector. If the class cannot be instantiated using the default
     *         constructor, or does not implement {@link Uberspect}, <code>null</code> is returned.
     */
    protected Uberspect instantiateUberspector(String classname)
    {
        Object o = null;
        try {
            o = ClassUtils.getNewInstance(classname);
        } catch (ClassNotFoundException e) {
            this.log.warn(String.format(
                "The specified uberspector [%s]" + " does not exist or is not accessible to the current classloader.",
                classname));
        } catch (IllegalAccessException e) {
            this.log.warn(
                String.format("The specified uberspector [%s] does not have a public default constructor.", classname));
        } catch (InstantiationException e) {
            this.log.warn(String.format("The specified uberspector [%s] cannot be instantiated.", classname));
        } catch (ExceptionInInitializerError e) {
            this.log.warn(
                String.format("Exception while instantiating the Uberspector [%s]: %s", classname, e.getMessage()));
        }

        if (!(o instanceof Uberspect)) {
            if (o != null) {
                this.log.warn("The specified class for Uberspect [{}] does not implement {}", classname,
                    Uberspect.class.getName());
            }
            return null;
        }
        return (Uberspect) o;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterator getIterator(Object obj, Info i)
    {
        Iterator it;
        for (Uberspect u : this.uberspectors) {
            it = u.getIterator(obj, i);
            if (it != null) {
                return it;
            }
        }
        return null;
    }

    @Override
    public VelMethod getMethod(Object obj, String methodName, Object[] args, Info i)
    {
        VelMethod method;
        for (Uberspect u : this.uberspectors) {
            method = u.getMethod(obj, methodName, args, i);
            if (method != null) {
                return method;
            }
        }
        return null;
    }

    @Override
    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i)
    {
        VelPropertyGet getter;
        for (Uberspect u : this.uberspectors) {
            getter = u.getPropertyGet(obj, identifier, i);
            if (getter != null) {
                return getter;
            }
        }
        return null;
    }

    @Override
    public VelPropertySet getPropertySet(Object obj, String identifier, Object arg, Info i)
    {
        VelPropertySet setter;
        for (Uberspect u : this.uberspectors) {
            setter = u.getPropertySet(obj, identifier, arg, i);
            if (setter != null) {
                return setter;
            }
        }
        return null;
    }
}
