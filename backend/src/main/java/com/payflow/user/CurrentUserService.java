package com.payflow.user;
import com.payflow.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
@Service
public class CurrentUserService {
  private final UserRepository users; public CurrentUserService(UserRepository u){users=u;}
  public User require(){var a=SecurityContextHolder.getContext().getAuthentication();if(a==null||!a.isAuthenticated())throw new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Authentication required");return users.findByEmail(a.getName()).orElseThrow(()->new ApiException(HttpStatus.UNAUTHORIZED,"UNAUTHORIZED","Authentication required"));}
}

