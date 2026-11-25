package com.payflow.transaction;
import com.payflow.auth.AuthService;
import com.payflow.common.ApiException;
import com.payflow.user.*;import com.payflow.wallet.*;
import org.springframework.http.HttpStatus;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/recipients")
public class RecipientController {
  private final UserRepository users;private final WalletRepository wallets;private final CurrentUserService current;
  public RecipientController(UserRepository u,WalletRepository w,CurrentUserService c){users=u;wallets=w;current=c;}
  public record Recipient(Long recipientId,String maskedName,String maskedPhone){}
  @GetMapping("/lookup") Recipient lookup(@RequestParam String phone){User me=current.require();User u=users.findByPhone(AuthService.normalizePhone(phone)).filter(x->!x.getId().equals(me.getId())&&!x.isLabAccount()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"RECIPIENT_NOT_FOUND","Recipient not found"));Wallet w=wallets.findByOwnerId(u.getId()).orElseThrow();return new Recipient(w.getId(),maskName(u.getFullName()),"******"+u.getPhone().substring(6));}
  private String maskName(String n){String[] p=n.trim().split("\\s+");return p[0].charAt(0)+"***"+(p.length>1?" "+p[p.length-1].charAt(0)+"***":"");}
}

