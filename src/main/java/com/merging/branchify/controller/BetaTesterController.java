package com.merging.branchify.controller;

import com.merging.branchify.dto.BetaTesterDTO;
import com.merging.branchify.entity.BetaTester;
import com.merging.branchify.service.BetaTesterService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branchify")
public class BetaTesterController {

    private final BetaTesterService betaTesterService;

    public BetaTesterController(BetaTesterService betaTesterService) {
        this.betaTesterService = betaTesterService;
    }

    @PostMapping("/beta-tester")
    public ResponseEntity<String> registerTester(@RequestBody BetaTesterDTO betaTesterDTO) {
        try {
            betaTesterService.register(betaTesterDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body("베타테스터 등록 성공");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/beta-tester")
    public List<String> getAllEmails() {
        return betaTesterService.getAllEmails();
    }
}
