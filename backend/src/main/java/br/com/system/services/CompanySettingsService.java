package br.com.system.services;

import br.com.system.data.dto.request.CompanySettingsRequestDTO;
import br.com.system.data.dto.response.CompanySettingsResponseDTO;
import br.com.system.exception.BusinessException;
import br.com.system.exception.DuplicateResourceException;
import br.com.system.exception.ResourceNotFoundException;
import br.com.system.model.Company;
import br.com.system.repository.CompanyRepository;
import br.com.system.security.CurrentCompany;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.ZoneId;

@Service
public class CompanySettingsService {
    private final CompanyRepository companyRepository;
    private final CurrentCompany currentCompany;

    public CompanySettingsService(CompanyRepository companyRepository, CurrentCompany currentCompany) {
        this.companyRepository = companyRepository;
        this.currentCompany = currentCompany;
    }

    @Transactional(readOnly = true)
    public CompanySettingsResponseDTO findCurrent() {
        return toResponse(findCompany());
    }

    @Transactional
    public CompanySettingsResponseDTO update(CompanySettingsRequestDTO dto) {
        Company company = findCompany();
        String documentNumber = normalizeDocument(dto.getDocumentNumber());

        if (documentNumber != null && companyRepository.existsByDocumentNumberAndIdNot(documentNumber, company.getId())) {
            throw new DuplicateResourceException("This document is already registered to another company!");
        }

        String timezone = dto.getTimezone().trim();
        try {
            ZoneId.of(timezone);
        } catch (DateTimeException exception) {
            throw new BusinessException("Invalid timezone!");
        }

        company.setLegalName(dto.getLegalName().trim());
        company.setTradeName(dto.getTradeName().trim());
        company.setDocumentNumber(documentNumber);
        company.setEmail(normalizeOptional(dto.getEmail()));
        company.setPhone(normalizeOptional(dto.getPhone()));
        company.setTimezone(timezone);

        return toResponse(companyRepository.save(company));
    }

    private Company findCompany() {
        return companyRepository.findById(currentCompany.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found!"));
    }

    private String normalizeDocument(String value) {
        if (value == null || value.isBlank()) return null;
        String digits = value.replaceAll("\\D", "");
        if (digits.length() != 11 && digits.length() != 14) {
            throw new BusinessException("Company document must contain 11 or 14 digits!");
        }
        return digits;
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private CompanySettingsResponseDTO toResponse(Company company) {
        return new CompanySettingsResponseDTO(
                company.getId(), company.getLegalName(), company.getTradeName(), company.getDocumentNumber(),
                company.getSlug(), company.getEmail(), company.getPhone(), company.getActive(),
                company.getTimezone(), company.getCurrency(), company.getCreatedAt());
    }
}
