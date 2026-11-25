package com.payflow.reconciliation;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/admin/reconciliation")
public class ReconciliationController {private final ReconciliationService service;public ReconciliationController(ReconciliationService s){service=s;}@GetMapping ReconciliationService.Report get(){return service.run();}@PostMapping("/run") ReconciliationService.Report run(){return service.run();}}

