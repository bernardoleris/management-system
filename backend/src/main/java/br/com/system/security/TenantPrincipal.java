package br.com.system.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class TenantPrincipal implements UserDetails {
    private final UserDetails delegate;
    private final Long userId;
    private final Long companyId;
    private final Long membershipId;
    private final Collection<? extends GrantedAuthority> authorities;

    public TenantPrincipal(
            UserDetails delegate,
            Long userId,
            Long companyId,
            Long membershipId,
            Collection<? extends GrantedAuthority> authorities) {
        this.delegate = delegate;
        this.userId = userId;
        this.companyId = companyId;
        this.membershipId = membershipId;
        this.authorities = authorities;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public Long getMembershipId() {
        return membershipId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return delegate.getPassword();
    }

    @Override
    public String getUsername() {
        return delegate.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return delegate.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return delegate.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return delegate.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return delegate.isEnabled();
    }
}
