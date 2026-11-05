package br.com.system.security.jwt;

import br.com.system.security.CustomUserDetailsService;
import br.com.system.security.TenantPrincipal;
import br.com.system.model.CompanyMembership;
import br.com.system.repository.CompanyMembershipRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CompanyMembershipRepository companyMembershipRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null && tokenProvider.validateToken(token)) {
            String login = tokenProvider.getLoginFromToken(token);
            Long userId = tokenProvider.getUserIdFromToken(token);
            Long companyId = tokenProvider.getCompanyIdFromToken(token);
            Long membershipId = tokenProvider.getMembershipIdFromToken(token);

            if (userId == null || companyId == null || membershipId == null) {
                filterChain.doFilter(request, response);
                return;
            }

            CompanyMembership membership = companyMembershipRepository
                    .findByIdAndUserIdAndCompanyIdAndActiveTrue(membershipId, userId, companyId)
                    .filter(value -> Boolean.TRUE.equals(value.getCompany().getActive()))
                    .orElse(null);

            if (membership == null) {
                filterChain.doFilter(request, response);
                return;
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(login);
            if (!userDetails.isEnabled()
                    || !userDetails.isAccountNonExpired()
                    || !userDetails.isAccountNonLocked()
                    || !userDetails.isCredentialsNonExpired()) {
                filterChain.doFilter(request, response);
                return;
            }

            TenantPrincipal principal = new TenantPrincipal(
                    userDetails,
                    userId,
                    companyId,
                    membershipId,
                    List.of(new SimpleGrantedAuthority(toAuthority(membership)))
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            principal.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String toAuthority(CompanyMembership membership) {
        return switch (membership.getRole()) {
            case COMPANY_OWNER -> "ROLE_OWNER";
            case ADMIN -> "ROLE_ADMIN";
            case OPERATOR -> "ROLE_OPERATOR";
        };
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }
}
