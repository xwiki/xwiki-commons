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

import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.util.RuntimeServicesAware;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.VelMethod;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.properties.ConverterManager;

/**
 * Chainable Velocity Uberspector that tries to convert method arguments to formal parameter types when the passed
 * arguments don't match the method signature. In other words, it looks for a method matching the passed arguments and
 * if none is found then it tries the convert the arguments to match the available method signatures (the available
 * methods with the same name and the same number of parameters but with different parameter types). E.g.:
 * 
 * <pre>
 * {@code $obj.someMethod('VALUE')
 * // will forward to
 * obj.someMethod(SomeEnum.VALUE)
 * // if obj has someMethod(SomeEnum) and not someMethod(String)}
 * </pre>
 * 
 * @since 4.1M2
 * @version $Id$
 * @see ChainableUberspector
 */
public class MethodArgumentsUberspector extends AbstractChainableUberspector implements RuntimeServicesAware
{
    /**
     * The component used to convert method arguments to formal parameter types.
     */
    private ConverterManager converterManager;

    @Override
    public void setRuntimeServices(RuntimeServices runtimeServices)
    {
        ComponentManager componentManager =
            (ComponentManager) runtimeServices.getApplicationAttribute(ComponentManager.class.getName());
        try {
            this.converterManager = componentManager.getInstance(ConverterManager.class);
        } catch (ComponentLookupException e) {
            this.log.warn("Failed to initialize " + this.getClass().getSimpleName(), e);
        }
    }

    @Override
    public VelMethod getMethod(Object obj, String methodName, Object[] args, Info i) throws Exception
    {
        VelMethod method = super.getMethod(obj, methodName, args, i);
        if (method == null && this.converterManager != null) {
            // Try to convert method arguments to formal parameter types.
            Object[] convertedArguments = this.convertArguments(obj, methodName, args);
            if (convertedArguments != null) {
                method = super.getMethod(obj, methodName, convertedArguments, i);
                if (method != null) {
                    // Overwrite the given arguments because they are used to invoke the method.
                    System.arraycopy(convertedArguments, 0, args, 0, args.length);
                }
            }
        }
        return method;
    }

    /**
     * Converts the given arguments to match a method with the specified name and the same number of formal parameters
     * as the number of arguments.
     * 
     * @param obj the object the method is invoked on, used to retrieve the list of available methods
     * @param methodName the method we're looking for
     * @param args the method arguments
     * @return a new array of arguments where some values have been converted to match the formal method parameter
     *         types, {@code null} if no such method is found
     */
    private Object[] convertArguments(Object obj, String methodName, Object[] args)
    {
        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().equalsIgnoreCase(methodName) && method.getParameterTypes().length == args.length) {
                try {
                    return convertArguments(args, method.getParameterTypes());
                } catch (Exception e) {
                    // Ignore and try the next method.
                }
            }
        }
        return null;
    }

    /**
     * Tries to convert the given arguments to match the specified formal parameters types.
     * <p>
     * Throws a runtime exception if the conversion fails.
     * 
     * @param arguments the method actual arguments
     * @param parameterTypes the method formal parameter types
     * @return a new array of arguments where some values have been converted to match the formal method parameter types
     */
    private Object[] convertArguments(Object[] arguments, Class< ? >[] parameterTypes)
    {
        Object[] convertedArguments = Arrays.copyOf(arguments, arguments.length);
        for (int i = 0; i < parameterTypes.length; i++) {
            // Try to convert the argument if it's not null and if it doesn't match the parameter type.
            if (arguments[i] != null && !parameterTypes[i].isInstance(arguments[i])) {
                convertedArguments[i] = this.converterManager.convert(parameterTypes[i], arguments[i]);
            }
        }
        return convertedArguments;
    }
}
