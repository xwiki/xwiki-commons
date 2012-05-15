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

import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.VelMethod;

/**
 * Chainable Velocity Uberspector that replaces {@link String} method arguments with {@link Enum} constants if needed.
 * If the specified method is not found by the default introspector then it looks for the first method with the same
 * name that expects {@link Enum} constants instead of strings and returns it. E.g.:
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
public class EnumMethodParameterUberspector extends AbstractChainableUberspector
{
    @Override
    public VelMethod getMethod(Object obj, String methodName, Object[] args, Info i) throws Exception
    {
        VelMethod method = super.getMethod(obj, methodName, args, i);
        if (method == null) {
            Object[] argumentsWithEnum = getArgumentsWithEnum(obj, methodName, args);
            if (argumentsWithEnum != null) {
                method = super.getMethod(obj, methodName, argumentsWithEnum, i);
                if (method != null) {
                    // Overwrite the given arguments because they are used to invoke the method.
                    System.arraycopy(argumentsWithEnum, 0, args, 0, args.length);
                }
            }
        }
        return method;
    }

    /**
     * Looks for a method with the specified name that expects {@link Enum} constants instead of the provided
     * {@link String} arguments and for which the provided {@link String} arguments correspond to valid {@link Enum}
     * values.
     * 
     * @param obj the object the method is invoked on
     * @param methodName the method name
     * @param args the method arguments
     * @return a new array of arguments where {@link String} values have been replaced with {@link Enum} constants if
     *         possible, {@code null} if no such method is found
     */
    private Object[] getArgumentsWithEnum(Object obj, String methodName, Object[] args)
    {
        for (Method method : obj.getClass().getMethods()) {
            if (method.getName().equalsIgnoreCase(methodName)) {
                Object[] argumentsWithEnum = getArgumentsWithEnum(method.getParameterTypes(), args);
                if (argumentsWithEnum != null) {
                    return argumentsWithEnum;
                }
            }
        }
        return null;
    }

    /**
     * Replaces the {@link String} arguments with {@link Enum} constants if the {@link String} arguments are valid
     * {@link Enum} values, matching the given parameter types.
     * 
     * @param parameterTypes the method formal parameter types
     * @param arguments the method actual arguments
     * @return a new array of arguments where {@link String} values have been replaced with {@link Enum} constants to
     *         match the method parameter types, {@code null} if the given {@link String} arguments don't match
     *         {@link Enum} parameter types
     */
    private Object[] getArgumentsWithEnum(Class< ? >[] parameterTypes, Object[] arguments)
    {
        Object[] argumentsWithEnum = null;
        if (parameterTypes.length == arguments.length) {
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i].isEnum() && arguments[i] instanceof String) {
                    try {
                        @SuppressWarnings({ "unchecked", "rawtypes" })
                        Object value = Enum.valueOf((Class< ? extends Enum>) parameterTypes[i], (String) arguments[i]);
                        if (argumentsWithEnum == null) {
                            argumentsWithEnum = Arrays.copyOf(arguments, arguments.length);
                        }
                        argumentsWithEnum[i] = value;
                    } catch (Exception e) {
                        // Ignore.
                    }
                }
            }
        }
        return argumentsWithEnum;
    }
}
