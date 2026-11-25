package com.payflow.user;
import org.springframework.web.bind.annotation.*; import java.time.Instant;
@RestController @RequestMapping("/api/v1/users")
public class UserController {
  private final CurrentUserService current; public UserController(CurrentUserService c){current=c;}
  public record UserView(String fullName,String email,String phone,String role,Instant createdAt){}
  @GetMapping("/me") UserView me(){User u=current.require();return new UserView(u.getFullName(),u.getEmail(),u.getPhone(),u.getRole().name(),u.getCreatedAt());}
}
