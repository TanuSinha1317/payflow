package com.payflow.auth;
import jakarta.validation.Valid; import jakarta.validation.constraints.*;
import org.springframework.http.*; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService service; public AuthController(AuthService s){service=s;}
  public record RegisterRequest(@NotBlank @Size(max=100) String fullName,@NotBlank @Email String email,@NotBlank String phone,@NotBlank @Size(min=8,max=72) String password){}
  public record LoginRequest(@NotBlank @Email String email,@NotBlank String password){}
  public record AuthResponse(String accessToken,String tokenType,String fullName,String role){}
  @PostMapping("/register") ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest r){return ResponseEntity.status(201).body(service.register(r));}
  @PostMapping("/login") AuthResponse login(@Valid @RequestBody LoginRequest r){return service.login(r);}
}

