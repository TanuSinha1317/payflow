package com.payflow.transaction;
import com.payflow.wallet.Wallet;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="financial_transactions")
public class FinancialTransaction {
  public enum Type { TOP_UP, TRANSFER }
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @Column(nullable=false,unique=true) private UUID reference;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private Type type;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="source_wallet_id",nullable=false) private Wallet sourceWallet;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="destination_wallet_id",nullable=false) private Wallet destinationWallet;
  @Column(name="amount_paise",nullable=false) private long amountPaise;
  @Column(nullable=false) private String status="COMPLETED";
  @Column(length=140) private String note;
  @Column(name="created_at",insertable=false,updatable=false) private Instant createdAt;
  protected FinancialTransaction(){}
  public FinancialTransaction(Type type,Wallet source,Wallet destination,long amount,String note){reference=UUID.randomUUID();this.type=type;sourceWallet=source;destinationWallet=destination;amountPaise=amount;this.note=note;}
  public Long getId(){return id;} public UUID getReference(){return reference;} public Type getType(){return type;} public Wallet getSourceWallet(){return sourceWallet;} public Wallet getDestinationWallet(){return destinationWallet;} public long getAmountPaise(){return amountPaise;} public String getStatus(){return status;} public String getNote(){return note;} public Instant getCreatedAt(){return createdAt;}
}

