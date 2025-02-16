package com.colak.springtutorial.controller;

import com.colak.springtutorial.jpa.Company;
import com.colak.springtutorial.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/save")
    public void save(@RequestBody CompanyDto dto) {
        Company company = new Company();
        company.setId(dto.getId());
        company.setName(dto.getName());
        companyService.save(company);
    }
}
