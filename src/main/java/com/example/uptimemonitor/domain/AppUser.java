package com.example.uptimemonitor.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "app_user")
public class AppUser {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String username;

  @Column(name = "password_hash", nullable = false, length = 200)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserRole role;

  @Column(nullable = false)
  private boolean enabled;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected AppUser() {}

  public AppUser(Long id, String username, String passwordHash, UserRole role, boolean enabled, Instant createdAt) {
    this.id = id;
    this.username = username;
    this.passwordHash = passwordHash;
    this.role = role;
    this.enabled = enabled;
    this.createdAt = createdAt;
  }

  public Long getId() { return id; }
  public String getUsername() { return username; }
  public String getPasswordHash() { return passwordHash; }
  public UserRole getRole() { return role; }
  public boolean isEnabled() { return enabled; }
  public Instant getCreatedAt() { return createdAt; }

  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public void setRole(UserRole role) { this.role = role; }
  public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
