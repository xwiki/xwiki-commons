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
package org.xwiki.component.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xwiki.stability.Unstable;

/**
 * Various Reflection utilities.
 *
 * @version $Id$
 * @since 2.1RC1
 */
public final class ReflectionUtils
{
    private static final String OPEN_GENERIC = "<";

    private static final String CLOSE_GENERIC = ">";

    /**
     * Utility class.
     */
    private ReflectionUtils()
    {
        // Utility class
    }

    /**
     * @param clazz the class for which to return all fields
     * @return all fields declared by the passed class and its superclasses
     */
    public static Collection<Field> getAllFields(Class<?> clazz)
    {
        // Note: use a linked hash map to keep the same order as the one used to declare the fields.
        Map<String, Field> fields = new LinkedHashMap<String, Field>();
        Class<?> targetClass = clazz;
        while (targetClass != null) {
            Field[] targetClassFields;
            try {
                targetClassFields = targetClass.getDeclaredFields();
            } catch (NoClassDefFoundError e) {
                // Provide a better exception message to more easily debug component loading issue.
                // Specifically with this error message we'll known which component failed to be initialized.
                throw new NoClassDefFoundError("Failed to get fields for class [" + targetClass.getName()
                    + "] because the class [" + e.getMessage() + "] couldn't be found in the ClassLoader.");
            }

            for (Field field : targetClassFields) {
                // Make sure that if the same field is declared in a class and its superclass
                // only the field used in the class will be returned. Note that we need to do
                // this check since the Field object doesn't implement the equals method using
                // the field name.
                if (!field.isSynthetic() && !fields.containsKey(field.getName())) {
                    fields.put(field.getName(), field);
                }
            }
            targetClass = targetClass.getSuperclass();
        }
        return fields.values();
    }

    /**
     * @param clazz the class for which to return all fields
     * @param fieldName the name of the field to get
     * @return the field specified from either the passed class or its superclasses
     * @exception NoSuchFieldException if the field doesn't exist in the class or superclasses
     */
    public static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException
    {
        Field resultField = null;
        Class<?> targetClass = clazz;
        while (targetClass != null) {
            try {
                resultField = targetClass.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException e) {
                // Look in superclass
                targetClass = targetClass.getSuperclass();
            }
        }

        if (resultField == null) {
            throw new NoSuchFieldException("No field named [" + fieldName + "] in class [" + clazz.getName()
                + "] or superclasses");
        }

        return resultField;
    }

    /**
     * Extract the main class from the passed {@link Type}.
     *
     * @param type the generic {@link Type}
     * @return the main Class of the generic {@link Type}
     * @since 4.0M1
     */
    public static Class getTypeClass(Type type)
    {
        Class typeClassClass = null;

        if (type instanceof Class) {
            typeClassClass = (Class) type;
        } else if (type instanceof ParameterizedType) {
            typeClassClass = (Class) ((ParameterizedType) type).getRawType();
        } else if (type instanceof GenericArrayType) {
            Class<?> arrrayParameter = getTypeClass(((GenericArrayType) type).getGenericComponentType());
            if (arrrayParameter != null) {
                typeClassClass = Array.newInstance(arrrayParameter, 0).getClass();
            }
        }

        return typeClassClass;
    }

    /**
     * Sets a value to a field using reflection even if the field is private.
     *
     * @param instanceContainingField the object containing the field
     * @param fieldName the name of the field in the object
     * @param fieldValue the value to set for the provided field
     */
    public static void setFieldValue(Object instanceContainingField, String fieldName, Object fieldValue)
    {
        // Find the class containing the field to set
        Class<?> targetClass = instanceContainingField.getClass();
        while (targetClass != null) {
            for (Field field : targetClass.getDeclaredFields()) {
                if (field.getName().equalsIgnoreCase(fieldName)) {
                    try {
                        boolean isAccessible = field.isAccessible();
                        try {
                            field.setAccessible(true);
                            field.set(instanceContainingField, fieldValue);
                        } finally {
                            field.setAccessible(isAccessible);
                        }
                    } catch (Exception e) {
                        // This shouldn't happen but if it does then the Component manager will not function properly
                        // and we need to abort. It probably means the Java security manager has been configured to
                        // prevent accessing private fields.
                        throw new RuntimeException("Failed to set field [" + fieldName + "] in instance of ["
                            + instanceContainingField.getClass().getName() + "]. The Java Security Manager has "
                            + "probably been configured to prevent settting private field values. XWiki requires "
                            + "this ability to work.", e);
                    }
                    return;
                }
            }
            targetClass = targetClass.getSuperclass();
        }
    }

    /**
     * Extract the last generic type from the passed field. For example <tt>private List&lt;A, B&gt; field</tt> would
     * return the {@code B} class.
     *
     * @param field the field from which to extract the generic type
     * @return the class of the last generic type or null if the field doesn't have a generic type
     */
    public static Class<?> getLastGenericFieldType(Field field)
    {
        return getTypeClass(getLastFieldGenericArgument(field));
    }

