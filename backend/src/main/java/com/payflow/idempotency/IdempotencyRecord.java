package com.payflow.idempotency;
import com.payflow.transaction.FinancialTransaction;
import com.payflow.user.User;
import jakarta.persistence.*;
@Entity @Table(name="idempotency_records",uniqueConstraints=@UniqueConstraint(columnNames={"owner_id","operation","idempotency_key"}))
public class IdempotencyRecord {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="owner_id",nullable=false) private User owner;
  @Column(nullable=false) private String operation;
  @Column(name="idempotency_key",nullable=false) private String key;
  @Column(name="request_fingerprint",nullable=false,length=64) private String fingerprint;
  @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="transaction_id") private FinancialTransaction transaction;
  @Column(name="response_json") private String responseJson;
  protected IdempotencyRecord(){}
  public Long getId(){return id;} public String getFingerprint(){return fingerprint;} public FinancialTransaction getTransaction(){return transaction;} public void complete(FinancialTransaction tx,String response){transaction=tx;responseJson=response;}
}

