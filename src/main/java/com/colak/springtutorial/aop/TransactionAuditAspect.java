package com.colak.springtutorial.aop;

import com.colak.springtutorial.aop.impl.ChangeManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class TransactionAuditAspect {

    @PersistenceContext
    private EntityManager entityManager;

    private ChangeManager changeManager;

    @Autowired
    public void setChangeManager(ChangeManager changeManager) {
        this.changeManager = changeManager;
    }

    @Around("execution(* org.springframework.data.repository.CrudRepository.save*(..)) || " +
            "execution(* org.springframework.data.repository.CrudRepository.delete*(..)) || " +
            "execution(* org.springframework.data.jpa.repository.JpaRepository.save*(..)) || " +
            "execution(* org.springframework.data.jpa.repository.JpaRepository.delete*(..))")
    public Object aroundCrudOperations(ProceedingJoinPoint joinPoint) throws Throwable {

        // Skip if not inside an @AuditChanges method
        if (!AnnotationChecker.isAnnotationPresent()) {
            return joinPoint.proceed();
        }

        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            log.info("No active transaction exists for {}. We can not generate Changes rows", joinPoint.getSignature());
            return joinPoint.proceed();
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        Object[] args = joinPoint.getArgs();

        Object entity = null;
        Object oldEntity = null;
        if (args.length > 0) {
            entity = args[0];
        }

        if (entity == null) {
            log.info("First argument is null for {}. We can not generate Changes rows", joinPoint.getSignature());
            return joinPoint.proceed();
        }

        ChangeManager.OperationType operationType = ChangeManager.OperationType.IGNORE;
        if (isSaveMethod(methodName)) {

            Object primaryKey = entityManager.getEntityManagerFactory()
                    .getPersistenceUnitUtil().getIdentifier(entity);
            if (primaryKey != null) {
                oldEntity = entityManager.find(entity.getClass(), primaryKey);
            }

            if (oldEntity != null) {
                operationType = ChangeManager.OperationType.UPDATE;
            } else {
                operationType = ChangeManager.OperationType.CREATE;
            }
        } else if (isDeleteMethod(methodName)) {
            operationType = ChangeManager.OperationType.DELETE;
        }


        // Execute the actual CRUD operation
        Object result = joinPoint.proceed();

        changeManager.writeChange(entity, oldEntity, operationType);

        return result;
    }


    private boolean isSaveMethod(String methodName) {
        return methodName.startsWith("save");
    }

    private boolean isDeleteMethod(String methodName) {
        return methodName.startsWith("delete");
    }
}
