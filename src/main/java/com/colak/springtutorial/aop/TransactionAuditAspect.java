package com.colak.springtutorial.aop;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

@Aspect
@Component
@Slf4j
public class TransactionAuditAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Around("execution(* org.springframework.data.repository.CrudRepository.save*(..)) || " +
            "execution(* org.springframework.data.repository.CrudRepository.delete*(..)) || " +
            "execution(* org.springframework.data.jpa.repository.JpaRepository.save*(..)) || " +
            "execution(* org.springframework.data.jpa.repository.JpaRepository.delete*(..))")
    public Object aroundCrudOperations(ProceedingJoinPoint joinPoint) throws Throwable {

        // Skip if not inside an @AuditChanges method
        if (!isAuditChangesMethod()) {
            return joinPoint.proceed();
        }

        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("No active transaction exists for " + joinPoint.getSignature());
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        Object[] args = joinPoint.getArgs();

        Object entity = null;

        if (args.length > 0) {
            entity = args[0];
        }

        if (entity == null) {
            throw new IllegalArgumentException("First argument cannot be null for " + joinPoint.getSignature());
        }

        if (isSaveMethod(methodName)) {
            Object oldEntity = null;

            Object primaryKey = entityManager.getEntityManagerFactory()
                    .getPersistenceUnitUtil().getIdentifier(entity);
            if (primaryKey != null) {
                oldEntity = entityManager.find(entity.getClass(), primaryKey);
            }

            if (oldEntity != null) {
                writeUpdateChanges(oldEntity, entity); // This is an update
            } else {
                writeCreateChanges(entity); // This a creation
            }
        } else if (isDeleteMethod(methodName)) {
            writeDeleteChanges(entity);
        }

        // Execute the actual CRUD operation
        return joinPoint.proceed();
    }


    private boolean isAuditChangesMethod() {
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

    private Method findMethod(Class<?> clazz, String methodName) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private boolean isSaveMethod(String methodName) {
        return methodName.startsWith("save");
    }

    private boolean isDeleteMethod(String methodName) {
        return methodName.startsWith("delete");
    }

    private void writeDeleteChanges(Object entity) {
        if (entity == null) return;

        Class<?> clazz = entity.getClass();

        log.info("Object is deleted");

        // AuditLog auditLog = new AuditLog(entityName, entityId, "DELETED", "EXISTED", "DELETED");
        // auditLogService.saveAuditLog(auditLog);
    }

    private void writeCreateChanges(Object entity) {
    }

    private void writeUpdateChanges(Object oldEntity, Object newEntity) {

        Class<?> clazz = oldEntity.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object oldValue = field.get(oldEntity);
                Object newValue = field.get(newEntity);

                if (!Objects.equals(oldValue, newValue)) {
                    log.info("Fields are different");

                    // AuditLog auditLog = new AuditLog(entityName, entityId, field.getName(),
                    //         oldValue != null ? oldValue.toString() : "null",
                    //         newValue != null ? newValue.toString() : "null");
                    //
                    // auditLogService.saveAuditLog(auditLog);
                }
            } catch (IllegalAccessException exception) {
                log.error("IllegalAccessException", exception);
            }
        }
    }
}
