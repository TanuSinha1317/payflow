package com.payflow.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payflow.common.ApiException;
import com.payflow.ledger.*;
import com.payflow.user.User;
import com.payflow.wallet.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Service
public class TransactionEngine {
  public record Result(UUID reference,String type,long amountPaise,String status,Instant createdAt,long updatedBalancePaise){}
  private final WalletRepository wallets; private final TransactionRepository transactions; private final LedgerRepository ledger;
  private final JdbcTemplate jdbc; private final ObjectMapper json; private final long transferMax; private final long topupMax;
  public TransactionEngine(WalletRepository w,TransactionRepository t,LedgerRepository l,JdbcTemplate j,ObjectMapper o,
      @Value("${payflow.transfer-max-paise}") long transferMax,@Value("${payflow.topup-max-paise}") long topupMax){wallets=w;transactions=t;ledger=l;jdbc=j;json=o;this.transferMax=transferMax;this.topupMax=topupMax;}

  @Transactional public Result transfer(User owner,Long destinationId,long amount,String note,String key){
    validateKey(key);validateAmount(amount,transferMax);Long sourceId=wallets.findIdByOwnerId(owner.getId()).orElseThrow();
    if(sourceId.equals(destinationId))throw new ApiException(HttpStatus.BAD_REQUEST,"SELF_TRANSFER_NOT_ALLOWED","Cannot transfer to your own wallet");
    String fingerprint=sha("TRANSFER|"+destinationId+"|"+amount+"|"+Objects.toString(note,""));
    Result replay=claim(owner.getId(),"TRANSFER",key,fingerprint);if(replay!=null)return replay;
    return move(owner.getId(),sourceId,destinationId,amount,note,FinancialTransaction.Type.TRANSFER,key,false);
  }

  @Transactional public Result topup(User owner,long amount,String key){
    validateKey(key);validateAmount(amount,topupMax);Long personalId=wallets.findIdByOwnerId(owner.getId()).orElseThrow();Long systemId=wallets.findSystemFundingAccountId().orElseThrow();
    String fingerprint=sha("TOP_UP|"+personalId+"|"+amount);Result replay=claim(owner.getId(),"TOP_UP",key,fingerprint);if(replay!=null)return replay;
    return move(owner.getId(),systemId,personalId,amount,"Simulated funding",FinancialTransaction.Type.TOP_UP,key,true);
  }

  /** Restricted fault hook used only by the conditional Engineering Lab service. */
  @Transactional public void transferWithInjectedFailure(User owner,Long destinationId,long amount){
    Long sourceId=wallets.findIdByOwnerId(owner.getId()).orElseThrow();List<Wallet> locked=wallets.lockAllByIdOrdered(List.of(sourceId,destinationId));
    Wallet source=locked.stream().filter(w->w.getId().equals(sourceId)).findFirst().orElseThrow();
    if(source.getBalancePaise()<amount)throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,"INSUFFICIENT_BALANCE","Insufficient wallet balance");
    source.debit(amount);wallets.saveAndFlush(source);throw new IllegalStateException("LAB_INJECTED_FAILURE_AFTER_DEBIT_FLUSH");
  }

  private Result move(Long ownerId,Long sourceId,Long destinationId,long amount,String note,FinancialTransaction.Type type,String key,boolean allowSignedSource){
    List<Wallet> locked=wallets.lockAllByIdOrdered(List.of(sourceId,destinationId));if(locked.size()!=2)throw new ApiException(HttpStatus.NOT_FOUND,"RECIPIENT_NOT_FOUND","Recipient not found");
    Wallet source=locked.stream().filter(w->w.getId().equals(sourceId)).findFirst().orElseThrow();Wallet destination=locked.stream().filter(w->w.getId().equals(destinationId)).findFirst().orElseThrow();
    if(!allowSignedSource&&source.getBalancePaise()<amount)throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,"INSUFFICIENT_BALANCE","Insufficient wallet balance");
    try{source.debit(amount);destination.credit(amount);}catch(ArithmeticException e){throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_AMOUNT","Amount exceeds supported range");}
    FinancialTransaction tx=transactions.saveAndFlush(new FinancialTransaction(type,source,destination,amount,note));
    ledger.saveAll(List.of(new LedgerEntry(tx,source,LedgerEntry.Direction.DEBIT,amount),new LedgerEntry(tx,destination,LedgerEntry.Direction.CREDIT,amount)));
    Result result=new Result(tx.getReference(),type.name(),amount,"COMPLETED",Instant.now(),destination.getId().equals(sourceId)?source.getBalancePaise():type==FinancialTransaction.Type.TOP_UP?destination.getBalancePaise():source.getBalancePaise());
    try{jdbc.update("UPDATE idempotency_records SET transaction_id=?, response_json=? WHERE owner_id=? AND operation=? AND idempotency_key=?",tx.getId(),json.writeValueAsString(result),ownerId,type.name(),key);}catch(JsonProcessingException e){throw new IllegalStateException(e);}
    return result;
  }

  private Result claim(Long owner,String operation,String key,String fingerprint){
    int inserted=jdbc.update("INSERT INTO idempotency_records(owner_id,operation,idempotency_key,request_fingerprint) VALUES (?,?,?,?) ON CONFLICT (owner_id,operation,idempotency_key) DO NOTHING",owner,operation,key,fingerprint);
    if(inserted==1)return null;
    Map<String,Object> row=jdbc.queryForMap("SELECT request_fingerprint,response_json FROM idempotency_records WHERE owner_id=? AND operation=? AND idempotency_key=? FOR UPDATE",owner,operation,key);
    if(!fingerprint.equals(row.get("request_fingerprint")))throw new ApiException(HttpStatus.CONFLICT,"IDEMPOTENCY_CONFLICT","Idempotency key was already used for a different request");
    Object saved=row.get("response_json");if(saved==null)throw new ApiException(HttpStatus.CONFLICT,"TEMPORARY_TRANSACTION_FAILURE","Original request is still processing");
    try{return json.readValue(saved.toString(),Result.class);}catch(JsonProcessingException e){throw new IllegalStateException(e);}
  }
  private void validateAmount(long amount,long max){if(amount<100||amount>max)throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_AMOUNT","Amount is outside the configured limits");}
  private void validateKey(String key){if(key==null||!key.matches("[A-Za-z0-9._:-]{8,100}"))throw new ApiException(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED","A valid Idempotency-Key header is required");}
  private String sha(String value){try{return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));}catch(Exception e){throw new IllegalStateException(e);}}
}

