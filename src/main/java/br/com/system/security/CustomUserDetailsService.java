package br.com.system.security;

import br.com.system.model.Administrator;
import br.com.system.repository.AdministratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private AdministratorRepository administratorRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Administrator administrator = administratorRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Administrator not found with login: " + login));

        return administrator.getUser();
    }
}