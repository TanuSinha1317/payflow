package com.payflow.engineeringlab;
import com.payflow.user.CurrentUserService;import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/v1/lab") @ConditionalOnProperty(name="payflow.lab-enabled",havingValue="true")
public class LabController {private final LabScenarioService lab;private final CurrentUserService current;public LabController(LabScenarioService l,CurrentUserService c){lab=l;current=c;}
  @GetMapping("/status") Map<String,Boolean> status(){current.require();return Map.of("enabled",true);}
  @PostMapping("/scenarios/{scenario}") LabScenarioService.Result run(@PathVariable String scenario){return lab.run(scenario,current.require().getId());}
}
