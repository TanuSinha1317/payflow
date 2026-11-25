package com.payflow.common;
import java.math.*;
import org.springframework.http.HttpStatus;
public final class Money {
  private Money(){}
  public static long parse(String value){
    try { BigDecimal d=new BigDecimal(value); if(d.scale()>2) throw new ArithmeticException(); return d.movePointRight(2).longValueExact(); }
    catch(Exception e){throw new ApiException(HttpStatus.BAD_REQUEST,"INVALID_AMOUNT","Amount must be a valid INR value with at most two decimals");}
  }
}

