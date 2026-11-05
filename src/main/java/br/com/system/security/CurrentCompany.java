package br.com.system.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentCompany {

    public Long getId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof TenantPrincipal principal) {
            return principal.getCompanyId();
        }

        throw new AccessDeniedException("Active company context is required!");
    }
}
