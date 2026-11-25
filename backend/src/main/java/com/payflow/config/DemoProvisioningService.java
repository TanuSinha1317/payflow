package com.payflow.config;
import com.payflow.user.*;import com.payflow.wallet.*;
import org.springframework.security.crypto.password.PasswordEncoder;import org.springframework.stereotype.Service;import org.springframework.transaction.annotation.Transactional;
@Service
public class DemoProvisioningService {
  private final UserRepository users;private final WalletRepository wallets;private final PasswordEncoder passwords;
  public DemoProvisioningService(UserRepository u,WalletRepository w,PasswordEncoder p){users=u;wallets=w;passwords=p;}
  @Transactional public User create(String name,String email,String phone,String password,boolean lab){return users.findByEmail(email).orElseGet(()->{User u=new User(name,email,phone,passwords.encode(password));u.setLabAccount(lab);u=users.save(u);wallets.save(new Wallet(u,lab?Wallet.AccountType.LAB:Wallet.AccountType.PERSONAL));return u;});}
  @Transactional public User createAdmin(String email,String phone,String password){return users.findByEmail(email).orElseGet(()->{User u=new User("PayFlow Administrator",email,phone,passwords.encode(password));u.setRole(User.Role.ADMIN);u=users.save(u);wallets.save(new Wallet(u,Wallet.AccountType.PERSONAL));return u;});}
}


