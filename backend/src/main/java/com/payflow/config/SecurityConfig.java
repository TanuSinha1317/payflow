package com.payflow.config;
import com.payflow.security.JwtFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
@Configuration
public class SecurityConfig {
  @Bean PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder();}
  @Bean SecurityFilterChain filter(HttpSecurity h,JwtFilter jwt,ObjectMapper json)throws Exception{return h.csrf(c->c.disable()).cors(c->{}).sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).exceptionHandling(e->e
    .authenticationEntryPoint((req,res,ex)->writeError(res,json,401,"UNAUTHORIZED","Authentication required"))
    .accessDeniedHandler((req,res,ex)->writeError(res,json,403,"FORBIDDEN","Access denied"))).authorizeHttpRequests(a->a
    .requestMatchers("/api/v1/auth/**","/actuator/health","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN").requestMatchers(HttpMethod.OPTIONS,"/**").permitAll().anyRequest().authenticated())
    .addFilterBefore(jwt,UsernamePasswordAuthenticationFilter.class).build();}
  @Bean CorsConfigurationSource corsConfigurationSource(@Value("${payflow.cors-origins}") String origins){var c=new CorsConfiguration();c.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::trim).filter(v->!v.isEmpty()).toList());c.setAllowedMethods(List.of("GET","POST","PUT","OPTIONS"));c.setAllowedHeaders(List.of("Authorization","Content-Type","Idempotency-Key"));var s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;}
  private static void writeError(HttpServletResponse response,ObjectMapper json,int status,String code,String message)throws java.io.IOException{response.setStatus(status);response.setContentType("application/json");json.writeValue(response.getOutputStream(),Map.of("timestamp",Instant.now().toString(),"code",code,"message",message,"fields",Map.of()));}
}