    /**
     * Extract the last generic type from the passed field. For example <tt>private List&lt;A, B&gt; field</tt> would
     * return the {@code B} class.
     *
     * @param field the field from which to extract the generic type
     * @return the type of the last generic type or null if the field doesn't have a generic type
     * @since 4.0M1
     */
    public static Type getLastFieldGenericArgument(Field field)
    {
        return getLastTypeGenericArgument(field.getGenericType());
    }

    /**
     * Extract the last generic type from the passed Type. For example <tt>private List&lt;A, B&gt; field</tt> would
     * return the {@code B} class.
     *
     * @param type the type from which to extract the generic type
     * @return the type of the last generic type or null if the field doesn't have a generic type
     * @since 4.0M1
     */
    public static Type getLastTypeGenericArgument(Type type)
    {
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            Type[] types = pType.getActualTypeArguments();
            if (types.length > 0) {
                return types[types.length - 1];
            }
        }

        return null;
    }

    /**
     * Extract the last generic type from the passed class. For example
     * <tt>public Class MyClass implements FilterClass&lt;A, B&gt;, SomeOtherClass&lt;C&gt;</tt> will return {@code B}.
     *
     * @param clazz the class to extract from
     * @param filterClass the class of the generic type we're looking for
     * @return the last generic type from the interfaces of the passed class, filtered by the passed filter class
     */
    public static Class<?> getLastGenericClassType(Class clazz, Class filterClass)
    {
        Type type = getGenericClassType(clazz, filterClass);

        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            if (filterClass.isAssignableFrom((Class) pType.getRawType())) {
                Type[] actualTypes = pType.getActualTypeArguments();
                if (actualTypes.length > 0 && actualTypes[actualTypes.length - 1] instanceof Class) {
                    return (Class) actualTypes[actualTypes.length - 1];
                }
            }
        }

        return null;
    }

    /**
     * Extract the real Type from the passed class. For example
     * <tt>public Class MyClass implements FilterClass&lt;A, B&gt;, SomeOtherClass&lt;C&gt;</tt> will return
     * <tt>FilterClass&lt;A, B&gt;, SomeOtherClass&lt;C&gt;</tt>.
     *
     * @param clazz the class to extract from
     * @param filterClass the class of the generic type we're looking for
     * @return the real Type from the interfaces of the passed class, filtered by the passed filter class
     * @since 4.0M1
     */
    public static Type getGenericClassType(Class clazz, Class filterClass)
    {
        for (Type type : clazz.getGenericInterfaces()) {
            if (type == filterClass) {
                return type;
            } else if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                if (filterClass.isAssignableFrom((Class) pType.getRawType())) {
                    return type;
                }
            }
        }

        return null;
    }

    /**
     * @param parameters the parameters of a direct superclass or interface
     * @param childType a extending class as Type
     * @return the actual parameters of the direct superclass or interface, return null if it's impossible to resolve
     */
    public static Type[] resolveSuperArguments(Type[] parameters, Type childType)
    {
        Type[] resolvedPrameters = null;

        if (parameters != null && childType instanceof ParameterizedType) {
            ParameterizedType parameterizedChildType = (ParameterizedType) childType;

            return resolveSuperArguments(parameters, parameterizedChildType.getClass(),
                parameterizedChildType.getActualTypeArguments());
        }

        return resolvedPrameters;
    }

    /**
     * @param parameters the parameters of a direct superclass or interface
     * @param childClass an extending class
     * @param childParameters the actual parameters of the extending class
     * @return the actual parameters of the direct superclass or interface, return null if it's impossible to resolve
     */
    public static Type[] resolveSuperArguments(Type[] parameters, Class childClass, Type[] childParameters)
    {
        Map<TypeVariable, Type> typeMapping;
        if (childParameters != null) {
            TypeVariable<Class>[] declaredChildParameters = childClass.getTypeParameters();

            typeMapping = new HashMap<TypeVariable, Type>();
            for (int i = 0; i < declaredChildParameters.length; ++i) {
                typeMapping.put(declaredChildParameters[i], childParameters[i]);
            }
        } else {
            typeMapping = Collections.emptyMap();
        }

        return resolveTypes(parameters, typeMapping);
    }

    /**
     * @param type the type to resolve
     * @param typeMapping the mapping between TypeVariable and real type
     * @return the resolved type, the passed type it does not need to be resolved or null if it can't be resolved
     */
    public static Type resolveType(Type type, Map<TypeVariable, Type> typeMapping)
    {
        Type resolvedType = type;

        if (type instanceof TypeVariable) {
            if (typeMapping == null) {
                return null;
            }

            resolvedType = typeMapping.get(type);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            Type[] arguments = parameterizedType.getActualTypeArguments();
            Type[] resolvedArguments = resolveTypes(arguments, typeMapping);

            if (resolvedArguments != arguments) {
                resolvedType =
                    new DefaultParameterizedType(parameterizedType.getOwnerType(),
                        (Class<?>) parameterizedType.getRawType(), resolvedArguments);
            } else {
                resolvedType = type;
            }
        }

        return resolvedType;
    }

    /**
     * @param types the types to resolve
     * @param typeMapping the mapping between TypeVariable and real type
     * @return the resolved types, the passed types if nothing need to be resolved or null if it can't be fully resolved
     */
    private static Type[] resolveTypes(Type[] types, Map<TypeVariable, Type> typeMapping)
    {
        Type[] resolvedTypes = types;

        for (int i = 0; i < types.length; ++i) {
            Type type = types[i];
            Type resovedType = resolveType(type, typeMapping);

            if (resovedType == null) {
                return null;
            }

            if (resovedType != type) {
                if (resolvedTypes == types) {
                    resolvedTypes = new Type[types.length];
                    for (int j = 0; j < i; ++j) {
                        resolvedTypes[j] = types[j];
                    }
                }
            }

            if (resolvedTypes != types) {
                resolvedTypes[i] = resovedType;
            }
        }

        return resolvedTypes;
    }

    /**
     * Find and replace the generic parameters with the real types.
     *
     * @param targetType the type for which to resolve the parameters
     * @param rootType an extending class as Type
     * @return the Type with resolved parameters
     */
    public static Type resolveType(Type targetType, Type rootType)
    {
        Type resolvedType;

        if (targetType instanceof ParameterizedType && rootType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) targetType;

            resolvedType =
                resolveType((Class<?>) parameterizedType.getRawType(), parameterizedType.getActualTypeArguments(),
                    getTypeClass(rootType));
        } else {
            resolvedType = resolveType(getTypeClass(rootType), null, getTypeClass(targetType));
        }

        return resolvedType;
    }

    /**
     * Find the real generic parameters of the passed target class from the extending/implementing root class and create
     * a Type from it.
     *
     * @param rootClass the root class from which to start searching
     * @param parameters the parameters of the root class
     * @param targetClass the class from which to resolve the generic parameters
     * @return a {@link ParameterizedType} version of the passed target class with resolved parameters
     */
    private static Type resolveType(Class<?> rootClass, Type[] parameters, Class<?> targetClass)
    {
        // Look at super interfaces
        for (Type interfaceType : rootClass.getGenericInterfaces()) {
            Type type = resolveType(interfaceType, rootClass, parameters, targetClass);
            if (type != null) {
                return type;
            }
        }

        Type superType = rootClass.getGenericSuperclass();
        if (superType != null) {
            return resolveType(superType, rootClass, parameters, targetClass);
        }

        return null;
    }

    /**
     * @param superType the type implemented/extended by the root class
     * @param rootClass the class containing the parameters
     * @param parameters the generic parameters
     * @param targetClass the target class
     * @return the passed type with the real parameters
     */
    private static Type resolveType(Type superType, Class<?> rootClass, Type[] parameters, Class<?> targetClass)
    {
        Type newType = superType;

        Class<?> interfaceClass;
        Type[] interfaceParameters;

        if (newType instanceof ParameterizedType) {
            ParameterizedType interfaceParameterizedType = (ParameterizedType) newType;

            interfaceClass = ReflectionUtils.getTypeClass(newType);
            Type[] variableParameters = interfaceParameterizedType.getActualTypeArguments();

            interfaceParameters = ReflectionUtils.resolveSuperArguments(variableParameters, rootClass, parameters);

            if (interfaceParameters == null) {
                newType = interfaceClass;
            } else if (interfaceParameters != variableParameters) {
                newType =
                    new DefaultParameterizedType(interfaceParameterizedType.getOwnerType(), interfaceClass,
                        interfaceParameters);
            }
        } else if (newType instanceof Class) {
            interfaceClass = (Class<?>) newType;
            interfaceParameters = null;
        } else {
            return null;
        }

        if (interfaceClass == targetClass) {
            return newType;
        }

        return resolveType(interfaceClass, interfaceParameters, targetClass);
    }

    /**
     * Retrieve a {@link Type} object from it's serialized form.
     *
     * @param serializedType the serialized form of the {@link Type} to retrieve
     * @param classLoader the {@link ClassLoader} to look into to find the given {@link Type}
     * @return the type built from the given {@link String}
     * @throws ClassNotFoundException if no class corresponding to the passed serialized type can be found
     */
    public static Type unserializeType(String serializedType, ClassLoader classLoader) throws ClassNotFoundException
    {
        String sType = serializedType.replaceAll(" ", "");
        Type type = null;

        // A real parser could be used here but it would probably be overkill.
        if (sType.contains(OPEN_GENERIC)) {
            // Parameterized type
            int firstInferior = sType.indexOf(OPEN_GENERIC);
            int lastSuperior = sType.lastIndexOf(CLOSE_GENERIC);
            String rawType = sType.substring(0, firstInferior);
            String sArguments = sType.substring(firstInferior + 1, lastSuperior);
            List<Type> argumentTypes = new ArrayList<Type>();
            int nestedArgsDepth = 0;
            int previousSplit = 0;
            // We'll go through all the Type arguments and they will be unserialized, since arguments can be
            // ParameterizedTypes themselves we need to avoid parsing their arguments, that's why we need the
            // nestedArgsDepth counter.
            for (int i = 0; i < sArguments.length(); i++) {
                char current = sArguments.charAt(i);
                switch (current) {
                    case '<':
                        nestedArgsDepth++;
                        break;
                    case '>':
                        nestedArgsDepth--;
                        break;
                    case ',':
                        if (nestedArgsDepth == 0) {
                            argumentTypes.add(unserializeType(sArguments.substring(previousSplit, i), classLoader));
                            previousSplit = i + 1;
                        }
                        break;
                    default:
                        break;
                }
                if (i == sArguments.length() - 1) {
                    // We're at the end of the parameter list, we need to unserialize the Type of the last element.
                    // If there was only one argument it will be unserialized here.
                    argumentTypes.add(unserializeType(sArguments.substring(previousSplit), classLoader));
                }
            }

            type =
                new DefaultParameterizedType(null, Class.forName(rawType, false, classLoader),
                    argumentTypes.toArray(new Type[1]));
        } else {
            // This was a simple type, no type arguments were found.
            type = Class.forName(sType, false, classLoader);
        }

        return type;
    }

    /**
     * Get the first found annotation with the provided class directly assigned to the provided {@link AnnotatedElement}
     * .
     *
     * @param <T> the type of the annotation
     * @param annotationClass the annotation class
     * @param element the class on which annotation are assigned
     * @return the found annotation or null if there is none
     */
    public static <T extends Annotation> T getDirectAnnotation(Class<T> annotationClass, AnnotatedElement element)
    {
        // Handle interfaces directly declared in the passed component class
        for (Annotation annotation : element.getDeclaredAnnotations()) {
            if (annotation.annotationType() == annotationClass) {
                return (T) annotation;
            }
        }

        return null;
    }

    /**
     * @param type the type from which to extract super type and interfaces
     * @return the direct super type and interfaces for the provided type
     */
    public static List<Type> getDirectTypes(Type type)
    {
        Class<?> clazz = getTypeClass(type);

        if (clazz == null) {
            return Collections.emptyList();
        }

        List<Type> types = new LinkedList<Type>();

        for (Type interfaceType : clazz.getGenericInterfaces()) {
            types.add(resolveType(interfaceType, type));
        }

        Type superType = clazz.getGenericSuperclass();
        if (superType != null) {
            types.add(resolveType(superType, type));
        }

        return types;
    }

    private static String getTypeName(Type type)
    {
        if (type instanceof Class) {
            return ((Class) type).getName();
        } else {
            return type.getTypeName();
        }
    }

    /**
     * Serialize a type in a String using a standard definition.
     * @param type the type to serialize.
     * @return a string representing this type.
     * @since 11.2RC1
     */
    @Unstable
    public static String serializeType(Type type)
    {
        if (type == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            String rawTypeName = getTypeName(parameterizedType.getRawType());

            if (parameterizedType.getOwnerType() != null) {
                if (parameterizedType.getOwnerType() instanceof Class) {
                    sb.append(((Class<?>) parameterizedType.getOwnerType()).getName());
                } else {
                    sb.append(parameterizedType.getOwnerType().toString());
                }

                sb.append('.');


                if (parameterizedType.getOwnerType() instanceof ParameterizedType) {
                    // Find simple name of nested type by removing the
                    // shared prefix with owner.
                    sb.append(rawTypeName.replace(
                        ((Class<?>) ((ParameterizedType) parameterizedType.getOwnerType()).getRawType())
                            .getName() + '$', ""));
                } else {
                    sb.append(rawTypeName);
                }
            } else {
                sb.append(rawTypeName);
            }

            if (parameterizedType.getActualTypeArguments() != null
                && parameterizedType.getActualTypeArguments().length > 0) {
                sb.append(OPEN_GENERIC);

                boolean first = true;
                for (Type typeArgument : parameterizedType.getActualTypeArguments()) {
                    if (!first) {
                        sb.append(", ");
                    }
                    sb.append(getTypeName(typeArgument));
                    first = false;
                }

                sb.append(CLOSE_GENERIC);
            }
        } else {
            sb.append(getTypeName(type));
        }

        return sb.toString();
    }
}
