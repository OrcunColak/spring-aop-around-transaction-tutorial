package com.colak.springtutorial.repository;

import com.colak.springtutorial.jpa.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<CompanyEntity,Long> {
}
