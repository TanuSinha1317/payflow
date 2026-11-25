package com.payflow.user;

import jakarta.persistence.*;
import java.time.Instant;

@Entity @Table(name="app_users")
public class User {
  public enum Role { USER, ADMIN }
  public enum Status { ACTIVE, DISABLED }
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(name="full_name", nullable=false) private String fullName;
  @Column(nullable=false, unique=true) private String email;
  @Column(nullable=false, unique=true, length=10) private String phone;
  @Column(name="password_hash", nullable=false) private String passwordHash;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private Role role = Role.USER;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status = Status.ACTIVE;
  @Column(name="lab_account", nullable=false) private boolean labAccount;
  @Column(name="created_at", insertable=false, updatable=false) private Instant createdAt;
  protected User() {}
  public User(String fullName,String email,String phone,String passwordHash){this.fullName=fullName;this.email=email;this.phone=phone;this.passwordHash=passwordHash;}
  public Long getId(){return id;} public String getFullName(){return fullName;} public String getEmail(){return email;} public String getPhone(){return phone;}
  public String getPasswordHash(){return passwordHash;} public Role getRole(){return role;} public Status getStatus(){return status;}
  public boolean isLabAccount(){return labAccount;} public Instant getCreatedAt(){return createdAt;}
  public void setRole(Role role){this.role=role;} public void setLabAccount(boolean value){labAccount=value;}
}

