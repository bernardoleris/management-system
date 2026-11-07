package br.com.system.security;

import br.com.system.model.Administrator;
import br.com.system.repository.AdministratorRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AdministratorRepository administratorRepository;

    public CustomUserDetailsService(AdministratorRepository administratorRepository) {
        this.administratorRepository = administratorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Administrator administrator = administratorRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Administrator not found with login: " + login));

        var user = administrator.getUser();
        return User.withUsername(administrator.getLogin())
                .password(administrator.getPassword())
                .authorities(java.util.List.of())
                .accountExpired(!Boolean.TRUE.equals(user.getAccountNonExpired()))
                .accountLocked(!Boolean.TRUE.equals(user.getAccountNonLocked()))
                .credentialsExpired(!Boolean.TRUE.equals(user.getCredentialsNonExpired()))
                .disabled(!Boolean.TRUE.equals(user.getActive()))
                .build();
    }
}
