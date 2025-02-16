package com.colak.springtutorial.repository;

import com.colak.springtutorial.jpa.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company,Long> {
}
