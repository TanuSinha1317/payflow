package com.payflow.common;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.util.*;
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log=LoggerFactory.getLogger(GlobalExceptionHandler.class);
  public record ErrorBody(Instant timestamp,String code,String message,Map<String,String> fields){}
  @ExceptionHandler(ApiException.class) ResponseEntity<ErrorBody> api(ApiException e){return ResponseEntity.status(e.status()).body(new ErrorBody(Instant.now(),e.code(),e.getMessage(),Map.of()));}
  @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ErrorBody> validation(MethodArgumentNotValidException e){Map<String,String> f=new LinkedHashMap<>();e.getBindingResult().getFieldErrors().forEach(x->f.putIfAbsent(x.getField(),x.getDefaultMessage()));return ResponseEntity.badRequest().body(new ErrorBody(Instant.now(),"VALIDATION_FAILED","Request validation failed",f));}
  @ExceptionHandler(MissingRequestHeaderException.class) ResponseEntity<ErrorBody> missingHeader(MissingRequestHeaderException e){return ResponseEntity.badRequest().body(new ErrorBody(Instant.now(),"VALIDATION_FAILED","Required header is missing: "+e.getHeaderName(),Map.of()));}
  @ExceptionHandler(NoResourceFoundException.class) ResponseEntity<ErrorBody> notFound(){return ResponseEntity.status(404).body(new ErrorBody(Instant.now(),"NOT_FOUND","The requested resource was not found",Map.of()));}
  @ExceptionHandler(HttpMessageNotReadableException.class) ResponseEntity<ErrorBody> unreadableBody(){return ResponseEntity.badRequest().body(new ErrorBody(Instant.now(),"VALIDATION_FAILED","Request body is malformed or contains an invalid value",Map.of()));}
  @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<ErrorBody> duplicate(){return ResponseEntity.status(409).body(new ErrorBody(Instant.now(),"DUPLICATE_ACCOUNT","Email or phone is already registered",Map.of()));}
  @ExceptionHandler(Exception.class) ResponseEntity<ErrorBody> fallback(Exception e){log.error("Unhandled API exception",e);return ResponseEntity.status(500).body(new ErrorBody(Instant.now(),"TEMPORARY_TRANSACTION_FAILURE","The request could not be completed",Map.of()));}
}

