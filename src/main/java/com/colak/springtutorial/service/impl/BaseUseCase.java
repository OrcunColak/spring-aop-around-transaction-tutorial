package com.colak.springtutorial.service.impl;

import com.colak.springtutorial.aop.AuditChanges;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

public abstract class BaseUseCase<C, E, B> implements ApplicationContextAware {

    private final Class<B> clazz;

    private ApplicationContext applicationContext;

    public BaseUseCase(Class<B> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }


    @Getter
    @Setter
    public class UseCaseContext {
        private C command;
        private E oldEntity;
        private E newEntity;
    }

    public void execute(C command) {

        BaseUseCase<C, E, B> proxy = (BaseUseCase<C, E, B>) applicationContext.getBean(clazz);

        UseCaseContext useCaseContext = createContext(command);

        proxy.readOldEntityInTransaction(useCaseContext);

        proxy.saveNewEntityInTransaction(useCaseContext);
    }

    @Transactional(readOnly = true)
    public void readOldEntityInTransaction(UseCaseContext useCaseContext) {
        readOldEntity(useCaseContext);
    }


    @Transactional
    @AuditChanges
    public void saveNewEntityInTransaction(UseCaseContext useCaseContext) {
        saveNewEntity(useCaseContext);
    }

    protected UseCaseContext createContext(C command) {
        UseCaseContext useCaseContext = new UseCaseContext();
        useCaseContext.setCommand(command);
        return useCaseContext;
    }

    protected void readOldEntity(UseCaseContext useCaseContext) {
    }

    protected void saveNewEntity(UseCaseContext useCaseContext) {

    }
}
