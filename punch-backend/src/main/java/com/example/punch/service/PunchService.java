package com.example.punch.service;

import com.example.punch.dto.EncryptedRequest;
import com.example.punch.dto.PunchRequest;
import com.example.punch.entity.Employee;
import com.example.punch.entity.PunchRecord;
import com.example.punch.repository.EmployeeRepository;
import com.example.punch.repository.PunchRecordRepository;
import com.example.punch.util.RsaCryptoUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service

public class PunchService {

    private final EmployeeRepository employeeRepository;
    private final PunchRecordRepository punchRecordRepository;
    private final RsaCryptoUtil rsaCryptoUtil;
    private final ObjectMapper objectMapper;
    public PunchService(EmployeeRepository employeeRepository, PunchRecordRepository punchRecordRepository, RsaCryptoUtil rsaCryptoUtil, ObjectMapper objectMapper) {
        this.employeeRepository = employeeRepository;
        this.punchRecordRepository = punchRecordRepository;
        this.rsaCryptoUtil = rsaCryptoUtil;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PunchRecord processPunch(EncryptedRequest encryptedRequest) {
        try {
            String decryptedJson = rsaCryptoUtil.decrypt(encryptedRequest.getData());
            PunchRequest request = objectMapper.readValue(decryptedJson, PunchRequest.class);

            Employee employee = employeeRepository.findByEmployeeId(request.getEmployeeId())
                    .orElseThrow(() -> new RuntimeException("001")); // Code 001: Unknown employee

            PunchRecord record = new PunchRecord();
            record.setEmployeeId(employee.getEmployeeId());
            record.setPunchType(Long.valueOf(request.getPunchType()));
            record.setPunchTime(LocalDateTime.now());

            return punchRecordRepository.save(record);
        } catch (RuntimeException e) {
            if ("001".equals(e.getMessage())) throw e;
            throw new RuntimeException("901"); // Unexpected
        } catch (Exception e) {
            throw new RuntimeException("801"); // Decryption or parse error
        }
    }

    public List<PunchRecord> getRecords() {
        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(7); // Default to last 7 days
        List<PunchRecord> records = punchRecordRepository.findAllByPunchTimeBetweenOrderByPunchTimeDesc(start, end);
        if (records.isEmpty()) {
            throw new RuntimeException("003"); // No data
        }
        return records;
    }
}
