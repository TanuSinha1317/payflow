package com.payflow.ledger;
import com.payflow.transaction.FinancialTransaction;
import com.payflow.wallet.Wallet;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="ledger_entries")
public class LedgerEntry {
  public enum Direction { DEBIT, CREDIT }
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="transaction_id",nullable=false) private FinancialTransaction transaction;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="wallet_id",nullable=false) private Wallet wallet;
  @Enumerated(EnumType.STRING) @Column(nullable=false) private Direction direction;
  @Column(name="amount_paise",nullable=false) private long amountPaise;
  @Column(name="created_at",insertable=false,updatable=false) private Instant createdAt;
  protected LedgerEntry(){}
  public LedgerEntry(FinancialTransaction tx,Wallet wallet,Direction direction,long amount){transaction=tx;this.wallet=wallet;this.direction=direction;amountPaise=amount;}
  public Long getId(){return id;} public FinancialTransaction getTransaction(){return transaction;} public Wallet getWallet(){return wallet;} public Direction getDirection(){return direction;} public long getAmountPaise(){return amountPaise;}
}

