package com.colak.springtutorial.aop.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChangeManager {

    // private final ChangeService changeService;

    public enum OperationType {
        IGNORE,
        CREATE,
        UPDATE,
        DELETE
    }
    public void writeChange(Object entity, Object oldEntity, OperationType operationType) {
        switch (operationType) {
            case CREATE -> logCreation(entity);
            case UPDATE -> logUpdate(entity, oldEntity);
            case DELETE -> logDeletion(entity);
        }
    }

    private void logCreation(Object entity) {
        log.info("Tracking creation of entity: {}", entity.getClass().getSimpleName());
    }

    private void logUpdate(Object entity, Object oldEntity) {

        log.info("Tracking update for entity of type: {}", entity.getClass().getSimpleName());

        for (Field field : entity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try {
                Object oldValue = field.get(oldEntity);
                Object newValue = field.get(entity);

                if (!Objects.equals(oldValue, newValue)) {
                    log.info("Field '{}' changed: Old value = {}, New value = {}",
                            field.getName(), oldValue, newValue);
                }

            } catch (IllegalAccessException e) {
                log.error("Error accessing field '{}': {}", field.getName(), e.getMessage(), e);
            }
        }
    }

    private void logDeletion(Object entity) {
        log.info("Tracking deletion of entity: {}", entity.getClass().getSimpleName());
    }

}
