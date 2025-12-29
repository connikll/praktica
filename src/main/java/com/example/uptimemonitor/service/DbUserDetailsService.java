package com.example.uptimemonitor.service;

import com.example.uptimemonitor.domain.AppUser;
import com.example.uptimemonitor.repo.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DbUserDetailsService implements UserDetailsService {

  private final UserRepository users;

  public DbUserDetailsService(UserRepository users) {
    this.users = users;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    AppUser u = users.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    return new User(
        u.getUsername(),
        u.getPasswordHash(),
        u.isEnabled(),
        true,
        true,
        true,
        List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name()))
    );
  }
}
