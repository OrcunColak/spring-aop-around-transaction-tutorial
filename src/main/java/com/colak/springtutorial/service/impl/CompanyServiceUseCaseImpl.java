package com.colak.springtutorial.service.impl;

import com.colak.springtutorial.jpa.CompanyEntity;
import com.colak.springtutorial.repository.CompanyRepository;
import com.colak.springtutorial.service.CompanyCommand;
import com.colak.springtutorial.service.CompanyServiceUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyServiceUseCaseImpl extends BaseUseCase<CompanyCommand, CompanyEntity, CompanyServiceUseCase>
        implements CompanyServiceUseCase {

    private CompanyRepository repository;

    public CompanyServiceUseCaseImpl() {
        super(CompanyServiceUseCase.class);
    }

    @Autowired
    public void setRepository(CompanyRepository repository) {
        this.repository = repository;
    }

    @Override
    public void process(CompanyCommand command) {
        execute(command);
    }


    @Override
    protected void saveNewEntity(UseCaseContext useCaseContext) {

        CompanyCommand command = useCaseContext.getCommand();

        CompanyEntity entity = new CompanyEntity();
        entity.setId(command.getId());
        entity.setName(command.getName());

        repository.save(entity);

    }


}
