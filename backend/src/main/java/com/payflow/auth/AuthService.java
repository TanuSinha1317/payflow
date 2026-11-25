package com.payflow.auth;
import com.payflow.common.ApiException;
import com.payflow.security.JwtService;
import com.payflow.user.*;
import com.payflow.wallet.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
@Service
public class AuthService {
  private final UserRepository users;private final WalletRepository wallets;private final PasswordEncoder passwords;private final JwtService jwt;
  public AuthService(UserRepository u,WalletRepository w,PasswordEncoder p,JwtService j){users=u;wallets=w;passwords=p;jwt=j;}
  @Transactional public AuthController.AuthResponse register(AuthController.RegisterRequest r){String email=normalizeEmail(r.email());String phone=normalizePhone(r.phone());if(users.existsByEmailOrPhone(email,phone))throw new ApiException(HttpStatus.CONFLICT,"DUPLICATE_ACCOUNT","Email or phone is already registered");try{User u=users.save(new User(r.fullName().trim(),email,phone,passwords.encode(r.password())));wallets.save(new Wallet(u,Wallet.AccountType.PERSONAL));return response(u);}catch(DataIntegrityViolationException e){throw new ApiException(HttpStatus.CONFLICT,"DUPLICATE_ACCOUNT","Email or phone is already registered");}}
  @Transactional(readOnly=true) public AuthController.AuthResponse login(AuthController.LoginRequest r){User u=users.findByEmail(normalizeEmail(r.email())).orElseThrow(()->unauthorized());if(!passwords.matches(r.password(),u.getPasswordHash())||u.getStatus()!=User.Status.ACTIVE)throw unauthorized();return response(u);}
  public static String normalizeEmail(String e){return e.trim().toLowerCase(Locale.ROOT);}
  public static String normalizePhone(String p){String n=p.replaceAll("\\D","");if(n.length()==12&&n.startsWith("91"))n=n.substring(2);if(!n.matches("[6-9][0-9]{9}"))throw new ApiException(HttpStatus.BAD_REQUEST,"VALIDATION_FAILED","Enter a valid Indian mobile number");return n;}
  private AuthController.AuthResponse response(User u){return new AuthController.AuthResponse(jwt.issue(u.getEmail(),u.getRole().name()),"Bearer",u.getFullName(),u.getRole().name());}
  private ApiException unauthorized(){return new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Invalid email or password");}
}

