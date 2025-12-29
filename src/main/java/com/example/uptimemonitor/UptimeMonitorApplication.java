package com.example.uptimemonitor;

import com.example.uptimemonitor.domain.AppUser;
import com.example.uptimemonitor.domain.UserRole;
import com.example.uptimemonitor.repo.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@SpringBootApplication
@EnableScheduling
public class UptimeMonitorApplication {

  public static void main(String[] args) {
    SpringApplication.run(UptimeMonitorApplication.class, args);
  }

  /**
   * Creates default users if the table is empty.
   * Task requirement: "authorization (users in DB)".
   */
  @Bean
  CommandLineRunner initUsers(UserRepository users, PasswordEncoder encoder) {
    return args -> {
      if (users.count() > 0) return;

      users.save(new AppUser(null, "admin", encoder.encode("admin"), UserRole.ADMIN, true, Instant.now()));
      users.save(new AppUser(null, "user", encoder.encode("user"), UserRole.USER, true, Instant.now()));
    };
  }
}
