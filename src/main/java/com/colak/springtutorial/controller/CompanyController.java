package com.colak.springtutorial.controller;

import com.colak.springtutorial.service.CompanyCommand;
import com.colak.springtutorial.service.impl.CompanyServiceUseCaseImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyServiceUseCaseImpl companyService;

    @PostMapping("/save")
    public void save(@RequestBody CompanyDto dto) {

        CompanyCommand command = new CompanyCommand();
        command.setId(dto.getId());
        command.setName(dto.getName());

        companyService.process(command);
    }
}
