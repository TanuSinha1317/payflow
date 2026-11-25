package com.payflow.wallet;

import com.payflow.user.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="wallets")
public class Wallet {
  public enum AccountType { PERSONAL, SYSTEM_FUNDING, LAB }
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false, unique=true) private UUID reference;
  @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="owner_id", unique=true) private User owner;
  @Column(nullable=false, length=3) private String currency="INR";
  @Enumerated(EnumType.STRING) @Column(name="account_type",nullable=false) private AccountType accountType;
  @Column(name="balance_paise",nullable=false) private long balancePaise;
  @Column(name="created_at",insertable=false,updatable=false) private Instant createdAt;
  @Column(name="updated_at",nullable=false) private Instant updatedAt;
  protected Wallet(){}
  public Wallet(User owner,AccountType type){this.reference=UUID.randomUUID();this.owner=owner;this.accountType=type;this.updatedAt=Instant.now();}
  public Long getId(){return id;} public UUID getReference(){return reference;} public User getOwner(){return owner;} public String getCurrency(){return currency;}
  public AccountType getAccountType(){return accountType;} public long getBalancePaise(){return balancePaise;} public Instant getCreatedAt(){return createdAt;} public Instant getUpdatedAt(){return updatedAt;}
  public void debit(long amount){balancePaise=Math.subtractExact(balancePaise,amount);updatedAt=Instant.now();}
  public void credit(long amount){balancePaise=Math.addExact(balancePaise,amount);updatedAt=Instant.now();}
}

