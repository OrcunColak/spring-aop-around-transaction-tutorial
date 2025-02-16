package com.colak.springtutorial.aop;

import java.lang.reflect.Method;

public class AnnotationChecker {

    static boolean isAnnotationPresent() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTrace) {
            try {
                Class<?> clazz = Class.forName(element.getClassName());
                Method method = findMethod(clazz, element.getMethodName());
                if (method != null && method.isAnnotationPresent(AuditChanges.class)) {
                    return true;
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
        return false;
    }

    private static Method findMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }
}
