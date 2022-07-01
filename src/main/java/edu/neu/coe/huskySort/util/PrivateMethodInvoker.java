/*
 * Copyright (c) 2018. Phasmid Software
 */

package edu.neu.coe.huskySort.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PrivateMethodInvoker {

    private final Object object;
    private final Class<?> clazz;

    public PrivateMethodInvoker(final Object object, final Class<?> clazz) {
        this.object = object;
        this.clazz = clazz;
    }

    public PrivateMethodInvoker(final Object object) {
        this(object, object.getClass());
    }

    public PrivateMethodInvoker(final Class<?> clazz) {
        this(null, clazz);
    }

    /**
     * Method to invoke a private method on the object of this PrivateMethodInvoker
     *
     * @param name       the name of the private method
     * @param parameters a variable number of parameters, each of which determines its corresponding class
     * @return the result of invoking the named private method with the given parameters.
     */
    public Object invokePrivate(final String name, final Object... parameters) {
        return findAndInvokePrivateMethod(name, getClasses(parameters), parameters, true);
    }

    /**
     * Method to invoke a private method on the object of this PrivateMethodInvoker but where we look for a method matching an explicit set of parameter classes.
     *
     * @param name       the name of the private method
     * @param classes    the classes of the corresponding parameters
     * @param parameters a variable number of parameters, each of which determines its corresponding class
     * @return the result of invoking the named private method with the given parameters.
     */
    public Object invokePrivateExplicit(final String name, final Class<?>[] classes, final Object... parameters) {
        final int length = parameters.length;
        if (classes.length != length) throw new RuntimeException(name + ": number of classes " + classes.length +
                " does not match the number of parameters: " + length);
        return findAndInvokePrivateMethod(name, classes, parameters, false);
    }

    private Object findAndInvokePrivateMethod(final String name, final Class<?>[] classes, final Object[] parameters, final boolean allowSubstitutions) {
        try {
            final Method m = getPrivateMethod(name, classes, classes.length, allowSubstitutions);
            return invokePrivateMethod(m, parameters);
        } catch (final NoSuchMethodException e) {
            final StringBuilder sb = new StringBuilder();
            final Method[] declaredMethods = clazz.getDeclaredMethods();
            for (final Method m : declaredMethods) sb.append(m).append(", ");
            throw new RuntimeException(name + ": method not found for given " + classes.length +
                    " parameter classes [did you consider that the method might be declared for a superclass or interface of one or more of your parameters? If so, use the invokePrivateExplicit method].\nHere is a list of declared methods: " + sb);
        }
    }

    private Method getPrivateMethod(final String name, final Class<?>[] classes, final int length, final boolean allowSubstitutions) throws NoSuchMethodException {
        if (length == 0) return getPrivateMethodNoParams(name);
        else return getPrivateMethodParams(name, classes, length, allowSubstitutions);
    }

    private Method getPrivateMethodParams(final String name, final Class<?>[] classes, final int length, final boolean allowSubstitutions) throws NoSuchMethodException {
        try {
            return findPrivateMethod(name, classes);
        } catch (final NoSuchMethodException nsme) {
            if (allowSubstitutions)
                return getMethodWithSubstitutions(name, classes, length);
            else
                throw nsme;
        }
    }

    private Method getMethodWithSubstitutions(final String name, final Class<?>[] classes, final int length) throws NoSuchMethodException {
        for (int i = 0; i < getCombinations(length); i++) {
            final Class<?>[] effectiveClasses = new Class<?>[length];
            System.arraycopy(classes, 0, effectiveClasses, 0, length);
            try {
                return getPrivateMethod(name, classes, i, effectiveClasses);
            } catch (final NoSuchMethodException e) {
                // NOTE: Ignore this exception: we keep looking in subsequent combinations of effective classes
            }
        }
        throw new NoSuchMethodException("private method " + name + " with " + classes.length + " parameters");
    }

    private Method getPrivateMethodNoParams(final String name) throws NoSuchMethodException {
        return findPrivateMethod(name, new Class<?>[0]);
    }

    private Method getPrivateMethod(final String name, final Class<?>[] classes, final int i, final Class<?>[] effectiveClasses) throws NoSuchMethodException {
        // TODO: This method will substitute primitive classes for object classes. But we need to substitute superclasses and interfaces as well.
        for (int j = 0; j < classes.length; j++) {
            if (((i >> j) & 1) == 1) effectiveClasses[j] = getPrimitiveClass(classes[j]);
            try {
                return findPrivateMethod(name, effectiveClasses);
            } catch (final NoSuchMethodException nsme) {
                // NOTE: Ignore this exception: we keep looking with different effective classes
            }
        }
        throw new NoSuchMethodException("private method " + name + " with " + classes.length + " parameters for combination " + i);
    }

    private Object invokePrivateMethod(final Method m, final Object[] parameters) {
        try {
            if (m != null)
                return m.invoke(object, parameters);
            else throw new RuntimeException("method to be invoked is null");
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (final InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }

    private Method findPrivateMethod(final String name, final Class<?>[] classes) throws NoSuchMethodException {
        try {
            final Method m = clazz.getDeclaredMethod(name, classes);
            m.setAccessible(true);
            return m;
        } catch (final NoSuchMethodException e) {
            // NOTE: we are trying to get a method from a super-class. Will this break anything???
            final Method m = clazz.getMethod(name, classes);
            m.setAccessible(true);
            return m;
        }
    }

    private static int getCombinations(final int length) {
        int all = 1;
        for (int i = 0; i < length; i++) all *= 2;
        return all;
    }

    private static Class<?>[] getClasses(final Object[] parameters) {
        final Class<?>[] classes = new Class<?>[parameters.length];
        for (int i = 0; i < parameters.length; i++) classes[i] = parameters[i].getClass();
        return classes;
    }

    private static Class<?> getPrimitiveClass(final Class<?> clazz) {
        if (clazz == Integer.class)
            return int.class;
        else if (clazz == Long.class)
            return long.class;
        else if (clazz == Double.class)
            return double.class;
        else if (clazz == Float.class)
            return float.class;
        else if (clazz == Boolean.class)
            return boolean.class;
        else if (clazz == Character.class)
            return char.class;
        else if (clazz == Byte.class)
            return byte.class;
        else
            return clazz;
    }
}
