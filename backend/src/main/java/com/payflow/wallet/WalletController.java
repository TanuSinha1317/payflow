package com.payflow.wallet;
import com.payflow.common.Money;
import com.payflow.transaction.*;
import com.payflow.user.*;
import jakarta.validation.Valid; import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import java.time.Instant; import java.util.UUID;
@RestController @RequestMapping("/api/v1/wallet")
public class WalletController {
  private final CurrentUserService current;private final WalletRepository wallets;private final TransactionEngine engine;
  public WalletController(CurrentUserService c,WalletRepository w,TransactionEngine e){current=c;wallets=w;engine=e;}
  public record WalletView(String accountHolder,UUID walletReference,long balancePaise,String currency,Instant createdAt){}
  public record AmountRequest(@NotBlank String amount){}
  @GetMapping WalletView wallet(){User u=current.require();Wallet w=wallets.findByOwnerId(u.getId()).orElseThrow();return new WalletView(u.getFullName(),w.getReference(),w.getBalancePaise(),w.getCurrency(),w.getCreatedAt());}
  @PostMapping("/top-ups") TransactionEngine.Result topup(@RequestHeader("Idempotency-Key") String key,@Valid @RequestBody AmountRequest r){return engine.topup(current.require(),Money.parse(r.amount()),key);}
}

