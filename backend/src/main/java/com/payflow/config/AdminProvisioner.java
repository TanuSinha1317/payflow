package com.payflow.config;
import org.springframework.beans.factory.annotation.Value;import org.springframework.boot.ApplicationArguments;import org.springframework.boot.ApplicationRunner;import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;import org.springframework.stereotype.Component;
@Component @ConditionalOnExpression("'${payflow.admin.email:}' != '' && '${payflow.admin.password:}' != ''")
public class AdminProvisioner implements ApplicationRunner {
  private final DemoProvisioningService provision;private final String email,phone,password;
  public AdminProvisioner(DemoProvisioningService p,@Value("${payflow.admin.email}") String e,@Value("${payflow.admin.phone}") String ph,@Value("${payflow.admin.password}") String pw){provision=p;email=e.trim().toLowerCase();phone=ph;password=pw;}
  public void run(ApplicationArguments args){if(password.length()<12)throw new IllegalStateException("ADMIN_PASSWORD must be at least 12 characters");provision.createAdmin(email,phone,password);}
}
