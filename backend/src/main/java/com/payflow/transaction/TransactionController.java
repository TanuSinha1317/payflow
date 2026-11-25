package com.payflow.transaction;
import com.payflow.common.*;import com.payflow.user.*;import com.payflow.wallet.*;
import jakarta.validation.Valid;import jakarta.validation.constraints.*;
import org.springframework.data.domain.*;import org.springframework.http.HttpStatus;import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;import java.util.UUID;
@RestController @RequestMapping("/api/v1")
public class TransactionController {
  private final TransactionEngine engine;private final CurrentUserService current;private final WalletRepository wallets;private final TransactionRepository txs;
  public TransactionController(TransactionEngine e,CurrentUserService c,WalletRepository w,TransactionRepository t){engine=e;current=c;wallets=w;txs=t;}
  public record TransferRequest(@NotNull Long recipientId,@NotBlank String amount,@Size(max=140) String note){}
  public record TxView(UUID reference,String type,String direction,long amountPaise,String status,String note,Instant createdAt,String counterparty){}
  @PostMapping("/transfers") TransactionEngine.Result transfer(@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody TransferRequest r){return engine.transfer(current.require(),r.recipientId(),Money.parse(r.amount()),r.note(),key);}
  @Transactional(readOnly=true) @GetMapping("/transactions") Page<TxView> history(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){Wallet w=wallets.findByOwnerId(current.require().getId()).orElseThrow();return txs.history(w.getId(),PageRequest.of(Math.max(0,page),Math.min(Math.max(1,size),100),Sort.by(Sort.Order.desc("createdAt"),Sort.Order.desc("id")))).map(t->view(t,w));}
  @Transactional(readOnly=true) @GetMapping("/transactions/{reference}") TxView detail(@PathVariable UUID reference){Wallet w=wallets.findByOwnerId(current.require().getId()).orElseThrow();FinancialTransaction t=txs.findByReference(reference).filter(x->x.getSourceWallet().getId().equals(w.getId())||x.getDestinationWallet().getId().equals(w.getId())).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"TRANSACTION_NOT_FOUND","Transaction not found"));return view(t,w);}
  private TxView view(FinancialTransaction t,Wallet mine){boolean out=t.getSourceWallet().getId().equals(mine.getId());Wallet other=out?t.getDestinationWallet():t.getSourceWallet();String cp=other.getAccountType()==Wallet.AccountType.SYSTEM_FUNDING?"PayFlow simulated funding":other.getOwner().getFullName();return new TxView(t.getReference(),t.getType().name(),out?"OUTGOING":"INCOMING",t.getAmountPaise(),t.getStatus(),t.getNote(),t.getCreatedAt(),cp);}
}

