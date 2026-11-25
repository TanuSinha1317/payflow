package com.payflow.config;
import com.payflow.transaction.TransactionEngine;import com.payflow.user.User;
import org.springframework.boot.ApplicationArguments;import org.springframework.boot.ApplicationRunner;import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;import org.springframework.stereotype.Component;
@Component @ConditionalOnProperty(name="payflow.demo-enabled",havingValue="true")
public class DemoDataSeeder implements ApplicationRunner {
  private final DemoProvisioningService provision;private final TransactionEngine engine;
  public DemoDataSeeder(DemoProvisioningService p,TransactionEngine e){provision=p;engine=e;}
  public void run(ApplicationArguments args){seed("Rahul Sharma","rahul@payflow.local","9876500001",100_000);seed("Priya Singh","priya@payflow.local","9876500002",75_000);seed("Arjun Mehta","arjun@payflow.local","9876500003",50_000);}
  private void seed(String name,String email,String phone,long amount){User u=provision.create(name,email,phone,"Demo@123",false);if(amount>0)engine.topup(u,amount,"demo-initial-funding-"+u.getId());}
}

