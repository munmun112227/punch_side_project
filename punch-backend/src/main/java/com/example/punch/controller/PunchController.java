package com.example.punch.controller;

import com.example.punch.dto.EncryptedRequest;
import com.example.punch.entity.PunchRecord;
import com.example.punch.service.PunchService;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/punches")

@CrossOrigin(origins = "${cors.allowed-origin}")
public class PunchController {

    private final PunchService punchService;
    public PunchController(PunchService punchService) {
        this.punchService = punchService;
    }

    @Value("${rsa.public-key.path}")
    private String publicKeyPath;

    @PostMapping
    public ResponseEntity<PunchRecord> submitPunch(@Valid @RequestBody EncryptedRequest request) {
        PunchRecord record = punchService.processPunch(request);
        return ResponseEntity.ok(record);
    }

    @GetMapping
    public ResponseEntity<List<PunchRecord>> getRecords() {
        List<PunchRecord> records = punchService.getRecords();
        return ResponseEntity.ok(records);
    }

    @GetMapping("/public-key")
    public ResponseEntity<String> getPublicKey() throws Exception {
        String keyContent = new String(Files.readAllBytes(Paths.get(publicKeyPath)));
        return ResponseEntity.ok(keyContent);
    }
}
