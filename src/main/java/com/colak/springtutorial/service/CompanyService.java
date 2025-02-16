package com.colak.springtutorial.service;

import com.colak.springtutorial.aop.AuditChanges;
import com.colak.springtutorial.jpa.Company;
import com.colak.springtutorial.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository repository;

    @Transactional
    @AuditChanges
    public Company save(Company company) {
        return repository.save(company);
    }

}
