package com.wychmod.middleware.db.router.util;

import jdk.internal.org.objectweb.asm.tree.TryCatchBlockNode;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @description: 属性工具类
 * @author: wychmod
 * @date: 2022/11/2
 */
public class PropertyUtil {

    private static int springBootVersion = 1;

    // JVM加载类时会执行这些静态代码块
    static {
        try {
            Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
        } catch (ClassNotFoundException e) {
            springBootVersion = 2;
        }
    }

    /**
     * Spring Boot 1.x is compatible with Spring Boot 2.x by Using Java Reflect.
     *
     * @param environment : the environment context
     * @param prefix      : the prefix part of property key
     * @param targetClass : the target class type of result
     * @param <T>         : refer to @param targetClass
     * @return T
     */
    @SuppressWarnings("unchecked")
    public static <T> T handle(final Environment environment, final String prefix, final Class<T> targetClass) {
        switch (springBootVersion) {
            case 1:
                return (T) v1(environment, prefix);
            default:
                return (T) v2(environment, prefix, targetClass);
        }
    }

    private static Object v1(final Environment environment, final String prefix) {
        try {
            // 获得class对象
            Class<?> resolverClass = Class.forName("org.springframework.boot.bind.RelaxedPropertyResolver");
            // 获得构造方法 需要传入参数类型是PropertyResolver  Environment继承PropertyResolver
            Constructor<?> resolverConstructor = resolverClass.getDeclaredConstructor(PropertyResolver.class);
            // 获得getSubProperties 方法
            Method getSubPropertiesMethod = resolverClass.getDeclaredMethod("getSubProperties", String.class);
            // 根据构造方法来生成实例
            Object resolverObject = resolverConstructor.newInstance(environment);
            String prefixParam = prefix.endsWith(".") ? prefix : prefix + ".";
            // 调用resolverObject实例的getSubPropertiesMethod方法，传入prefixParam
            return getSubPropertiesMethod.invoke(resolverObject, prefixParam);

        } catch (final ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException
                | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private static Object v2(final Environment environment, final String prefix, final Class<?> targetClass) {
        try {
            Class<?> binderClass = Class.forName("org.springframework.boot.context.properties.bind.Binder");
            Method getMethod = binderClass.getDeclaredMethod("get", Environment.class);
            Method bindMethod = binderClass.getDeclaredMethod("bind", String.class, Class.class);
            Object binderObject = getMethod.invoke(null, environment);
            String prefixParam = prefix.endsWith(".") ? prefix.substring(0, prefix.length() - 1) : prefix;
            Object bindResultObject = bindMethod.invoke(binderObject, prefixParam, targetClass);
            Method resultGetMethod = bindResultObject.getClass().getDeclaredMethod("get");
            return resultGetMethod.invoke(bindResultObject);
        } catch (final ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

}
